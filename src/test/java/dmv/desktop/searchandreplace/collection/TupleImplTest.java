package dmv.desktop.searchandreplace.collection;

public class TupleImplTest extends TupleTest {

    @Override
    protected <F, L> Tuple<F, L> createTarget(F first, L last) {
        return new TupleImpl<>(first, last);
    }

    @Override
    protected <F, L> Tuple<F, L> createTarget() {
        return new TupleImpl<>();
    }

}
