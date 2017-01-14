package dmv.desktop.searchandreplace.collection;

/**
 * Class <tt>TupleImpl.java</tt> is a collection of
 * two objects of defined types (i.e. generic). 
 * <p>
 * Two values are completely independent from each other.
 * Duplicates are allowed to be stored in both positions.
 * This class does not implement Collection interface due
 * to generic incompatibility, yet it provides some similar
 * or logically similar methods.
 * @author dmv
 * @since 2016 December 27
 */
public class TupleImpl<F, L> implements Tuple<F, L> {

    private int size;
    private int hashCode;
    private F first;
    private L last;
    
    /**
     * Creates empty collection
     */
    public TupleImpl() {
        clear();
    }
    
    /**
     * Creates TupleImpl with provided elements
     * in it. Either can be null - so the
     * non-null other one will be added.
     * @param first element
     * @param last (second) element
     */
    public TupleImpl(F first, L last) {
        size = 0;
        if (first != null) size++;
        if (last != null) size++;
        this.first = first;
        this.last = last;
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
    
    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.collection.Tuple#setFirst(F)
     */
    @Override
    public boolean setFirst(F first) {
        if (first != null) {
            if (this.first == null) size++;
            else if (this.first.equals(first))
                return false;
            this.first = first;
            return true;
        }
        return removeFirst();
    }
    
    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.collection.Tuple#setLast(L)
     */
    @Override
    public boolean setLast(L last) {
        if (last != null) {
            if (this.last == null) size++;
            else if (this.last.equals(last))
                return false;
            this.last = last;
            return true;
        }
        return removeLast();
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.collection.Tuple#removeFirst()
     */
    @Override
    public boolean removeFirst() {
        if (first != null) {
            first = null;
            size--;
            return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.collection.Tuple#removeLast()
     */
    @Override
    public boolean removeLast() {
        if (last != null) {
            last = null;
            size--;
            return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.collection.Tuple#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object obj) {
        if (obj == null) return false;
        boolean modified = false;
        if (first != null && 
            first.equals(obj)) {
            first = null;
            size--;
            modified = true;
        }
        if (last != null &&
            last.equals(obj)) {
            last = null;
            size--;
            modified = true;
        }
        return modified;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.collection.Tuple#clear()
     */
    @Override
    public void clear() {
        first = null;
        last = null;
        size = 0;
    }

    @Override
    public int hashCode() {
        if (hashCode != 0) return hashCode;
        int prime = 31;
        hashCode = 1;
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
        // Defense from null-nonNull checks
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
