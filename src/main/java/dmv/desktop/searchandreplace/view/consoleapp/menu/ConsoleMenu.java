/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.menu;

import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;

/**
 * Interface <tt>ConsoleMenu.java</tt> describes
 * methods for a single console menu
 * @author dmv
 * @since 2017 January 25
 */
public interface ConsoleMenu extends ConsoleApplication {
    
    /**
     * Print out the menu on a console
     */
    void showMenu();
    
    /**
     * Print out the menu help section
     */
    void showMenuHelp();

    /**
     * Pass user input into the menu
     * @param args command line arguments
     * @throws IllegalArgumentException if input is incorrect
     */
    void accept(String[] args);
    
    /**
     * Get next menu based on state of current one.
     * @return The next menu to go to or null if nothing to do
     */
    ConsoleMenu next();
    
    /**
     * Remove menu's next pointer, usually after it has been used
     */
    void clearNext();

}
