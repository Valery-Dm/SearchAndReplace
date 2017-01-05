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
 * Interface <tt>SearchFolder.java</tt> describes an object
 * that will collect information about some folder, and how to search it
 * @author dmv
 * @since 2016 December 31
 */
public interface SearchFolder {
    
    /**
     * Default Charset that will be used for reading and writing
     * file names is {@link StandardCharsets#UTF_16}
     */
    static final Charset defaultCharset = StandardCharsets.UTF_16; 

    /**
     * Set root folder to be search inside
     * @param folder A path to a folder
     * @throws NullPointerException if argument is null
     */
    SearchFolder setFolder(Path folder);
    
    /**
     * Get current folder
     * @return current folder
     */
    Path getFolder();
    
    /**
     * Set charset which will be used for reading and writing.
     * If not set, the {@link SearchFolder#defaultCharset defaultCharset} 
     * will be used.
     * @param charset A charset object
     * @throws NullPointerException if argument is null
     */
    SearchFolder setCharset(Charset charset);
    
    /**
     * Get current Charset
     * @return current Charset
     */
    Charset getCharset();
    
    /**
     * Add a file types that will be searched through.
     * These are NOT MIME types but naming patterns.
     * Should be eligible patterns like 'partofthename', '*.txt', 'foo/bar'.
     * The {@link FileSystem#getPathMatcher(String) PathMatcher} 
     * with 'glob:*.{given fileTypes}' pattern will be constructed
     * <p>
     * Previously added types will be replaced by this set
     * or removed if new set is empty.
     * @param fileTypes file types to search through
     */
    SearchFolder setFileTypes(String... fileTypes);
    
    /**
     * Get a PathMatcher object with added file types.
     * To make changes in what file types to search 
     * use {@link #setFileTypes(String...)} method.
     * @return A PathMatcher object with currently added file types
     */
    PathMatcher getFileTypes();
    
    /**
     * Set if subfolders should also be searched
     * @param subfolders true if you want subfolders to be searched
     */
    SearchFolder setSubfolders(boolean subfolders);
    
    /**
     * If subfolders are set to be searched
     * @return true if subfolders are set to be searched
     */
    boolean isSubfolders();
}
