/**
 * 
 */
package dmv.desktop.searchandreplace.collection;


/**
 * Class <tt>Triple.java</tt> is a collection of
 * three objects of defined types (i.e. generic). 
 * <em>Null entries</em> are also allowed to be stored.
 * <p>
 * It tries to provide immutability but does not
 * guarantee it if specified types are not immutable
 * themselves. No defense from <em>escaped identity</em> provided,
 * so its usage may be risky in concurrent environment.
 * @author dmv
 * @since 2016 December 27
 */
public class Triple<F, S, T> {

    private final F first;
    private final S second;
    private final T third;
    
    public Triple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public F getFirst() {
        return first;
    }
    
    public S getSecond() {
        return second;
    }
    
    public T getThird() {
        return third;
    }
}
