package dmv.desktop.searchandreplace.model;

import java.nio.file.Path;
import java.util.List;

import dmv.desktop.searchandreplace.collection.Tuple;
import dmv.desktop.searchandreplace.exceptions.ResourceCantBeModifiedException;
import dmv.desktop.searchandreplace.exceptions.ResourceNotExistsException;

/**
 * Class <tt>FileSearchResult.java</tt> is one of variants
 * of how results should be delivered, especially for a
 * 'preview' part. It is planned to be an immutable object
 * of strings 'before' and 'after' replacements were made.
 * One Instance per file with any modifications or exceptions
 * happened.
 * @author dmv
 * @since 2017 January 01
 */
public interface FileSearchResult {

    /**
     * Get name of file, possibly modified.
     * First String is the original name.
     * Second is the modified version or,
     * if second is null, there were no modifications made
     * @return Tuple with original and modified file name,
     *         second parameter may be null if no modifications
     *         were made within file name
     */
    Tuple<Path, Path> getModifiedName();
    
    /**
     * List of file lines before and after modifications.
     * List entries should not be null. The whole list
     * may be null if processing was interrupted exceptionally
     * @return List of file lines before and after modifications.
     */
    List<Tuple<String, String>> getModifiedContent();
    
    /**
     * Number of modifications made including file name
     * replacements.
     * @return Number of modifications made
     */
    int numberOfModificationsMade();
    
    /**
     * If result is empty, but it supposed to be present,
     * it may mean that operation has been interrupted.
     * The cause of interruption may be given in {@link #getCause()}
     * method
     * @return true if process was interrupted exceptionally
     */
    boolean isExceptional();
    
    /**
     * Returns the cause of interruption or null if
     * processing was successful.
     * Usual exception types are {@link ResourceNotExistsException}
     * if specified file does not exist anymore (i.e. somehow it was
     * found during search but it may be deleted by now)
     * and {@link ResourceCantBeModifiedException} if file is not
     * readable or writable (this exception can be thrown during first
     * 'search' step for either not readable or not writable case)
     * @return Cause of interruption
     */
    Throwable getCause();
}
