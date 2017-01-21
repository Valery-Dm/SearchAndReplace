/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.nio.file.*;

/**
 * Interface <tt>SearchPath.java</tt> describes an object
 * that will collect information about some path, and how to search it.
 * <p>
 * The instances of this type are guaranteed to be immutable and 
 * contain main Path, other arguments will be set to their defaults
 * if not provided by corresponding setters.
 * @author dmv
 * @since 2016 December 31
 */
public interface SearchPath {
    
    /**
     * Default {@link FileSystem#getPathMatcher(String) PathMatcher} 
     * for file names. Find everything - glob:{**}.
     */
    static final PathMatcher defaultPattern = FileSystems.getDefault()
                                                         .getPathMatcher("glob:{**}"); 
    /**
     * Subfolders won't be searched by default
     */
    static final boolean defaultSubfolders = false;

    /**
     * Set the path to be searched. Usually it's a folder
     * @param path A path to a file or folder
     * @return new instance of this type
     * @throws NullPointerException if argument is null
     */
    SearchPath setPath(Path path);
    
    /**
     * Get current path
     * @return current Path to search 
     */
    Path getPath();
    
    /**
     * Add a file naming patterns that will be searched through.
     * Should be eligible patterns like 'partofthename', '*.txt', 'foo/bar'.
     * The {@link FileSystem#getPathMatcher(String) PathMatcher} 
     * with 'glob:*.{given pattern}' object will be constructed
     * <p>
     * Previously added patterns will be replaced by this set
     * or removed if new set is empty or null.
     * @param pattern naming patterns for files to search
     * @return new instance of this type
     * @throws IllegalArgumentException if malformed pattern provided
     */
    SearchPath setNamePattern(String... pattern);
    
    /**
     * Get a PathMatcher object with added naming patterns.
     * To make changes in what files needed to be searched
     * use {@link #setNamePattern(String...)} method.
     * @return A PathMatcher object with currently added patterns
     *         or null if no patterns have been set
     */
    PathMatcher getNamePattern();
    
    /**
     * Set if subfolders should also be searched (if given {@code path}
     * is a directory then subdirectories will also be scanned)
     * @param subfolders true if you want subfolders to be searched
     * @return new instance of this type
     */
    SearchPath setSubfolders(boolean subfolders);
    
    /**
     * If subfolders are set to be searched
     * @return true if subfolders are set to be searched
     */
    boolean isSubfolders();
}
