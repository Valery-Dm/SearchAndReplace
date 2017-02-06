package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.DO_COMMAND;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.MENU_GO_BACK;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.MENU_RESULTS_PREVIEW;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.PROMPT;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.listResults;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import dmv.desktop.searchandreplace.collection.Tuple;
import dmv.desktop.searchandreplace.collection.TupleImpl;
import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.model.SearchResultImpl;
import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;
import dmv.desktop.searchandreplace.view.consoleapp.ReplaceFilesConsoleApplication;


public class PreviewResultsMenuTest extends ConsoleMenuTest {
    
    private PreviewResultsMenu thisTarget;
    private ReplaceFilesConsoleApplication app = mock(ReplaceFilesConsoleApplication.class);
    private List<SearchResult> results;

    @Override
    protected ConsoleMenu getTarget(ConsoleApplication app, ConsoleMenu from) {
        if (app == null) return new PreviewResultsMenu(null, from);
        Tuple<Path, Path> modName = new TupleImpl<>(Paths.get("findMefile.txt"), 
                                                    Paths.get("replacedfile.txt"));
        List<Tuple<String, String>> modContent = 
                  Arrays.asList(new TupleImpl<>("Some text", null),
                                new TupleImpl<>("Some text with findMe", "Some text with replaced"));
        results = Arrays.asList(SearchResultImpl.getBuilder()
                                                .setExceptional(true)
                                                .setCause(new IOException("can't read file unreadable.txt"))
                                                .build(),
                                SearchResultImpl.getBuilder()
                                                .setNumberOfModificationsMade(2)
                                                .setModifiedName(modName)
                                                .setModifiedContent(modContent)
                                                .build());
        when(((ReplaceFilesConsoleApplication) app).preview())
                                                   .thenReturn(results);
        thisTarget = new PreviewResultsMenu((ReplaceFilesConsoleApplication) app, from);
        return thisTarget;
    }

    @Override
    protected boolean isMenuShown(String output) {
        return (listResults(results) + MENU_RESULTS_PREVIEW + MENU_GO_BACK + PROMPT).equals(output);
    }

    @Override
    protected boolean isMenuHelpShown(String output) {
        return (MENU_RESULTS_PREVIEW + MENU_GO_BACK + PROMPT).equals(output);
    }

    @Override
    protected ConsoleApplication getApp() {
        return app;
    }

    @Test(expected=IllegalArgumentException.class)
    public void tooBigNumberSelected() {
        thisTarget.accept(new String[]{"10"});
    }

    @Test(expected=IllegalArgumentException.class)
    public void tooSmallNumberSelected() {
        thisTarget.accept(new String[]{"-1"});
    }

    @Test(expected=NumberFormatException.class)
    public void notNumber() {
        thisTarget.accept(new String[]{"qwerty"});
    }
    
    @Test
    public void rightNumberSelected() {
        thisTarget.accept(new String[]{"1"});
        assertTrue(thisTarget.next() instanceof ResultDetailsMenu);
        thisTarget.clearNext();
        thisTarget.accept(new String[]{"2"});
        assertTrue(thisTarget.next() instanceof ResultDetailsMenu);
    }

    @Test
    public void doCommand() {
        thisTarget.accept(new String[]{DO_COMMAND});
        assertTrue(thisTarget.next() instanceof ReplaceResultsMenu);
    }
}
