package dmv.desktop.searchandreplace.worker;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_16;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dmv.desktop.searchandreplace.model.Exclusions;
import dmv.desktop.searchandreplace.worker.FilesWallker.FilesWallkerBuilder;


public class FilesWallkerBuilderTest {
    
    private FilesWallkerBuilder target;
    private String dirName;
    private String toFind;
    private String replaceWith;
    
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        dirName = "src/test/resources";
        target = new FilesWallkerBuilder(dirName);
        toFind = "FindMe";
        replaceWith = "Replaced";
    }

    @Test
    public void nullDir() {
        exception.expect(NullPointerException.class);
        target = new FilesWallkerBuilder(null);
    }
    
    @Test
    public void setDir() {
        setDefaults();
        assertThat(target.build().getRootDirectory(), is(Paths.get(dirName)));
        
        exception.expect(IllegalArgumentException.class);
        target = new FilesWallkerBuilder("/as\\er#q");
    }

    @Test
    public void nullReplaceWith() {
        exception.expect(NullPointerException.class);
        target.setReplaceWith(null);
    }

    @Test
    public void emptyReplaceWith() {
        exception.expect(IllegalArgumentException.class);
        target.setReplaceWith("");
    }

    @Test
    public void replaceWithNotSet() {
        exception.expect(IllegalArgumentException.class);
        target.setToFind(toFind);
        target.build();
    }

    @Test
    public void replaceWithSet() {
        setDefaults();
        assertThat(target.build().getReplaceWith(), is(replaceWith));
    }
    
    @Test
    public void nullToFind() {
        exception.expect(NullPointerException.class);
        target.setToFind(null);
    }
    
    @Test
    public void emptyToFind() {
        exception.expect(IllegalArgumentException.class);
        target.setToFind("");
    }
    
    @Test
    public void toFindSet() {
        setDefaults();
        assertThat(target.build().getToFind(), is(toFind));
    }
    
    @Test
    public void toFindNotSet() {
        exception.expect(IllegalArgumentException.class);
        target.setReplaceWith(replaceWith);
        target.build();
    }

    @Test
    public void buildDefaults() {
        setDefaults();
        assertThat(target.build(), isA(FilesWallker.class));
    }

    private void setDefaults() {
        target.setToFind(toFind);
        target.setReplaceWith(replaceWith);
    }

    @Test
    public void buildCharSet() {
        setDefaults();
        
        target.setCharSet(ISO_8859_1);
        assertThat(target.build().getCharSet(), is(ISO_8859_1));
        
        target.setCharSet(UTF_16);
        assertThat(target.build().getCharSet(), is(UTF_16));
        
        exception.expect(NullPointerException.class);
        target.setCharSet(null);
    }

    @Test
    public void buildIsFileNames() {
        setDefaults();

        target.setFileNames(true);
        assertTrue(target.build().isFileNames());
        
        target.setFileNames(false);
        assertFalse(target.build().isFileNames());
    }

    @Test
    public void buildIsSubfolders() {
        setDefaults();
        
        target.setSubfolders(true);
        assertTrue(target.build().isSubfolders());
        
        target.setSubfolders(false);
        assertFalse(target.build().isSubfolders());
    }
    
    @Test
    public void setFileTypes() {
        setDefaults();
        
        // Should be ignored
        //target.setFileTypes(null);
        
        target.setFileTypes("@#", "java", "./class");
        PathMatcher fileTypes = target.build().getFileTypes();
        assertTrue(fileTypes.matches(Paths.get(".@#")));
        assertTrue(fileTypes.matches(Paths.get(".java")));
        assertTrue(fileTypes.matches(Paths.get(".class")));

        exception.expect(IllegalArgumentException.class);
        target.setFileTypes(".*mal.for*med");
        target.build();
    }
    
    @Test
    public void setFileTypesLeadingDot() {
        setDefaults();
        
        exception.expect(IllegalArgumentException.class);
        target.setFileTypes(".dot");
        target.build();
    }
    
    @Test
    public void setFileTypesNull() {
        exception.expect(IllegalArgumentException.class);
        target.setFileTypes(".dot", null);
    }
    
    @Test
    public void setExclusions() {
        setDefaults();
        
        // should be ignored
        //target.setExclusions(null);
        target.setExclusions("", null);
        
        target.setExclusions("DoNot" + toFind, toFind + "Not");
        Exclusions exclusionsTrie = target.build().getExclusions();
        assertTrue(exclusionsTrie.containsPrefix("toNoD", false));
        assertTrue(exclusionsTrie.containsSuffix("Not"));
        
        exception.expect(IllegalArgumentException.class);
        target.setExclusions("DoNot" + toFind, "Not");
        target.build();
    }
}
