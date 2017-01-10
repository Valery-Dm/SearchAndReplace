/**
 * 
 */
package dmv.desktop.searchandreplace.worker;

import java.io.IOException;
import java.nio.file.Path;

import dmv.desktop.searchandreplace.model.FileSearchResult;
import dmv.desktop.searchandreplace.model.SearchProfile;

/**
 * Interface <tt>FileReplacer.java</tt> describes methods for
 * accepting file along with {@link SearchProfile search profile}
 * and reading, replacing, writing operations that can return
 * {@link FileSearchResult} for review of what was done.
 * @author dmv
 * @since 2017 January 03
 */
public interface FileReplacer {
    
    /**
     * Set file path. It could be modified if
     * file renaming enabled in a profile and 
     * replace markers found for it.
     * @param file Path to a file
     */
    void setFile(Path file);
    
    /**
     * Set Search Profile ('How or What to find and replace' information)
     * @param profile Description of what to find and replace
     */
    void setProfile(SearchProfile profile);
    
    /**
     * Read file's content and find what is needed to be
     * replaced according current profile. {@link IOException}
     * will be saved as exceptional result and won't be
     * propagated further.
     * @throws IllegalStateException if either file or profile
     *                               was not set before
     */
    void readFile();
    
    /**
     * If file's content has not been read before,
     * this call triggers {@link #readFile()} method.
     * In case of {@link IOException} exceptional state
     * will be saved in object and this method will return
     * true, as it has an information that way.
     * <p>
     * Normally, true if there is something found that 
     * could be replaced, ignoring current exclusions
     * and file rename rules (so these profile changes
     * won't lead to the new reading file operation).
     * False if there is nothing to change.
     * This state may be changed later given new profile.
     * @return true if there is something found or IO
     *              exception caught. False if there is nothing
     *              to change and the object could be dropped.
     */
    boolean hasReplacements();
    
    /**
     * Make replacements according to current profile
     * and return {@link FileSearchResult result} for a preview
     * @return 'Would-be-replaced' content of a file
     * @throws IllegalStateException if either file or profile
     *                               was not set before
     */
    FileSearchResult getResult();
    
    /**
     * Make replacements according to current profile,
     * write them into a file (may include file renaming operation),
     * and return {@link FileSearchResult result} of what was done.
     * {@link IOException} will be saved as exceptional result and 
     * won't be propagated further.
     * @return Actually replaced content of a file
     * @throws IllegalStateException if either file or profile
     *                               was not set before
     */
    FileSearchResult writeResult();
}
