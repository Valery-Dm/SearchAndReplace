/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dmv.desktop.searchandreplace.collection.Triple;

/**
 * Class <tt>FileReplacements.java</tt> is a collection that
 * stores original file content and a list of markers with
 * found 'to-be-replaced' spots in a single file.
 * <p>
 * It also provides 'after-found' operations like 'changing and
 * applying exclusion', 'file renaming'
 * @author dmv
 * @since 2016 December 30
 */
public class FileReplacements {

    private Path filePath;
    private SearchProfile profile;
    private Throwable cause;
    
    private String fileName;
    private String replaceWith;
    private String mofifiedName;
    private List<String> content;
    private List<String> modifiedContent;
    private List<Triple<Integer, Integer, Integer>> fileNameMarkers;
    private List<Triple<Integer, Integer, Integer>> fileContentMarkers;
    
    
    public FileReplacements(Path file, SearchProfile profile) {
        filePath = file;
        fileName = file.getFileName().toString();
        this.profile = profile;
        content = new ArrayList<>();
    }
    
    /**
     * Create empty collection for a given file
     * @param fileName File Path
     * @param replaceWith Replacement string
     * @throws NullPointerException if either path or replacement is null
     * @throws IllegalArgumentException if file doesn't exist or cant'be read
     */
    public FileReplacements(String fileName, String replaceWith) {
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(replaceWith);
        this.fileName = fileName;
        filePath = Paths.get(fileName);
        content = new ArrayList<>();
        fileNameMarkers = new ArrayList<>();
        fileContentMarkers = new ArrayList<>();
    }
    
    public Path getFilePath() {
        return filePath;
    }
    
    public SearchProfile getProfile() {
        return profile;
    }
    
    public void addCause(Throwable cause) {
        this.cause = cause;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void addContentLine(String line) {
        Objects.requireNonNull(line);
        content.add(line);
    }
    
    public void addFileNameMarker(Triple<Integer, Integer, Integer> marker) {
        Objects.requireNonNull(marker);
        fileNameMarkers.add(marker);
    }

    public void addFileContentMarker(Triple<Integer, Integer, Integer> marker) {
        Objects.requireNonNull(marker);
        fileContentMarkers.add(marker);
    }
    
    public String getModifiedName() {
//        if (mofifiedName != null)
//            return mofifiedName;
//        if (fileNameMarkers.size() > 0) {
//            StringBuilder newName = new StringBuilder(fileName);
//            fileNameMarkers.forEach(marker -> {
//                newName.replace(marker.getSecond(), marker.getThird(), replaceWith);
//            });
//            mofifiedName = newName.toString();
//            return mofifiedName;
//        }
        return fileName;
    }
    
    public List<String> getModifiedContent() {
//        if (modifiedContent != null)
//            return modifiedContent;
//        if (content.size() > 0 && fileContentMarkers.size() > 0) {
//            modifiedContent = new ArrayList<>();
//            String line;
//            StringBuilder newLine;
//            int m = 0;
//            Triple<Integer, Integer, Integer> marker = fileContentMarkers.get(m);
//            for (int l = 0; l < content.size(); l++) {
//                line = content.get(l);
//                if (marker.getFirst() == l) {
//                    newLine = new StringBuilder(line);
//                    do {
//                        newLine.replace(marker.getSecond(), marker.getThird(), replaceWith);
//                        marker = fileContentMarkers.get(++m);;
//                    } while (marker.getFirst() == l);
//                    modifiedContent.add(newLine.toString());
//                } else {
//                    modifiedContent.add(line);
//                }
//            }
//            
//        }
        return content;
    }
}
