/**
 * 
 */
package dmv.desktop.searchandreplace.exception;


/**
 * <tt>NothingToReplaceException.java</tt> expected to be
 * thrown when nothing was found in specified resource
 * or execution was interrupted abnormally
 * @author dmv
 * @since 2017 January 19
 */
public class NothingToReplaceException extends RuntimeException {

    private static final long serialVersionUID = -528290042507194289L;

    /**
     * 
     */
    public NothingToReplaceException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public NothingToReplaceException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public NothingToReplaceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public NothingToReplaceException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public NothingToReplaceException(Throwable cause) {
        super(cause);
    }

    
}
