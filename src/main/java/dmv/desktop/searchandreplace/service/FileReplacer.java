/**
 * 
 */
package dmv.desktop.searchandreplace.service;

import java.nio.charset.Charset;
import java.nio.file.Path;

import dmv.desktop.searchandreplace.model.Exclusions;
import dmv.desktop.searchandreplace.model.SearchProfile;
import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.service.SearchAndReplace.State;

/**
 * Interface <tt>FileReplacer.java</tt> describes methods for
 * accepting file along with {@link SearchProfile search profile}
 * and reading, replacing, writing operations that can return
 * {@link SearchResult} for review of what was done.
 * @author dmv
 * @since 2017 January 03
 */
public interface FileReplacer {
    
    /**
     * Get current {@link SearchAndReplace.State State}
     * of operations made by this object. Initial State is
     * {@link SearchAndReplace.State#BEFORE_FIND BEFORE_FIND}
     * @return Current State
     */
    State getState();
    
    /**
     * Set file path. It could be modified if
     * file renaming enabled in a profile and 
     * replace markers found for it. Must not be null.
     * <p>
     * This setting will require another file reading operation
     * even if given path is the same as before.
     * (i.e. State will become {@link SearchAndReplace.State#BEFORE_FIND
     * BEFORE_FIND} again)
     * @param file Path to a file
     * @throws NullPointerException if argument is null
     */
    void setFile(Path file);
    
    /**
     * Set {@link SearchProfile} with 'How or What to find and 
     * replace' information.
     * <p>
     * May require another file reading operation or re-computation:
     * <p>
     * new {@link Charset} will set for a new file reading operation
     * (i.e. State will become {@link SearchAndReplace.State#BEFORE_FIND
     * BEFORE_FIND} again), 
     * <p>
     * new 'What to find' expression will require cached content rescanning 
     * (State will be {@link SearchAndReplace.State#FIND_OTHER FIND_OTHER}
     *  if it was more advanced at the time). 
     * <p>
     * new set of {@link Exclusions} requires markers rescanning
     * (State will be {@link SearchAndReplace.State#EXCLUDE_OTHER
     * EXCLUDE_OTHER} if it was more advanced at the time), 
     * <p>
     * replaceWith string will be used during results creation
     * operation so, it will change State to {@link 
     * SearchAndReplace.State#AFTER_FOUND AFTER_FOUND}
     * <p>
     * the 'file renaming' rule: {@link SearchAndReplace.State State}
     * won't be changed
     * @param profile Description of what to find and replace
     * @throws NullPointerException if argument is null
     */
    void setProfile(SearchProfile profile);
    
    /**
     * If file's content has not been read before,
     * this call triggers file reading operation.
     * In case of interruption, exceptional state
     * will be saved in Result object and this method 
     * will return true, as it has an information that way.
     * <p>
     * Normally, true if there is something found that 
     * could be replaced, ignoring current exclusions
     * and file rename rules (so these profile changes
     * won't lead to the new reading file operation).
     * False if there is nothing to change.
     * State is no less than {@link SearchAndReplace.State#AFTER_FOUND}
     * This state may be changed later given new profile.
     * @return true if there is something found or an
     *              exception caught. False if there is nothing
     *              to change and the object could be dropped.
     * @throws IllegalStateException if either file or profile
     *                               was not set before
     */
    boolean hasReplacements();
    
    /**
     * If file's content has not been read before,
     * this call triggers file reading operation.
     * Then, it will make replacements according to current profile
     * and return {@link SearchResult result} for a preview.
     * State is no less than {@link SearchAndReplace.State#COMPUTED}
     * @return 'Would-be-replaced' content of a file
     * @throws IllegalStateException if either file or profile
     *                               was not set before
     */
    SearchResult getResult();
    
    /**
     * If file's content has not been read before,
     * this call triggers file reading operation.
     * Then, it will make replacements according to current profile,
     * write them into a file (may include file renaming operation),
     * and return {@link SearchResult result} of what was done.
     * Any caught exception will be returned as exceptional result and 
     * won't be propagated further.
     * State is no less than {@link SearchAndReplace.State#REPLACED}
     * @return Actually replaced content of a file
     * @throws IllegalStateException if either file or profile
     *                               was not set before
     */
    SearchResult writeResult();
}
