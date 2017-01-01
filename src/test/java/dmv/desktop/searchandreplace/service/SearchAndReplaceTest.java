package dmv.desktop.searchandreplace.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Objects;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import dmv.desktop.searchandreplace.model.Exclusions;

@RunWith(MockitoJUnitRunner.class)
public class SearchAndReplaceTest {
    
    private SearchAndReplace<String, String> target; 
    
    @Rule
    public ExpectedException expected = ExpectedException.none();
    
    private String root = "new root";
    private String toFind = "find me";
    private String replaceWith = "replaced";
    
    @Mock
    private Exclusions exclusions;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testRootElement() {
        // First check is not mandatory as parameters
        // may be set upon construction
        //assertTrue(Objects.isNull(target.getRootElement()));
        
        target.setRootElement(root);
        assertThat(target.getRootElement(), is(root));
        
        expected.expect(NullPointerException.class);
        target.setRootElement(null);
        
    }

    @Test
    public void testToFind() {
        //assertTrue(Objects.isNull(target.getToFind()));
        
        target.setToFind(toFind);
        assertThat(target.getToFind(), is(toFind));
        
        expected.expect(IllegalArgumentException.class);
        target.setToFind("");
    }
    
    @Test
    public void testToFindNull() {
        expected.expect(IllegalArgumentException.class);
        target.setToFind(null);
    }

    @Test
    public void testReplaceWith() {
        //assertTrue(Objects.isNull(target.getReplaceWith()));
        
        target.setReplaceWith(replaceWith);
        assertThat(target.getReplaceWith(), is(replaceWith));
        
        target.setReplaceWith(null);
        assertTrue(Objects.isNull(target.getReplaceWith()));

        target.setReplaceWith(replaceWith);
        assertThat(target.getReplaceWith(), is(replaceWith));
        
        target.setReplaceWith("");
        assertTrue(Objects.isNull(target.getReplaceWith()));
    }
    
    @Test
    public void testExclusions() {
        //assertTrue(Objects.isNull(target.getExclusions()));
        
        target.setExclusions(exclusions);
        assertThat(target.getExclusions(), is(exclusions));
        
        target.setExclusions(null);
        assertTrue(Objects.isNull(target.getExclusions()));
    }

}
