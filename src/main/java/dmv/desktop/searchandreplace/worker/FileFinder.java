/**
 * 
 */
package dmv.desktop.searchandreplace.worker;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Consumer;

import dmv.desktop.searchandreplace.model.FileReplacements;

/**
 * Consumer <tt>FileFinder.java</tt> will
 * be given one file and all information of what to
 * find in it in a form of {@link FileReplacements} 
 * object, and it will collect 'to-be-replaced' markers
 * inside given object. 
 * <p>
 * File reading operation will be synchronized on given object
 * @author dmv
 * @since 2016 December 31
 */
public class FileFinder implements Consumer<FileReplacements> {
    
    /* Has all information for searching through the file */
    private FileReplacements replacements;

    /**
     * Accepts object filled with file and 'what to find'
     * information and fill it up with replace markers.
     * Note, that any previously collected markers will
     * be overridden during this find operation.
     * It does not check for any Exclusions, i.e. it collects
     * all needed to be found places as markers inside
     * given object.
     * @throws NullPointerException if given object is null
     */
    @Override
    public void accept(FileReplacements replacements) {
        Objects.requireNonNull(replacements);
        this.replacements = replacements;
        synchronized (this.replacements) {
            readFile();
        }
    }

    private void readFile() {
        int idx = 0;
        try {
            for (String line : Files.readAllLines(replacements.getFilePath())) {
                containsReplacement(line, idx++);
            }
        } catch (IOException e) {
            replacements.getFileName();
        }
    }
    
    private boolean containsReplacement(String line, int idx) {
//        int excludedBefore = replaceMarkers.size();
//        int sh;
//        int shift = toFind.length() - replaceWith.length();
//        int shiftCount = 0;
//        int start = 0, finder = 0, end = 0;
//        char find = toFind.charAt(finder), ch;
//        boolean started = false;
//        for (; end < line.length(); end++) {
//            ch = line.charAt(end);
//            if (ch == find && ++finder < toFind.length()) {
//                if (!started) {
//                    start = end;
//                    started = true;
//                }
//                find = toFind.charAt(finder);
//            } else {
//                if (started) {
//                    if (finder == toFind.length() && !isExcluded(start, end + 1, line)) {
//                        sh = shift * shiftCount++;
//                        replaceMarkers.add(new Triple<>(idx, start - sh, end + 1 - sh));
//                    }
//                    started = false;
//                    finder = 0;
//                    find = toFind.charAt(finder);
//                }
//            }
//        }
//        return excludedBefore != replaceMarkers.size();
        return false;
    }

}
