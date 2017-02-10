package dmv.desktop.searchandreplace.view.profile;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.*;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import dmv.desktop.searchandreplace.exception.WrongProfileException;
import dmv.desktop.searchandreplace.model.*;
import dmv.desktop.searchandreplace.service.FolderWalker;
import dmv.desktop.searchandreplace.service.SearchAndReplace;


/**
 * Class <tt>ReplaceFilesProfileImpl.java</tt> implements
 * {@link ReplaceFilesProfile} interface providing storage
 * of incoming arguments, their validation at first time on
 * service creation, then after on argument set and service
 * update operations, and also an ability to read and save profiles.
 * @author dmv
 * @since 2017 January 23
 */
public class ReplaceFilesProfileImpl implements ReplaceFilesProfile {
    
    /* Strings for toString method generation */
    private static final String CURRENTLY_NOT_SET = "Currently not set";
    private static final String OVERWRITE_PROFILE = "\n-overwrite\n";
    private static final String NAME_OF_A_PROFILE = "-name\n";
    private static final int longVersion = 1;
    private static final String PATH_KEY = "\n\n" + KEYS_PATH.get(longVersion) + " ## required\n";
    private static final String FIND_KEY = "\n" + KEYS_FIND.get(longVersion) + " ## required\n";
    private static final String REPLACE_KEY = "\n" + KEYS_REPLACE.get(longVersion) + "\n";
    private static final String CHARSET_KEY = "\n" + KEYS_CHARSET.get(longVersion) + "\n";
    private static final String FILENAMES_KEY = "\n" + KEYS_FILENAMES.get(longVersion) + "\n";
    private static final String SUBFOLDERS_KEY = "\n" + KEYS_SUBFOLDERS.get(longVersion) + "\n";
    private static final String PATTERNS_KEY = "\n" + KEYS_PATTERNS.get(longVersion) + "\n";
    private static final String EXCLUDE_KEY = "\n" + KEYS_EXCLUDE.get(longVersion) + "\n";
    
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
    /* cached service with profiles */
    private SearchAndReplace<SearchPath, SearchProfile, SearchResult> service;
    private SearchPath searchPath;
    private SearchProfile searchProfile;
    
