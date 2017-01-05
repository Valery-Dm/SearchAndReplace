package dmv.desktop.searchandreplace.collection;

/**
 * Class <tt>Tuple.java</tt> is a collection of
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
public class Tuple<F, L> {

    private int size;
    private int hashCode;
    private F first;
    private L last;
    
    /**
     * Creates empty collection
     */
    public Tuple() {
        clear();
    }
    
    /**
     * Creates Tuple with provided elements
     * in it. Either can be null - so the
     * non-null other one will be added.
     * @param first element
     * @param last (second) element
     */
    public Tuple(F first, L last) {
        size = 0;
        if (first != null) size++;
        if (last != null) size++;
        this.first = first;
        this.last = last;
    }

    /**
     * Get first element
     * @return first element or null
     */
    public F getFirst() {
        return first;
    }
    
    /**
     * Get last element
     * @return last element or null
     */
    public L getLast() {
        return last;
    }

    /**
     * Size can be either 0, 1 or 2. Note that:
     * size 1 does not imply that it is the first object
     * exists, there could be only last one.
     * @return current size of this collection
     */
    public int size() {
        return size;
    }

    /**
     * If this collection is empty means first
     * and last objects are null
     * @return true if no objects stored
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Consequently checks first then last
     * objects for equality using their 
     * equals method implementations. 
     * @param obj Object to look for
     * @return true whether first or last
     *         object is equal to this one,
     *         false otherwise
     */
    public boolean contains(Object obj) {
        if (obj == null) return false;
        if (first != null && first.getClass() == obj.getClass())
            return first.equals(obj);
        if (last != null && last.getClass() == obj.getClass())
            return last.equals(obj);
        return false;
    }
    
    /**
     * Adds First element to the collection.
     * If you need to remove first object
     * use {@link #removeFirst()} method (recommended),
     * although you can also pass null argument here.
     * @param first Element to be added at first position
     * @return true if collection has been modified
     */
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
    
    /**
     * Adds Last element to the collection.
     * If you need to remove last object
     * use {@link #removeLast()} method (recommended),
     * although you can also pass null argument here.
     * @param last Element to be added at last position
     * @return true if collection has been modified
     */
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

    /**
     * Remove first element if exists
     * @return true if collection has been modified
     */
    public boolean removeFirst() {
        if (first != null) {
            first = null;
            size--;
            return true;
        }
        return false;
    }
    
    /**
     * Remove Last element if exists
     * @return true if collection has been modified
     */
    public boolean removeLast() {
        if (last != null) {
            last = null;
            size--;
            return true;
        }
        return false;
    }
    
    /**
     * Remove given object if exists.
     * Both objects can be removed if they are
     * equal to given one (as duplicates are allowed).
     * @param obj Object to be removed from Tuple
     * @return true if collection has been modified
     */
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

    /**
     * Emptying the collection
     */
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
        if (obj == null || obj.getClass() != getClass()) return false;
        
        Tuple other = (Tuple) obj;
        // Defense from null-nonNull checks
        if (size != other.size) return false;
        
        if (first != null && !first.equals(other.first)) 
            return false;
        
        if (last!= null && !last.equals(other.last)) 
            return false;
        
        return true;
    }

    @Override
    public String toString() {
        return String.format("Tuple [first=%s, last=%s]", first, last);
    }
    
}
