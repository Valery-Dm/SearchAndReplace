/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.menu;

import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;
import dmv.desktop.searchandreplace.view.consoleapp.ReplaceFilesConsoleApplication;
import dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils;

/**
 * Class <tt>HelpMenu.java</tt>
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
        // it's kind of last menu so it points to itself by default
        setNext(this);
    }

    @Override
    public void showMenu() {
        System.out.print(CmdUtils.HELP);
        endInfo();
    }

    @Override
    public void showMenuHelp() {
        // usually shows brief help
        System.out.print(CmdUtils.BRIEF_HELP);
        endInfo();
    }

    @Override
    public void accept(String[] args) {
        if (!defaultCommand(args))
            throw new IllegalArgumentException("unknown command");
    }
}
