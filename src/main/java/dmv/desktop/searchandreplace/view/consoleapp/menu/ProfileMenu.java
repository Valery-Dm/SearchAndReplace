package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.DO_COMMAND;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.PROFILE_MENU;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.PROFILE_MENU_GO_BACK;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.getSingleOperator;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.printPrompt;

import java.util.List;

import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.view.consoleapp.ReplaceFilesConsoleApplication;

/**
 * Class <tt>ProfileMenu.java</tt>
 * @author dmv
 * @since 2017 February 06
 */
public class ProfileMenu extends ReplaceFilesConsoleMenuAbs {

    public ProfileMenu(ReplaceFilesConsoleApplication mainProgram,
                       ConsoleMenu previousMenu) {
        super(mainProgram, previousMenu);
    }

    @Override
    public void showMenu() {
        System.out.println(getMainProgram().getReplaceProfile());
        System.out.print(PROFILE_MENU);
        System.out.print(PROFILE_MENU_GO_BACK);
        printPrompt();
    }

    @Override
    public void showMenuHelp() {
        System.out.print(PROFILE_MENU_GO_BACK);
        printPrompt();
    }

    @Override
    public void accept(String[] args) {
        String operator = getSingleOperator(args);
        if (!isMainCommand(operator)) {
            if (DO_COMMAND.equals(operator)) {
                getMainProgram().updateService();
                if (getMainProgram().isReplace())
                    setNext(new ReplaceResultsMenu(getMainProgram(), this));
                else 
                    setNext(new PreviewResultsMenu(getMainProgram(), this));
            }
        }
    }

    @Override
    protected List<SearchResult>
            getResults(ReplaceFilesConsoleApplication mainProgram) {
        return null;
    }

}
