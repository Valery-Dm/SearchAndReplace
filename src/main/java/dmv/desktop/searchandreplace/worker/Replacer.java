package dmv.desktop.searchandreplace.worker;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import dmv.desktop.searchandreplace.collection.Triple;
import dmv.desktop.searchandreplace.model.Exclusions;

public class Replacer implements Runnable {
    
    private boolean preview;
    private boolean renameFiles;
    private String toFind;
    private String replaceWith;
    private Exclusions exclusions;
    private Path filePath;
    private Charset charSet;
    /* 
     * First: line index in list 'chars' below; 
     * Second: 'start' index of toFind word (inclusive);
     * Third: 'end' index of toFind word (exclusive)
     */
    private List<Triple<Integer, Integer, Integer>> replaceMarkers;
    private List<StringBuilder> lines;

    public Replacer(Path filePath, 
                    String toFind, 
                    String replaceWith, 
                    Exclusions exclusions,
                    Charset charSet,
                    boolean renameFiles,
                    boolean preview) {
        this.filePath = filePath;
        this.renameFiles = renameFiles;
        this.toFind = toFind;
        this.replaceWith = replaceWith;
        this.exclusions = exclusions;
        this.charSet = charSet;
        this.preview = preview;
        replaceMarkers = new ArrayList<>();
        lines = new ArrayList<>();
    }

    private void rename() throws IOException {
        String fileName = filePath.getFileName().toString();
        if (containsReplacement(fileName, lines.size())) {
            if (preview)
                System.out.println(fileName);
            else
                replaceName(fileName);
        }
    }
    
    private void replaceName(String fileName) throws IOException {
        int len = replaceMarkers.size();
        StringBuilder newName = new StringBuilder(fileName);
        if (len > 0) {
            int i = 1;
            Triple<Integer, Integer, Integer> marker = replaceMarkers.get(len - i);
            while (marker.getFirst() >= lines.size()) {
                newName.replace(marker.getSecond(), marker.getThird(), replaceWith);
                marker = replaceMarkers.get(len - ++i);
            }
        }
        String newFilePath = filePath.getParent() + "/" + newName;
        Files.move(filePath, filePath.resolveSibling(newFilePath));
    }

    private void findAndReplace() throws IOException {
        int idx = 0;
        for (String line : Files.readAllLines(filePath, charSet)) {
            lines.add(new StringBuilder(line));
            containsReplacement(line, idx++);
        }
        if (preview) printFoundLines();
        else         {
            replaceMarkers();
            Files.write(filePath, lines, charSet);
        }
    }
    
    private void printFoundLines() {
        StringBuilder sb = new StringBuilder();
        replaceMarkers.forEach(m -> sb.append(lines.get(m.getFirst())).append("\n"));
        System.out.println(sb);
    }

    private void replaceMarkers() {
        // FileName replace markers are not there yet
        replaceMarkers.forEach(m -> {
            lines.get(m.getFirst()).replace(m.getSecond(), m.getThird(), replaceWith);
        });
    }

    private boolean containsReplacement(String line, int idx) {
        int excludedBefore = replaceMarkers.size();
        int sh;
        int shift = toFind.length() - replaceWith.length();
        int shiftCount = 0;
        int start = 0, finder = 0, end = 0;
        char find = toFind.charAt(finder), ch;
        boolean started = false;
        for (; end < line.length(); end++) {
            ch = line.charAt(end);
            if (ch == find && ++finder < toFind.length()) {
                if (!started) {
                    start = end;
                    started = true;
                }
                find = toFind.charAt(finder);
            } else {
                if (started) {
                    if (finder == toFind.length() && !isExcluded(start, end + 1, line)) {
                        sh = shift * shiftCount++;
                        replaceMarkers.add(new Triple<>(idx, start - sh, end + 1 - sh));
                    }
                    started = false;
                    finder = 0;
                    find = toFind.charAt(finder);
                }
            }
        }
        return excludedBefore != replaceMarkers.size();
    }

    private boolean isExcluded(int s, int e, String line) {
        int start = s - exclusions.maxPrefix();
        start = start < 0 ? 0 : start;
        if (exclusions.containsAnyPrefixes(line.substring(start, s)))
            return true;
        int end = e + exclusions.maxSuffix();
        end = end > line.length() ? line.length() : end;
        if (exclusions.containsAnySuffixes(line.substring(e, end)))
            return true;
        return false;
    }

    @Override
    public void run() {
        try {
            findAndReplace();
            if (renameFiles) rename();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
