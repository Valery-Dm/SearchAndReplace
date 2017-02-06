/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.getSingleOperator;

import java.util.List;

import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.view.consoleapp.ReplaceFilesConsoleApplication;

/**
 * Class <tt>ReplaceResultsMenu.java</tt> acquires and then
 * shows the list of results of replace operation.
 * So, after this object created all replacements could be done.
 * @author dmv
 * @since 2017 January 26
 */
public class ReplaceResultsMenu extends ReplaceFilesConsoleMenuAbs {

    /**
     * @see ReplaceFilesConsoleMenuAbs#ReplaceFilesConsoleMenuAbs(ReplaceFilesConsoleApplication, ConsoleMenu)
     */
    public ReplaceResultsMenu(ReplaceFilesConsoleApplication mainProgram,
                              ConsoleMenu previousMenu) {
        super(mainProgram, previousMenu);
    }

    @Override
    public void showMenu() {
        System.out.print(getResultsMenu());
        //System.out.print(MENU_RESULTS_REPLACE);
        endInfo();
    }

    @Override
    public void showMenuHelp() {
        //System.out.print(MENU_RESULTS_REPLACE);
        endInfo();
    }

    @Override
    public void accept(String[] args) {
        String operator = getSingleOperator(args);
        if (!isMainCommand(operator)) {
            if (operator.equals("editprofile")) 
                setNext(new ReplaceResultsMenu(getMainProgram(), this));
            else 
                setNext(new ResultDetailsMenu(getMainProgram(), this, 
                                              getResult(Integer.parseInt(operator))));
        }
    }

    @Override
    protected List<SearchResult>
            getResults(ReplaceFilesConsoleApplication mainProgram) {
        return mainProgram.replace();
    }

}
