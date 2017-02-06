package dmv.desktop.searchandreplace.view.consoleapp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dmv.desktop.searchandreplace.collection.Tuple;
import dmv.desktop.searchandreplace.collection.TupleImpl;
import dmv.desktop.searchandreplace.exception.WrongProfileException;
import dmv.desktop.searchandreplace.model.*;
import dmv.desktop.searchandreplace.service.SearchAndReplace;
import dmv.desktop.searchandreplace.view.profile.ReplaceFilesProfile;


public class ReplaceFilesConsoleApplicationTest {
    
    @Mock
    private SearchAndReplace<SearchPath, SearchProfile, SearchResult> replacer;
    @Mock
    private ReplaceFilesProfile replaceProfile;
    @InjectMocks
    private ReplaceFilesConsoleApplication target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void test1() {
      target.parseCommand("-p", "res/subfolder", "-f", "findMe", "-r", "replaced");
      SearchProfile searchProfile = target.getReplacer().getProfile();
      SearchPath searchPath = target.getReplacer().getRootElement();
      assertThat(searchPath.getPath(), is(Paths.get("res/subfolder")));
      assertThat(searchProfile.getToFind(), is("findMe"));
    }

    @Test
    public void test() throws WrongProfileException {
//        when(replacer.getState())
//            .thenReturn(SearchAndReplace.State.FIND_OTHER);
//        State state = target.getReplacer().getState();
//        System.out.println(state);
        

        
        Tuple<Path, Path> modName = new TupleImpl<>(Paths.get("findMefile.txt"), 
                                                    Paths.get("replacedfile.txt"));
        List<Tuple<String, String>> modContent = 
                Arrays.asList(new TupleImpl<>("Some text", null),
                              new TupleImpl<>("Some text with findMe", "Some text with replaced"));
        when(replacer.preview())
            .thenReturn(Arrays.asList(SearchResultImpl.getBuilder()
                                                      .setExceptional(true)
                                                      .setCause(new IOException("no such file exists"))
                                                      .build(),
                                      SearchResultImpl.getBuilder()
                                                      .setNumberOfModificationsMade(2)
                                                      .setModifiedName(modName)
                                                      .setModifiedContent(modContent)
                                                      .build()));
        
        List<SearchResult> preview = target.getReplacer().preview();
        System.out.println(preview);
    }

}
