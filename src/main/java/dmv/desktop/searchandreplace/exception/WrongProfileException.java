/**
 * 
 */
package dmv.desktop.searchandreplace.exception;

/*
 * This class designed to be checked exception but it is changed
 * to be a runtime type. It's because this exception needs to go
 * through lambda function (Java 8 feature) in one place and...
 * lambdas can't propagate checked exceptions up the stack.
 * So, this is a compromise.
 */
/**
 * <tt>WrongProfileException.java</tt> is about to be thrown in 
 * service construction time (at first) if some of parameters were incorrect
 * or right after the wrong parameter was given (when service exists).
 * @author dmv
 * @since 2017 January 27
 */
public class WrongProfileException extends RuntimeException {

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
