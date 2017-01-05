/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Class <tt>SearchFileImpl.java</tt> implements 
 * {@link SearchFile} interface keeping its invariants
 * (like 'what to find should not be null or empty').
 * @author dmv
 * @since 2017 January 02
 */
public class SearchFileImpl implements SearchFile {
    
    private static final Exclusions EMPTY_EXCL = 
            new Exclusions(new ArrayList<>(), new ArrayList<>(), false);
    private static final String EMPTY_REPLACE = "";
    
    private Charset charset;
    private boolean filename;
    private String toFind;
    private String replaceWith;
    private Exclusions exclusions;
    
    /**
     * Creates profile with default settings:
     * charset is {@link SearchFile#defaultCharset};
     * filename - false;
     * replaceWith and exclusions are empty.
     * 'What to find' string can't be null or empty.
     * @param toFind
     * @throws IllegalArgumentException if toFind is null or empty
     */
    public SearchFileImpl(String toFind) {
        setToFind(toFind);
        setCharset(null);
        setFilename(false);
        setReplaceWith(null);
        setExclusions(null);
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFile#getCharset()
     */
    @Override
    public Charset getCharset() {
        return charset;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFile#setCharset(java.nio.charset.Charset)
     */
    @Override
    public SearchFile setCharset(Charset charset) {
        if (charset != null) this.charset = charset;
        else this.charset = defaultCharset;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFile#isFileName()
     */
    @Override
    public boolean isFileName() {
        return filename;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFile#setFilename(boolean)
     */
    @Override
    public SearchFile setFilename(boolean filename) {
        this.filename = filename;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFile#getToFind()
     */
    @Override
    public String getToFind() {
        return toFind;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFile#setToFind(java.lang.String)
     */
    @Override
    public SearchFile setToFind(String toFind) {
        if (toFind == null || toFind.length() < 1)
            throw new IllegalArgumentException("What to find should be at least one character long");
        this.toFind = toFind;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFile#getReplaceWith()
     */
    @Override
    public String getReplaceWith() {
        return replaceWith;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFile#setReplaceWith(java.lang.String)
     */
    @Override
    public SearchFile setReplaceWith(String replaceWith) {
        if (replaceWith != null)
            this.replaceWith = replaceWith;
        else
            this.replaceWith = EMPTY_REPLACE;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFile#getExclusions()
     */
    @Override
    public Exclusions getExclusions() {
        return exclusions;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFile#setExclusions(dmv.desktop.searchandreplace.model.Exclusions)
     */
    @Override
    public SearchFile setExclusions(Exclusions exclusions) {
        if (exclusions != null)
            this.exclusions = exclusions;
        else 
            this.exclusions = EMPTY_EXCL;
        return this;
    }

}
