package dmv.desktop.searchandreplace.model;

import java.nio.charset.Charset;
import java.util.Set;

import dmv.desktop.searchandreplace.service.FolderWalker;

/**
 * Class <tt>ReplaceFilesProfile.java</tt> describes methods for
 * creating and editing program profiles and build
 * {@link SearchPath} and {@link SearchProfile} objects.
 * to be used with {@link FolderWalker} service.
 * @author dmv
 * @since 2017 January 21
 */
public interface ReplaceFilesProfile {
    
    /**
     * Profile name should not exceed this length
     */
    static final int NAME_SIZE = 30;
    
    /**
     * Get current profile name
     * @return current profile name
     */
    String getName();
    
    /**
     * Set profile name. It will be a name of a file,
     * it may contain only ASCII letters and numbers
     * and underscores. File extension will be added
     * automatically. Any existing file with the
     * same name will be overwritten.
     * <p>
     * Maximum length is {@value #NAME_SIZE} symbols
     * @param name Profile name
     * @return this object
     * @throws IllegalArgumentException if name contains disallowed symbols
     *                                  or its length is greater than
     *                                  {@value #NAME_SIZE} symbols
     */
    ReplaceFilesProfile setName(String name);

    /**
     * Get string representation of path to a file or 
     * to a folder where program will do its search
     * saved in a profile
     * @return current path saved in a profile
     */
    String getPath();
    
    /**
     * Set new path to a file or folder where program
     * will run its search. It won't be validated upon
     * storage, but when the {@link SearchPath} acquired
     * malformed path won't be saved in it. 
     * <p>
     * Maximum size is 255 symbols
     * @param path File or folder path
     * @return this object
     */
    ReplaceFilesProfile setPath(String path);
    
    /**
     * Get current file naming patterns which will
     * be included in {@code search and replace} operation.
     * @return current file naming patterns
     */
    String getIncludeNamePatterns();
    
    /**
     * Set new file naming patterns which will
     * be included in {@code search and replace} operation.
     * They won't be validated upon creation of
     * {@link SearchPath} object. Malformed patterns
     * won't be saved in it.
     * @return this object
     */
    ReplaceFilesProfile setIncludeNamePatterns();
    
    /**
     * Is subfolders should be scanned {@code true}
     * or not {@code false}.
     * @return current subfolders setting
     */
    String isSubfolders();
    
    /**
     * Set if subfolders should be scanned or not using
     * strings {@code true} or {@code false}.
     * @param subfolders {@code true} - include subfolders, {@code false} - skip them
     * @return this object
     * @throws IllegalArgumentException if string is not {@code true} or {@code false}
     */
    ReplaceFilesProfile setSubfolders(String subfolders);
    
    /**
     * Get current charset name.
     * @return current charset
     */
    String getCharset();
    
    /**
     * Set charset name as described {@link Charset here}.
     * It won't be validated upon {@link SearchProfile}
     * creation though and unrecognizable charset won't
     * be stored in it.
     * @param charset Charset name
     * @return this object
     */
    ReplaceFilesProfile setCharset(String charset);
    
    /**
     * If file names should be searched and replaced with the
     * same rule as for their content. Using words {@code true} or {@code false}.
     * @return {@code true} if names will be modified and {@code false} if not
     */
    String isFilenames();
    
    /**
     * Set if file names should be searched and replaced with the
     * same rule as for their content. Use words {@code true} or {@code false}.
     * @param filenames {@code true} if names will be modified 
     * and {@code false} if not.
     * @return this object
     * @throws IllegalArgumentException if string is not {@code true} or {@code false}
     */
    ReplaceFilesProfile setFilenames(String filenames);
    
    /**
     * Get current {@code what to find} string
     * @return current {@code what to find} string
     */
    String getToFind();
    
    /**
     * Set {@code what to find} string. It should contain at least
     * one character. It won't be validated upon {@link SearchProfile}
     * creation though and wrong string won't be stored in it.
     * @param toFind {@code what to find} string
     * @return this object
     */
    ReplaceFilesProfile setToFind(String toFind);
    
    /**
     * Get current string that will be placed instead of found one
     * @return current {@code replace with} string
     */
    String getReplaceWith();
    
    /**
     * Set string that will be placed instead of found one
     * @param replaceWith what to place instead of found string
     * @return this object
     */
    ReplaceFilesProfile setReplaceWith(String replaceWith);
    
    /**
     * Get current set of exclusions (strings that are contain
     * {@code toFind} string in them plus some prefix or suffix
     * and if found such a combination in a file it won't be replaced)
     * @return current set of exclusions
     */
    Set<String> getExclusions();
    
    /**
     * Provide a set of exclusions (strings that are contain
     * {@code toFind} string in them plus some prefix or suffix
     * and if found such a combination in a file it won't be replaced).
     * It won't be checked until creation of {@link SearchProfile}
     * and set with wrong exclusions won't be stored in there.
     * @param exclusions what to exclude from {@code search and replace} operation
     * @return this object
     */
    ReplaceFilesProfile setExclusions(Set<String> exclusions);
}
