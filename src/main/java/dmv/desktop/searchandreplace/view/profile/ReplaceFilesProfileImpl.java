/**
 * 
 */
package dmv.desktop.searchandreplace.view.profile;

import java.util.*;


/**
 * Class <tt>ReplaceFilesProfileImpl.java</tt> implements
 * {@link ReplaceFilesProfile} interface providing simple 
 * refinements of incoming arguments and also an ability 
 * to read and save profile to disk.
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
        this.name = name;
        return this;
    }

    @Override
    public String getPath() {
        return path;
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
    public String getSubfolders() {
        return subfolders;
    }

    @Override
    public ReplaceFilesProfile setSubfolders(String subfolders) {
        this.subfolders = refine(subfolders);
        return this;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    @Override
    public ReplaceFilesProfile setCharset(String charset) {
        this.charset = refine(charset);
        return this;
    }

    @Override
    public String getFilenames() {
        return filenames;
    }

    @Override
    public ReplaceFilesProfile setFilenames(String filenames) {
        this.filenames = refine(filenames);
        return this;
    }

    @Override
    public String getToFind() {
        return toFind;
    }

    @Override
    public ReplaceFilesProfile setToFind(String toFind) {
        this.toFind = toFind;
        return this;
    }

    @Override
    public String getReplaceWith() {
        return replaceWith;
    }

    @Override
    public ReplaceFilesProfile setReplaceWith(String replaceWith) {
        this.replaceWith = replaceWith;
        return this;
    }

    @Override
    public Set<String> getExclusions() {
        return exclusions;
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
    public String toString() {
        final int maxLen = 10;
        return String.format(
                "ReplaceFilesProfile [name=%s, overwriteExisting=%s, path=%s, " +
                "toFind=%s, replaceWith=%s, filenames=%s, subfolders=%s, charset=%s, " +
                "includeNamePatterns=%s, exclusions=%s]",
                name, overwriteExisting, path, toFind, replaceWith, filenames,
                subfolders, charset,
                includeNamePatterns != null
                        ? toString(includeNamePatterns, maxLen) : null,
                exclusions != null ? toString(exclusions, maxLen) : null);
    }

    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext()
                && i < maxLen; i++) {
            if (i > 0) builder.append(", ");
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }

    private Set<String> newSet() {
        return new HashSet<>();
    }

    private String refine(String param) {
        return param != null ? param.toLowerCase() : "";
    }

}
