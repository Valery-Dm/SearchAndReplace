package dmv.desktop.searchandreplace.model;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;


public class SearchPathImplTest extends SearchPathTest {

    @Override
    protected SearchPath getSearchPath(Path path) {
        return SearchPathImpl.getBuilder(path).build();
    }

    @Test
    public void testNullFolderBuilder() {
        getSearchPath(Paths.get("res")).toString();
        expected.expect(NullPointerException.class);
        getSearchPath(null);
    }

}
