/**
 * 
 */
package dmv.desktop.searchandreplace.worker;

import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.AFTER_FOUND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.BEFORE_FIND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.INTERRUPTED;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.PARTIALLY_REPLACED;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.REPLACED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dmv.desktop.searchandreplace.collection.Tuple;
import dmv.desktop.searchandreplace.model.*;
import dmv.desktop.searchandreplace.service.SearchAndReplace.State;


/**
 * Class <tt>FileReplacerImpl.java</tt>
 * @author dmv
 * @since 2017 January 06
 */
public class FileReplacerImpl implements FileReplacer {
    
    private Path file;
    private SearchProfile profile;
    private boolean replacements;
    private State state;
    private List<String> content;
    private List<ReplaceMarker> filenameMarkers;
    private List<ReplaceMarker> contentMarkers;
    
    /* Temporary variables for result creation */
    private FileSearchResult result;
    private int modifications;
    private String fileName;
    private String replaceWith;
    private int toFindLength;
    private int shift;
    
    /*
     * There is no default constructor by-design,
     * if one is needed, it must call reset() method
     * from within for the sake of object's integrity.
     */
    /**
     * Creates new instance with given arguments
     * @param file Path to a file
     * @param profile 'What to find and replace' profile
     * @throws NullPointerException if either of arguments is null
     */
    public FileReplacerImpl(Path file, SearchProfile profile) {
        setFile(file);
        setProfile(profile);
    }

    /* 
     * This setting requires file reading operation
     */
    @Override
    public void setFile(Path file) {
        Objects.requireNonNull(file);
        reset();
        this.file = file;
        fileName = file.getFileName().toString();
    }

    /* 
     * May require file reading operation or markers rescan operation.
     * New Charset and/or 'What to find' will trigger new file reading
     * operation, new set of Exclusion requires markers rescanning.
     * ReplaceWith string will be used during results creation
     * operation only - and this one always starts anew.
     * Same goes with 'file renaming' rule - file name markers will be 
     * collected always but actual renaming will be done just once.
     */
    @Override
    public void setProfile(SearchProfile profile) {
        Objects.requireNonNull(profile);
        if (this.profile != null) {
            if (!this.profile.getCharset().equals(profile.getCharset()) ||
                !this.profile.getToFind().equals(profile.getToFind()))
                reset();
            else if (!this.profile.getExclusions().equals(profile.getExclusions()))
                state = PARTIALLY_REPLACED;
        }
        replaceWith = profile.getReplaceWith();
        toFindLength = profile.getToFind().length();   
        shift = replaceWith.length() - toFindLength;
        this.profile = profile;
    }

    /* 
     * Read file if state is BEFORE_FIND and find Replace markers along
     * the way, in case of IOException create exceptional result
     */
    @Override
    public void readFile() {
        checkProfile();
        if (state.equals(BEFORE_FIND)) {
            try {
                parseName();
                Files.readAllLines(file, profile.getCharset())
                     .forEach(this::parseContentLine);
                state = AFTER_FOUND;
            } catch (IOException e) {
                interrupt(e, 0);
            }
        }
    }

    /*
     * Try to read file first
     */
    @Override
    public boolean hasReplacements() {
        readFile();
        return replacements;
    }

    /* 
     * Make sure file has been read before, check
     * if markers need to be adjusted, then create
     * and return result
     */
    @Override
    public FileSearchResult getResult() {
        readFile();
        checkMarkers();
        return createResult();
    }

    /* 
     * Make sure file has been read before, check
     * if markers need to be adjusted, result will
     * be created during Write operation
     */
    @Override
    public FileSearchResult writeResult() {
        readFile();
        checkMarkers();
        return writeFile();
    }

    private FileSearchResult createResult() {
        if (state.equals(INTERRUPTED)) return result;
        modifications = 0;
        /* modifications will be computed in methods below */
        Tuple<Path, Path> modifiedName = getModifiedName();
        List<Tuple<String, String>> modifiedContent = getModifiedContent();
        return new FileSearchResultImpl(modifications, 
                                        modifiedName, 
                                        modifiedContent, 
                                        false, null);
    }
    
    private List<Tuple<String, String>> getModifiedContent() {
        List<Tuple<String, String>> modifiedContent = new ArrayList<>();
        if (contentMarkers.size() > 0) {
            /* this empty set will be skipped as not modified */
            int idx = -1;
            String line = "";
            StringBuilder newLine = null;
            boolean modified = false;
            /* each marker except first one may be shifted */
            int shiftCount = 0, start, end;
            for (ReplaceMarker marker : contentMarkers) {
                if (idx < marker.getLineNumber()) {
                    idx = marker.getLineNumber();
                    if (modified)
                        modifiedContent.add(new Tuple<>(line, newLine.toString()));
                    modified = false;
                    shiftCount = 0;
                    line = content.get(idx);
                    newLine = new StringBuilder(line);
                }
                if (!marker.isExcluded()) {
                    start = marker.getStartIndex() + shift * shiftCount++;
                    end = start + toFindLength;
                    newLine.replace(start, end, replaceWith);
                    modified = true;
                    ++modifications;
                }
            }
            if (modified)
                modifiedContent.add(new Tuple<>(line, newLine.toString()));
        }
        return modifiedContent;
    }

