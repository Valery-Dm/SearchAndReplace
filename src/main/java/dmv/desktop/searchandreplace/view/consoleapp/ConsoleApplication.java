package dmv.desktop.searchandreplace.view.consoleapp;

/**
 * Interface <tt>ConsoleApplication.java</tt> describes methods
 * for main commands that Console Application should support.
 * @author dmv
 * @since 2017 January 27
 */
public interface ConsoleApplication {

    /**
     * Show main program's help
     */
    void showMainHelp();
    
    /**
     * Cancel current operation or go back to previous 
     * screen or else, exit the program
     */
    void cancel();

    /**
     * Exit the program
     */
    void exit();

}