/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.describeResult;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.getSingleOperator;

import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;
import dmv.desktop.searchandreplace.view.consoleapp.ReplaceFilesConsoleApplication;

/**
 * Class <tt>ResultDetailsMenu.java</tt> provides a console
 * with single result details
 * @author dmv
 * @since 2017 January 26
 */
public class ResultDetailsMenu extends ConsoleMenuAbs {
    
    private SearchResult result;

    /**
     * @see ConsoleMenuAbs#ConsoleMenuAbs(ReplaceFilesConsoleApplication, ConsoleMenu)
     * @param result the result which details to show
     */
    public ResultDetailsMenu(ConsoleApplication mainProgram,
                             ConsoleMenu previousMenu, SearchResult result) {
        super(mainProgram, previousMenu);
        if (result == null)
            throw new IllegalArgumentException("Result cannot be null");
        this.result = result;
    }

    @Override
    public void showMenu() {
        System.out.print(describeResult(result));
        endInfo();
    }

    @Override
    public void showMenuHelp() {
        endInfo();
    }

    @Override
    public void accept(String[] args) {
        String operator = getSingleOperator(args);
        if (!isMainCommand(operator)) 
            throw new IllegalArgumentException("unknown command");
    }

}
