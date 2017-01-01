package dmv.desktop.searchandreplace.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Objects;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class SearchFolderTest {
    
    private SearchFolder target;
    @Rule
    public ExpectedException expected = ExpectedException.none();
    
    private Charset charset = StandardCharsets.UTF_8;
    private Path folder = Paths.get("res");
    private String[] types1 = {"*.txt", "res/*.cs", "foo.?"};
    private String[] files1 = {"file.txt", "res/file.cs", "foo.z"};
    private String[] types2 = {"*.*", "*/*"};
    private String illegalType = "{{}}";

    @Before
    public void setUp() throws Exception {
        target = new SearchFolderImpl();
    }

    @Test
    public void testSetFolder() {
        assertTrue(Objects.isNull(target.getFolder()));
        target.setFolder(folder);
        assertThat(target.getFolder(), is(folder));
    }

    @Test
    public void testNullFolder() {
        expected.expect(NullPointerException.class);
        target.setFolder(null);
    }

    @Test
    public void testCharset() {
        assertThat(target.getCharset(), is(SearchFolder.defaultCharset));
        target.setCharset(charset);
        assertThat(target.getCharset(), is(charset));
    }
    
    @Test
    public void testNullCharset() {
        expected.expect(NullPointerException.class);
        target.setCharset(null);
    }

    @Test
    public void testSetFileTypes() {
        assertTrue(Objects.isNull(target.getFileTypes()));
        
        target.setFileTypes(types1);
        PathMatcher fileTypes = target.getFileTypes();
        for (String s : files1)
            assertTrue(s, fileTypes.matches(Paths.get(s)));
        
        target.setFileTypes(new String[]{});
        assertTrue(Objects.isNull(target.getFileTypes()));

        target.setFileTypes(types2);
        fileTypes = target.getFileTypes();
        for (String s : files1)
            assertTrue(s, fileTypes.matches(Paths.get(s)));
        
        try {
            target.setFileTypes(illegalType);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertTrue(Objects.isNull(target.getFileTypes()));
        }
    }

    @Test
    public void testSetFileNames() {
        assertFalse(target.isFileNames());
        target.setFileNames(true);
        assertTrue(target.isFileNames());
    }

    @Test
    public void testSetSubfolders() {
        assertFalse(target.isSubfolders());
        target.setSubfolders(true);
        assertTrue(target.isSubfolders());
    }

}