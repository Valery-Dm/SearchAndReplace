/**
 * 
 */
package dmv.desktop.searchandreplace.view.profile;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import dmv.desktop.searchandreplace.model.*;
import dmv.desktop.searchandreplace.service.FolderWalker;
import dmv.desktop.searchandreplace.service.SearchAndReplace;


/**
 * Class <tt>ReplaceFilesProfileImpl.java</tt> implements
 * {@link ReplaceFilesProfile} interface providing simple 
 * refinements of incoming arguments and also an ability 
 * to read and save profiles onto disk.
 * @author dmv
 * @since 2017 January 23
 */
public class ReplaceFilesProfileImpl implements ReplaceFilesProfile {
    
    /* this profile name */
    private String name;
    /* is this profile allowed to overwrite existing one */
    private boolean overwriteExisting;
    /* parameters */
    private String path;
    private String toFind;
    private String replaceWith;
    private String filenames;
    private String subfolders;
    private String charset;
    private Set<String> includeNamePatterns;
    private Set<String> exclusions;
    
    /**
     * Create new profile. 
     * Set parameters to their defaults.
     */
    public ReplaceFilesProfileImpl() {
        this.name = "";
        this.path = "";
        this.toFind = "";
        this.replaceWith = "";
        this.filenames = "false";
        this.subfolders = "false";
        this.charset = "";
        this.includeNamePatterns = newSet();
        this.exclusions = newSet();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ReplaceFilesProfile setName(String name) {
        this.name = checkName(name);
        return this;
    }

    @Override
    public Path getPath() throws WrongProfileException {
        try {
            if (path.length() == 0) throw new Exception();
            return Paths.get(path);
        } catch (Exception e) {
            throw new WrongProfileException("Wrong path provided");
        }
    }

    @Override
    public ReplaceFilesProfile setPath(String path) {
        this.path = refine(path);
        return this;
    }

    @Override
    public String[] getIncludeNamePatterns() {
        return includeNamePatterns.toArray(new String[0]);
    }

    @Override
    public ReplaceFilesProfile addIncludeNamePattern(String pattern) {
        if (pattern == null || pattern.length() == 0) 
            includeNamePatterns = newSet();
        else includeNamePatterns.add(pattern);
        return this;
    }

    @Override
    public boolean getSubfolders() throws WrongProfileException {
        return getBoolean(subfolders);
    }

    @Override
    public ReplaceFilesProfile setSubfolders(String subfolders) {
        this.subfolders = refine(subfolders);
        return this;
    }

    @Override
    public Charset getCharset() throws WrongProfileException {
        try {
            return charset.length() == 0 ? null :
                   Charset.forName(charset);
        } catch (Exception e) {
            throw new WrongProfileException("Wrong charset name");
        }
    }

    @Override
    public ReplaceFilesProfile setCharset(String charset) {
        this.charset = refine(charset);
        return this;
    }

    @Override
    public boolean getFilenames() throws WrongProfileException {
        return getBoolean(filenames);
    }

    @Override
    public ReplaceFilesProfile setFilenames(String filenames) {
        this.filenames = refine(filenames);
        return this;
    }

    @Override
    public String getToFind() throws WrongProfileException {
        if (toFind.length() < 1)
            throw new WrongProfileException("What to find was not given");
        return toFind;
    }

    @Override
    public ReplaceFilesProfile setToFind(String toFind) {
        this.toFind = refine(toFind);
        return this;
    }

    @Override
    public String getReplaceWith() {
        return replaceWith;
    }

    @Override
    public ReplaceFilesProfile setReplaceWith(String replaceWith) {
        this.replaceWith = refine(replaceWith);
        return this;
    }

    @Override
    public Exclusions getExclusions() throws WrongProfileException {
        try {
            return exclusions.size() == 0 ? null :
                new ExclusionsTrie(exclusions, getToFind(), true);
        } catch (IllegalArgumentException e) {
            throw new WrongProfileException(e);
        }
    }

    @Override
    public ReplaceFilesProfile addExclusion(String exclusion) {
        if (exclusion == null || exclusion.length() == 0) 
            exclusions = newSet();
        else exclusions.add(exclusion);
        return this;
    }

    @Override
    public ReplaceFilesProfile useProfile(String profileName) {
        // TODO read profile and apply its non-empty settings
        // or throw IllegalArgumentException if profile can't
        // be read or parsed
        
        // when no exceptions were thrown set {@code overwriteExisting}
        // to true
        overwriteExisting = true;
        return this;
    }

    @Override
    public ReplaceFilesProfile saveProfie(String profileName) {
        // TODO save profile with given name or (argument empty)
        // with existing name or auto-generated name.
        // If profile with given name already exists and
        // {@code overwriteExisting} is false
        // throw IllegalArgumentException to notify program user
        if (overwriteExisting) {
            
        }
        return this;
    }
    
    @Override
    public ReplaceFilesProfile setOverwrite() {
        overwriteExisting = true;
        return this;
    }
    
    @Override
    public SearchAndReplace<SearchPath, SearchProfile, SearchResult> 
                                createService() throws WrongProfileException {
        try {
            SearchPath folder = SearchPathImpl.getBuilder(getPath())
                    .setSubfolders(getSubfolders())
                    .setNamePattern(getIncludeNamePatterns())
                    .build();
            SearchProfile profile = SearchProfileImpl.getBuilder(getToFind())
                    .setCharset(getCharset())
                    .setReplaceWith(getReplaceWith())
                    .setFilename(getFilenames())
                    .setExclusions(getExclusions())
                    .build();
            return new FolderWalker(folder, profile);
        } catch (Exception e) {
            throw new WrongProfileException(e);
        }
    }
    
    @Override
    public String toString() {
        /* as described in javadoc */     
        return String.format(
                "Name of a profile:\n%s\nOverwrite profile with the same name:\n%s\n" +
                "Where to search, path (required):\n%s\nWhat to find (required):\n%s\n" +
                "What to put in replace:\n%s\nCharset to use:\n%s\nModify filenames:\n%s\n" +
                "Scan subfolders:\n%s\nInclude file name patterns:\n%s\n%s",
                toString(name), overwriteExisting, toString(path), toString(toFind), replaceWith, 
                toString(charset), filenames, subfolders, toString(includeNamePatterns), toStrings(exclusions));
    }

    private String toStrings(Set<String> exclusions) {
        if (exclusions.size() == 0) return "exclusion:\nCurrently not set\n";;
        StringBuilder lines = new StringBuilder();
        exclusions.forEach(exclusion -> lines.append("exclusion:\n")
                                             .append(exclusion)
                                             .append("\n")); 
        return lines.toString();
    }

    private String toString(Set<String> set) {
        return set.size() > 0 ? String.join(", ", set) : "Currently not set";
    }

    private String toString(String s) {
        return s.length() > 0 ? s : "Currently not set";
    }

    private String checkName(String name) {
        name = refine(name);
        if (name.endsWith(FILE_EXTENSION)) 
            name = name.substring(0, name.length() - FILE_EXTENSION.length());
        if (name.length() == 0 || name.length() > NAME_SIZE)
            throw new IllegalArgumentException("wrong name length");
        for (char ch : name.toCharArray())
            if (!isAllowed(ch))
                throw new IllegalArgumentException("wrong character used");
        return name;
    }

    private boolean isAllowed(char ch) {
        return (ch == 95) || 
               (ch >= 48 && ch <= 57) ||
               (ch >= 65 && ch <= 90) || 
               (ch >= 97 && ch <= 122);
    }

    private Set<String> newSet() {
        return new HashSet<>();
    }

    private String refine(String param) {
        return param != null ? param : "";
    }
    
    private boolean getBoolean(String setting) throws WrongProfileException {
        /*
         * It may be done just by using Boolean.parse method, but...
         * I want to notify user in case of unintentionally wrong input.
         * For instance, user typed word 'ture' (yeah, simply mistyped word 'true')
         * and my program will silently apply it as 'false', and run with this
         * unexpected setting, and therefore create undesirable output.
         * It won't happen if program will throw an exception instead.
         */
        setting = setting.toLowerCase();
        if (RECOGNIZED_BOOLEANS.containsKey(setting)) 
            return RECOGNIZED_BOOLEANS.get(setting);
        throw new WrongProfileException("unknown state - must be true or false");
    }
}
