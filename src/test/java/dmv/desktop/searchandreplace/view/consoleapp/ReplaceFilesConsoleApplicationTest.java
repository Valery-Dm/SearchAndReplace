package dmv.desktop.searchandreplace.view.consoleapp;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import dmv.desktop.searchandreplace.model.SearchPath;
import dmv.desktop.searchandreplace.model.SearchProfile;
import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.service.SearchAndReplace;
import dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils;

@SuppressWarnings("static-access")
public class ReplaceFilesConsoleApplicationTest {

    private static ByteArrayOutputStream output;
    
    private ReplaceFilesConsoleApplication target;

    @Before
    public void setUp() throws Exception {
        target = new ReplaceFilesConsoleApplication();
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, "UTF-8"));
    }

    @Test
    public void testHelp() {
        String[] commandLine = new String[]{"help"};
        target.main(commandLine);
        assertThat(output.toString(), is(CmdUtils.HELP));
    }

    @Test
    public void testHelp1() {
        String[] commandLine = new String[]{"h"};
        target.main(commandLine);
        assertThat(output.toString(), is(CmdUtils.HELP));
    }

    @Test
    public void testHelp2() {
        String[] commandLine = new String[]{"/?"};
        target.main(commandLine);
        assertThat(output.toString(), is(CmdUtils.HELP));
    }

    @Test
    public void testHelpWrongCommand() {
        String[] commandLine = new String[]{"!!"};
        target.main(commandLine);
        assertThat(output.toString(), is(CmdUtils.HELP));
    }
    
    /* test various scenarios */
    
    @Test
    public void scenarioMinimalSet() {
        String dir = "src/test/resources/apptest";
        String find = "HamiltonianCycle";
        String commandLine = "-p " + dir + " -f " + find;
        target.main(commandLine.split("\\s+"));
        SearchAndReplace<SearchPath, SearchProfile, SearchResult> replacer = target.getReplacer();
        assertThat(replacer.getRootElement().getPath(), is(dir));
        assertThat(replacer.getProfile().getToFind(), is(find));
    }
    
    @After
    public void tearDown() throws Exception {
        System.setOut(System.out);
    }

}
