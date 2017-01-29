/**
 * 
 */
package dmv.desktop.searchandreplace.view.profile;


/**
 * Class <tt>WrongProfileException.java</tt>
 * @author dmv
 * @since 2017 January 27
 */
public class WrongProfileException extends Exception {

    private static final long serialVersionUID = -7633140048864295022L;

    /**
     * 
     */
    public WrongProfileException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public WrongProfileException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public WrongProfileException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public WrongProfileException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public WrongProfileException(Throwable cause) {
        super(cause);
    }

}
