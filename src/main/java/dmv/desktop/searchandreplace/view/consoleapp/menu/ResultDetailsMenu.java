/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.menu;

import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;
import dmv.desktop.searchandreplace.view.consoleapp.ReplaceFilesConsoleApplication;

/**
 * Class <tt>ResultDetailsMenu.java</tt> provides a menu
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
        builder.append("\n");
        System.out.print(builder);
        endInfo();
    }

    @Override
    public void showMenuHelp() {
        endInfo();
    }

    @Override
    public void accept(String[] args) {
        if (!defaultCommand(args)) 
            throw new IllegalArgumentException("unknown command");
    }

}
