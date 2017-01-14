/**
 * 
 */
package dmv.desktop.searchandreplace.service;

import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.*;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import dmv.desktop.searchandreplace.collection.Tuple;
import dmv.desktop.searchandreplace.collection.TupleImpl;
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
     * if it was not created before. 
     * In case of any writing problems exceptional result
     * will be cached and returned
     */
    @Override
    public SearchResult writeResult() {
        getResult();
        return writeFile();
    }

    /* 
     * Read file if state is BEFORE_FIND or rescan cached content
     * if state is FIND_OTHER, and find Replace markers along
     * the way, in case of IOException create exceptional result.
     * So, state after this method - either AFTER_FOUND or INTERRUPTED
     */
    private void findReplacements() {
        checkInitialRequirements();
        if (state.getAdvance() < EXCLUDE_OTHER.getAdvance()) {
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
        Tuple<Path, Path> modifiedName = new TupleImpl<>(file, rename(profile));
        List<Tuple<String, String>> modifiedContent = getModifiedContent();
        result = SearchResultImpl.getBuilder()
                                 .setNumberOfModificationsMade(modifications)
                                 .setModifiedName(modifiedName)
                                 .setModifiedContent(modifiedContent)
                                 .build();
        state = COMPUTED;
        return result;
    }
    
    private List<Tuple<String, String>> getModifiedContent() {
        List<Tuple<String, String>> modifiedContent = addOriginalLines();
        checkContentType(modifiedContent);
        if (contentMarkers.size() > 0) {
            /* this empty set will be skipped as not modified */
            int idx = -1;
            StringBuilder newLine = null;
            boolean modified = false;
            /* each marker except first one may be shifted */
            int shiftCount = 0, start, end;
            for (ReplaceMarker marker : contentMarkers) {
                if (idx < marker.getLineNumber()) {
                    /* add modified line */
                    if (modified) 
                        modifiedContent.get(idx)
                                       .setLast(newLine.toString());
                    idx = marker.getLineNumber();
                    modified = false;
                    shiftCount = 0;
                    newLine = new StringBuilder(content.get(idx));
                }
                if (!marker.isExcluded()) {
                    start = marker.getStartIndex() + shift * shiftCount++;
                    end = start + toFindLength;
                    newLine.replace(start, end, replaceWith);
                    modified = true;
                    ++modifications;
                }
            }
            /* last modified line */
            if (modified) 
                modifiedContent.get(idx)
                               .setLast(newLine.toString());
        }
        return modifiedContent;
    }

    private List<Tuple<String, String>> addOriginalLines() {
        return content.stream()
                      .map(line -> new TupleImpl<String, String>(line, null))
                      .collect(Collectors.toList());
    }

    private Path rename(SearchProfile profile) {
        int trackModifications = modifications;
        StringBuilder newName = null;
        if (profile.isFileName() && filenameMarkers.size() > 0) {
            newName = new StringBuilder(fileName);
            int shiftCount = 0, start, end;
            for (ReplaceMarker marker : filenameMarkers) {
                if (!marker.isExcluded()) {
                    start = marker.getStartIndex() + shift * shiftCount++;
                    end = start + toFindLength;
                    newName.replace(start, end, replaceWith);
                    ++modifications;
                }
            }
        }
        return trackModifications < modifications ? 
                   Paths.get(file.getParent() + "/" + newName) : null;
    }

    private SearchResult writeFile() {
        if (state.equals(INTERRUPTED)) return result;
        checkComputedState();
        
        try (BufferedWriter writer = 
                Files.newBufferedWriter(file, profile.getCharset(), TRUNCATE_EXISTING)) {
            // write file and save result object
            // with what has been replaced or if
            // interrupted by IOException save exceptional
            
            // rename file if required
            if (result.getModifiedName().getLast() != null) {

                // replace path since it was changed
                //file = result.getModifiedName().getLast();
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
         * start  - index mark where toFind word starts 
         * end    - content line cursor
         * finder - toFind word cursor
         */
        int start = 0, finder = 0, end = 0;
        /* if we are scanning inside toFind word */
        boolean started = false;
        String toFind = profile.getToFind();
        char findChar = toFind.charAt(finder), lineChar;
        for (; end < line.length(); end++) {
            lineChar = line.charAt(end);
            // reset finder on miss
            if (started && lineChar != findChar) {
                started = false;
                finder = 0;
                findChar = toFind.charAt(finder);
            }
            if (lineChar == findChar) {
                if (!started) {
                    start = end;
                    started = true;
                }
                // reset finder on hit, add marker    
                if (++finder == toFind.length()) {
                    markers.add(createMarker(idx, start, end + 1, line));
                    started = false;
                    finder = 0;
                }
                findChar = toFind.charAt(finder);
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
        assert(file != null && profile != null) : "Initial requirements not enforced";
    }

    private void checkComputedState() {
        assert(state.equals(COMPUTED)) : "Illegal state, must be COMPUTED at this point";
    }

    private void checkContentType(List<?> list) {
        assert(list instanceof RandomAccess) : "List should be of RAM Type";
    }

    private void checkFileName(SearchProfile profile) {
        if (result != null && !result.isExceptional()) {
            Tuple<Path, Path> modifiedName = result.getModifiedName();
            // cancel previous modification count
            if (modifiedName.getLast() != null) modifications--;
            Tuple<Path, Path> newModifiedName = new TupleImpl<>(modifiedName.getFirst(), rename(profile));
            result = SearchResultImpl.getBuilder()
                                     .setResult(result)
                                     .setNumberOfModificationsMade(modifications)
                                     .setModifiedName(newModifiedName)
                                     .build();
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
            else if (this.profile.isFileName() != profile.isFileName())
                checkFileName(profile);
        }
    }

    /* 'State change' methods */
    
    private void interrupt(Exception e) {
        result = SearchResultImpl.getBuilder()
                                 .setExceptional(true)
                                 .setCause(e)
                                 .build();
        replacements = true;
        state = INTERRUPTED;
    }

    private void resetToBeforeFind() {
        state = BEFORE_FIND;
        content = new ArrayList<>();
        resetReplacements();
    }

    private void resetToFindOther() {
        if (state.getAdvance() > FIND_OTHER.getAdvance()) {
            state = FIND_OTHER;
            resetReplacements();
        }
    }

    private void resetToExcludeOther() {
        if (state.getAdvance() > EXCLUDE_OTHER.getAdvance()) {
            state = EXCLUDE_OTHER;
            removeResult();
        }
    }

    private void resetToAfterFound() {
        if (state.getAdvance() > AFTER_FOUND.getAdvance()) {
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
