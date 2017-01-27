/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.MAIN_KEYS;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.SINGLE_RESULT;

import dmv.desktop.searchandreplace.model.SearchResult;

/**
 * Class <tt>ResultDetailsMenu.java</tt> provides a menu
 * with single result details
 * @author dmv
 * @since 2017 January 26
 */
public class ResultDetailsMenu implements ConsoleMenu {
    
    private SearchResult result;
    private ConsoleMenu previousMenu;
    private ConsoleMenu nextMenu;

    /**
     * Create menu with given parameters
     * @param fromMenu from what menu it goes
     * @param result the result which details to show
     */
    public ResultDetailsMenu(ConsoleMenu fromMenu, SearchResult result) {
        if (result == null)
            throw new IllegalArgumentException("Result cannot be null");
        this.previousMenu = fromMenu;
        this.result = result;
    }

    @Override
    public void showMenu() {
        String filename = "unknown name";
        if (result.getModifiedName() != null && 
            result.getModifiedName().getFirst() != null)
            filename = result.getModifiedName().getFirst().toString();
        StringBuilder builder = new StringBuilder("Results for " + filename);
        builder.append(":\n");
        if (result.isExceptional())
            builder.append("Cause of exception is:\n")
                   .append(result.getCause())
                   .append("\n");
        else {
            builder.append("number of modifications - ")
                   .append(result.numberOfModificationsMade());
            result.getModifiedContent()
                  .stream()
                  .filter(tuple -> tuple.getLast() != null)
                  .forEach(tuple -> builder.append("\nOriginal: ")
                                           .append(tuple.getFirst())
                                           .append("\nModified: ")
                                           .append(tuple.getLast()));
        }
        builder.append(":\n")
               .append(SINGLE_RESULT);
       System.out.println(builder);
    }

    @Override
    public void showMenuHelp() {
        System.out.println(SINGLE_RESULT);
    }

    @Override
    public void accept(String[] args) {
        if (args == null || args.length != 1 || args[0] == null || args[0].length() == 0)
            throw new IllegalArgumentException("Single operator is expected");
        String command = args[0];
        Runnable mainCommand = MAIN_KEYS.get(command);
        if (mainCommand != null) mainCommand.run();
        else {
            int number = Integer.parseInt(args[0]);
            if (number != 0)
                throw new IllegalArgumentException("The 0 is expected");
            nextMenu = previousMenu;
        }
    }

    @Override
    public ConsoleMenu next() {
        return nextMenu;
    }

}
