/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.nio.file.Path;
import java.util.List;

import dmv.desktop.searchandreplace.collection.Tuple;


/**
 * Immutable <tt>SearchResultImpl.java</tt> is a collection
 * of computed result of file searching and replacing operation
 * @author dmv
 * @since 2017 January 03
 */
public class SearchResultImpl implements SearchResult {
    
    private final int numberOfModificationsMade;
    private final Tuple<Path, Path> modifiedName;
    private final List<Tuple<String, String>> modifiedContent;
    
    private final boolean exceptional;
    private final Throwable cause;
    
    /**
     * Create result with given parameters
     * @param numberOfModificationsMade How many replacements were done in total
     *                                  (including filename changes)
     * @param modifiedName Contains both original and modified name,
     *                     if name was not modified, last path should be null
     * @param modifiedContent Contains original and modified content lines,
     *                        should only contain lines with done replacements 
     * @param exceptional Is this result creation was interrupted. Usually
     *                    other parameters may not present in this case
     * @param cause The cause of interruption
     */
    public SearchResultImpl(int numberOfModificationsMade,
                            Tuple<Path, Path> modifiedName,
                            List<Tuple<String, String>> modifiedContent, 
                            boolean exceptional,
                            Throwable cause) {
        this.numberOfModificationsMade = numberOfModificationsMade;
        this.modifiedName = modifiedName;
        this.modifiedContent = modifiedContent;
        this.exceptional = exceptional;
        this.cause = cause;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchResult#getModifiedName()
     */
    @Override
    public Tuple<Path, Path> getModifiedName() {
        return modifiedName;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchResult#getModifiedContent()
     */
    @Override
    public List<Tuple<String, String>> getModifiedContent() {
        return modifiedContent;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchResult#numberOfModificationsMade()
     */
    @Override
    public int numberOfModificationsMade() {
        return numberOfModificationsMade;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchResult#isExceptional()
     */
    @Override
    public boolean isExceptional() {
        return exceptional;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchResult#getCause()
     */
    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        StringBuilder modContent = new StringBuilder();
        if (modifiedName != null) {
            String modName = modifiedName.getLast() == null ?
                    "name was not modified" : "new file name is " +
                    modifiedName.getLast().getFileName();
            modContent.append("\nResults for a file ")
                      .append(modifiedName.getFirst())
                      .append(":\n")
                      .append(modName)
                      .append("\nNumber Of modifications = ")
                      .append(numberOfModificationsMade);
        }
        if (modifiedContent != null) {
            int maxLen = 10;
            for (Tuple<String, String> line : modifiedContent) {
                if (maxLen-- == 0) break;
                modContent.append("\noriginal: ")
                          .append(line.getFirst())
                          .append("\nmodified: ")
                          .append(line.getLast());
            }
        }
        if (exceptional && cause != null)
            modContent.append("\nprocess was interrupted because:\n")
                      .append(cause.getMessage());
        return modContent.toString();
    }

}
