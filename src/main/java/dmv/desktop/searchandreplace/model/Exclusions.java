/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import dmv.desktop.searchandreplace.collection.ExactSearchTrie;

/**
 * Immutable Class <tt>Exclusions.java</tt> will collect
 * prefixes and suffixes given at construction time.
 * Prefixes may be reversed for backward scanning.
 * @author dmv
 * @since 2016 December 27
 */
public class Exclusions {
    
    private static final IllegalArgumentException EMPTY = new IllegalArgumentException("Empty collection of exclusions or empty String toFind");
    private static final IllegalArgumentException NOT_SUBS = new IllegalArgumentException("Given word is not a substring of exclusion");

    private final ExactSearchTrie prefixes;
    private final ExactSearchTrie suffixes;
    
    private String longestPrefix;
    private String longestSuffix;
    
    /**
     * Construct prefixes and suffixes from the word
     * to be found and a set of exclusions. Arguments must
     * not be null, or contain null entries.
     * It is expected that word toFind is a substring of
     * any exclusion in a set
     * @param exclude A set of words to be excluded from search
     *                the prefixes and suffixes will be constructed 
     *                from them
     * @param toFind A word that will be subtracted from each exclusion
     *               in order to find prefix or suffix in there
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if set contains null pointers
     *                                  or words for which word 'toFind' 
     *                                  is not a substring
     */
    public Exclusions(Set<String> exclude, String toFind, boolean reversePrefixes) {
        Objects.requireNonNull(exclude);
        Objects.requireNonNull(toFind);
        if (exclude.size() == 0 || toFind.length() == 0)
            throw EMPTY;
        longestPrefix = "";
        longestSuffix = "";
        prefixes = new ExactSearchTrie();
        suffixes = new ExactSearchTrie();
        
        StringBuilder sb = new StringBuilder();
        String s = null;
        int index = 0;
        for (String w : exclude) {
            if (w == null || (index = w.indexOf(toFind)) == -1)
                throw NOT_SUBS;
            if (index > 0) {
                s = reversePrefixes ? 
                        collectBackward(w, sb, index - 1, -1) : 
                        collectForward(w, sb, 0, index);
                trackMaxPrefix(s);
                prefixes.add(s);
                
            } 
            index += toFind.length();
            if (index < w.length()) {
                s = collectForward(w, sb, index, w.length());
                trackMaxSuffix(s);
                suffixes.add(s);
            }
        }
    }
    
    private String collectBackward(String w, StringBuilder sb, int from, int to) {
        sb = new StringBuilder();
        for (int i = from; i > to; i--)
            sb.append(w.charAt(i));
        return sb.toString();
    }
    
    private String collectForward(String w, StringBuilder sb, int from, int to) {
        sb = new StringBuilder();
        for (int i = from; i < to; i++)
            sb.append(w.charAt(i));
        return sb.toString();
    }

    /**
     * Stores given prefixes and suffixes inside collection.
     * Arguments must not be null.
     * If any list contains empty strings or null entries
     * they will not be stored.
     * <p>
     * For efficient search prefixes should be reversed.
     * Set <em>reversePrefixes</em> to true if you want
     * them to be reversed at construction time
     * @param prefixes A list of prefixes
     * @param suffixes A list of suffixes
     * @param reversePrefixes prefixes will be reversed if true
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if set contains null pointers
     *                                  or words for which word 'toFind' 
     *                                  is not a substring
     * @throws NullPointerException if any of lists is null
     */
    public Exclusions(List<String> prefixes, List<String> suffixes, boolean reversePrefixes) {
        Objects.requireNonNull(prefixes);
        Objects.requireNonNull(suffixes);
        longestPrefix = "";
        longestSuffix = "";
        this.prefixes = new ExactSearchTrie();
        this.suffixes = new ExactSearchTrie();
        
        if (reversePrefixes) 
            prefixes.forEach(p -> {
                if (trackMaxPrefix(p)) 
                    this.prefixes.add(new StringBuilder(p).reverse().toString());
            });
        else 
            prefixes.stream()
                    .filter(this::trackMaxPrefix)
                    .forEach(this.prefixes::add);
        suffixes.stream()
                .filter(this::trackMaxSuffix)
                .forEach(this.suffixes::add);
        
    }

    private boolean trackMaxSuffix(String s) {
        if (s == null) return false;
        if (s.length() > longestSuffix.length())
            longestSuffix = s;
        return s.length() > 0;
    }
    
    private boolean trackMaxPrefix(String p) {
        if (p == null) return false;
        if (p.length() > longestPrefix.length())
            longestPrefix = p;
        return p.length() > 0;
    }

    /**
     * If prefix (entire word) exists in collection
     * @param prefix 
     * @return true if prefix found
     */
    public boolean containsPrefix(String prefix) {
        return prefixes.contains(prefix);
    }
    
    /**
     * If suffix (entire word) exists in collection
     * @param suffix
     * @return true if suffix found
     */
    public boolean containsSuffix(String suffix) {
        return suffixes.contains(suffix);
    }

    /**
     * If collection contains any of prefixes that
     * could be constructed from given word.
     * <p>
     * Prefixes will be constructed from reversed word
     * <pre>
     *   For String 'It has' next prefixes are exist:
     *   s
     *   sa
     *   sah
     *   sah 
     *   sah t
     *   sah tI
     * </pre>
     * @param word
     * @return true if collection contains any of prefixes that
     *         could be constructed from given word
     */
    public boolean containsAnyPrefixes(String word) {
        return prefixes.containsAnyFrom(new StringBuilder(word).reverse().toString());
    }
    
    /**
     * If collection contains any of suffixes that
     * could be constructed from given word, as it described
     * in {@link ExactSearchTrie#containsAnyFrom(String)}
     * @param word
     * @return true if collection contains any of suffixes that
     *         could be constructed from given word
     */
    public boolean containsAnySuffixes(String word) {
        return suffixes.containsAnyFrom(word);
    }
    
    /**
     * @return Size of longest prefix
     */
    public int maxPrefix() {
        return longestPrefix.length();
    }
    
    /**
     * @return Size of longest suffix
     */
    public int maxSuffix() {
        return longestSuffix.length();
    }
}
