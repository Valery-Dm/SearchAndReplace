/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import static java.util.Collections.emptyList;

import java.nio.charset.Charset;

/**
 * Class <tt>SearchProfileImpl.java</tt> implements 
 * {@link SearchProfile} interface keeping its invariants
 * (like 'what to find should not be null or empty').
 * @author dmv
 * @since 2017 January 02
 */
public class SearchProfileImpl implements SearchProfile {
    
    private static final Exclusions EMPTY_EXCL = 
                     new ExclusionsTrie(emptyList(), emptyList(), false);
    private static final String EMPTY_REPLACE = "";
    
    private Charset charset;
    private boolean filename;
    private String toFind;
    private String replaceWith;
    private Exclusions exclusions;
    
    /**
     * Creates profile with default settings:
     * charset is {@link SearchProfile#defaultCharset};
     * filename - false;
     * replaceWith and exclusionsTrie are empty.
     * 'What to find' string can't be null or empty.
     * @param toFind
     * @throws IllegalArgumentException if toFind is null or empty
     */
    public SearchProfileImpl(String toFind) {
        setToFind(toFind);
        setCharset(null);
        setFilename(false);
        setReplaceWith(null);
        setExclusions(null);
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchProfile#getCharset()
     */
    @Override
    public Charset getCharset() {
        return charset;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchProfile#setCharset(java.nio.charset.Charset)
     */
    @Override
    public SearchProfile setCharset(Charset charset) {
        if (charset != null) this.charset = charset;
        else this.charset = defaultCharset;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchProfile#isFileName()
     */
    @Override
    public boolean isFileName() {
        return filename;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchProfile#setFilename(boolean)
     */
    @Override
    public SearchProfile setFilename(boolean filename) {
        this.filename = filename;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchProfile#getToFind()
     */
    @Override
    public String getToFind() {
        return toFind;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchProfile#setToFind(java.lang.String)
     */
    @Override
    public SearchProfile setToFind(String toFind) {
        if (toFind == null || toFind.length() < 1)
            throw new IllegalArgumentException("'What to find' should be at least one character long");
        this.toFind = toFind;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchProfile#getReplaceWith()
     */
    @Override
    public String getReplaceWith() {
        return replaceWith;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchProfile#setReplaceWith(java.lang.String)
     */
    @Override
    public SearchProfile setReplaceWith(String replaceWith) {
        this.replaceWith = replaceWith != null ? replaceWith : EMPTY_REPLACE;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchProfile#getExclusions()
     */
    @Override
    public Exclusions getExclusions() {
        return exclusions;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchProfile#setExclusions(dmv.desktop.searchandreplace.model.ExclusionsTrie)
     */
    @Override
    public SearchProfile setExclusions(Exclusions exclusions) {
        this.exclusions = exclusions != null ? exclusions : EMPTY_EXCL;
        return this;
    }

}
