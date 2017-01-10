package dmv.desktop.searchandreplace.collection;

/**
 * Class <tt>Trie.java</tt> describes Retrieval Data Structure
 * with bare minimum method to be used with 'Search and Replace' application.
 * It offers methods to store strings {@link #add(String)}, count the number 
 * of them {@link #size()}, and then check if string exists in Trie {@link #contains(String)}
 * and {@link #containsAnyFrom(String)}. (Without deletion right now)
 * Will capital letters be considered or ignored - depends on implementation
 * @author dmv
 * @since 2016 December 26
 */
public interface Trie {
    
    int size();
    
    boolean isEmpty();
    
    /**
     * Adds given word to a trie.
     * If given word is null or has zero length
     * or already exists in DS it will be silently
     * ignored
     * @param word A word to be added
     */
    void add(String word);
    
    /**
     * Check if given word exits in a trie
     * @param word A word to search for
     * @return 'true' if word is found. If word is null
     *         or doesn't exist then 'false' will be returned
     */
    boolean contains(String word);
    
    /**
     * Check if Trie contains strings that are prefix
     * substrings of given word. 
     * <p> For example:
     * <pre>
     *     String 'word' has prefixes:
     *     w
     *     wo
     *     wor
     *     word
     * </pre>
     * This method does not create String objects and
     * it also efficiently continues its search until
     * the shortest prefix found or the next character is absent.
     * @param word A word to look for
     * @return true if any of prefixes exist in a Trie
     */
    boolean containsAnyFrom(String word);
    
}
