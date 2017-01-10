package dmv.desktop.searchandreplace.model;

public class SearchProfileImplTest extends SearchProfileTest {

    @Override
    protected SearchProfile buildTarget() {
        return new SearchProfileImpl("FindMe");
    }

    @Override
    protected String getToFind() {
        return "FindMe";
    }

}
