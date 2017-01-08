package dmv.desktop.searchandreplace.exceptions;

import dmv.desktop.searchandreplace.model.FileReplacements;

public class ResourceReadException extends RuntimeException {

    private static final long serialVersionUID = -5691692820098829744L;
    
    private FileReplacements repl;

    /**
     * Failed resource with exceptional cause embedded
     * @param repl
     */
    public ResourceReadException(FileReplacements repl) {
        this.repl = repl;
    }
    
    public FileReplacements getRepl() {
        return repl;
    }

}
