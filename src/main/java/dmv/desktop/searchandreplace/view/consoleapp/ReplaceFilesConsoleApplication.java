package dmv.desktop.searchandreplace.view.consoleapp;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import dmv.desktop.searchandreplace.exception.AccessResourceException;
import dmv.desktop.searchandreplace.exception.NothingToReplaceException;
import dmv.desktop.searchandreplace.exception.WrongProfileException;
import dmv.desktop.searchandreplace.model.SearchPath;
import dmv.desktop.searchandreplace.model.SearchProfile;
import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.service.SearchAndReplace;
import dmv.desktop.searchandreplace.view.consoleapp.menu.ConsoleMenu;
import dmv.desktop.searchandreplace.view.consoleapp.menu.PreviewResultsMenu;
import dmv.desktop.searchandreplace.view.consoleapp.menu.ReplaceResultsMenu;
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
    public void parseCommand(String... args) {
        replace = false;
        replaceProfile = new ReplaceFilesProfileImpl();
        try {
            int i = 0;
            if (args[i].charAt(0) != '-') 
                doMainCommand(args[i++]);
            if (!exit) {
                collectParameters(args, i);
                replacer = replaceProfile.createService();
                ConsoleMenu menu = replace ? new ReplaceResultsMenu(this, null) :
                                             new PreviewResultsMenu(this, null);
                menuFlowFrom(menu);
            }
        } catch (WrongProfileException wpe) {
            System.out.println(wpe.getMessage());
        } catch (NothingToReplaceException ntpe) {
            System.out.println(NOTHING_WAS_FOUND);
        } catch(AccessResourceException ase) {
            System.out.println(RESOURCE_ACCESS);
        } catch (Exception e) {
            showMainHelp();
        }
        exit();
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
            // if next word is a key (i.e. parameter was empty)
            if (param.length() > 0 && param.charAt(0) == '-') {
                param = "";
                i--;
            } 
            // NPE will be caught in wrapper method
            PARAMETER_KEYS.get(key)
                          .apply(replaceProfile, param);
            collectParameters(args, ++i);
        }
    }

    private void doMainCommand(String s) {
        Consumer<ConsoleApplication> command = MAIN_COMMANDS.get(s);
        if (command != null) {
            command.accept(this);
            exit();
        } else if (isNoPreview(s)) {
            setReplace();
        } else throw new IllegalArgumentException("unknown command");
    }
    
    /* recursively go through program menus */
    private void runMenu(ConsoleMenu menu, BufferedReader reader, int attempts) {
        /* simple overflow protection */
        if (attempts <= 0) {
            printTooManyAttempts();
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

    private void menuFlowFrom(ConsoleMenu menu) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            menu.showMenu();
            runMenu(menu, reader, ATTEMPTS);
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
        printMainHelp();
    }
    
    @Override
    public void exit() {
        exit = true;
    }

    @Override
    public void cancel() {
        // there is no operations here, so just
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
        shutdown();
    }

    private static void shutdown() {
        //System.out.println("shutdown program...");
        if (!EXECS_POOL.isShutdown())
            EXECS_POOL.shutdownNow();
    }

    private static final int UNITS = Runtime.getRuntime().availableProcessors() * 2;
    private static final ExecutorService EXECS_POOL = Executors.newFixedThreadPool(UNITS);
    
    
}
