/**
 * 
 */
package dmv.desktop.searchandreplace.service;

import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.AFTER_FOUND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.BEFORE_FIND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.INTERRUPTED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Objects.requireNonNull(profile);
        this.profile = profile;
    }

    @Override
    public Stream<SearchResult> preview() {
        return preview(COMMON_POOL);
    }

    @Override
    public Stream<SearchResult> preview(Executor exec) {
        Objects.requireNonNull(exec);
        checkInitialRequirements();
        return walk(exec, false);
    }

    @Override
    public Stream<SearchResult> replace() {
        return replace(COMMON_POOL);
    }

    @Override
    public Stream<SearchResult> replace(Executor exec) {
        Objects.requireNonNull(exec);
        checkInitialRequirements();
        return walk(exec, true);
    }

    private Stream<SearchResult> walk(Executor exec, boolean replace) {
        checkState();
        try {
            List<CompletableFuture<SearchResult>> futures = 
                    getFoundFiles(exec)
                         .map(future -> 
                                  future.thenApplyAsync(
                                      replacer -> 
                                          produceResult(replacer, replace), exec))
                         .collect(Collectors.<CompletableFuture<SearchResult>>toList());
            state = AFTER_FOUND;
            return futures.stream()
                          .map(this::complete)
//                          .map(res -> {
//                              System.out.println(res.getModifiedName().getFirst());
//                              return res;
//                          })
                          .filter(result -> result.numberOfModificationsMade() > 0); 
        } catch (IOException e) {
            return Stream.of(interrupt(e));
        } finally {
            COMMON_POOL.shutdown();
        }
    }

    private Stream<CompletableFuture<FileReplacer>> 
                           getFoundFiles(Executor exec) throws IOException {
        if (state.equals(BEFORE_FIND)) {
            foundFiles = new ConcurrentLinkedQueue<>();
            return Files.walk(folder.getPath(), folder.isSubfolders() ? Integer.MAX_VALUE : 1)
                        .filter(this::isPathValid)
                        .map(this::createReplacer)
                        .map(replacer -> CompletableFuture.supplyAsync(() -> replacer, exec))
                        .map(future -> future.thenApplyAsync(this::readFileContent, exec));
        }
        return foundFiles.stream()
                         .map(replacer -> CompletableFuture.supplyAsync(() -> replacer, exec));
    }

    private SearchResult complete(CompletableFuture<SearchResult> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return interrupt(e);
        }
    }

    private SearchResult interrupt(Throwable cause) {
        state = INTERRUPTED;
        return SearchResultImpl.getBuilder()
                               .setExceptional(true)
                               .setCause(cause)
                               .build();
    }
    
    private FileReplacer readFileContent(FileReplacer replacer) {
        if (replacer.hasReplacements()) 
            foundFiles.add(replacer);
        return replacer;
    }
    
    private SearchResult produceResult(FileReplacer replacer, boolean replace) {
        return replace ? replacer.writeResult() : replacer.getResult();
    }
    
    private boolean isPathValid(Path file) {
        return !Files.isDirectory(file) &&
                folder.getNamePattern().matches(file.getFileName());
    }
    
    private FileReplacer createReplacer(Path file) {
        return new FileReplacerImpl(file, profile);
    }
    
    private void checkState() {
        if (state.equals(INTERRUPTED))
            throw new IllegalStateException(
                    "current state is INTERRUPTED, change either folder or profile first");
    }

    private void checkInitialRequirements() {
//        if (folder == null || profile == null)
//            throw new IllegalStateException(
//                    "Either folder or profile was not specified");
        assert(folder != null || profile != null) : "initial requirements are not enforced";
    }
    
}
