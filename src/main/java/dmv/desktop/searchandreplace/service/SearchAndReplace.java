/**
 * 
 */
package dmv.desktop.searchandreplace.service;

import java.util.concurrent.Executor;
import java.util.stream.Stream;

/**
 * <tt>SearchAndReplace.java</tt> API describes methods
 * for 'search and replace' operation that can be made
 * upon an arbitrary resource W 'Where to search' (resource 
 * type will be specified by a concrete implementation).
 * Using profile H 'How to search and replace'.
 * 'Results' of specified type R will be returned upon call
 * on {@link #preview()} and {@link #replace()} methods
 * @author dmv
 * @since 2016 December 31
 */
public interface SearchAndReplace<W, H, R> {
    
    /**
     * Describes state of current operation.
     * Can be used for caching and cache overriding
     * operations or the like and for a feedback.
     */
    static enum State {
        BEFORE_FIND, AFTER_FOUND, REPLACED,
        PARTIALLY_REPLACED, INTERRUPTED
    }
    
    /**
     * Get current root element W
     * Describing 'Where to search'
     * @return Current root element
     */
    W getRootElement();
    
    /**
     * Set root 'Where to search' element 
     * (like root folder or database table)
     * to be searched for replacements. It can't be null
     * @param root Root element
     * @throws NullPointerException if given argument is null
     */
    void setRootElement(W root);

    /**
     * Get current profile describing what is needed to be found
     * and what to put in place of it and any additional info
     * of how to read and write to the 'Where' resource
     * @return current 'How to search and replace' profile
     */
    H getProfile();
    
    /**
     * Set profile describing what is needed to be found
     * and what to put in place of it and any additional info
     * of how to read and write to the 'Where' resource
     * It is not appropriate to have null or empty string in this role
     * @param profile 'How to search and replace' profile
     * @throws NullPointerException if given argument is null
     */
    void setProfile(H profile);
    
    
    
    /**
     * Search for spots to be replaced and serve possible results
     * without actual resource modification.
     * @return Results of future replacements
     * @throws IllegalStateException if root element or profile
     *                               was not given prior to call
     *                               to this method
     */
    Stream<R> preview();
    
    /**
     * Search for spots to be replaced and serve possible results
     * without actual resource modification. This method will
     * execute using provided Executor (must not be null).
     * @return Results of future replacements
     * @throws NullPointerException if given argument is null
     * @throws IllegalStateException if root element or profile
     *                               was not given prior to call
     *                               to this method
     */
    Stream<R> preview(Executor exec);
    
    /**
     * Get actual results of what has been changed (may differ
     * from that returned by {@link #preview()} method since
     * resources may become unavailable.
     * @return Results of done replacements
     * @throws IllegalStateException if root element or profile
     *                               was not given prior to call
     *                               to this method
     */
    Stream<R> replace();
    
    /**
     * Get actual results of what has been changed (may differ
     * from that returned by {@link #preview()} method since
     * resources may become unavailable. This method will
     * execute using provided Executor (must not be null).
     * @return Results of done replacements
     * @throws NullPointerException if given argument is null
     * @throws IllegalStateException if root element or profile
     *                               was not given prior to call
     *                               to this method
     */
    Stream<R> replace(Executor exec);
}
