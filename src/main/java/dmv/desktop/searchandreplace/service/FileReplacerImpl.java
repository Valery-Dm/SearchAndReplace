/**
 * 
 */
package dmv.desktop.searchandreplace.service;

import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.*;

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
 * Class <tt>FileReplacerImpl.java</tt> implements
 * {@link  FileReplacer} interface enforcing its invariants
 * (i.e. file and profile must not be null).
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
    
    /* Cached result */
    private SearchResult result;
    /* Temporary variables for result creation */
    private String fileName;
    private String replaceWith;
    private int modifications;
    private int toFindLength;
    private int shift;

    /*
     * There is no default constructor by-design,
     * if one is needed, it must call resetToBeforeFind() method
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
    
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setFile(Path file) {
        Objects.requireNonNull(file);
        resetToBeforeFind();
        // Path object is immutable
        this.file = file;
        fileName = file.getFileName().toString();
    }

    @Override
    public void setProfile(SearchProfile profile) {
        Objects.requireNonNull(profile);
        checkProfile(profile);
        replaceWith = profile.getReplaceWith();
        toFindLength = profile.getToFind().length();   
        shift = replaceWith.length() - toFindLength;
        // Getting a copy of SearchProfile
        this.profile = profile.clone();
    }

    @Override
    public boolean hasReplacements() {
        findReplacements();
        return replacements;
    }

    @Override
    public SearchResult getResult() {
        findReplacements();
        return createResult();
    }

    /* 
     * Result will be created during Write operation
     */
    @Override
    public SearchResult writeResult() {
        findReplacements();
        return writeFile();
    }

    /* 
     * Read file if state is BEFORE_FIND or rescan cached content
     * if state is FIND_OTHER, and find Replace markers along
     * the way, in case of IOException create exceptional result.
     * So, state after this method either AFTER_FOUND or INTERRUPTED
     */
    private void findReplacements() {
        checkInitialRequirements();
        // A bit fragile as it depends on State fields ordering
        if (state.ordinal() < EXCLUDE_OTHER.ordinal()) {
            checkStateOrdering();
            readContent();
        } else if (state.equals(EXCLUDE_OTHER)) {
            checkMarkers();
        }
    }

    private void checkMarkers() {
        filenameMarkers.forEach(marker -> {
            checkMarker(marker, fileName);
        });
        contentMarkers.forEach(marker -> {
            checkMarker(marker, content.get(marker.getLineNumber()));
        });
        state = AFTER_FOUND;
    }

    private void readContent() {
        try {
            parseName();
            if (state.equals(BEFORE_FIND))
                Files.readAllLines(file, profile.getCharset())
                     .forEach(this::parseContentLine);
            else rescanContent();
            state = AFTER_FOUND;
        } catch (IOException | SecurityException e) {
            content = new ArrayList<>();
            interrupt(e);
        }
    }

    private SearchResult createResult() {
        if (result != null) return result;
        modifications = 0;
        /* modifications will be computed in methods below */
        Tuple<Path, Path> modifiedName = new Tuple<>(file, rename());
        List<Tuple<String, String>> modifiedContent = getModifiedContent();
        result = new SearchResultImpl(modifications, 
                                      modifiedName, 
                                      modifiedContent, 
                                      false, null);
        state = COMPUTED;
        return result;
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

    private Path rename() {
        String newPath = null;
        int trackModifications = modifications;
        if (profile.isFileName() && filenameMarkers.size() > 0) {
            StringBuilder newName = new StringBuilder(fileName);
            int shiftCount = 0, start, end;
            for (ReplaceMarker marker : filenameMarkers) {
                if (!marker.isExcluded()) {
                    start = marker.getStartIndex() + shift * shiftCount++;
                    end = start + toFindLength;
                    newName.replace(start, end, replaceWith);
                    ++modifications;
                }
            }
            if (trackModifications < modifications)
                newPath = file.getParent() + "/" + newName;
        }
        return newPath != null ? Paths.get(newPath) : null;
    }

    private SearchResult writeFile() {
        if (state.equals(INTERRUPTED)) return result;
        try {
            //createResult();
            // or create result anew as lines are writing
            // in order to catch partial result in case
            // of interruption

            // write file and save result object
            // with what has been replaced or if
            // interrupted by IOException save exceptional
            System.out.println(file.getFileName() + " is writing");
            // rename file if required
            if (result.getModifiedName().getLast() != null) {

                // replace path since it was changed
                file = result.getModifiedName().getLast();
            }
            // change state to final
            state = REPLACED;
        } catch (Exception e) {
            interrupt(e);
        }
        
        return result;
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

    private void rescanContent() {
        int idx = 0;
        for (String line : content) 
            if (containsReplacement(contentMarkers, line, idx++))
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

    private ReplaceMarker createMarker(int idx, int start, int end, String line) {
        return new ReplaceMarker(idx, start, isExcluded(profile.getExclusions(), start, end, line));
    }
    
    private void checkMarker(ReplaceMarker marker, String line) {
        int start = marker.getStartIndex();
        int end = start + profile.getToFind().length();
        marker.setExcluded(isExcluded(profile.getExclusions(), start, end, line));
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

    /* Integrity check */
    
    private void checkInitialRequirements() {
//        if (file == null || profile == null)
//            throw new IllegalStateException("File or profile was not given");
        assert(file != null && profile != null) : "Initial requirements not enforced";
    }

    private void checkStateOrdering() {
        assert(BEFORE_FIND.ordinal() < FIND_OTHER.ordinal() && 
               FIND_OTHER.ordinal() < EXCLUDE_OTHER.ordinal() &&
               EXCLUDE_OTHER.ordinal() < AFTER_FOUND.ordinal() &&
               AFTER_FOUND.ordinal() < COMPUTED.ordinal() &&
               COMPUTED.ordinal() < REPLACED.ordinal() &&
               REPLACED.ordinal() < INTERRUPTED.ordinal()) : 
              "restore State fields ordering: BEFORE_FIND, FIND_OTHER, " +
              "EXCLUDE_OTHER, AFTER_FOUND, COMPUTED, REPLACED, INTERRUPTED";
    }

    private void checkFileName(SearchProfile profile) {
        if (result != null && !result.isExceptional()) {
            Tuple<Path, Path> modifiedName = result.getModifiedName();
            if (profile.isFileName() && modifiedName.getLast() == null)
                modifiedName.setLast(rename());
            else if (!profile.isFileName())
                modifiedName.setLast(null);
        }
    }

    private void checkProfile(SearchProfile profile) {
        if (this.profile != null) {
            if (!this.profile.getCharset().equals(profile.getCharset()) &&
                !state.equals(REPLACED)) 
                resetToBeforeFind();
            else if (!this.profile.getToFind().equals(profile.getToFind())) 
                resetToFindOther();
            else if (!this.profile.getExclusions().equals(profile.getExclusions())) 
                resetToExcludeOther();
            else if (!this.profile.getReplaceWith().equals(profile.getReplaceWith()))
                resetToAfterFound();
            else
                checkFileName(profile);
        }
    }

    /* 'State change' methods */
    
    private void interrupt(Exception e) {
        result = new SearchResultImpl(modifications, null, null, true, e);
        replacements = true;
        state = INTERRUPTED;
    }

    private void resetToBeforeFind() {
        state = BEFORE_FIND;
        content = new ArrayList<>();
        resetReplacements();
    }

    private void resetToFindOther() {
        if (state.ordinal() > FIND_OTHER.ordinal()) {
            checkStateOrdering();
            state = FIND_OTHER;
            resetReplacements();
        }
    }

    private void resetToExcludeOther() {
        if (state.ordinal() > EXCLUDE_OTHER.ordinal()) {
            checkStateOrdering();
            state = EXCLUDE_OTHER;
            removeResult();
        }
    }

    private void resetToAfterFound() {
        if (state.ordinal() > AFTER_FOUND.ordinal()) {
            checkStateOrdering();
            state = AFTER_FOUND;
            removeResult();
        }
    }

    private void resetReplacements() {
        filenameMarkers = new ArrayList<>();
        contentMarkers = new ArrayList<>();
        replacements = false;
        removeResult();
    }

    private void removeResult() {
        result = null;
        modifications = 0;
    }

}
