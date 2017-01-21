package dmv.desktop.searchandreplace.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;


public class ReplaceMarkerTest {
    
    private ReplaceMarker target;
    private int lineNumber;
    private int startIndex;
    private boolean excluded;

    @Before
    public void setUp() throws Exception {
        lineNumber = 2;
        startIndex = 5;
        excluded = false;
        target = new ReplaceMarker(lineNumber, startIndex, excluded);
    }

    @Test
    public void lineNumber() {
        target.toString();
        int hashCode = target.hashCode();
        assertThat(target.getLineNumber(), is(lineNumber));
        target.setLineNumber(lineNumber + 1);
        assertThat(target.hashCode(), is(not(hashCode)));
        assertThat(target.getLineNumber(), is(not(lineNumber)));
        assertFalse(target.equals(new ReplaceMarker(lineNumber, startIndex, excluded)));
    }
    
    @Test
    public void startIndex() {
        int hashCode = target.hashCode();
        assertThat(target.getStartIndex(), is(startIndex));
        target.setStartIndex(startIndex + 1);
        assertThat(target.hashCode(), is(not(hashCode)));
        assertThat(target.getStartIndex(), is(not(startIndex)));
        assertFalse(target.equals(new ReplaceMarker(lineNumber, startIndex, excluded)));
    }
    
    @Test
    public void excluded() {
        int hashCode = target.hashCode();
        assertThat(target.isExcluded(), is(excluded));
        target.setExcluded(!excluded);
        // hashCode is not excluded marker dependent 
        assertThat(target.hashCode(), is(hashCode));
        assertThat(target.isExcluded(), is(!excluded));
        assertTrue(target.equals(new ReplaceMarker(lineNumber, startIndex, excluded)));
    }
    
    @Test
    public void equals() {
        assertTrue(target.equals(target));
        assertTrue(target.equals(new ReplaceMarker(lineNumber, startIndex, excluded)));
        assertFalse(target.equals(null));
        assertFalse(target.equals("String"));
        
    }
}
