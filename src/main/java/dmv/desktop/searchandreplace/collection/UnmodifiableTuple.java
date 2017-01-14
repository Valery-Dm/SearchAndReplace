/**
 * 
 */
package dmv.desktop.searchandreplace.collection;


/**
 * Class <tt>UnmodifiableTuple.java</tt> create unmodifiable
 * collection where all mutative methods will throw an
 * {@link UnsupportedOperationException} on call.
 * @author dmv
 * @since 2017 January 13
 */
public class UnmodifiableTuple<F, L> implements Tuple<F, L> {
    
    private static final String UNSUPPORTED = "Mutative operatins are not supported by this type";
    
    private final int size;
    private final int hashCode;
    private final F first;
    private final L last;
    
    /**
     * Create empty UnmodifiableTuple
     */
    public UnmodifiableTuple() {
        this(null, null);
//        size = 0;
//        first = null;
//        last = null;
//        hashCode = hashCode();
    }
    
    /**
     * Create Tuple with given arguments
     * @param first The element to be stored as first one
     * @param last  The element to be stored as last one
     */
    public UnmodifiableTuple(F first, L last) {
        this.first = first;
        this.last = last;
        size = getSize(first, last);
        hashCode = hashCode();
    }
    
    /**
     * Create collection from other Tuple object.
     * May be used for wrapping modifiable types
     * @param tuple Another Tuple
     */
    public UnmodifiableTuple(Tuple<F, L> tuple) {
        this.first = tuple.getFirst();
        this.last = tuple.getLast();
        size = tuple.size();
        hashCode = tuple.hashCode();
    }

    private int getSize(F first, L last) {
        int size = 0;
        if (first != null) size++;
        if (last != null) size++;
        return size;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.collection.Tuple#getFirst()
     */
    @Override
    public F getFirst() {
        return first;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.collection.Tuple#getLast()
     */
    @Override
    public L getLast() {
        return last;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.collection.Tuple#size()
     */
    @Override
    public int size() {
        return size;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.collection.Tuple#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.collection.Tuple#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object obj) {
        if (obj == null) return false;
        return (first == null ? false : first.equals(obj)) ||
               (last == null ? false : last.equals(obj));
    }

    /**
     * This operation is not supported 
     * @throws UnsupportedOperationException on call
     */
    @Override
    public boolean setFirst(F first) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    /**
     * This operation is not supported 
     * @throws UnsupportedOperationException on call
     */
    @Override
    public boolean setLast(L last) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    /**
     * This operation is not supported 
     * @throws UnsupportedOperationException on call
     */
    @Override
    public boolean removeFirst() {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    /**
     * This operation is not supported 
     * @throws UnsupportedOperationException on call
     */
    @Override
    public boolean removeLast() {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    /**
     * This operation is not supported 
     * @throws UnsupportedOperationException on call
     */
    @Override
    public boolean remove(Object obj) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    /**
     * This operation is not supported 
     * @throws UnsupportedOperationException on call
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public int hashCode() {
        if (hashCode != 0) return hashCode;
        int hashCode = 1;
        int prime = 31;
        hashCode = prime * hashCode + ((first == null) ? 0 : first.hashCode());
        hashCode = prime * hashCode + ((last == null) ? 0 : last.hashCode());
        hashCode = prime * hashCode + size;
        return hashCode;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Tuple)) return false;
        
        Tuple other = (Tuple) obj;
        if (size != other.size()) return false;
        
        if (first != null && !first.equals(other.getFirst())) 
            return false;
        
        if (last!= null && !last.equals(other.getLast())) 
            return false;
        
        return true;
    }

    @Override
    public String toString() {
        return String.format("T[F[%s], L[%s]]", first, last);
    }
    
}
