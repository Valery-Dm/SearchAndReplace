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
import dmv.desktop.searchandreplace.worker.FileReplacer;
import dmv.desktop.searchandreplace.worker.FileReplacerImpl;


/**
 * Class <tt>FolderWalker.java</tt> implements {@link SearchAndReplace} 
 * interface for Folders and Files. So, it can read files in folder,
 * then search for what needed to be replaced, then return found results
 * for preview or actually replace files content and also rename files if needed.
 * Everything in found files will be cached in memory, then 'to find' spots replaced,
 * then modified content will be written back into files. State became 'REPLACED'.
 * Any parameter may be overridden at any stage, some changes may lead to change in 
 * object's state (to 'BEFORE_FOUND') which means that files on disk will be scanned again.
 * 
 * @author dmv
 * @since 2017 January 02
 */
public class FolderWalker
        implements SearchAndReplace<SearchFolder, SearchProfile, FileSearchResult> {
    
    private static final ForkJoinPool COMMON_POOL = ForkJoinPool.commonPool();
    
    private SearchFolder folder;
    private SearchProfile profile;
    private Queue<FileReplacer> foundFiles;
    private State state;
    
    /**
     * Constructs a Walker with required parameters.
     * They could be changed later.
     * @param folder 'where to search' parameter
     * @param profile 'what to find' parameter
     */
    public FolderWalker(SearchFolder folder, SearchProfile profile) {
        setRootElement(folder);
        setProfile(profile);
    }

    @Override
    public SearchFolder getRootElement() {
        return folder;
    }

    @Override
    public void setRootElement(SearchFolder folder) {
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
    public Stream<FileSearchResult> preview(Executor exec) {
        Objects.requireNonNull(exec);
        if (folder == null || profile == null)
            throw new IllegalStateException("Either folder or profile was not specified");
        try {
            List<CompletableFuture<FileSearchResult>> collect = 
                    getFoundFiles(exec)
                         .map(future -> future.thenApplyAsync(this::produceResult, exec))
                         .collect(Collectors.<CompletableFuture<FileSearchResult>>toList());
            state = AFTER_FOUND;
            return collect.stream()
                          .map(this::getResult);
        } catch (IOException e) {
            state = INTERRUPTED;
            e.printStackTrace();
            // TODO find a way to notify app's user without crashing
        }
        // must be removed
        return null;
    }

    @Override
    public Stream<FileSearchResult> preview() {
        /* Explicitly set the default pool of CompletableFuture */
        return preview(COMMON_POOL);
    }

    @Override
    public Stream<FileSearchResult> replace() {
        // TODO create replace method
        return null;
    }

    @Override
    public Stream<FileSearchResult> replace(Executor exec) {
        // TODO create replace method
        return null;
    }

    private FileSearchResult getResult(CompletableFuture<FileSearchResult> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return new FileSearchResultImpl(0, null, null, true, e);
        }
    }
    
    private FileReplacer readFileContent(FileReplacer replacer) {
        //replacer.readFile();
        if (replacer.hasReplacements()) 
            foundFiles.add(replacer);
        return replacer;
    }
    
    private FileSearchResult produceResult(FileReplacer replacer) {
        return replacer.getResult();
    }
    
    private FileReplacer produceExceptional(Throwable cause) {
        // Or cancel whole operation?
        return null;
    }
    
    private boolean isPathValid(Path file) {
        return !Files.isDirectory(file) &&
                folder.getFileTypes().matches(file.getFileName());
    }
    
    private Stream<CompletableFuture<FileReplacer>> getFoundFiles(Executor exec) throws IOException {
        if (state.equals(AFTER_FOUND)) {
            return foundFiles.stream()
                             .map(replacer -> CompletableFuture.supplyAsync(() -> replacer, exec));
        }
        foundFiles = new ConcurrentLinkedQueue<>();
        return Files
                .walk(folder.getFolder(), folder.isSubfolders() ? Integer.MAX_VALUE : 1)
                .filter(this::isPathValid)
                .map(file -> new FileReplacerImpl(file, profile))
                .map(replacer -> CompletableFuture.supplyAsync(() -> replacer, exec))
                .map(future -> future.thenApplyAsync(this::readFileContent, exec))
                .map(future -> future.exceptionally(this::produceExceptional));
    }
    
}
