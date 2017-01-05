/**
 * 
 */
package dmv.desktop.searchandreplace.service;

import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.BEFORE_FIND;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dmv.desktop.searchandreplace.collection.Tuple;
import dmv.desktop.searchandreplace.model.*;


/**
 * Class <tt>FolderWalker.java</tt> implements {@link SearchAndReplace} 
 * interface for Folders and Files. So, it can read files in folder,
 * then search for what needed to be replace, then return found results
 * for preview or actually replace files content and also rename files if needed.
 * Everything in found files will be cached in memory, then 'to find' spots replaced,
 * then modified content will be written back into files. State became 'after-found'.
 * Any parameter may be overridden at any stage, some changes may lead to change in 
 * object's state (to 'before-found') which means that files on disk will be scanned again.
 * and 
 * @author dmv
 * @since 2017 January 02
 */
public class FolderWalker
        implements SearchAndReplace<SearchFolder, SearchProfile, FileSearchResult> {
    
    private SearchFolder folder;
    private SearchProfile profile;
    private State state;
    
    private Random rand = new Random();
    private long[] delays = {100, 500, 1000, 1500, 2000, 2500, 3000};
    
    /**
     * Constructs a Walker with required parameters.
     * They could be reset or changed later.
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
        if (this.profile != null &&
           (!this.profile.getCharset().equals(profile.getCharset()) ||
            !this.profile.getToFind().equals(profile.getToFind())))
            state = BEFORE_FIND;
        this.profile = profile;
    }
    
    private FileReplacements readFileContent(FileReplacements repl) {
        // read and put it into repl
        repl.addContentLine(repl.getFileName() + " read");

        System.out.println(repl.getFileName() + " read");
        try {
            Thread.sleep(delays[rand.nextInt(delays.length)]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return repl;
    }
    
    private FileSearchResult produceResult(FileReplacements repl) {
        if (repl == null) return null;
        // replace markers and build result
        Tuple<Path, Path> modName = new Tuple<>(repl.getFilePath(), null);
        List<Tuple<String, String>> modLines = repl.getModifiedContent()
            .stream()
            .map(line -> new Tuple<>(line, line + ", and modified"))
            .collect(Collectors.<Tuple<String, String>>toList());
        // sum modified lines plus rename
        int mod = repl.getModifiedContent().size() + 1;
        FileSearchResult result = 
                new FileSearchResultImpl(mod, modName, modLines, false, null);
        
        return result;
    }
    
    private FileReplacements produceExceptional(Throwable cause) {
        FileReplacements repl = null;
        
        // Get repl object from Custom Exception where it was saved
        
        return null;
    }
    
    private boolean isValid(Path file) {
        System.out.println(file);
        if (!Files.isDirectory(file) &&
             Files.isReadable(file)) {
            System.out.println(folder.getFileTypes().matches(file.getFileName()));
            return true;
        }
        return false;
    }
    
    @Override
    public Stream<FileSearchResult> preview(Executor exec) {
        if (folder == null || profile == null)
            throw new IllegalStateException("Either folder or profile was not specified");
        try {
            List<CompletableFuture<FileSearchResult>> collect = Files
                 .walk(folder.getFolder(), folder.isSubfolders() ? Integer.MAX_VALUE : 1)
                 .filter(this::isValid)
                 .map(file -> CompletableFuture.supplyAsync(() -> new FileReplacements(file, profile)))
                 .map(future -> future.thenApplyAsync(this::readFileContent, exec))
                 //.map(future -> future.exceptionally(this::produceExceptional))
                 .map(future -> future.thenApplyAsync(this::produceResult, exec))
                 .collect(Collectors.<CompletableFuture<FileSearchResult>>toList());
            
            return collect.stream()
                          .map(future -> {
                                try {
                                    return future.get();
                                } catch (InterruptedException | ExecutionException e) {
                                    throw new IllegalStateException(e);
                                }
                            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Override
    public Stream<FileSearchResult> preview() {
        if (folder == null || profile == null)
            throw new IllegalStateException("Either folder or profile was not specified");
        try {
            List<CompletableFuture<FileSearchResult>> collect = Files
                 .walk(folder.getFolder(), folder.isSubfolders() ? Integer.MAX_VALUE : 1)
                 .filter(this::isValid)
                 .map(file -> CompletableFuture.supplyAsync(() -> new FileReplacements(file, profile)))
                 .map(future -> future.thenApplyAsync(this::readFileContent))
                 .map(future -> future.exceptionally(this::produceExceptional))
                 .map(future -> future.thenApplyAsync(this::produceResult))
                 .collect(Collectors.<CompletableFuture<FileSearchResult>>toList());
            
            return collect.parallelStream()
                          .map(future -> {
                                try {
                                    return future.get();
                                } catch (InterruptedException | ExecutionException e) {
                                    throw new IllegalStateException(e);
                                }
                            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Stream<FileSearchResult> replace() {
        return null;
    }

    @Override
    public Stream<FileSearchResult> replace(Executor exec) {
        return null;
    }

}
