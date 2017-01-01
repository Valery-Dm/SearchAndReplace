package dmv.desktop.searchandreplace.collection;

import static java.lang.Character.MAX_VALUE;
import static java.lang.Character.toChars;

import java.util.*;

import org.junit.Before;
import org.junit.BeforeClass;

import dmv.desktop.searchandreplace.collection.ExactSearchTrie;
import dmv.desktop.searchandreplace.collection.Trie;

public abstract class TrieTest {

    protected Trie target;
    protected Trie targetEmpty;
    protected static List<String> words;
    protected static Set<String> randomWords;
    
    @BeforeClass
    public static void wordsToAdd() {
        words = new ArrayList<>();
        words.addAll(Arrays.asList(new String[]{
                        "a", "ab", "ac", "abb", "abc", "abbo", 
                        "abbat", "cb!iO32", "asldSDalkFjlkdsfje1",
                        "asldSDa2lkFjlkdsfje", "asldSDalkFjlkdsf"
                    }));
        int W = 30_000, WL = 100;
        Random rand = new Random();
        randomWords = new HashSet<>(W);
        StringBuilder sb = new StringBuilder(WL);
        while (W-- > 0) {
            while (sb.length() < WL) 
                sb.append(toChars(rand.nextInt(MAX_VALUE)));
            randomWords.add(sb.toString());
            sb = new StringBuilder(WL);
        }
    }
    
    @Before
    public void setUp() throws Exception {
        target = new ExactSearchTrie();
        words.forEach(target::add);
        targetEmpty = new ExactSearchTrie();
    }
   
}