    /**
     * Create new profile. 
     * Set parameters to their defaults
     * (everything is empty or false).
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

    private Path getPath() {
        return path.length() == 0 ? null :
               Paths.get(path);
    }

    @Override
    public ReplaceFilesProfile setPath(String path) {
        // Yes, we save even the possibly wrong path here,
        // because it is a user input, and we are about to
        // show it back exactly as it was given. 
        path = refine(path);
        if (!path.equals(this.path)) {
            this.path = path;
            // Plus, update cached searchPath on second call
            if (searchPath != null) {
                try {
                    searchPath = searchPath.setPath(getPath());
                } catch (Exception e) {
                    throw new WrongProfileException("Wrong or empty path provided", e);
                }
            }
        }
        return this;
    }

    private String[] getIncludeNamePatterns() {
        return includeNamePatterns.toArray(new String[0]);
    }

    @Override
    public ReplaceFilesProfile addIncludeNamePattern(String pattern) {
        boolean changed = false;
        if (pattern == null || pattern.length() == 0) {
            if (includeNamePatterns.size() > 0)
                changed = true;
            includeNamePatterns = newSet();
        } else {
            changed = includeNamePatterns.add(pattern);
        }
        if (changed && searchPath != null) {
            try {
                searchPath = searchPath.setNamePattern(getIncludeNamePatterns());
            } catch (Exception e) {
                throw new WrongProfileException("Wrong naming pattern", e);
            }
        }
        return this;
    }

    private boolean getSubfolders() {
        return getBoolean(subfolders);
    }

    @Override
    public ReplaceFilesProfile setSubfolders(String subfolders) throws WrongProfileException {
        subfolders = refine(subfolders);
        if (!subfolders.equals(this.subfolders)) {
            this.subfolders = subfolders;
            if (searchPath != null) {
                try {
                    searchPath = searchPath.setSubfolders(getSubfolders());
                } catch (Exception e) {
                    throw new WrongProfileException("Unexpected setting for subfolders", e);
                }
            }
        }
        return this;
    }

    private Charset getCharset() {
        return charset.length() == 0 ? null :
               Charset.forName(charset);
    }

    @Override
    public ReplaceFilesProfile setCharset(String charset) {
        charset = refine(charset);
        if (!charset.equals(this.charset)) {
            this.charset = charset;
            if (searchProfile != null) {
                try {
                    searchProfile = searchProfile.setCharset(getCharset());
                } catch (Exception e) {
                    throw new WrongProfileException("Not supported charset", e);
                }
            }
        }
        return this;
    }

    private boolean getFilenames() {
        return getBoolean(filenames);
    }

    @Override
    public ReplaceFilesProfile setFilenames(String filenames) {
        filenames = refine(filenames);
        if (!filenames.equals(this.filenames)) {
            this.filenames = filenames;
            if (searchProfile != null) {
                try {
                    searchProfile = searchProfile.setFilename(getFilenames());
                } catch (Exception e) {
                    throw new WrongProfileException("Unexpected setting for filenames", e);
                }
            }
        }
        return this;
    }

    private String getToFind() {
        return toFind;
    }

    @Override
    public ReplaceFilesProfile setToFind(String toFind) {
        toFind = refine(toFind);
        if (!toFind.equals(this.toFind)) {
            this.toFind = toFind;
            if (searchProfile != null) {
                try {
                    searchProfile = searchProfile.setToFind(getToFind());
                } catch (Exception e) {
                    throw new WrongProfileException("What to find was not given", e);
                }
            }
        }
        return this;
    }

    private String getReplaceWith() {
        return replaceWith;
    }

    @Override
    public ReplaceFilesProfile setReplaceWith(String replaceWith) {
        replaceWith = refine(replaceWith);
        if (!replaceWith.equals(this.replaceWith)) {
            this.replaceWith = replaceWith;
            if (searchProfile != null) 
                searchProfile = searchProfile.setReplaceWith(getReplaceWith());
        }
        return this;
    }

    private Exclusions getExclusions() {
        return exclusions.size() == 0 ? null :
               new ExclusionsTrie(exclusions, getToFind(), true);
    }

    @Override
    public ReplaceFilesProfile addExclusion(String exclusion) {
        boolean changed = false;
        if (exclusion == null || exclusion.length() == 0) {
            if (exclusions.size() > 0) 
                changed = true;            
            exclusions = newSet();
        } else {
            changed = exclusions.add(exclusion);
        }
        if (changed && searchProfile != null) {
            try {
                searchProfile = searchProfile.setExclusions(getExclusions());
            } catch (Exception e) {
                throw new WrongProfileException("Wrong exclusion", e);
            }
        }
        return this;
    }

    @Override
    public ReplaceFilesProfile useProfile(String profileName) {
        name = checkName(profileName);
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
        if (profileName != null && profileName.length() > 0)
            name = checkName(profileName);
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
    public SearchAndReplace<SearchPath, SearchProfile, SearchResult> createService() {
        try {
            if (service != null) {
                service.setRootElement(searchPath);
                service.setProfile(searchProfile);
            } else {
                SearchPath folder = 
                        SearchPathImpl.getBuilder(getPath())
                                      .setSubfolders(getSubfolders())
                                      .setNamePattern(getIncludeNamePatterns())
                                      .build();
                SearchProfile profile = 
                        SearchProfileImpl.getBuilder(getToFind())
                                         .setCharset(getCharset())
                                         .setReplaceWith(getReplaceWith())
                                         .setFilename(getFilenames())
                                         .setExclusions(getExclusions())
                                         .build();
                service = new FolderWalker(folder, profile);
                searchPath = folder;
                searchProfile = profile;
            }
            return service;
        } catch (Exception e) {
            throw new WrongProfileException("Unsuccessful service creation", e);
        }
    }
    
    @Override
    public String toString() {
        /* as described in javadoc */ 
        StringBuilder builder = new StringBuilder(NAME_OF_A_PROFILE);
        builder.append(toString(name))
               .append(OVERWRITE_PROFILE)
               .append(overwriteExisting)
               .append(PATH_KEY)
               .append(toString(path))
               .append(FIND_KEY)
               .append(toString(toFind))
               .append(REPLACE_KEY)
               .append(replaceWith)
               .append(CHARSET_KEY)
               .append(toString(charset))
               .append(FILENAMES_KEY)
               .append(filenames)
               .append(SUBFOLDERS_KEY)
               .append(subfolders)
               .append(PATTERNS_KEY)
               .append(toString(includeNamePatterns))
               .append(toStringExclusions(exclusions));
        return builder.toString();
    }

    private String toStringExclusions(Set<String> exclusions) {
        StringBuilder lines = new StringBuilder();
        if (exclusions.size() == 0) 
            lines.append(EXCLUDE_KEY)
                 .append(CURRENTLY_NOT_SET);
        else
            exclusions.forEach(exclusion -> lines.append(EXCLUDE_KEY)
                                                 .append(exclusion)); 
        return lines.toString();
    }

    private String toString(Set<String> set) {
        return set.size() > 0 ? String.join(", ", set) : CURRENTLY_NOT_SET;
    }

    private String toString(String s) {
        return s.length() > 0 ? s : CURRENTLY_NOT_SET;
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
    
    private boolean getBoolean(String setting) {
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
        throw new IllegalArgumentException("unknown state - must be true or false");
    }
}
