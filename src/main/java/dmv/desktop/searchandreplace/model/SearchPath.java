/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

/**
 * Interface <tt>SearchPath.java</tt> describes an object
 * that will collect information about some path, and how to search it
 * @author dmv
 * @since 2016 December 31
 */
public interface SearchPath {
    
    /**
     * Default Charset that will be used for reading and writing
     * file names is {@link StandardCharsets#UTF_16}
     */
    static final Charset defaultCharset = StandardCharsets.UTF_16; 

    /**
     * Set the path to be searched. Usually it's a folder
     * @param path A path to a file or folder
     * @throws NullPointerException if argument is null
     */
    SearchPath setPath(Path path);
    
    /**
     * Get current path
     * @return current Path to search 
     */
    Path getPath();
    
    /**
     * Set charset which will be used for reading and writing.
     * If not set, the {@link SearchPath#defaultCharset defaultCharset} 
     * will be used.
     * @param charset A charset object
     * @throws NullPointerException if argument is null
     */
    SearchPath setCharset(Charset charset);
    
    /**
     * Get current Charset
     * @return current Charset
     */
    Charset getCharset();
    
    /**
     * Add a file naming patterns that will be searched through.
     * Should be eligible patterns like 'partofthename', '*.txt', 'foo/bar'.
     * The {@link FileSystem#getPathMatcher(String) PathMatcher} 
     * with 'glob:*.{given pattern}' object will be constructed
     * <p>
     * Previously added patterns will be replaced by this set
     * or removed if new set is empty or null.
     * @param pattern naming patterns for files to search
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
     */
    SearchPath setSubfolders(boolean subfolders);
    
    /**
     * If subfolders are set to be searched
     * @return true if subfolders are set to be searched
     */
    boolean isSubfolders();
}
