package dmv.desktop.searchandreplace.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dmv.desktop.searchandreplace.collection.Tuple;
import dmv.desktop.searchandreplace.collection.TupleImpl;


public abstract class SearchResultTest {
    
    private SearchResult target;
    private SearchResult notRenamedTarget;
    private SearchResult exceptionalTarget;
    
    private Tuple<Path, Path> modifiedPath;
    private Tuple<Path, Path> notModifiedPath;
    private List<Tuple<String, String>> modifiedContent;
    
    private int numberOfModifications = 2;
    
    private Throwable cause = new IOException();

    @Before
    public void setUp() throws Exception {
        modifiedPath = new TupleImpl<>(Paths.get("res/testFindMe.txt"),
                                   Paths.get("res/testReplaced.txt"));
        notModifiedPath = new TupleImpl<>(Paths.get("res/test.txt"), null);
        modifiedContent = new ArrayList<>(Arrays.asList(
                                        new TupleImpl<>("some text with FindMe word", 
                                                    "some text with Replaced word"),
                                        new TupleImpl<>("some text without that word", null)));
        target = getSearchResult(modifiedPath, modifiedContent, numberOfModifications, false, null);
        notRenamedTarget = getSearchResult(notModifiedPath, modifiedContent, 
                                           numberOfModifications, false, null);
        exceptionalTarget = getSearchResult(null, null, 0, true, cause);
    }
    
    protected SearchResult getTarget() {
        return target;
    }
    
    protected SearchResult getNotRenamedTarget() {
        return notRenamedTarget;
    }
    
    protected SearchResult getExceptionalTarget() {
        return exceptionalTarget;
    }
    
    protected Tuple<Path, Path> getModifiedPath() {
        return modifiedPath;
    }

    protected Tuple<Path, Path> getNotModifiedPath() {
        return notModifiedPath;
    }
    
    protected List<Tuple<String, String>> getModifiedContent() {
        return modifiedContent;
    }

    protected int getNumberOfModifications() {
        return numberOfModifications;
    }
    
    protected Throwable getCause() {
        return cause;
    }
    
    protected abstract SearchResult getSearchResult(Tuple<Path, Path> modifiedPath, 
                                                    List<Tuple<String, String>> modifiedContent, 
                                                    int numberOfModifications,
                                                    boolean exceptional, Throwable cause);

    @Test
    public void getModifiedName() {
        Tuple<Path, Path> modifiedName = target.getModifiedName();
        assertThat(modifiedName, is(modifiedPath));

        assertThat(notRenamedTarget.getModifiedName(), is(notModifiedPath));
        
        /* immutability check */
        
        modifiedPath.setFirst(null);
        assertFalse(modifiedName.equals(modifiedPath));
        
        notModifiedPath.setLast(Paths.get("res/testReplaced.txt"));
        assertFalse(notRenamedTarget.getModifiedName().equals(notModifiedPath));
        
        // can be either defensive copying or modification exception
        try {
            modifiedName.setFirst(Paths.get("res"));
            assertFalse(target.getModifiedName().equals(modifiedName));
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }
    
    @Test
    public void modifiedContent() {
        List<Tuple<String, String>> targetContent = target.getModifiedContent();
        assertThat(targetContent, is(modifiedContent));
        
        /* immutability check */
        
        modifiedContent.add(new TupleImpl<>("some additional text", "with wrong result"));
        assertFalse(targetContent.equals(modifiedContent));
        
        modifiedContent.remove(modifiedContent.size() - 1);
        modifiedContent.get(0).setFirst(null);
        assertFalse(targetContent.equals(modifiedContent));

        // can be either defensive copying or modification exception
        try {
            targetContent.add(new TupleImpl<>());
            assertFalse(target.getModifiedContent().equals(targetContent));
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
        
        try {
            targetContent.get(0).setFirst("other string");
            assertFalse(target.getModifiedContent().equals(targetContent));
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }
    
    @Test
    public void numberOfModificationsMade() {
        assertThat(target.numberOfModificationsMade(), is(numberOfModifications));
    }

    @Test
    public void exceptional() {
        assertFalse(target.isExceptional());
        assertThat(target.getCause(), is(nullValue()));
        
        assertTrue(exceptionalTarget.isExceptional());
        assertThat(exceptionalTarget.getCause(), is(cause));

        /* immutability check */
        
        StackTraceElement[] stack = new StackTraceElement[]{new StackTraceElement("class", "method", "file", 1)};
        cause.setStackTrace(stack);
        assertFalse(exceptionalTarget.getCause().getStackTrace().equals(stack));
    }
}
