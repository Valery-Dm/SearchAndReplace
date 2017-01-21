package dmv.desktop.searchandreplace.model;

import static dmv.desktop.searchandreplace.model.SearchPath.defaultPattern;
import static dmv.desktop.searchandreplace.model.SearchPath.defaultSubfolders;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public abstract class SearchPathTest {
    
    private SearchPath target;
    @Rule
    public ExpectedException expected = ExpectedException.none();
    
    private Path path = Paths.get("res");
    private String[] types1 = {"*.txt", "res/*.cs", "foo.?"};
    private String[] files1 = {"file.txt", "res/file.cs", "foo.z"};
    private String[] types2 = {"*.*", "*/*"};
    private String illegalType = "{{}}";
    
    abstract protected SearchPath getSearchPath(Path path);

    @Before
    public void setUp() throws Exception {
        target = getSearchPath(path);
    }

    @Test
    public void testSetFolder() {
        assertThat(target.getPath(), is(path));
        Path newPath = path.resolve("foo");
        assertThat(target.setPath(newPath)
                         .getPath(), is(newPath));
    }

    @Test
    public void testNullFolder() {
        expected.expect(NullPointerException.class);
        target.setPath(null);
    }

    @Test
    public void testSetNamePattern() {
        assertThat(target.getNamePattern(), is(SearchPath.defaultPattern));
        
        PathMatcher fileTypes = target.setNamePattern(types1)
                                      .getNamePattern();
        for (String s : files1)
            assertTrue(s, fileTypes.matches(Paths.get(s)));
        
        fileTypes = target.setNamePattern(types2)
                          .getNamePattern();
        for (String s : files1)
            assertTrue(s, fileTypes.matches(Paths.get(s)));
        
        assertThat(target.setNamePattern(new String[]{})
                         .getNamePattern(), is(defaultPattern));

        // varargs warning
//        assertThat(target.setNamePattern(null)
//                         .getNamePattern(), is(SearchPath.defaultPattern));

        // exception must not change previous value
        try {
            target.setNamePattern(illegalType);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertThat(target.getNamePattern(), is(defaultPattern));
        }
        
        try {
            target = target.setNamePattern(types2);
            fileTypes = target.getNamePattern();
            target.setNamePattern(illegalType);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertThat(target.getNamePattern(), is(fileTypes));
        }
    }

    @Test
    public void testSetSubfolders() {
        assertThat(target.isSubfolders(), is(defaultSubfolders));
        assertThat(target.setSubfolders(!defaultSubfolders)
                         .isSubfolders(), is(!defaultSubfolders));
    }
    
    @Test
    public void immutabilityCheck() {
        target.setPath(path.resolve("foo"));
        assertThat(target.getPath(), is(path));
        
        target.setNamePattern(files1);
        assertThat(target.getNamePattern(), is(defaultPattern));
        
        target.setSubfolders(!defaultSubfolders);
        assertThat(target.isSubfolders(), is(defaultSubfolders));
    }

}
