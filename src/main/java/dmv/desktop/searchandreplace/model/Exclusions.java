/**
 * 
 */
package dmv.desktop.searchandreplace.model;

/**
 * Interface <tt>Exclusions.java</tt> describes collection of
 * prefixes and suffixes. Their combinations with string needed to
 * be found and replaced should be excluded from 'search and
 * replace' operation.
 * @author dmv
 * @since 2017 January 05
 */
public interface Exclusions {
    
    /**
     * How many prefixes exist in collection
     * @return Number of prefixes added
     */
    int numberOfPrefixes();
    
    /**
     * How many suffixes exist in collection
     * @return Number of suffixes added
     */
    int numberOfSuffixes();
    
    /**
     * If neither prefixes nor suffixes exist in collection
     * it is empty.
     * @return true if no prefixes or suffixes were added
     */
    boolean isEmpty();

    /**
     * If prefix (entire word) exists in the collection.
     * Prefixes may be collected backwards, i.e.
     * in word 'prefixWord' prefix may be collected as
     * 'xiferp', if this is the case and collection has all prefixes
     * stored reversed then set second argument to false, otherwise
     * if collection and argument are not consistent set it to true.
     * @param prefix Prefix to look for
     * @param reverse True if given prefix is reversed
     * @return true if prefix found
     */
    boolean containsPrefix(String prefix, boolean reverse);
    
    /**
     * If suffix (entire word) exists in the collection
     * @param suffix Suffix to look for
     * @return true if suffix found
     */
    boolean containsSuffix(String suffix);

    /**
     * See if collection contains any of prefixes that
     * could be constructed from given word.
     * <p>
     * Next prefixes could be constructed from string 'It has':
     * <pre>
     * I
     * It
     * It 
     * It h
     * It ha
     * It has
     * </pre>
     * If any of these words exist in the underlying collection
     * true will be returned on first found.
     * Prefixes may be collected backwards, i.e.
     * in word 'prefixWord' prefix may be collected as
     * 'xiferp', if this is the case and collection has all prefixes
     * stored reversed then set second argument to false, otherwise
     * if collection and argument are not consistent set it to true.
     * @param prefixes String containing prefixes
     * @param reverse If true given prefix will be reversed before scan
     * @return true if collection contains any of prefixes that
     *         could be constructed from given word
     */
    boolean containsAnyPrefixes(String prefixes, boolean reverse);
    
    /**
     * See if collection contains any of suffixes that
     * could be constructed from given word.
     * <p>
     * Next suffixes could be constructed from string 'It has':
     * <pre>
     * I
     * It
     * It 
     * It h
     * It ha
     * It has
     * </pre>
     * If any of those words exist in the underlying collection
     * true will be returned on first found.
     * @param suffixes String containing suffixes
     * @return true if collection contains any of suffixes that
     *         could be constructed from given word
     */
    boolean containsAnySuffixes(String suffixes);
    
    /**
     * Length of the longest prefix stored in the collection
     * @return Length of longest prefix
     */
    int maxPrefixSize();
    
    /**
     * Length of the longest suffix stored in the collection
     * @return Length of longest suffix
     */
    int maxSuffixSize();
}
