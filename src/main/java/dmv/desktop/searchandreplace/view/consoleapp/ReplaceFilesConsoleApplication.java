package dmv.desktop.searchandreplace.view.consoleapp;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.ATTEMPTS;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.MAIN_KEYS;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.PARAMETER_KEYS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import dmv.desktop.searchandreplace.model.*;
import dmv.desktop.searchandreplace.service.FolderWalker;
import dmv.desktop.searchandreplace.service.SearchAndReplace;
import dmv.desktop.searchandreplace.view.consoleapp.menu.ConsoleMenu;
import dmv.desktop.searchandreplace.view.consoleapp.menu.PreviewResultsMenu;
import dmv.desktop.searchandreplace.view.consoleapp.menu.ReplaceResultsMenu;
import dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils;
import dmv.desktop.searchandreplace.view.profile.ReplaceFilesProfile;
import dmv.desktop.searchandreplace.view.profile.ReplaceFilesProfileImpl;

/**
 * Class <tt>ReplaceFilesConsoleApplication.java</tt> is a main program
 * that runs in a console view and offering command line operations
 * for Searching and Replacing file contents.
 * @author dmv
 * @since 2017 January 20
 */
public class ReplaceFilesConsoleApplication implements ConsoleApplication {
    
    private SearchAndReplace<SearchPath, SearchProfile, SearchResult> replacer;
    private ReplaceFilesProfile replaceProfile;
    private boolean replace;
    private boolean exit;
    
    /**
     * Run program from initial command line arguments,
     * just like in a static main method. This could
     * recognize main program's commands and parameter keys.
     * @param args command line arguments
     */
    public void parseCommand(String[] args) {
        replace = false;
        replaceProfile = new ReplaceFilesProfileImpl();
        try {
            if (args[0].charAt(0) != '-') {
                // every main command at this step
                // will finalize the program
                if (!doMainCommand(args[0]))
                    collectParameters(args, 1);
            } else 
                collectParameters(args, 0);
            if (!exit) createReplacer();
        } catch (Exception e) {
            showMainHelp();
            exit();
        }
    }

    private void createReplacer() {
        try {
            SearchPath folder = SearchPathImpl.getBuilder(replaceProfile.getPath())
                    .setSubfolders(replaceProfile.getSubfolders())
                    .setNamePattern(replaceProfile.getIncludeNamePatterns())
                    .build();
            SearchProfile profile = SearchProfileImpl.getBuilder(replaceProfile.getToFind())
                    .setCharset(replaceProfile.getCharset())
                    .setReplaceWith(replaceProfile.getReplaceWith())
                    .setFilename(replaceProfile.getFilenames())
                    .setExclusions(replaceProfile.getExclusions())
                    .build();
            replacer = new FolderWalker(folder, profile);
        } catch (Exception e) {
            // TODO show the whole profile to user, and
            exit();
        }
    }

    public SearchAndReplace<SearchPath, SearchProfile, SearchResult> getReplacer() {
        return replacer;
    }

    /* recursively read parameters (may appear single or in pairs) */
    private void collectParameters(String[] args, int i) {
        if (!exit) {
            if (i == args.length) return;
            String key = args[i];
            if (key.charAt(0) != '-')
                throw new IllegalArgumentException("A key expected at this position");
            String param = ++i < args.length ? args[i] : "";
            // if next is a key (i.e. parameter was empty)
            if (param.length() > 0 && param.charAt(0) == '-') {
                param = "";
                i--;
            } 
            PARAMETER_KEYS.get(key)
                          .apply(replaceProfile, param);
            collectParameters(args, ++i);
        }
    }

    private boolean doMainCommand(String s) {
        Consumer<ConsoleApplication> command = MAIN_KEYS.get(s);
        
        if (command != null) {
            command.accept(this);
            exit();
            return true;
        }
        else if (s.equals(NOPREVIEW)) {
            setReplace();
            return false;
        }
        else throw new IllegalArgumentException("unknown command");
    }
    
    private void runMenu(ConsoleMenu menu, BufferedReader reader, int attempts) {
        /* simple overflow protection */
        if (attempts <= 0) {
            System.out.println("Go, play some toys");
            return;
        }
        try {
            menu.accept(reader.readLine().split("\\s+"));
            ConsoleMenu next = menu.next();
            if (next != null) {
                next.showMenu();
                runMenu(next, reader, ATTEMPTS);
            }
        } catch (Exception e) {
            menu.showMenuHelp();
            runMenu(menu, reader, attempts - 1);
        }
    }

    private void insideProgram() {
        if (!exit) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                ConsoleMenu menu = replace ? new ReplaceResultsMenu(this, null) :
                                             new PreviewResultsMenu(this, null);
                menu.showMenu();
                runMenu(menu, reader, ATTEMPTS);
            } catch (Exception e) {
                CmdUtils.tellAbout(e);
                exit();
            }
        }
    }

    public List<SearchResult> preview() {
        return replacer.preview(EXECS_POOL);
    }
    
    public List<SearchResult> replace() {
        return replacer.replace(EXECS_POOL);
    }
    
    @Override
    public void showMainHelp() {
        System.out.print(CmdUtils.HELP);
    }
    
    @Override
    public void exit() {
        exit = true;
    }

    @Override
    public void cancel() {
        // there is no operations, so
        exit();
    }

    public void setReplace() {
        replace = true;
    }
    
    /* Statics */

    public static void main(String[] args) {
        /* in case of ^C interruption */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
        ReplaceFilesConsoleApplication app = new ReplaceFilesConsoleApplication();
        app.parseCommand(args);
        app.insideProgram();
        shutdown();
    }

    private static void shutdown() {
        System.out.println("shutdown program...");
        if (!EXECS_POOL.isShutdown())
            EXECS_POOL.shutdownNow();
    }

    private static final String NOPREVIEW = "nopreview";
    private static final int UNITS = Runtime.getRuntime().availableProcessors() * 2;
    private static final ExecutorService EXECS_POOL = Executors.newFixedThreadPool(UNITS);
    
    
}
