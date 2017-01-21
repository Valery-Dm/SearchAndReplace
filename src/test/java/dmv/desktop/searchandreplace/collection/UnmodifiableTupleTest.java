package dmv.desktop.searchandreplace.collection;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UnmodifiableTupleTest extends TupleTest {
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    private Tuple<String, Integer> target;
    
    @Before
    public void localSetUp() {
        target = createTarget("1", 2);
    }

    @Override
    protected <F, L> Tuple<F, L> createTarget(F first, L last) {
        return new UnmodifiableTuple<>(first, last);
    }

    @Override
    protected <F, L> Tuple<F, L> createTarget() {
        return new UnmodifiableTuple<>();
    }

    @Override
    public void additionsAndRemovals() {
        /* expected exceptions see below */
    }

    @Override
    public void testClear() {
        /* expected exceptions see below */
    }

    @Test
    public void mutationException1() {
        target.toString();
        exception.expect(UnsupportedOperationException.class);
        target.setFirst("3");
    }

    @Test
    public void mutationException2() {
        exception.expect(UnsupportedOperationException.class);
        target.setLast(3);
    }

    @Test
    public void mutationException3() {
        exception.expect(UnsupportedOperationException.class);
        target.removeFirst();
    }

    @Test
    public void mutationException4() {
        exception.expect(UnsupportedOperationException.class);
        target.removeLast();
    }

    @Test
    public void mutationException5() {
        exception.expect(UnsupportedOperationException.class);
        target.remove("1");
    }

    @Test
    public void mutationException6() {
        exception.expect(UnsupportedOperationException.class);
        target.clear();
    }
    
}
