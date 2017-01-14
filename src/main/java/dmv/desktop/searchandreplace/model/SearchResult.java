package dmv.desktop.searchandreplace.model;

import java.nio.file.Path;
import java.util.List;

import dmv.desktop.searchandreplace.collection.Tuple;

/**
 * Class <tt>SearchResult.java</tt> is one of variants
 * of how results should be delivered, especially for a
 * 'preview' part. It is planned to be an immutable object
 * of tuples with strings 'before' and 'after' replacements were made.
 * One Instance per file with any modifications or exceptions
 * happened.
 * @author dmv
 * @since 2017 January 01
 */
public interface SearchResult {

    /**
     * This exception could be thrown at construction time.
     * It checks result for consistency (not a deep check, though):
     * <p>
     * Provide either Exceptional result only, where Cause exists,
     * number of modifications is 0, and all content is null, or 
     * Non-exceptional result with modified content provided and no Exceptions included
     */
    static final IllegalArgumentException NOT_CONSISTENT = new IllegalArgumentException(
                                             "Provide either Exceptional result only, where Cause exists, " +
                                             "number of modifications is 0, and all content is null, or " +
                                             "Non-exceptional result with modified content provided and no Exceptions included");
    
    /**
     * Get name of a file, possibly modified.
     * First String is the original name.
     * Second is the modified version or,
     * if second is null, there were no modifications made
     * for a file name.
     * @return TupleImpl with original and modified file name,
     *         second parameter may be null if no modifications
     *         were made within the file name
     */
    Tuple<Path, Path> getModifiedName();
    
    /**
     * List of file lines before and after modifications.
     * If second entry is null, there were no modifications made
     * for this content line. The whole list may be null if processing 
     * was interrupted exceptionally.
     * @return List of file lines before and after modifications.
     */
    List<Tuple<String, String>> getModifiedContent();
    
    /**
     * Number of modifications made including changes made in a file name
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
     * @return Cause of interruption
     */
    Throwable getCause();
}
