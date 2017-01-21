package dmv.desktop.searchandreplace.service;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import dmv.desktop.searchandreplace.exception.AccessResourceException;
import dmv.desktop.searchandreplace.exception.NothingToReplaceException;
import dmv.desktop.searchandreplace.model.Exclusions;

/**
 * <tt>SearchAndReplace.java</tt> API describes methods
 * for 'search and replace' operation that can be made
 * upon an arbitrary resource W 'Where to search' (resource 
 * type will be specified by a concrete implementation).
 * Using profile H 'How to search and replace'.
 * 'Results' of specified type R will be returned upon call
 * on {@link #preview()} or {@link #replace()} method.
 * <p>
 * It's not thread safe itself but offers parallel execution
 * in case of multiple resources under Root Element (for 
 * instance, series of files and folders)
 * @author dmv
 * @since 2016 December 31
 */
public interface SearchAndReplace<W, H, R> {
    
    /**
     * Describes the State of current operation.
     * States are:
     * <p>
     * {@link #BEFORE_FIND}
     * <p>
     * {@link #FIND_OTHER}
     * <p>
     * {@link #EXCLUDE_OTHER}
     * <p>
     * {@link #AFTER_FOUND}
     * <p>
     * {@link #COMPUTED}
     * <p>
     * {@link #REPLACED}
     * <p>
     * {@link #INTERRUPTED}
     */
    static enum State {
        /**
         * Empty object as no resource has been read yet
         * or after essential parameters were reset.
         * Next state will be no less than {@link #AFTER_FOUND}
         */
        BEFORE_FIND(0), 
        /**
         * Intermittent state means that 'What to find' has changed
         * and cached content needs to be rescanned.
         * Next state will be no less than {@link #AFTER_FOUND}
         */
        FIND_OTHER(10),
        /**
         * Another intermittent state means that set of {@link Exclusions}
         * was replaced and all found markers needs to be rescanned.
         * Next state will be no less than {@link #AFTER_FOUND}
         */
        EXCLUDE_OTHER(20),
        /**
         * Resource has been read and cached, and markers of 'What to find' set
         * and possibly marked as excluded according to current profile, 
         * but nothing was replaced yet.
         */
        AFTER_FOUND(30),
        /**
         * Usually triggered by {@link SearchAndReplace#preview() preview}
         * method. Means that all computations were done and Result is ready,
         * but nothing was replaced yet in underlying resource.
         */
        COMPUTED(40), 
        /**
         * Result is written to the resource. All operations completed successfully.
         */
        REPLACED(50), 
        /**
         * Any of operations above was interrupted. The cause of interruption
         * may be stored inside Result object and returned with it.
         */
        INTERRUPTED(60);
        
        private int advance;

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
     * Get current {@link SearchAndReplace.State State}
     * @return current {@link SearchAndReplace.State State}
     */
    State getState();
    
    /**
     * Get current root element W
     * describing 'Where to search'
     * @return Current root element
     */
    W getRootElement();
    
    /**
     * Set root of 'Where to search' element 
     * (like root folder or database table)
     * to be searched for replacements. It can't be null.
     * {@link SearchAndReplace.State State} will become
     * {@link SearchAndReplace.State#BEFORE_FIND BEFORE_FIND}
     * even if the same resource is given repeatedly.
     * @param root Root element of resource (or 'where to start')
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
     * of how to read and write to the 'Where' resource.
     * It is not appropriate to have null or empty object in this role.
     * {@link SearchAndReplace.State State} will become
     * {@link SearchAndReplace.State#BEFORE_FIND BEFORE_FIND}
     * if profile has new {@link Charset} or 'What to find' word.
     * Otherwise, state will be 
     * {@link SearchAndReplace.State#AFTER_FOUND AFTER_FOUND},
     * or, if current state is exceptional (and these changes are
     * not sufficient enough to reset it) IllegalStateException
     * will be thrown.
     * @param profile 'How to search and replace' profile
     * @throws NullPointerException if given argument is null
     * @throws IllegalStateException when changes are insufficient to
     *                               reset current exceptional state
     */
    void setProfile(H profile);
    
    
    
    /**
     * {@code exec} defaults to {@link ForkJoinPool#commonPool()}
     * @see #preview(Executor)
     */
    List<R> preview();
    
    /**
     * Search for spots to be replaced and serve possible results
     * without actual resource modification. This method will
     * execute using provided Executor (must not be null).
     * @param exec An {@link Executor} to run with
     * @return Results of future replacements
     * @throws NullPointerException if given argument is null
     * @throws AccessResourceException when specified resource cannot be 
     *                                 read or modified
     * @throws NothingToReplaceException if nothing has been found in
     *                                   the entire resource
     * @throws IllegalStateException if root element or profile
     *                               was not given prior to call
     *                               to this method or if current
     *                               state either REPLACED or
     *                               INTERRUPTED
     */
    List<R> preview(Executor exec);
    
    /**
     * {@code exec} defaults to {@link ForkJoinPool#commonPool()}
     * @see #replace(Executor)
     */
    List<R> replace();
    
    /**
     * Get actual results of what has been changed (may differ
     * from that returned by {@link #preview()} method since
     * resources may become unavailable. This method will
     * execute using provided Executor (must not be null).
     * @return Results of actual replacements
     * @param exec An {@link Executor} to run with
     * @throws NullPointerException if given argument is null
     * @throws AccessResourceException when specified resource cannot be 
     *                                 read or modified
     * @throws NothingToReplaceException if nothing has been found in
     *                                   the entire resource
     * @throws IllegalStateException if root element or profile
     *                               was not given prior to call
     *                               to this method or if current
     *                               state either REPLACED or
     *                               INTERRUPTED
     */
    List<R> replace(Executor exec);
}
