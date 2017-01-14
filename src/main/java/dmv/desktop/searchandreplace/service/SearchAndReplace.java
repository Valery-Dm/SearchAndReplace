package dmv.desktop.searchandreplace.service;

import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import dmv.desktop.searchandreplace.model.Exclusions;

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
     * Describes the State of current operation,
     * which will be stored in Result and returned.
     * BEFORE_FIND means that no file has been read,
     * AFTER_FOUND: file has been read but not computed,
     * COMPUTED: file's content has been computed (virtually replaced) 
     * REPLACED: computed content has been written back to a file
     * INTERRUPTED: any of operations above has been interrupted 
     * with an Exception, that exception will be returned as a part
     * of Result Type (stored inside exceptional Result)
     */
    static enum State {
        /**
         * Empty object as no file has been read yet
         * or after 'Where to find' parameter was reset.
         * <p>
         * It will also fall back into this State from any
         * other except {@link #REPLACED} if new {@link Charset}
         * was given in a profile, so to fix reading errors.
         * Next state will be {@link #AFTER_FOUND}
         */
        BEFORE_FIND(0), 
        /**
         * Intermittent state means that 'What to find' has changed
         * and cached content needs to be rescanned.
         * Next state will be {@link #AFTER_FOUND}
         */
        FIND_OTHER(10),
        /**
         * Another intermittent state means that set of {@link Exclusions}
         * was replaced and all found markers needs to be rescanned.
         * Next state will be {@link #AFTER_FOUND}
         */
        EXCLUDE_OTHER(20),
        /**
         * File has been read and cached, and markers of 'What to find' set
         * and marked as excluded according to current profile, nothing is replaced yet.
         */
        AFTER_FOUND(30),
        /**
         * Usually triggered by {@link SearchAndReplace#preview() preview}
         * method. Means that all computations were done and Result is ready
         */
        COMPUTED(40), 
        /**
         * Result is written to a file. All operations completed successfully.
         * <p>
         * If new profile was set with a new {@link Charset} while object in
         * this State, the next call to {@link SearchAndReplace#replace() replace}
         * method will write cached content to a file with that new encoding.
         */
        REPLACED(50), 
        /**
         * Any of operations above was interrupted. The cause of interruption
         * will be stored inside Result object and returned with it.
         */
        INTERRUPTED(60);
        
        private int advance;

        /**
         * Create advanced state
         * @param advance How advanced the state is
         */
        private State(int advance) {
            this.advance = advance;
        }

        /**
         * How advanced the state is
         * @return advance number
         */
        public int getAdvance() {
            return advance;
        }
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
