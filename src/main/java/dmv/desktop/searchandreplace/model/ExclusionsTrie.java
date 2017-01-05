/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.util.List;
import java.util.Set;

import dmv.desktop.searchandreplace.collection.ExactSearchTrie;

/**
 * Immutable Class <tt>ExclusionsTrie.java</tt> will collect
 * prefixes and suffixes given at construction time in a Trie DS
 * for fast scan inside <em>containsAny...</em> methods.
 * <p>
 * Prefixes may be reversed for backward scanning. If you have 
 * collected them reversed you should be consistent asking for them,
 * i.e. use <em>reverse</em> parameter in <em>contains...</em> methods
 * as needed.
 * @author dmv
 * @since 2016 December 27
 */
public class ExclusionsTrie implements Exclusions {
    
    private final ExactSearchTrie prefixes;
    private final ExactSearchTrie suffixes;
    
    private String longestPrefix;
    private String longestSuffix;
    
    /**
     * Construct prefixes and suffixes from the word
     * to be found and a set of exclusions. Arguments must
     * not be null or empty, given set should
     * not contain null entries.
     * It is expected that word <em>toFind</em> is a substring of
     * any exclusion in a set.
     * <p>
     * If you need an empty collection use second constructor
     * with nulls or empty lists as arguments.
     * @param exclude A set of words to be excluded from search,
     *                prefixes and/or suffixes will be extracted 
     *                from them using <em>toFind</em> parameter
     * @param toFind A word that will be subtracted from each exclusion
     *               in order to find prefix or suffix in there
     * @param reversePrefixes Set it to true if you need prefixes to be reversed
     *                        in constructor prior to insertion
     * @throws IllegalArgumentException If any of arguments is null or empty,
     *                                  if set contains null pointers
     *                                  or words for which word 'toFind' 
     *                                  is not a substring
     */
    public ExclusionsTrie(Set<String> exclude, String toFind, boolean reversePrefixes) {
        if (exclude == null || exclude.size() == 0)
            throw new IllegalArgumentException("Set of exclusions must not be empty");
        if (toFind == null || toFind.length() == 0)
            throw new IllegalArgumentException("What to find was not specified");
        longestPrefix = "";
        longestSuffix = "";
        prefixes = new ExactSearchTrie();
        suffixes = new ExactSearchTrie();
        
        int index = 0;
        for (String word : exclude) {
            if (word == null || (index = word.indexOf(toFind)) == -1)
                throw new IllegalArgumentException(toFind + " is not a substring of " + word);

            if (index > 0) 
                addPrefix(reversePrefixes ? 
                                collectBackward(word, index - 1, -1) : 
                                collectForward(word, 0, index));
            index += toFind.length();
            if (index < word.length()) 
                addSuffix(collectForward(word, index, word.length()));
        }
    }

    /**
     * Stores given prefixes and suffixes inside collection.
     * If arguments are null or empty - empty collection will be created.
     * If any list contains empty strings or null entries
     * they will not be stored (i.e. silently ignored).
     * <p>
     * For backwards search prefixes may be reversed.
     * Set <em>reversePrefixes</em> to true if you want
     * them to be reversed at construction time
     * @param prefixes A list of prefixes or null if prefixes not needed
     * @param suffixes A list of suffixes or null if suffixes not needed
     * @param reversePrefixes prefixes will be reversed if set to true
     */
    public ExclusionsTrie(List<String> prefixes, List<String> suffixes, boolean reversePrefixes) {
        longestPrefix = "";
        longestSuffix = "";
        this.prefixes = new ExactSearchTrie();
        this.suffixes = new ExactSearchTrie();
        
        if (prefixes != null && prefixes.size() > 0) {
            if (reversePrefixes) 
                prefixes.stream()
                        .map(this::reverse)
                        .forEach(this::addPrefix);
            else 
                prefixes.forEach(this::addPrefix);
        }
        if (suffixes != null && suffixes.size() > 0)
            suffixes.forEach(this::addSuffix);
        
    }

    @Override
    public boolean containsPrefix(String prefix, boolean reverse) {
        return reverse ? prefixes.contains(reverse(prefix)) :
                         prefixes.contains(prefix);
    }
    
    @Override
    public boolean containsSuffix(String suffix) {
        return suffixes.contains(suffix);
    }

    @Override
    public boolean containsAnyPrefixes(String word, boolean reverse) {
        return reverse ? prefixes.containsAnyFrom(reverse(word)) : 
                         prefixes.containsAnyFrom(word);
    }
    
    @Override
    public boolean containsAnySuffixes(String word) {
        return suffixes.containsAnyFrom(word);
    }
    
    @Override
    public int maxPrefixSize() {
        return longestPrefix.length();
    }

    @Override
    public int maxSuffixSize() {
        return longestSuffix.length();
    }
    
    private String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }
    
    private String collectBackward(String w, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i > to; i--)
            sb.append(w.charAt(i));
        return sb.toString();
    }
    
    private String collectForward(String w, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++)
            sb.append(w.charAt(i));
        return sb.toString();
    }

    private void addSuffix(String s) {
        if (s == null) return;
        if (s.length() > longestSuffix.length())
            longestSuffix = s;
        suffixes.add(s);
    }
    
    private void addPrefix(String p) {
        if (p == null) return;
        if (p.length() > longestPrefix.length())
            longestPrefix = p;
        prefixes.add(p);
    }

}
