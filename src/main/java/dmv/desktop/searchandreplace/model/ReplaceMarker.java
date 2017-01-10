/**
 * 
 */
package dmv.desktop.searchandreplace.model;


/**
 * Class <tt>ReplaceMarker.java</tt> is a POJO
 * with information of some place in a file or filename
 * that is found by search engine and is about to be replaced.
 * <p>
 * It has {@code lineNumber} - the index of content line;
 * {@code startIndex} - index of first character of found word
 * and {@code excluded} boolean which specifies if this marker
 * will be excluded from 'replace' operation (i.e. ignored).
 * @author dmv
 * @since 2017 January 06
 */
public class ReplaceMarker {
    
    private int lineNumber;
    private int startIndex;
    private boolean excluded;
    private int hashCode;
    
    /**
     * Create new Marker
     * @param lineNumber Number of line in a file content,
     *                   -1 for filename marker
     * @param startIndex Index of first letter of found word
     * @param excluded   Is this marker excluded from replacement
     */
    public ReplaceMarker(int lineNumber, int startIndex, boolean excluded) {
        this.lineNumber = lineNumber;
        this.startIndex = startIndex;
        this.excluded = excluded;
    }

    /**
     * @return lineNumber Number of line in a file content,
     *                    -1 for filename marker
     */
    public int getLineNumber() {
        return lineNumber;
    }
    /**
     * @param lineNumber Number of line in a file content,
     *                   -1 for filename marker
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
        hashCode = 0;
    }
    
    /**
     * @return Index of first letter of found word
     */
    public int getStartIndex() {
        return startIndex;
    }
    
    /**
     * @param startIndex Index of first letter of found word
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
        hashCode = 0;
    }
    
    /**
     * Is this marker excluded from replacement
     * @return true if it's excluded
     */
    public boolean isExcluded() {
        return excluded;
    }
    
    /**
     * @param excluded Is this marker excluded from replacement
     */
    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    @Override
    public String toString() {
        return String.format(
                "ReplaceMarker [lineNumber=%s, startIndex=%s, excluded=%s]",
                lineNumber, startIndex, excluded);
    }

    @Override
    public int hashCode() {
        if (hashCode != 0) return hashCode;
        /* Ignore 'excluded' boolean */
        final int prime = 31;
        hashCode = 1;
        hashCode = prime * hashCode + lineNumber;
        hashCode = prime * hashCode + startIndex;
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) 
            return false;

        /* Ignore 'excluded' boolean */
        ReplaceMarker other = (ReplaceMarker) obj;
        return lineNumber == other.lineNumber &&
               startIndex == other.startIndex;
    }

}
