/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.MAIN_KEYS;

import java.util.function.Consumer;

import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;
import dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils;

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
     * Track main program and previous menu
     * @param mainProgram The program this menu belongs to
     * @param previousMenu The previous menu to return to
     */
    public ConsoleMenuAbs(ConsoleApplication mainProgram,
                          ConsoleMenu previousMenu) {
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
        System.out.print(CmdUtils.TYPE_ZERO);
        CmdUtils.showPrompt();
    }
    
    protected boolean defaultCommand(String[] args) {
        String operator = CmdUtils.getSingleOperator(args);
        Consumer<ConsoleApplication> command = MAIN_KEYS.get(operator);
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
