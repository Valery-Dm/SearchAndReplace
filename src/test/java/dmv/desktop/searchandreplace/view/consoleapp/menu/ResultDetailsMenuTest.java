package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.MENU_GO_BACK;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.PROMPT;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.describeResult;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;


public class ResultDetailsMenuTest extends ConsoleMenuTest {

    private SearchResult result;
    
    @Override
    protected ConsoleMenu getTarget(ConsoleApplication app, ConsoleMenu from) {
        result = mock(SearchResult.class);
        return new ResultDetailsMenu(app, from, result);
    }

    @Override
    protected boolean isMenuShown(String output) {
        return (describeResult(result) + MENU_GO_BACK + PROMPT).equals(output);
    }

    @Override
    protected boolean isMenuHelpShown(String output) {
        return (MENU_GO_BACK + PROMPT).equals(output);
    }

    @Test(expected=IllegalArgumentException.class)
    public void constructorNullResult() {
        new ResultDetailsMenu(mock(ConsoleApplication.class), 
                              mock(ConsoleMenu.class), null);
    }

    @Override
    protected ConsoleApplication getApp() {
        return null;
    }
}
