/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;


/**
 * Class <tt>SearchPathImpl.java</tt> implements
 * {@link SearchPath} interface
 * @author dmv
 * @since 2016 December 31
 */
public class SearchPathImpl implements SearchPath {
    
    private Path path;
    private Charset charset;
    private boolean subfolders;
    private PathMatcher fileNamePatterns;
    
    /**
     * Creates new object with given Path and default 
     * {@link StandardCharsets#UTF_16 Charset}.
     * It enforces that these two parameter must be set.
     * @param path
     * @throws NullPointerException if path is null
     */
    public SearchPathImpl(Path path) {
        setPath(path);
        charset = SearchPath.defaultCharset;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchPath#setFolder(java.nio.file.Path)
     */
    @Override
    public SearchPathImpl setPath(Path path) {
        Objects.requireNonNull(path);
        this.path = path;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchPath#getFolder()
     */
    @Override
    public Path getPath() {
        return path;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchPath#setCharset(java.nio.charset.Charset)
     */
    @Override
    public SearchPathImpl setCharset(Charset charset) {
        Objects.requireNonNull(charset);
        this.charset= charset;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchPath#getCharset()
     */
    @Override
    public Charset getCharset() {
        return charset;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchPath#setFileTypes(java.lang.String[])
     */
    @Override
    public SearchPathImpl setNamePattern(String... pattern) {
        fileNamePatterns = null;
        if (pattern != null && pattern.length > 0) {
            StringBuilder result = new StringBuilder("glob:{");
            for (String fileType : pattern) {
                result.append(fileType);
                result.append(',');
            }
            result.append("}");
            try {
                fileNamePatterns = FileSystems.getDefault()
                                              .getPathMatcher(result.toString());
            } catch (Exception e) {
                fileNamePatterns = null;
                throw new IllegalArgumentException(e);
            }
        }
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchPath#getFileTypes()
     */
    @Override
    public PathMatcher getNamePattern() {
        return fileNamePatterns;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchPath#setSubfolders(boolean)
     */
    @Override
    public SearchPathImpl setSubfolders(boolean subfolders) {
        this.subfolders = subfolders;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchPath#isSubfolders()
     */
    @Override
    public boolean isSubfolders() {
        return subfolders;
    }

}
