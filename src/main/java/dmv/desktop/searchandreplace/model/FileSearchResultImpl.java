/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.util.List;

import dmv.desktop.searchandreplace.collection.Tuple;


/**
 * Class <tt>FileSearchResultImpl.java</tt>
 * @author dmv
 * @since 2017 January 03
 */
public class FileSearchResultImpl implements FileSearchResult {
    
    private int numberOfModificationsMade;
    private Tuple<String, String> modifiedName;
    private List<Tuple<String, String>> modifiedContent;
    
    private boolean exceptional;
    private Throwable cause;
    
    /**
     * @param numberOfModificationsMade
     * @param modifiedName
     * @param modifiedContent
     * @param exceptional
     * @param cause
     */
    public FileSearchResultImpl(int numberOfModificationsMade,
            Tuple<String, String> modifiedName,
            List<Tuple<String, String>> modifiedContent, boolean exceptional,
            Throwable cause) {
        this.numberOfModificationsMade = numberOfModificationsMade;
        this.modifiedName = modifiedName;
        this.modifiedContent = modifiedContent;
        this.exceptional = exceptional;
        this.cause = cause;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.FileSearchResult#getModifiedName()
     */
    @Override
    public Tuple<String, String> getModifiedName() {
        return modifiedName;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.FileSearchResult#getModifiedContent()
     */
    @Override
    public List<Tuple<String, String>> getModifiedContent() {
        return modifiedContent;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.FileSearchResult#numberOfModificationsMade()
     */
    @Override
    public int numberOfModificationsMade() {
        return numberOfModificationsMade;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.FileSearchResult#isExceptional()
     */
    @Override
    public boolean isExceptional() {
        return exceptional;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.FileSearchResult#getCause()
     */
    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        return String
                .format("FileSearchResultImpl [numberOfModificationsMade=%s, modifiedName=%s, modifiedContent=%s, exceptional=%s, cause=%s]",
                        numberOfModificationsMade, modifiedName,
                        modifiedContent != null ? modifiedContent.subList(0,
                                Math.min(modifiedContent.size(), maxLen))
                                : null,
                        exceptional, cause);
    }

}
