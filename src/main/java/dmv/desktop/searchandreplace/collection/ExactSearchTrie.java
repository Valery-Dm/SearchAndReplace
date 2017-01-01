package dmv.desktop.searchandreplace.collection;

import java.util.HashMap;
import java.util.Map;

/**
 * Class <tt>ExactSearchTrie.java</tt> will store
 * words without any char-conversions (like converting
 * capitals to lower case) and search for them later.
 * <p>
 * The method {@link #contains(String)} is safe for
 * concurrent access if all modifications to a Trie
 * were done before. 
 * <p>
 * Other than that, this class is not thread safe.
 * @author dmv
 * @since 2016 December 26
 */
public class ExactSearchTrie implements Trie {
    
    private int size;
    /*
     * HashMap of chars on the top level with
     * Ternary Search Tries underneath
     */
    private Map<Character, TST> topLevel;
    
    public ExactSearchTrie() {
        size = 0;
        topLevel = new HashMap<>();
    }

    @Override
    public void add(String word) {
        if (word == null || word.length() == 0) return;
        char ch = word.charAt(0);
        TST tst = topLevel.get(ch);
        if (tst == null) {
            tst = new TST(ch);
            topLevel.put(ch, tst);
            if (word.length() == 1) {
                markNewWord(tst);
                return;
            }
        }
        for (int i = 1; i < word.length(); i++) {
            ch = word.charAt(i);
            tst = tst.insert(ch);
        }
        
        if (!tst.isWord) 
            markNewWord(tst);
    }

    private void markNewWord(TST tst) {
        size++;
        tst.isWord = true;
    }

    @Override
    public boolean contains(String word) {
        if (word == null || word.length() == 0) return false;
        char ch = word.charAt(0);
        TST tst = topLevel.get(ch);
        if (tst == null) 
            return false;
        for (int i = 1; i < word.length(); i++) {
            ch = word.charAt(i);
            if ((tst = tst.getNext(ch)) == null)
                return false;
        }
        return tst.isWord;
    }

    @Override
    public boolean containsAnyFrom(String word) {
        if (word == null || word.length() == 0) return false;
        char ch = word.charAt(0);
        TST tst = topLevel.get(ch);
        if (tst == null) 
            return false;
        if (tst.isWord)
            return true;
        for (int i = 1; i < word.length(); i++) {
            ch = word.charAt(i);
            tst = tst.getNext(ch);
            if (tst == null)
                return false;
            // Return if prefix found
            if (tst.isWord)
                return true;
        }
        return tst.isWord;
    }
    
    private static class TST {
        char ch;
        boolean isWord;
        TST left, middle, right;
        
        TST(char ch) {
            this.ch = ch;
        }
        
        TST insert(char ch) {
            // get new level
            TST next = middle;
            if (next == null) {
                middle = new TST(ch);
                return middle;
            }
            // add to that level
            while (true) {
                if (ch < next.ch) {
                    if (next.left == null) {
                        next.left = new TST(ch);
                        return next.left;
                    }
                    next = next.left;
                } else if (ch > next.ch){
                    if (next.right == null) {
                        next.right = new TST(ch);
                        return next.right;
                    }
                    next = next.right;
                } else return next;
            }
        }
        
        TST getNext(char ch) {
            // get new level
            TST next = middle;
            if (next == null) 
                return next;
            // search that level
            while (true) {
                if (ch < next.ch) {
                    if (next.left == null) 
                        return next.left;
                    next = next.left;
                } else if (ch > next.ch){
                    if (next.right == null) 
                        return next.right;
                    next = next.right;
                } else return next;
            }
        }

        @Override
        public String toString() {
            return String.format("TST [ch=%s, isWord=%s]", ch, isWord);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

}
