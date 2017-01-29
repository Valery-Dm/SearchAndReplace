/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.RESULTS;

import java.util.ArrayList;
import java.util.List;

import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.view.consoleapp.ReplaceFilesConsoleApplication;

/**
 * Class <tt>PreviewResultsMenu.java</tt>
 * @author dmv
 * @since 2017 January 25
 */
public class PreviewResultsMenu extends ConsoleMenuAbs {
    
    private List<SearchResult> normalResults;
    private List<SearchResult> exceptionalResults;
    private int resultsInTotal;
    private String resultsMenu;
    private int normalShift;
    private int exceptionalShift;
    
    /**
     * @see ConsoleMenuAbs#ConsoleMenuAbs(ReplaceFilesConsoleApplication, ConsoleMenu)
     */
    public PreviewResultsMenu(ReplaceFilesConsoleApplication mainProgram,
                              ConsoleMenu previousMenu) {
        super(mainProgram, previousMenu);
        List<SearchResult> results = mainProgram.preview();
        resultsInTotal = results.size();
        normalResults = new ArrayList<>();
        exceptionalResults = new ArrayList<>();
        resultsMenu = createMenu(results);
    }

    private String createMenu(List<SearchResult> results) {
        StringBuilder resultsList = new StringBuilder("Results are:\n");
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
        if (exceptionalResults.size() > 0) {
            resultsList.append("\nNext results were exceptional:\n");
            for (SearchResult result : exceptionalResults)
                resultsList.append(i++)
                           .append(": ")
                           .append(result.getCause())
                           .append("\n");
        }
        resultsList.append(RESULTS);
        exceptionalShift = normalResults.size() + normalShift;
        return resultsList.toString();
    }

    @Override
    public void showMenu() {
        System.out.print(resultsMenu);
        endInfo();
    }

    @Override
    public void showMenuHelp() {
        System.out.print(RESULTS);
        endInfo();
    }

    @Override
    public void accept(String[] args) {
        if (!defaultCommand(args)) {
            if (args[0].equals("do")) setNext(new ReplaceResultsMenu(getMainProgram(), this));
            else {
                int number = Integer.parseInt(args[0]);
                if (number < 1 || number > resultsInTotal)
                    throw new IllegalArgumentException("There is no result with number " + number);
                setNext(new ResultDetailsMenu(getMainProgram(), this, getResult(number)));
            }
        }
    }

    private SearchResult getResult(int number) {
        return number > normalResults.size() ? 
                        exceptionalResults.get(number - exceptionalShift) :
                        normalResults.get(number - normalShift);
    }
}
