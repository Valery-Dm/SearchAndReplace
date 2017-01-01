/**
 * 
 */
package dmv.desktop.searchandreplace.exceptions;


/**
 * Class <tt>ResourceNotExistsException.java</tt>
 * @author dmv
 * @since 2017 January 01
 */
public class ResourceNotExistsException extends Throwable {

    private static final long serialVersionUID = -2962912709624596421L;
    
    public ResourceNotExistsException() {
        super();
    }
    
    public ResourceNotExistsException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

}
