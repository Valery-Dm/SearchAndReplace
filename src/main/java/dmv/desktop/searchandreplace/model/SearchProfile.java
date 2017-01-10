/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Interface <tt>SearchProfile.java</tt> describes methods
 * of 'How to find and replace' profile which includes info
 * of how to read a file, what to find in it, what to exclude 
 * from search and, finally, what to set in replace
 * @author dmv
 * @since 2017 January 02
 */
public interface SearchProfile {

    /**
     * Default Charset that will be used for reading and writing
     * is {@link StandardCharsets#UTF_16}
     */
    static final Charset defaultCharset = StandardCharsets.UTF_16;
    
    /**
     * Get current Charset. The {@link #defaultCharset} will be
     * returned if it was not explicitly set.
     * @return Current {@link Charset} 
     */
    Charset getCharset();
    
    /**
     * Set Charset which will be used for reading and 
     * writing into a file. If the null is passed 
     * then the {@link #defaultCharset} will be used
     * @param charset {@link Charset} 
     */
    SearchProfile setCharset(Charset charset);
    
    /**
     * Will file names be searched and replaced or not.
     * This parameter is false by default.
     * @return true if file needs to be renamed
     */
    boolean isFileName();
    
    /**
     * Set it to true if you need to rename files
     * with the same 'search and replace' rule as 
     * for their content. The word given in method
     * {@link #setReplaceWith(String)} should not
     * contain symbols that are invalid for filenames,
     * it won't be enforced though.
     * @param filename true - rename file, false - skip file name
     */
    SearchProfile setFilename(boolean filename);
    
    /**
     * Get current string that is needed to be found and replaced.
     * @return Current 'what to find' string. It can't be null or empty
     */
    String getToFind();
    
    /**
     * Set string to be found and replaced.
     * It is not appropriate to have a null pointer
     * or an empty string in this role.
     * It should contain at least one character.
     * @param toFind String to be found
     * @throws IllegalArgumentException if given argument is null or empty
     */
    SearchProfile setToFind(String toFind);

    /**
     * Get current string that will be placed instead
     * of 'what to find' one, can be empty string.
     * @return current string to be replaced with, or empty string
     *         if it was not specified before or previous set 
     *         was given null or empty string
     */
    String getReplaceWith();
    
    /**
     * Set new string that will be placed instead 
     * of 'what to find'. If this object is null or empty
     * means that found strings will be removed.
     * Note, that if {@link #setFilename(boolean)} set to true
     * this string should not contain symbols that are invalid 
     * for filenames, it won't be enforced or checked though.
     * @param replaceWith String to be replaced with
     */
    SearchProfile setReplaceWith(String replaceWith);
    
    /**
     * Get current exclusions: suffixes and reversed prefixes
     * of toFind word. Those combinations will not be replaced
     * during 'search and replace' routine. Can be empty.
     * @return Current exclusions
     */
    Exclusions getExclusions();
    
    /**
     * Set new exclusions: suffixes and reversed prefixes
     * of toFind word. Those combinations will not be replaced.
     * Current exclusion set will be overwritten by this one, and
     * if given argument is null or empty nothing will be excluded
     * during the next 'search and replace' operation
     * @param exclusions New set of exclusions
     */
    SearchProfile setExclusions(Exclusions exclusions);

    /**
     * Create a new shallow copy of the underlying object
     * @return shallow copy of this object
     */
    SearchProfile clone();
}
