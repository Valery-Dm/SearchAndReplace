/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.MAIN_KEYS;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.RESULTS;

import java.util.ArrayList;
import java.util.List;

import dmv.desktop.searchandreplace.model.SearchResult;

/**
 * Class <tt>PreviewResultsMenu.java</tt>
 * @author dmv
 * @since 2017 January 25
 */
public class PreviewResultsMenu implements ConsoleMenu {
    
    private ConsoleMenu previousMenu;
    private ConsoleMenu nextMenu;
    private List<SearchResult> normalResults;
    private List<SearchResult> exceptionalResults;
    private int resultsInTotal;
    private StringBuilder resultsList;
    private int normalShift;
    private int exceptionalShift;
    
    public PreviewResultsMenu(ConsoleMenu fromMenu, List<SearchResult> results) {
        if (results == null || results.size() == 0)
            throw new IllegalArgumentException("At least one result expected");
        resultsInTotal = results.size();
        previousMenu = fromMenu;
        normalResults = new ArrayList<>();
        exceptionalResults = new ArrayList<>();
        createMenu(results);
    }

    private void createMenu(List<SearchResult> results) {
        resultsList = new StringBuilder("Results are:\n");
        normalShift = 1;
        int i = normalShift;
        for (SearchResult result : results) {
            if (result.isExceptional())
                exceptionalResults.add(result);
            else {
                normalResults.add(result);
                resultsList.append(i++)
                            .append(": ")
                            .append(result.getModifiedName().getFirst())
                            .append(" with ")
                            .append(result.numberOfModificationsMade())
                            .append(" modifications\n");
            }
        }
        resultsList.append("\n");
        if (exceptionalResults.size() > 0) {
            resultsList.append("Next results were exceptional:\n");
            for (SearchResult result : exceptionalResults)
                resultsList.append(i++)
                           .append(": ")
                           .append(result.getCause())
                           .append("\n");
        }
        resultsList.append("\n");
        resultsList.append(RESULTS);
        resultsList.append("\n");
        exceptionalShift = normalResults.size() + normalShift;
    }

    @Override
    public void showMenu() {
        System.out.println(resultsList);
    }

    @Override
    public void showMenuHelp() {
        System.out.println(RESULTS);
    }

    @Override
    public void accept(String[] args) {
        if (args == null || args.length != 1 || args[0] == null || args[0].length() == 0)
            throw new IllegalArgumentException("Single operator is expected");
        String command = args[0];
        Runnable mainCommand = MAIN_KEYS.get(command);
        if (mainCommand != null) mainCommand.run();
        else if (command.equals("do")) nextMenu = new ReplaceResultsMenu();
        else {
            int number = Integer.parseInt(command);
            if (number < 0 || number > resultsInTotal)
                throw new IllegalArgumentException("There is no result with number " + number);
            if (number == 0) nextMenu = previousMenu;
            else nextMenu = new ResultDetailsMenu(this, getResult(number));
        }
    }

    private SearchResult getResult(int number) {
        return number > normalResults.size() ? 
                        exceptionalResults.get(number - exceptionalShift) :
                        normalResults.get(number - normalShift);
    }

    @Override
    public ConsoleMenu next() {
        return nextMenu;
    }
}
