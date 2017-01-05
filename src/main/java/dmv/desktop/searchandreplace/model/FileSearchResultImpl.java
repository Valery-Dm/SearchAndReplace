/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.nio.file.Path;
import java.util.List;

import dmv.desktop.searchandreplace.collection.Tuple;


/**
 * Class <tt>FileSearchResultImpl.java</tt>
 * @author dmv
 * @since 2017 January 03
 */
public class FileSearchResultImpl implements FileSearchResult {
    
    private int numberOfModificationsMade;
    private Tuple<Path, Path> modifiedName;
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
     * @see dmv.desktop.searchandreplace.model.FileSearchResult#getModifiedName()
     */
    @Override
    public Tuple<Path, Path> getModifiedName() {
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
        int maxLen = 10;
        StringBuilder modContent = new StringBuilder(maxLen);
        for (Tuple<String, String> line : modifiedContent) {
            if (maxLen-- == 0) break;
            modContent.append("\noriginal: ")
                      .append(line.getFirst())
                      .append("\nmodified: ")
                      .append(line.getLast());
        }
        String modName = modifiedName.getLast() == null ?
                         "name was not modified" : "new file name is " +
                         modifiedName.getLast().getFileName();
        return String.format("\nResults for a file: \n%s \n" +
               "Number Of modifications = %s \n%s %s \nexceptional=%s, cause=%s]",
                modifiedName.getFirst(),        
                numberOfModificationsMade, modName,
                modContent, exceptional, cause);
    }

}
