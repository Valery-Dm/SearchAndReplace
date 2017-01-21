package dmv.desktop.searchandreplace.exception;


public class AccessResourceException extends RuntimeException {

    private static final long serialVersionUID = 1700728431085924883L;

    /**
     * 
     */
    public AccessResourceException() {
        super();
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public AccessResourceException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * @param message
     * @param cause
     */
    public AccessResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public AccessResourceException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public AccessResourceException(Throwable cause) {
        super(cause);
    }

    
}
