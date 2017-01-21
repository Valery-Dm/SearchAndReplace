/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.nio.file.*;
import java.util.Objects;


/**
 * Class <tt>SearchPathImpl.java</tt> implements
 * {@link SearchPath} interface creating immutable
 * instances of that type using builder returned by
 * {@link SearchPathImpl#getBuilder(Path) getBuilder} method
 * <p>
 * Setters will return new instance with replaced parameter.
 * @author dmv
 * @since 2016 December 31
 */
public class SearchPathImpl implements SearchPath {
    
    private final Path path;
    private final boolean subfolders;
    private final PathMatcher fileNamePatterns;
    
    /* to be used with builder */
    private SearchPathImpl(Path path,
                           PathMatcher fileNamePatterns, 
                           boolean subfolders) {
        this.path = path;
        this.fileNamePatterns = fileNamePatterns;
        this.subfolders = subfolders;
    }

    /**
     * @throws NullPointerException if argument is null
     */
    @Override
    public SearchPath setPath(Path path) {
        return new SearchPathBuilder(this)
                        .setPath(path)
                        .build();
    }

    @Override
    public Path getPath() {
        return path;
    }

    /**
     * @throws IllegalArgumentException if malformed pattern provided
     */
    @Override
    public SearchPath setNamePattern(String... pattern) {
        return new SearchPathBuilder(this)
                        .setNamePattern(pattern)
                        .build();
    }

    @Override
    public PathMatcher getNamePattern() {
        return fileNamePatterns;
    }

    @Override
    public SearchPath setSubfolders(boolean subfolders) {
        return new SearchPathBuilder(this)
                        .setSubfolders(subfolders)
                        .build();
    }

    @Override
    public boolean isSubfolders() {
        return subfolders;
    }
    
    @Override
    public String toString() {
        return String.format(
                "SearchPath [path=%s, subfolders=%s, fileNamePatterns=%s]",
                path, subfolders, fileNamePatterns);
    }

    /**
     * Creates new object with given Path which must not be null.
     * @param path Main path to search
     * @throws NullPointerException if path is null
     */
    public static SearchPathBuilder getBuilder(Path path) {
        return new SearchPathBuilder(path);
    }

    /**
     * Creates new {@link SearchPath} object with given Path.
     */
    public static class SearchPathBuilder {
        
        private Path path;
        private boolean subfolders;
        private PathMatcher fileNamePatterns;
        
        private SearchPathBuilder(Path path) {
            Objects.requireNonNull(path);
            this.path = path;
            subfolders = defaultSubfolders;
            fileNamePatterns = defaultPattern;
        }
        
        private SearchPathBuilder(SearchPath searchPath) {
            path = searchPath.getPath();
            subfolders = searchPath.isSubfolders();
            fileNamePatterns = searchPath.getNamePattern();
        }
        
        /**
         * Creates new {@link SearchPath} object with provided
         * parameters or their default values.
         * @return new immutable {@link SearchPath} object
         */
        public SearchPath build() {
            return new SearchPathImpl(path, fileNamePatterns, subfolders);
        }
        
        /**
         * Set the path to be searched. Usually it's a folder
         * @param path A path to a file or folder
         * @return this builder
         * @throws NullPointerException if argument is null
         */
        public SearchPathBuilder setPath(Path path) {
            Objects.requireNonNull(path);
            this.path = path;
            return this;
        }
        
        /**
         * Add a file naming patterns that will be searched through.
         * Should be eligible patterns like 'partofthename', '*.txt', 'foo/bar'.
         * The {@link FileSystem#getPathMatcher(String) PathMatcher} 
         * with 'glob:*.{given pattern}' object will be constructed
         * <p>
         * Previously added patterns will be replaced by this set
         * or removed if new set is empty or null.
         * @param pattern naming patterns for files to search
         * @return this builder
         * @throws IllegalArgumentException if malformed pattern provided
         */
        public SearchPathBuilder setNamePattern(String... pattern) {
            if (pattern != null && pattern.length > 0) {
                StringBuilder result = new StringBuilder("glob:{");
                for (String fileType : pattern) {
                    result.append(fileType);
                    result.append(',');
                }
                result.append("}");
                try {
                    PathMatcher temp = FileSystems.getDefault()
                                                  .getPathMatcher(result.toString());
                    fileNamePatterns = temp;
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            } else fileNamePatterns = defaultPattern;
            return this;
        }
        
        /**
         * Set if subfolders should also be searched (if given {@code path}
         * is a directory then subdirectories will also be scanned)
         * @param subfolders true if you want subfolders to be searched
         * @return this builder
         */
        public SearchPathBuilder setSubfolders(boolean subfolders) {
            this.subfolders = subfolders;
            return this;
        }
    }
}
