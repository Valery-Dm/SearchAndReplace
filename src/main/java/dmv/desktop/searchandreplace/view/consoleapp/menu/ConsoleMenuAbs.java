/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.MAIN_COMMANDS;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.printGoBackMenu;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.printPrompt;

import java.util.function.Consumer;

import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;

/**
 * Class <tt>ConsoleMenuAbs.java</tt>
 * @author dmv
 * @since 2017 January 27
 */
public abstract class ConsoleMenuAbs implements ConsoleMenu {
    
    private ConsoleApplication mainProgram;
    private ConsoleMenu previousMenu;
    private ConsoleMenu nextMenu;

    /**
     * Track main program and previous console
     * @param mainProgram The program this console belongs to
     * @param previousMenu The previous console to return to
     * @throws IllegalArgumentException if mainProgram is null
     */
    public ConsoleMenuAbs(ConsoleApplication mainProgram,
                          ConsoleMenu previousMenu) {
        if (mainProgram == null)
            throw new IllegalArgumentException("mainProgram must not be null");
        this.mainProgram = mainProgram;
        if (previousMenu != null) {
            // clear its next pointer
            previousMenu.clearNext();
            this.previousMenu = previousMenu;
        }
    }
    
    protected ConsoleApplication getMainProgram() {
        return mainProgram;
    }
    
    protected ConsoleMenu getPreviousMenu() {
        return previousMenu;
    }
    
    protected void setNext(ConsoleMenu menu) {
        this.nextMenu = menu;
    }
    
    protected void endInfo() {
        printGoBackMenu();
        printPrompt();
    }
    
    protected boolean isMainCommand(String operator) {
        Consumer<ConsoleApplication> command = MAIN_COMMANDS.get(operator);
        if (command != null) {
            command.accept(this);
            return true;
        }
        return false;
    }

    @Override
    public void clearNext() {
        nextMenu = null;
    }

    @Override
    public ConsoleMenu next() {
        return nextMenu;
    }

    @Override
    public void exit() {
        clearNext();
        mainProgram.exit();
    }

    @Override
    public void cancel() {
        // just go back by-default
        setNext(getPreviousMenu());
    }

    @Override
    public void showMainHelp() {
        setNext(new HelpMenu(mainProgram, this));
    }

}
