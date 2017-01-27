package dmv.desktop.searchandreplace.view.consoleapp;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.ATTEMPTS;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.MAIN_KEYS;
import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.PARAMETER_KEYS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 * 
 * Class <tt>ReplaceFilesConsoleApplication.java</tt> is a main program
 * that runs in a console view and offering command line operations
 * for Searching and Replacing file contents.
 * @author dmv
 * @since 2017 January 20
 */
public class ReplaceFilesConsoleApplication {
    
    private SearchAndReplace<SearchPath, SearchProfile, SearchResult> replacer;
    private ReplaceFilesProfile replaceProfile;
    private boolean replace;
    private boolean exit;
    
    public void parseCommand(String[] args) {
        replace = false;
        replaceProfile = new ReplaceFilesProfileImpl();
        try {
            if (args[0].charAt(0) != '-') {
                doMainCommand(args[0]);
                collectParameters(args, 1);
            } else 
                collectParameters(args, 0);
            createReplacer();
        } catch (Exception e) {
            CmdUtils.printHelp();
            exit = true;
        }
    }

    private void createReplacer() {
        SearchPath folder = SearchPathImpl.getBuilder(getPath())
                                          .setSubfolders(getBoolean(replaceProfile.getSubfolders()))
                                          .setNamePattern(replaceProfile.getIncludeNamePatterns())
                                          .build();
        SearchProfile profile = SearchProfileImpl.getBuilder(replaceProfile.getToFind())
                                                 .setCharset(getCharset())
                                                 .setReplaceWith(replaceProfile.getReplaceWith())
                                                 .setFilename(getBoolean(replaceProfile.getFilenames()))
                                                 .setExclusions(getExclusions())
                                                 .build();
        replacer = new FolderWalker(folder, profile);
    }

    private Exclusions getExclusions() {
        Set<String> exclusions = replaceProfile.getExclusions();
        return exclusions.size() == 0 ? null :
               new ExclusionsTrie(exclusions, replaceProfile.getToFind(), true);
    }

    private Charset getCharset() {
        String charset = replaceProfile.getCharset();
        return charset.length() == 0 ? null :
               Charset.forName(replaceProfile.getCharset());
    }

    private boolean getBoolean(String setting) {
        if (setting.equals("true")) return true;
        if (setting.equals("false")) return false;
        throw new IllegalStateException("unknown state - must be true or false");
    }

    private Path getPath() {
        return Paths.get(replaceProfile.getPath());
    }

    public SearchAndReplace<SearchPath, SearchProfile, SearchResult> getReplacer() {
        return replacer;
    }

    /* recursively read parameters (may appear single or in pairs) */
    private void collectParameters(String[] args, int i) {
        if (i == args.length) return;
        String key = args[i];
        if (key.charAt(0) != '-')
            throw new IllegalArgumentException("expect a key at this place");
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

    private void doMainCommand(String s) {
        Runnable command = MAIN_KEYS.get(s);
        
        if (command != null) command.run();
        else if (s.equals(NOPREVIEW)) replace = true;
        
        throw new IllegalArgumentException("unknown command");
    }
    
    private void runMenu(ConsoleMenu menu, BufferedReader reader, int attempts) {
        /* overflow protection */
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
                ConsoleMenu menu = replace ? new ReplaceResultsMenu() :
                                             new PreviewResultsMenu(null, replacer.preview(EXECS_POOL));
                menu.showMenu();
                runMenu(menu, reader, ATTEMPTS);
                CmdUtils.exit();
            } catch (Exception e) {
                CmdUtils.tellAbout(e);
                CmdUtils.exit();
            }
        }
    }

    public void setReplace() {
        replace = true;
    }
    
    /* Statics */

    public static void main(String[] args) {
        /* in case of ^C interruption */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("shutdown program...");
            if (!EXECS_POOL.isShutdown())
                EXECS_POOL.shutdownNow();
        }));
        ReplaceFilesConsoleApplication app = new ReplaceFilesConsoleApplication();
        app.parseCommand(args);
        app.insideProgram();
    }

    private static final String NOPREVIEW = "nopreview";
    private static final int UNITS = Runtime.getRuntime().availableProcessors() * 2;
    private static final ExecutorService EXECS_POOL = Executors.newFixedThreadPool(UNITS);
    
    
}
