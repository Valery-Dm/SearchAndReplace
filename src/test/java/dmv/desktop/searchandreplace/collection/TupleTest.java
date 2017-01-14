package dmv.desktop.searchandreplace.collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public abstract class TupleTest {

    private Tuple<String, Integer> target;
    private Tuple<String, Integer> emptyTarget;
    private String first = "1";
    private Integer last = 2;

    @Before
    public void setUp() {
        target = createTarget(first, last);
        emptyTarget = createTarget();
    }

    protected abstract <F, L> Tuple<F, L> createTarget(F first, L last);
    
    protected abstract <F, L> Tuple<F, L> createTarget();

    @Test
    public void defaultConstructor() {
        assertThat(emptyTarget.size(), is(0));
        assertTrue(emptyTarget.isEmpty());
        assertThat(emptyTarget.getFirst(), is(nullValue()));
        assertThat(emptyTarget.getLast(), is(nullValue()));
    }

    @Test
    public void argumentsConstructor() {
        assertFalse(target.isEmpty());
        assertThat(target.size(), is(2));
        assertThat(target.getFirst(), is(first));
        assertThat(target.getLast(), is(last));
    
        target = createTarget(first, null);
        assertThat(target.size(), is(1));
        assertThat(target.getFirst(), is(first));
        assertThat(target.getLast(), is(nullValue()));
    
        target = createTarget(null, last);
        assertThat(target.size(), is(1));
        assertThat(target.getFirst(), is(nullValue()));
        assertThat(target.getLast(), is(last));
    }

    @Test
    public void additionsAndRemovals() {
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
        assertThat(target.size(), is(0));
        
        
        target = createTarget(first, last);
        
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
        Tuple<String, String> special = createTarget();
        String dup = "duplicate";
        special.setFirst(dup);
        special.setLast(dup);
        assertThat(special.size(), is(2));
        
        assertTrue(special.remove(dup));
        assertThat(special.size(), is(0));
    
    }

    @Test
    public void testContains() {
        target = createTarget();
        assertFalse(target.contains(null));
        assertFalse(target.contains(first));
        assertFalse(target.contains(last));
        
        last = Integer.parseInt(first);
        target = createTarget(first, last);
        assertTrue(target.contains(first));
        assertTrue(target.contains(last));
        assertTrue(target.contains(last + ""));
        assertFalse(target.contains(null));
        assertFalse(target.contains(first + last));
        assertFalse(target.contains(true));
        
        Tuple<String, String> other = createTarget("first", "last");
        assertTrue(other.contains("first"));
        assertTrue(other.contains("last"));
    }

    @Test
    public void testClear() {
        target = createTarget(first, last);
        target.clear();
        assertThat(target.getFirst(), is(nullValue()));
        assertThat(target.getLast(), is(nullValue()));
        assertThat(target.size(), is(0));
    }

    @Test
    public void testEquals() {
        target = createTarget(first, last);
        assertFalse(target.equals(null));
        assertFalse(target.equals(true));
        assertFalse(target.equals(createTarget(first, last + "")));
        assertTrue(target.equals(target));
    
        Tuple<String, Integer> other = createTarget(first, last);
        assertTrue(target.equals(other));
        other = createTarget(null, last);
        assertFalse(target.equals(other));
        assertFalse(other.equals(target));
        
        other = createTarget(new String(first), last + 0);
        assertTrue(target.equals(other));
        other = createTarget(new String(first), null);
        assertFalse(target.equals(other));
        assertFalse(other.equals(target));
    
        other = createTarget(first, last + 1);
        assertFalse(target.equals(other));
        target = createTarget(null, last);
        assertFalse(target.equals(other));
        assertFalse(other.equals(target));
        other = createTarget(first, null);
        assertFalse(target.equals(other));
        assertFalse(other.equals(target));
        
        target = createTarget();
        other = createTarget();
        assertTrue(target.equals(other));
    }

}