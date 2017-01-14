package dmv.desktop.searchandreplace.collection;

public interface Tuple<F, L> {

    /**
     * Get first element
     * @return first element or null
     */
    F getFirst();

    /**
     * Get last element
     * @return last element or null
     */
    L getLast();

    /**
     * Size can be either 0, 1 or 2. Note that:
     * size 1 does not imply that it is the first object
     * that exists, there could be only last one.
     * @return current size of this collection
     */
    int size();

    /**
     * If this collection is empty means first
     * and last objects are null
     * @return true if no objects stored
     */
    boolean isEmpty();

    /**
     * Consequently checks first then last
     * objects for equality using their 
     * equals method implementations. 
     * @param obj Object to look for
     * @return true whether first or last
     *         object is equal to this one,
     *         false otherwise
     */
    boolean contains(Object obj);

    /**
     * Adds First element to the collection.
     * If you need to remove first object
     * use {@link #removeFirst()} method (recommended),
     * although you can also pass null argument here.
     * @param first Element to be added at first position
     * @return true if collection has been modified
     */
    boolean setFirst(F first);

    /**
     * Adds Last element to the collection.
     * If you need to remove last object
     * use {@link #removeLast()} method (recommended),
     * although you can also pass null argument here.
     * @param last Element to be added at last position
     * @return true if collection has been modified
     */
    boolean setLast(L last);

    /**
     * Remove first element if exists
     * @return true if collection has been modified
     */
    boolean removeFirst();

    /**
     * Remove Last element if exists
     * @return true if collection has been modified
     */
    boolean removeLast();

    /**
     * Remove given object if exists.
     * Both objects can be removed if they are
     * equal to given one (as duplicates are allowed).
     * @param obj Object to be removed from TupleImpl
     * @return true if collection has been modified
     */
    boolean remove(Object obj);

    /**
     * Emptying the collection
     */
    void clear();

}