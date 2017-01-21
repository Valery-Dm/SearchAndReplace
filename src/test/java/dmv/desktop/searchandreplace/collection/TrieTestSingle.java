package dmv.desktop.searchandreplace.collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TrieTestSingle extends TrieTest {
    
    @Test
    public void invariant1() {
        targetEmpty.toString();
        targetEmpty.add("3");
        targetEmpty.add("3");
        targetEmpty.add("33");
        assertThat(targetEmpty.size(), is(2));
    }
    
    @Test
    public void invariant2() {
        assertTrue(targetEmpty.isEmpty());
        targetEmpty.add("31");
        targetEmpty.add("3");
        assertThat(targetEmpty.size(), is(2));
        assertFalse(targetEmpty.isEmpty());
    }
    
    @Test
    public void invariant3() {
        targetEmpty.add("3");
        targetEmpty.add("31");
        targetEmpty.add("311");
        targetEmpty.add("331");
        targetEmpty.add("31");
        targetEmpty.add("33");
        assertThat(targetEmpty.size(), is(5));
    }
    
    @Test
    public void contains() {
        target.toString();
        assertTrue(target.contains("ab"));
        assertFalse(target.contains(""));
        assertFalse(target.contains(null));
        assertFalse(targetEmpty.contains("ab"));
    }
    
    @Test
    public void containsAnyFrom() {
        assertFalse(target.containsAnyFrom(null));
        assertFalse(target.containsAnyFrom(""));
        assertFalse(targetEmpty.containsAnyFrom("a"));
        assertTrue(target.containsAnyFrom("a"));
        assertTrue(target.containsAnyFrom("ab"));
        assertTrue(target.containsAnyFrom("abb"));
        assertFalse(target.containsAnyFrom("cabb"));
        assertTrue(target.containsAnyFrom("cb!iO32"));
    }
    
    @Test
    public void aBunchOfWords() {
        words.forEach(w -> assertTrue(target.contains(w)));
        assertThat(target.size(), is(words.size()));
    }
    
    @Test
    public void aBunchOfRandomWords() {
        boolean print = false;
        if (randomWords.size() > 0) {
            Runtime runtime = Runtime.getRuntime();
            long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
            if (print) {
                System.out.printf("Populating trie with %d words of length %d\n", 
                        randomWords.size(), randomWords.iterator().next().length());
                System.out.println("Memory before:    " + getMBs(memoryBefore) + "Mb");
            }
            randomWords.forEach(targetEmpty::add);
            long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
            if (print) {
                System.out.println("Memory after:     " + getMBs(memoryAfter) + "Mb");
                System.out.println("Memory increased: " + getMBs(memoryAfter - memoryBefore) + "Mb");
            }
            randomWords.forEach(targetEmpty::add);
            assertThat(targetEmpty.size(), is(randomWords.size()));
            randomWords.forEach(w -> assertTrue(targetEmpty.contains(w)));
        }
    }
    
    private long getMBs(long bytes) {
        return bytes / 1_000_000;
    }
    
    @Test
    public void nullAndEmpty() {
        int sizeBefore = target.size();
        assertFalse(target.contains(null));
        assertFalse(target.contains(""));

        target.add(null);
        target.add("");
        assertTrue(sizeBefore == target.size());
        assertFalse(target.contains(null));
        assertFalse(target.contains(""));
    }
    
    @Test
    public void addDuplicates() {
        int sizeBefore = target.size();
        words.forEach(target::add);
        assertTrue(sizeBefore == target.size());
    }

}
