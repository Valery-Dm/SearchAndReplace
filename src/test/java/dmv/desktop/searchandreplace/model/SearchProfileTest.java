package dmv.desktop.searchandreplace.model;

import static dmv.desktop.searchandreplace.model.SearchProfile.defaultCharset;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public abstract class SearchProfileTest {
    
    @Rule
    public ExpectedException expected = ExpectedException.none();
    
    private SearchProfile target;
    private String toFind;
    
    private Charset charset;
    private String replaceWith;
    private Exclusions exclusions;

    @Before
    public void setUp() throws Exception {
        target = buildTarget();
        charset = ISO_8859_1;
        toFind = getToFind();
        replaceWith = "Replaced";
        exclusions = new ExclusionsTrie(Arrays.asList("prefix1", "prefix2"), 
                                        Arrays.asList("suffix1", "suffix2"), false);
    }

    protected abstract SearchProfile buildTarget();
    
    protected abstract String getToFind();

    @Test
    public void testCharset() {
        assertThat(target.getCharset(), is(defaultCharset));
        
        assertThat(target.setCharset(charset)
                         .getCharset(), is(charset));
        
        assertThat(target.setCharset(null)
                         .getCharset(), is(defaultCharset));
    }

    @Test
    public void testIsFileName() {
        /* false by default */
        assertFalse(target.isFileName());
        
        assertTrue(target.setFilename(true)
                         .isFileName());
    }

    @Test
    public void testToFind() {
        /* must be set by default */
        assertThat(target.getToFind(), is(toFind));
        
        String other = toFind + "other";
        assertThat(target.setToFind(other)
                         .getToFind(), is(other));
    }
    
    @Test
    public void nullToFind() {
        expected.expect(IllegalArgumentException.class);
        target.setToFind(null);
    }
    
    @Test
    public void emptyToFind() {
        expected.expect(IllegalArgumentException.class);
        target.setToFind("");
    }

    @Test
    public void testReplaceWith() {
        /* empty by default */
        assertThat(target.getReplaceWith(), is(""));
        
        assertThat(target.setReplaceWith(replaceWith)
                         .getReplaceWith(), is(replaceWith));
        
        assertThat(target.setReplaceWith(null)
                         .getReplaceWith(), is(""));

        assertThat(target.setReplaceWith(replaceWith + "!")
                         .getReplaceWith(), is(replaceWith + "!"));

        assertThat(target.setReplaceWith("")
                         .getReplaceWith(), is(""));
    }

    @Test
    public void testExclusions() {
        /* empty by default */
        assertTrue(target.getExclusions().isEmpty());
        
        assertThat(target.setExclusions(exclusions)
                         .getExclusions(), is(exclusions));
    }
    
}
