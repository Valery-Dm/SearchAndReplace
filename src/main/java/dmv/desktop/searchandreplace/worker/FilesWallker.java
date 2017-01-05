package dmv.desktop.searchandreplace.worker;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import dmv.desktop.searchandreplace.model.Exclusions;
import dmv.desktop.searchandreplace.model.FileReplacements;

/**
 * Class <tt>FilesWallker.java</tt> is expected to operate
 * on root directory and 'walks' its content in order to
 * find files to be modified. It will cache those files
 * in memory for possibility of 'after-found' adjustments, like 
 * add or remove exclusions, change replace-with string etc.
 * All 'pre-find' changes (like CharSet, what to find, file types, etc)
 * will remove cached files, so to read them from disk again.
 * @author dmv
 * @since 2016 December 31
 */
public class FilesWallker {

    private static ReentrantLock builderLock = new ReentrantLock();
    private ExecutorService pool;
    
    private Charset charSet;
    private Path rootDirectory;
    private PathMatcher fileTypes;
    private String toFind;
    private String replaceWith;
    private Exclusions exclusions;
    private boolean isExclusionsMarked;
    private boolean isFileNames;
    private boolean isSubfolders;
    private boolean isPreview;
    
    private List<FileReplacements> replacements;
    
    private FilesWallker(Path rootDirectory, 
                         Charset charSet,
                         PathMatcher fileTypes, 
                         boolean isFileNames, boolean isSubfolders,
                         String toFind, String replaceWith, 
                         Exclusions exclusions) {
        this.rootDirectory = rootDirectory;
        this.charSet = charSet;
        this.fileTypes = fileTypes;
        this.isFileNames = isFileNames;
        this.isSubfolders = isSubfolders;
        this.toFind = toFind;
        this.replaceWith = replaceWith;
        this.exclusions = exclusions;
        pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);
    }

    public void preview() throws IOException {
        isPreview = true;
        process();
    }

    public void preview(ExecutorService exec) throws IOException {
        if (exec != null) pool = exec;
        isPreview = true;
        process();
    }
    
    public void replace() throws IOException {
        isPreview = false;
        process();
    }
    
    public void replace(ExecutorService exec) throws IOException {
        if (exec != null) pool = exec;
        isPreview = false;
        process();
    }
    
    public void process() throws IOException {
        getFiles().forEach(this::process);
    }

    private Stream<Path> getFiles() throws IOException {
        return replacements != null ? replacements.stream().map(r -> r.getFilePath()) :
                Files.walk(rootDirectory, isSubfolders ? Integer.MAX_VALUE : 1)
                     .filter(path -> !Files.isDirectory(path) && 
                                      Files.isWritable(path) &&
                                      fileTypes.matches(path.getFileName()));
    }

    private void process(Path path) {
        pool.execute(new Replacer(path, toFind, replaceWith, exclusions, charSet, isFileNames, isPreview));
    }
    
    public Charset getCharSet() {
        return charSet;
    }
    
    public void setCharset(Charset newCharSet) {
        Objects.requireNonNull(newCharSet);
        charSet = newCharSet;
        replacements = null;
    }
    
    public Path getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(Path newDir) {
        Objects.requireNonNull(newDir);
        rootDirectory = newDir;
        replacements = null;
    }
    
    public PathMatcher getFileTypes() {
        return fileTypes;
    }

    public void setFileTypes(PathMatcher newTypes) {
        Objects.requireNonNull(newTypes);
        fileTypes = newTypes;
        replacements = null;
    }
    
    public String getToFind() {
        return toFind;
    }

    public void setToFind(String newFind) {
        Objects.requireNonNull(newFind);
        toFind = newFind;
        replacements = null;
    }
    
    public boolean isSubfolders() {
        return isSubfolders;
    }

    public void setSubfolders(boolean subfolders) {
        isSubfolders = subfolders;
        replacements = null;
    }
    
    public String getReplaceWith() {
        return replaceWith;
    }
    
    public void setReplaceWith(String replaceWith) {
        Objects.requireNonNull(replaceWith);
        this.replaceWith = replaceWith;
    }

    /**
     * @return Current exclusions object for insertion or deletion
     */
    public Exclusions getExclusions() {
        return exclusions;
    }
    
    /**
     * Replace current exclusions with new set,
     * null or empty set means 'no exclusions'.
     * @param exclusions New set of exclusions
     */
    public void setExclusions(Exclusions exclusions) {
        this.exclusions = exclusions;
        isExclusionsMarked = false;
    }
    
    public boolean isFileNames() {
        return isFileNames;
    }
    
    public void setFileNames(boolean isFileNames) {
        this.isFileNames = isFileNames;
    }
    
    public static FilesWallkerBuilder getBuilder(String dirName) {
        builderLock.lock();
        try {
            return new FilesWallkerBuilder(dirName);
        } finally {
            builderLock.unlock();
        }
    }

    public static class FilesWallkerBuilder {
        
        private Charset charSet;
        private Path rootDirectory;
        private List<Path> fileTypes;
        private String toFind;
        private String replaceWith;
        private Set<String> exclusions;
        private boolean isFileNames;
        private boolean isSubfolders;
        
        public FilesWallkerBuilder(String dirName) {
            Objects.requireNonNull(dirName);
            rootDirectory = Paths.get(dirName);
            if (!Files.isDirectory(rootDirectory) || 
                !Files.isReadable(rootDirectory) ||
                !Files.isWritable(rootDirectory))
                throw new IllegalArgumentException("Can't access provided directory");
            /* Set defaults */
            charSet = StandardCharsets.UTF_16;
            isFileNames = false;
            isSubfolders = true;
        }

        public FilesWallker build() {
            if (toFind == null || replaceWith == null)
                throw new IllegalArgumentException("Nothing to do, as what to replace was not specified");
            return new FilesWallker(rootDirectory, 
                                    charSet, 
                                    setFileTypes(), 
                                    isFileNames, isSubfolders, 
                                    toFind, replaceWith, 
                                    setExclusions());
        }
        
        private Exclusions setExclusions() {
            if (exclusions != null) 
                return new Exclusions(exclusions, toFind, true);
            return null;
        }
        
        private PathMatcher setFileTypes() {
            if (fileTypes != null) {
                StringBuilder pattern = new StringBuilder("glob:*.{");
                for (Path path : fileTypes) {
                    pattern.append(path.getFileName());
                    pattern.append(',');
                }
                //pattern.delete(pattern.length() - 1, pattern.length());
                pattern.append("}");
                return FileSystems.getDefault().getPathMatcher(pattern.toString());
            }
            return null;
        }
        
        public FilesWallkerBuilder setCharSet(Charset charSet) {
            Objects.requireNonNull(charSet);
            this.charSet = charSet;
            return this;
        }
        
        public FilesWallkerBuilder setFileTypes(String... fileTypes) {
            if (fileTypes != null && fileTypes.length > 0) {
                try {
                    this.fileTypes = new ArrayList<>();
                    for (String fileType : fileTypes) {
                        Path path = Paths.get(fileType);
                        if (path.getFileName().toString().startsWith("."))
                            throw new Exception("leading dots should be ommited");
                        this.fileTypes.add(path);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
            return this;
        }

        public FilesWallkerBuilder setToFind(String toFind) {
            Objects.requireNonNull(toFind);
            if (toFind.length() < 1)
                throw new IllegalArgumentException("Empty word provided");
            this.toFind = toFind;
            return this;
        }

        public FilesWallkerBuilder setReplaceWith(String replaceWith) {
            Objects.requireNonNull(replaceWith);
            if (replaceWith.length() < 1)
                throw new IllegalArgumentException("Empty word provided");
            this.replaceWith = replaceWith;
            return this;
        }

        public FilesWallkerBuilder setExclusions(String... exclusions) {
            if (exclusions != null && exclusions.length > 0) {
                this.exclusions = new HashSet<>();
                for (String exclusion : exclusions)
                    this.exclusions.add(exclusion);
            }
            return this;
        }
        
        public FilesWallkerBuilder setFileNames(boolean isFileNames) {
            this.isFileNames = isFileNames;
            return this;
        }
        
        public FilesWallkerBuilder setSubfolders(boolean isSubfolders) {
            this.isSubfolders = isSubfolders;
            return this;
        }
    }
}
