/**
 * 
 */
package dmv.desktop.searchandreplace.exceptions;


/**
 * Class <tt>ResourceCantBeModifiedException.java</tt>
 * @author dmv
 * @since 2017 January 01
 */
public class ResourceCantBeModifiedException extends Throwable {

    private static final long serialVersionUID = 4484919269203694366L;
    
    public ResourceCantBeModifiedException() {
        super();
    }
    
    public ResourceCantBeModifiedException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

}
