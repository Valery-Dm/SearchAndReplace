package dmv.desktop.searchandreplace.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import dmv.desktop.searchandreplace.collection.Tuple;


public abstract class SearchResultTest {
    
    private SearchResult target;
    private SearchResult notRenamedTarget;
    private SearchResult exceptionalTarget;
    
    private Path originalPath = Paths.get("res/testFindMe.txt");
    private Path modifiedPath = Paths.get("res/testReplaced.txt");
    private Path nullPointer = null;
    
    private List<String> originalContent = Arrays.asList("some text with FindMe word", 
                                                         "some text without");
    private List<String> modifiedContent = Arrays.asList("some text with Replaced word", 
                                                         "some text without");
    
    private int numberOfModifications = 2;
    
    private Throwable cause = new IOException();

    @Before
    public void setUp() throws Exception {
        target = getSearchResult(originalPath, modifiedPath, 
                                 originalContent, modifiedContent, 
                                 numberOfModifications, false, null);
        notRenamedTarget = getSearchResult(originalPath, nullPointer, 
                                           originalContent, modifiedContent, 
                                           numberOfModifications, false, null);
        exceptionalTarget = getSearchResult(null, null, null, null, numberOfModifications, true, cause);
    }

    protected abstract SearchResult getSearchResult(Path originalPath, Path modifiedPath, 
                                                    List<String> originalContent, 
                                                    List<String> modifiedContent, 
                                                    int numberOfModifications,
                                                    boolean exceptional, Throwable cause);

    @Test
    public void getModifiedName() {
        Tuple<Path, Path> name = target.getModifiedName();
        assertThat(name.getFirst(), is(originalPath));
        assertThat(name.getLast(), is(modifiedPath));
        
        name = notRenamedTarget.getModifiedName();
        assertThat(name.getFirst(), is(originalPath));
        assertThat(name.getLast(), is(nullPointer));
    }
    
    @Test
    public void getModifiedContent() {
        List<Tuple<String, String>> content = target.getModifiedContent();
        List<String> origContent = content.stream()
               .map(tuple -> tuple.getFirst())
               .collect(Collectors.toList());
        List<String> modContent = content.stream()
                .map(tuple -> tuple.getLast())
                .collect(Collectors.toList());
        assertThat(origContent, is(originalContent));
        assertThat(modContent, is(modifiedContent));
    }
    
    @Test
    public void numberOfModificationsMade() {
        assertThat(target.numberOfModificationsMade(), is(numberOfModifications));
    }

    @Test
    public void exceptional() {
        assertFalse(target.isExceptional());
        assertThat(target.getCause(), is(nullPointer));
        
        assertTrue(exceptionalTarget.isExceptional());
        assertThat(exceptionalTarget.getCause(), is(cause));
    }
}
