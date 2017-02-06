/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.getSingleOperator;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.printMainHelp;

import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;
import dmv.desktop.searchandreplace.view.consoleapp.ReplaceFilesConsoleApplication;

/**
 * Class <tt>HelpMenu.java</tt> represents menu with
 * program's help information. It is the last menu item.
 * So it can only go back or exit.
 * @author dmv
 * @since 2017 January 27
 */
public class HelpMenu extends ConsoleMenuAbs {
    
    /**
     * @see ConsoleMenuAbs#ConsoleMenuAbs(ReplaceFilesConsoleApplication, ConsoleMenu)
     */
    public HelpMenu(ConsoleApplication mainProgram,
                    ConsoleMenu previousMenu) {
        super(mainProgram, previousMenu);
        // it's kind of the last console item, so it points to itself by default
        setNext(this);
    }

    @Override
    public void showMenu() {
        printMainHelp();
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
