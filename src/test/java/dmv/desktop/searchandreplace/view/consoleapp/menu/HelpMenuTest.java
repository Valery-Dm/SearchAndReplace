package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.MAIN_HELP;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.MENU_GO_BACK;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.PROMPT;

import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;


public class HelpMenuTest extends ConsoleMenuTest {

    @Override
    protected ConsoleMenu getTarget(ConsoleApplication app, ConsoleMenu from) {
        return new HelpMenu(app, from);
    }

    @Override
    protected boolean isMenuShown(String output) {
        return (MAIN_HELP + MENU_GO_BACK + PROMPT).equals(output);
    }

    @Override
    protected boolean isMenuHelpShown(String output) {
        return (MENU_GO_BACK + PROMPT).equals(output);
    }

    @Override
    protected ConsoleApplication getApp() {
        return null;
    }

}
