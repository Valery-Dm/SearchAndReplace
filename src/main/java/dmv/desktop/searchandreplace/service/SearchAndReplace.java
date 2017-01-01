/**
 * 
 */
package dmv.desktop.searchandreplace.service;

import dmv.desktop.searchandreplace.model.Exclusions;

/**
 * <tt>SearchAndReplace.java</tt> API describes methods
 * for 'search and replace' operation that can be made
 * upon an arbitrary resource T (resource type will be specified 
 * by a concrete implementation).
 * Types of what to find and replace are strings.
 * Results of specified type R will be returned upon call
 * on {@link #preview()} and {@link #replace()} methods
 * @author dmv
 * @since 2016 December 31
 */
public interface SearchAndReplace<T, R> {
    
    /**
     * Get current root element 
     * @return Current root element
     */
    T getRootElement();
    
    /**
     * Set root element 
     * (like root folder or database table)
     * to be searched for replacements. It can't be null
     * @param root Root element
     * @throws NullPointerException if given argument is null
     */
    void setRootElement(T root);

    /**
     * Get current string needed to be found
     * @return current string to be found
     */
    String getToFind();
    
    /**
     * Set string to be found and replaced.
     * It is not appropriate to have null
     * or empty string in this role
     * @param toFind String to be found
     * @throws IllegalArgumentException if given argument is null or empty
     */
    void setToFind(String toFind);
    
    /**
     * Get current string that will be placed instead
     * of toFind one, or null if no replacements needed
     * @return current string to be replaced with, or null if
     *         it was not specified before or previous set 
     *         was given null or empty string
     */
    String getReplaceWith();
    
    /**
     * Set new string that will be placed instead 
     * of toFind. If this object is null or empty
     * means that found toFind strings will be removed
     * (i.e. replaced by 0-length strings).
     * @param replaceWith String to be replaced with
     */
    void setReplaceWith(String replaceWith);
    
    /**
     * Get current exclusions: suffixes and reversed prefixes
     * of toFind word. Those combinations will not be replaced
     * during 'search and replace' routine.
     * @return Current exclusions
     */
    Exclusions getExclusions();
    
    /**
     * Set new exclusions: suffixes and reversed prefixes
     * of toFind word. Those combinations will not be replaced.
     * Current exclusion will be overwritten by this set, and
     * if given argument is null or empty nothing will be excluded
     * by the next 'search and replace' operation
     * @param exclusions New set of exclusions
     */
    void setExclusions(Exclusions exclusions);
    
    /**
     * Search for spots to be replaced and serve possible results
     * without actual resource modification.
     * @return Results of future replacements
     */
    R preview();
    
    /**
     * Get actual results of what has been changed (may differ
     * from that returned by {@link #preview()} method since
     * resources may become unavailable.
     * @return Results of done replacements
     */
    R replace();
}
