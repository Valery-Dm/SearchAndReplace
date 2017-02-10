package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.listResults;

import java.util.ArrayList;
import java.util.List;

import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;
import dmv.desktop.searchandreplace.view.consoleapp.ReplaceFilesConsoleApplication;

public abstract class ReplaceFilesConsoleMenuAbs extends ConsoleMenuAbs {

    private List<SearchResult> normalResults;
    private List<SearchResult> exceptionalResults;
    private int resultsInTotal;
    private int normalShift;
    private int exceptionalShift;
    private String resultsMenu;
    
    /**
     * This menu works with file replacement applications so the 
     * {@link ReplaceFilesConsoleApplication} type should be provided
     * @see ConsoleMenuAbs#ConsoleMenuAbs(ConsoleApplication, ConsoleMenu)
     */
    public ReplaceFilesConsoleMenuAbs(ReplaceFilesConsoleApplication mainProgram,
                                      ConsoleMenu previousMenu) {
        super(mainProgram, previousMenu);
        List<SearchResult> results = getResults(mainProgram);
        if (results != null) {
            // save normal results first, then exceptional
            cacheResults(results);
            // This menu is expected to be shown several times
            // (going back after single result menus).
            // So it's better to cache it.
            resultsMenu = listResults(results);
        }
    }
    
    protected abstract List<SearchResult> getResults(ReplaceFilesConsoleApplication mainProgram);
    
    protected String getResultsMenu() {
        return resultsMenu;
    }

    @Override
    protected ReplaceFilesConsoleApplication getMainProgram() {
        return (ReplaceFilesConsoleApplication) super.getMainProgram();
    }
    
    protected SearchResult getResult(int number) {
        if (number < 1 || number > resultsInTotal)
            throw new IllegalArgumentException("There is no result with number " + number);
        return number > normalResults.size() ? 
                        exceptionalResults.get(number - exceptionalShift) :
                        normalResults.get(number - normalShift);
    }

    private void cacheResults(List<SearchResult> results) {
        resultsInTotal = results.size();
        normalResults = new ArrayList<>();
        exceptionalResults = new ArrayList<>();
        normalShift = 1;
        for (SearchResult result : results) {
            if (result.isExceptional())
                exceptionalResults.add(result);
            else 
                normalResults.add(result);
        }
        exceptionalShift = normalResults.size() + normalShift;
    }

}