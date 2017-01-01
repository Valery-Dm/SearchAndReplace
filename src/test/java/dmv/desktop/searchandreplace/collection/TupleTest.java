package dmv.desktop.searchandreplace.collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class TupleTest {
    
    Tuple<String, Integer> target;
    
    private Object nullPointer = null;
    private String first = "1";
    private Integer last = 2;

    @Test
    public void defaultConstructor() {
        target = new Tuple<>();
        assertThat(target.size(), is(0));
        assertThat(target.getFirst(), is(nullPointer));
        assertThat(target.getLast(), is(nullPointer));
    }

    @Test
    public void argumentsConstructor() {
        target = new Tuple<>(first, last);
        assertThat(target.size(), is(2));
        assertThat(target.getFirst(), is(first));
        assertThat(target.getLast(), is(last));

        target = new Tuple<>(first, null);
        assertThat(target.size(), is(1));
        assertThat(target.getFirst(), is(first));
        assertThat(target.getLast(), is(nullPointer));

        target = new Tuple<>(null, last);
        assertThat(target.size(), is(1));
        assertThat(target.getFirst(), is(nullPointer));
        assertThat(target.getLast(), is(last));
    }

    @Test
    public void additionsAndRemovals() {
        target = new Tuple<>(first, last);
        assertFalse(target.isEmpty());
        
        /* Duplicates */
        assertFalse(target.setFirst(first));
        assertThat(target.size(), is(2));
        
        assertFalse(target.setLast(last));
        assertThat(target.size(), is(2));

        /* Nulls */
        assertTrue(target.setFirst(null));
        assertThat(target.size(), is(1));
        
        assertFalse(target.isEmpty());
        
        assertTrue(target.setLast(null));
        assertThat(target.size(), is(0));
        
        assertTrue(target.isEmpty());

        /* Insertions */
        assertTrue(target.setFirst(first));
        assertThat(target.size(), is(1));
        
        assertTrue(target.setLast(last));
        assertThat(target.size(), is(2));

        /* Removals */
        assertTrue(target.removeFirst());
        assertThat(target.size(), is(1));
        
        assertTrue(target.removeLast());
        assertThat(target.size(), is(0));

        assertFalse(target.removeFirst());
        assertFalse(target.removeLast());
        
        
        target = new Tuple<>(first, last);
        
        /* Replacements */
        assertTrue(target.setFirst("" + last));
        assertThat(target.size(), is(2));
        assertThat(target.getFirst(), is("" + last));
        
        assertTrue(target.setLast(Integer.parseInt(first)));
        assertThat(target.size(), is(2));
        assertThat(target.getLast(), is(Integer.parseInt(first)));
        
        /* Object removal */
        assertFalse(target.remove(null));
        assertThat(target.size(), is(2));

        assertFalse(target.remove(first));
        assertThat(target.size(), is(2));

        assertFalse(target.remove(last));
        assertThat(target.size(), is(2));

        assertTrue(target.remove("" + last));
        assertThat(target.size(), is(1));

        assertTrue(target.remove(Integer.parseInt(first)));
        assertThat(target.size(), is(0));

        assertFalse(target.remove("" + last));
        assertFalse(target.remove(Integer.parseInt(first)));
        
        /* Special case: double deletion */
        Tuple<String, String> special = new Tuple<>();
        String dup = "duplicate";
        special.setFirst(dup);
        special.setLast(dup);
        assertThat(special.size(), is(2));
        
        assertTrue(special.remove(dup));
        assertThat(special.size(), is(0));

    }

    @Test
    public void testContains() {
        target = new Tuple<>();
        assertFalse(target.contains(null));
        assertFalse(target.contains(first));
        assertFalse(target.contains(last));
        
        last = Integer.parseInt(first);
        
        target.setFirst(first);
        target.setLast(last);
        assertTrue(target.contains(first));
        assertTrue(target.contains(last));
        assertTrue(target.contains(last + ""));
        assertFalse(target.contains(null));
        assertFalse(target.contains(first + last));
        assertFalse(target.contains(true));
    }

    @Test
    public void testClear() {
        target = new Tuple<>(first, last);
        target.clear();
        assertThat(target.getFirst(), is(nullPointer));
        assertThat(target.getLast(), is(nullPointer));
        assertThat(target.size(), is(0));
    }

    @Test
    public void testEquals() {
        target = new Tuple<>(first, last);
        assertFalse(target.equals(null));
        assertFalse(target.equals(true));
        assertFalse(target.equals(new Tuple<String, String>(first, last + "")));
        assertTrue(target.equals(target));

        Tuple<String, Integer> other = new Tuple<>(first, last);
        assertTrue(target.equals(other));
        other.removeFirst();
        assertFalse(target.equals(other));
        assertFalse(other.equals(target));
        
        other = new Tuple<>(new String(first), last + 0);
        assertTrue(target.equals(other));
        other.removeLast();
        assertFalse(target.equals(other));
        assertFalse(other.equals(target));

        other = new Tuple<>(first, last + 1);
        assertFalse(target.equals(other));
        target.removeFirst();
        assertFalse(target.equals(other));
        assertFalse(other.equals(target));
        other.removeLast();
        assertFalse(target.equals(other));
        assertFalse(other.equals(target));
        
        target = new Tuple<>();
        other = new Tuple<>();
        assertTrue(target.equals(other));
    }

}
