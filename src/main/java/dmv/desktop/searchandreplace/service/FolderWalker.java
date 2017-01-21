/**
 * 
 */
package dmv.desktop.searchandreplace.service;

import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.AFTER_FOUND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.BEFORE_FIND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.COMPUTED;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.INTERRUPTED;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.REPLACED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dmv.desktop.searchandreplace.exception.AccessResourceException;
import dmv.desktop.searchandreplace.exception.NothingToReplaceException;
import dmv.desktop.searchandreplace.model.*;


/**
 * Class <tt>FolderWalker.java</tt> implements {@link SearchAndReplace} 
 * interface for Folders and Files. So, it can read files in folder,
 * then search for what needed to be replaced, then return found results
 * for preview or actually replace files content and also rename files if needed.
 * Everything in found files will be cached in memory, then 'to find' spots replaced,
 * then modified content will be written back into files. State will become 'REPLACED'.
 * Any parameter may be overridden at any stage, some changes may lead to change in 
 * object's state which may lead to re-computation or re-reading file's content.
 * 
 * @author dmv
 * @since 2017 January 02
 */
public class FolderWalker
        implements SearchAndReplace<SearchPath, SearchProfile, SearchResult> {
    /* Explicitly set the default pool of CompletableFuture */
    private static final ForkJoinPool COMMON_POOL = ForkJoinPool.commonPool();
    
    private SearchPath folder;
    private SearchProfile profile;
    private Queue<FileReplacer> foundFiles;
    private State state;
    
    /**
     * Constructs a Walker with required parameters.
     * They could be changed later.
     * @param folder 'where to search' parameter
     * @param profile 'what to find' parameter
     * @throws NullPointerException if either of arguments is null
     */
    public FolderWalker(SearchPath folder, SearchProfile profile) {
        setRootElement(folder);
        setProfile(profile);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public SearchPath getRootElement() {
        return folder;
    }

    @Override
    public void setRootElement(SearchPath folder) {
        Objects.requireNonNull(folder);
        state = BEFORE_FIND;
        this.folder = folder;
    }

    @Override
    public SearchProfile getProfile() {
        return profile;
    }

    @Override
    public void setProfile(SearchProfile profile) {
        checkProfile(profile);
        this.profile = profile;
    }

    @Override
    public List<SearchResult> preview() {
        return preview(COMMON_POOL);
    }

    @Override
    public List<SearchResult> preview(Executor exec) {
        Objects.requireNonNull(exec);
        return walk(exec, false);
    }

    @Override
    public List<SearchResult> replace() {
        return replace(COMMON_POOL);
    }

    @Override
    public List<SearchResult> replace(Executor exec) {
        Objects.requireNonNull(exec);
        return walk(exec, true);
    }

    private List<SearchResult> walk(Executor exec, boolean replace) {
        checkInitialRequirements();
        checkState();
        try (Stream<SearchResult> results = getFutures(exec, replace)
                                              .stream()
                                              .map(this::completeFuture)
                                              .filter(this::hasInformation)) {
            return changeStateAndReturn(results.collect(Collectors.toList()), replace);
        } catch (IOException e) {
            state = INTERRUPTED;
            throw new AccessResourceException(e);
        } 
    }

    private SearchResult completeFuture(CompletableFuture<SearchResult> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return SearchResultImpl.getBuilder()
                    .setExceptional(true)
                    .setCause(e)
                    .build();
        }
    }

    private boolean hasInformation(SearchResult result) {
        return result.isExceptional() || result.numberOfModificationsMade() > 0;
    }

    private List<SearchResult> changeStateAndReturn(List<SearchResult> list, boolean replace) {
        if (list.size() == 0) {
            state = INTERRUPTED;
            throw new NothingToReplaceException("There is nothing to be replaced");
        }
        state = replace ? REPLACED : COMPUTED;
        return list;
    }
    
    private List<CompletableFuture<SearchResult>> 
                       getFutures(Executor exec, boolean replace) throws IOException {
        Stream<CompletableFuture<FileReplacer>> futures = null;
        
        if (state.equals(BEFORE_FIND)) futures = readFiles(exec);
        else                           futures = readCache(exec);
        // Break ties with main thread stream by creating a list of CompletableFutures
        return futures.map(future -> 
                           future.thenApplyAsync(
                                    replacer -> produceResult(replacer, replace), exec))
                      .collect(Collectors.toList());
    }

    private Stream<CompletableFuture<FileReplacer>> 
                      readFiles(Executor exec) throws IOException {
        foundFiles = new ConcurrentLinkedQueue<>();
        return Files.walk(folder.getPath(), 
                          folder.isSubfolders() ? Integer.MAX_VALUE : 1)
                    .filter(this::isPathValid)
                    .map(this::createReplacer)
                    .map(createFuture(exec))
                    .map(future -> future.thenApplyAsync(this::readFileContent, exec));
    }

    private Stream<CompletableFuture<FileReplacer>> readCache(Executor exec) {
        Stream<CompletableFuture<FileReplacer>> futures;
        futures = foundFiles.stream() 
                            .map(createFuture(exec));
        if (state.equals(AFTER_FOUND))
            futures = futures.map(future -> future.thenApplyAsync(this::updateProfile, exec));
        return futures;
    }

    private Function<? super FileReplacer, ? extends CompletableFuture<FileReplacer>>
                                                         createFuture(Executor exec) {
        return replacer -> CompletableFuture.supplyAsync(() -> replacer, exec);
    }

    private FileReplacer updateProfile(FileReplacer replacer) {
        replacer.setProfile(profile);
        return replacer;
    }
    
    private FileReplacer readFileContent(FileReplacer replacer) {
        //System.out.println("run on thread " + Thread.currentThread().getName());
        // cache only objects with possible replacements
        if (replacer.hasReplacements()) 
            foundFiles.add(replacer);
        return replacer;
    }
    
    private SearchResult produceResult(FileReplacer replacer, boolean replace) {
        return replace ? replacer.writeResult() : replacer.getResult();
    }
    
    private boolean isPathValid(Path file) {
        return !Files.isDirectory(file) &&
                folder.getNamePattern().matches(file);
    }
    
    private FileReplacer createReplacer(Path file) {
        return new FileReplacerImpl(file, profile);
    }
    
    private void checkProfile(SearchProfile profile) {
        Objects.requireNonNull(profile);
        // check for null at initialization time
        if (this.profile != null && !this.profile.equals(profile)) {
            if (!this.profile.getToFind().equals(profile.getToFind()) ||
                !this.profile.getCharset().equals(profile.getCharset()))
                state = BEFORE_FIND;
            else if (state.getAdvance() > AFTER_FOUND.getAdvance() &&
                     state.getAdvance() < INTERRUPTED.getAdvance())
                state = AFTER_FOUND;
        }
        checkState();
    }

    private void checkState() {
        if (state.equals(INTERRUPTED))
            throw new IllegalStateException(
                    "current state is INTERRUPTED, reset it to BEFORE_FIND first");
        if (state.equals(REPLACED)) {
            state = INTERRUPTED;
            throw new IllegalStateException(
                    "Nothing to do because everything has been already replaced, "
                      + "change either folder or profile for continue");
        }
    }

    private void checkInitialRequirements() {
        assert(folder != null || profile != null) 
                : "initial requirements are not enforced";
    }
    
}
