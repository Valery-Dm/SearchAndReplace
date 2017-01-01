/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Objects;


/**
 * Class <tt>SearchFolderImpl.java</tt>
 * @author dmv
 * @since 2016 December 31
 */
public class SearchFolderImpl implements SearchFolder {
    
    private Path folder;
    private Charset charset;
    private boolean fileNames;
    private boolean subfolders;
    private PathMatcher fileTypesMatcher;
    
    public SearchFolderImpl() {
        charset = SearchFolder.defaultCharset;
    }
    
    public SearchFolderImpl(Path folder) {
        setFolder(folder);
        charset = SearchFolder.defaultCharset;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFolder#setFolder(java.nio.file.Path)
     */
    @Override
    public SearchFolderImpl setFolder(Path folder) {
        Objects.requireNonNull(folder);
        this.folder = folder;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFolder#getFolder()
     */
    @Override
    public Path getFolder() {
        return folder;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFolder#setCharset(java.nio.charset.Charset)
     */
    @Override
    public SearchFolderImpl setCharset(Charset charset) {
        Objects.requireNonNull(charset);
        this.charset= charset;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFolder#getCharset()
     */
    @Override
    public Charset getCharset() {
        return charset;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFolder#setFileTypes(java.lang.String[])
     */
    @Override
    public SearchFolderImpl setFileTypes(String... fileTypes) {
        fileTypesMatcher = null;
        if (fileTypes != null && fileTypes.length > 0) {
            StringBuilder pattern = new StringBuilder("glob:{");
            for (String fileType : fileTypes) {
                pattern.append(fileType);
                pattern.append(',');
            }
            pattern.append("}");
            try {
                fileTypesMatcher = FileSystems.getDefault()
                                              .getPathMatcher(pattern.toString());
            } catch (Exception e) {
                fileTypesMatcher = null;
                throw new IllegalArgumentException(e);
            }
        }
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFolder#getFileTypes()
     */
    @Override
    public PathMatcher getFileTypes() {
        return fileTypesMatcher;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFolder#setFileNames(boolean)
     */
    @Override
    public SearchFolderImpl setFileNames(boolean fileNames) {
        this.fileNames = fileNames;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFolder#isFileNames()
     */
    @Override
    public boolean isFileNames() {
        return fileNames;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFolder#setSubfolders(boolean)
     */
    @Override
    public SearchFolderImpl setSubfolders(boolean subfolders) {
        this.subfolders = subfolders;
        return this;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchFolder#isSubfolders()
     */
    @Override
    public boolean isSubfolders() {
        return subfolders;
    }

}