    private Tuple<Path, Path> getModifiedName() {
        String newName = rename();
        Path newFile = newName != null ? Paths.get(newName) : null;
        return new Tuple<>(file, newFile);
    }

    private String rename() {
        String result = null;
        int trackModifications = modifications;
        if (profile.isFileName() && filenameMarkers.size() > 0) {
            StringBuilder newName = new StringBuilder(fileName);
            int shiftCount = 0;
            for (ReplaceMarker marker : filenameMarkers) {
                if (!marker.isExcluded()) {
                    int start = marker.getStartIndex() + shift * shiftCount++;
                    int end = start + toFindLength;
                    newName.replace(start, end, replaceWith);
                    ++modifications;
                }
            }
            if (trackModifications < modifications)
                result = file.getParent() + "/" + newName;
        }
        return result;
    }

    private FileSearchResult writeFile() {
        if (state.equals(INTERRUPTED)) return result;
        // write file and save result object
        // with what has been replaced or if
        // interrupted by IOException
        System.out.println(file.getFileName() + " is writing");
        // change state to final
        state = REPLACED;
        return null;
    }

    private void interrupt(IOException e, int modificationsDone) {
        reset();
        replacements = true;
        state = INTERRUPTED;
        result = new FileSearchResultImpl(modificationsDone, null, null, true, e);
    }

    private void parseName() {
        /* filename index */
        int idx = -1;
        if (containsReplacement(filenameMarkers, fileName, idx))
            replacements = true;
    }

    private void parseContentLine(String line) {
        // copy line and find markers
        content.add(line);
        int idx = content.size() - 1;
        if (containsReplacement(contentMarkers, line, idx))
            replacements = true;
    }
    
    private boolean containsReplacement(List<ReplaceMarker> markers, String line, int idx) {
        /* track changes */
        int markedBefore = markers.size();
        /* 
         * start - index where toFind word starts 
         * end - content line pointer 
         * finder - toFind word pointer
         */
        int start = 0, finder = 0, end = 0;
        String toFind = profile.getToFind();
        char find = toFind.charAt(finder);
        /* if we are scanning inside toFind word */
        boolean started = false;
        for (; end < line.length(); end++) {
            if (line.charAt(end) == find && ++finder < toFind.length()) {
                if (!started) {
                    start = end;
                    started = true;
                }
                find = toFind.charAt(finder);
            } else {
                if (started) {
                    if (finder == toFind.length()) 
                        markers.add(createMarker(idx, start, end + 1, line));
                    started = false;
                    finder = 0;
                    find = toFind.charAt(finder);
                } else if (line.charAt(end) == find && toFind.length() == 1) {
                    // Special case: toFind is one character long
                    finder = 0;
                    markers.add(createMarker(idx, end, end + 1, line));
                }
            }
        }
        return markedBefore != markers.size();
    }

    private void checkMarkers() {
        if (state.equals(PARTIALLY_REPLACED)) {
            filenameMarkers.forEach(marker -> {
                checkMarker(marker, fileName);
            });
            contentMarkers.forEach(marker -> {
                checkMarker(marker, content.get(marker.getLineNumber()));
            });
            state = AFTER_FOUND;
        }
    }
    
    private void checkMarker(ReplaceMarker marker, String line) {
        int start = marker.getStartIndex();
        int end = start + profile.getToFind().length();
        marker.setExcluded(isExcluded(profile.getExclusions(), start, end, line));
    }

    private ReplaceMarker createMarker(int idx, int start, int end, String line) {
        return new ReplaceMarker(idx, start, isExcluded(profile.getExclusions(), start, end, line));
    }
    
    private boolean isExcluded(Exclusions exclusions, int s, int e, String line) {
        int start = s - exclusions.maxPrefixSize();
        start = start < 0 ? 0 : start;
        if (exclusions.containsAnyPrefixes(line.substring(start, s), true))
            return true;
        int end = e + exclusions.maxSuffixSize();
        end = end > line.length() ? line.length() : end;
        if (exclusions.containsAnySuffixes(line.substring(e, end)))
            return true;
        return false;
    }

    private void checkProfile() {
        if (file == null || profile == null)
            throw new IllegalStateException("File or profile was not given");
    }

    private void reset() {
        state = BEFORE_FIND;
        content = new ArrayList<>();
        filenameMarkers = new ArrayList<>();
        contentMarkers = new ArrayList<>();
        replacements = false;
        result = null;
        modifications = 0;
    }

}
