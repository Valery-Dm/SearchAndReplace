package dmv.desktop.searchandreplace.view.consoleapp.utility;

import static java.text.MessageFormat.format;
import static java.util.ResourceBundle.getBundle;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import dmv.desktop.searchandreplace.model.SearchProfile;
import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;
import dmv.desktop.searchandreplace.view.consoleapp.menu.HelpMenu;
import dmv.desktop.searchandreplace.view.profile.ReplaceFilesProfile;

/**
 * Class <tt>CmdUtils.java</tt> is an utility class
 * which contains various command line arguments recognizable
 * by this application, menu texts etc.
 * @author dmv
 * @since 2017 January 21
 */
public class CmdUtils {
    
    /** console prompt */
    public static final String PROMPT;
    /** maximum number of wrong user input attempts */
    public static final int ATTEMPTS = 10;
    /** when in results view, this command triggers replace operation */
    public static final String DO_COMMAND;
    /** means do replace without preview */
    public static final String NO_PREVIEW_COMMAND;
    /** cancel operation or go back to previous menu */
    public static final List<String> GO_BACK_COMMANDS;
    /** commands for showing program help */
    public static final List<String> HELP_COMMANDS;
    /** commands for exiting the program */
    public static final List<String> EXIT_COMMANDS;
    /** commands to answer yes or true */
    public static final List<String> TRUE_COMMANDS;
    /** commands to answer no or false */
    public static final List<String> FALSE_COMMANDS;
    /** keys that may precede the path parameter -p, -path */
    public static final List<String> KEYS_PATH;
    /** keys that may precede the find parameter -f, -find */
    public static final List<String> KEYS_FIND;
    /** keys that may precede the replace parameter */
    public static final List<String> KEYS_REPLACE;
    /** keys that may precede the exclusion parameter */
    public static final List<String> KEYS_EXCLUDE;
    /** keys that may precede the replace filenames parameter */
    public static final List<String> KEYS_FILENAMES;
    /** keys that may precede the scan subfolders parameter */
    public static final List<String> KEYS_SUBFOLDERS;
    /** keys that may precede the include name patterns parameter */
    public static final List<String> KEYS_PATTERNS;
    /** keys that may precede the charset parameter */
    public static final List<String> KEYS_CHARSET;
    /** keys that may precede the name of profile to use parameter */
    public static final List<String> KEYS_USE_PROFILE;
    /** keys that may precede the name of profile to save parameter */
    public static final List<String> KEYS_SAVE_PROFILE;
    /** main program help */
    public static final String MAIN_HELP;
    /** menu with commands for the results view */
    public static final String MENU_RESULTS_PREVIEW;
    /** menu with go back commands */
    public static final String MENU_GO_BACK;
    /** for single result menu */
    public static final String UNKNOWN_NAME;
    /** for single result menu */
    public static final String RESULTS_FOR;
    /** for single result menu */
    public static final String EXCEPTION_CAUSE;
    /** for single result menu */
    public static final String MOD_NUMBER;
    /** for single result menu */
    public static final String ORIGINAL;
    /** for single result menu */
    public static final String MODIFIED;
    /** for single result menu */
    public static final String MODIFIED_NAME;
    /** for single result menu */
    public static final String NON_MODIFIED_NAME;
    /** for list of results menu */
    public static final String RESULTS_ARE;
    /** for list of results menu */
    public static final String RESULTS_WITH;
    /** for list of results menu */
    public static final String RESULTS_MOD;
    /** for list of results menu */
    public static final String RESULTS_EXCEPT;
    /** common usage character */
    public static final String NEW_LINE;
    /** common usage character */
    public static final String COLON;
    /** message that max number of input attempts is reached */
    public static final String TOO_MANY_ATTEMPTS;
    /** exceptional message for user */
    public static final String NOTHING_WAS_FOUND;
    /** exceptional message for user */
    public static final String RESOURCE_ACCESS;
    /** main commands and their corresponding actions */
    public static final Map<String, Consumer<ConsoleApplication>> MAIN_COMMANDS;
    /** command line keys and their corresponding actions */
    public static final Map<String, 
                             BiFunction<ReplaceFilesProfile, 
                                        String, 
                                        ReplaceFilesProfile>> PARAMETER_KEYS;
    
    static {
        PROMPT = "\nsnr> ";
        ResourceBundle bundle = getBundle("console.menu");
        UNKNOWN_NAME = bundle.getString("menuResultUnknownName");
        RESULTS_FOR = bundle.getString("menuResultResultsFor");
        EXCEPTION_CAUSE = bundle.getString("menuResultExceptionCause");
        MOD_NUMBER = bundle.getString("menuResultModificationNumber");
        ORIGINAL = bundle.getString("menuResultOriginal");
        MODIFIED = bundle.getString("menuResultModified");
        MODIFIED_NAME = bundle.getString("menuResultModifiedName");
        NON_MODIFIED_NAME = bundle.getString("menuResultNonModifiedName");
        RESULTS_ARE = bundle.getString("menuResultsResultsAre");
        RESULTS_WITH = " " + bundle.getString("menuResultsWith");
        RESULTS_MOD = " " + bundle.getString("menuResultsModifications");
        RESULTS_EXCEPT = bundle.getString("menuResultsExceptional");
        NOTHING_WAS_FOUND = bundle.getString("exceptionNothingFound");
        RESOURCE_ACCESS = bundle.getString("exceptionResourceAccess");
        NEW_LINE = "\n";
        COLON = ": ";
        DO_COMMAND = "do";
        NO_PREVIEW_COMMAND = "nopreview";
        TRUE_COMMANDS     = makeListFrom("true", "1", "yes", "y");
        FALSE_COMMANDS    = makeListFrom("false", "0", "no", "n");
        GO_BACK_COMMANDS  = makeListFrom("0", "cancel", "c");
        HELP_COMMANDS     = makeListFrom("help", "h", "/?");
        EXIT_COMMANDS     = makeListFrom("exit", "quit", "q");
        KEYS_PATH         = makeListFrom("-p", "-path");
        KEYS_FIND         = makeListFrom("-f", "-find");
        KEYS_REPLACE      = makeListFrom("-r", "-replace");
        KEYS_EXCLUDE      = makeListFrom("-x", "-exclude");
        KEYS_FILENAMES    = makeListFrom("-fn", "-filenames");
        KEYS_SUBFOLDERS   = makeListFrom("-sf", "-subfolders");
        KEYS_PATTERNS     = makeListFrom("-np", "-namepattern");
        KEYS_CHARSET      = makeListFrom("-cs", "-charset");
        KEYS_USE_PROFILE  = makeListFrom("-up", "-useprofile");
        KEYS_SAVE_PROFILE = makeListFrom("-sp", "-saveprofile");
        MENU_GO_BACK  = format(bundle.getString("menuGoBack"), 
                               String.join(" or ", GO_BACK_COMMANDS));
        MENU_RESULTS_PREVIEW  = format(bundle.getString("menuResults"), DO_COMMAND);
        MAIN_HELP     = format(bundle.getString("mainHelp"),
                               String.join(" or ", HELP_COMMANDS),
                               String.join(" or ", EXIT_COMMANDS),
                               NO_PREVIEW_COMMAND,
                               String.join(" or ", KEYS_PATH),
                               String.join(" or ", KEYS_FIND),
                               String.join(" or ", KEYS_REPLACE),
                               String.join(" or ", KEYS_EXCLUDE),
                               String.join(" or ", KEYS_FILENAMES),
                               String.join(" or ", TRUE_COMMANDS),
                               String.join(" or ", FALSE_COMMANDS),
                               String.join(" or ", KEYS_SUBFOLDERS),
                               String.join(" or ", KEYS_PATTERNS),
                               String.join(" or ", KEYS_CHARSET),
                               SearchProfile.defaultCharset.name(),
                               String.join(" or ", KEYS_USE_PROFILE),
                               String.join(" or ", KEYS_SAVE_PROFILE));
        TOO_MANY_ATTEMPTS = bundle.getString("tooManyAttempts");
        MAIN_COMMANDS  = fillMainCommands();
        PARAMETER_KEYS = fillParamKeys();
    }

    private static List<String> makeListFrom(String...s) {
        return Collections.unmodifiableList(Arrays.asList(s));
    }
    
    public static void printPrompt() {
        System.out.print(PROMPT);
    }
    
    public static void printMainHelp() {
        System.out.print(MAIN_HELP);
    }
    
    public static void printGoBackMenu() {
        System.out.print(MENU_GO_BACK);
    }
    
    public static void printTooManyAttempts() {
        System.out.print(TOO_MANY_ATTEMPTS);
    }
    
    public static boolean isNoPreview(String param) {
        return param != null ? param.equals(NO_PREVIEW_COMMAND) : false;
    }

    public static void exit(ConsoleApplication application) {
        if (application != null)
            application.exit();
    }

    public static void cancel(ConsoleApplication application) {
        if (application != null)
            application.cancel();
    }
    
    public static void showMainHelp(ConsoleApplication application) {
        if (application != null && !(application instanceof HelpMenu))
            application.showMainHelp();
    }

    public static String getSingleOperator(String[] args) {
        if (args == null || args.length != 1 || args[0] == null || args[0].length() == 0)
            throw new IllegalArgumentException("Single operator is expected");
        return args[0];
    }
    
    public static BiFunction<ReplaceFilesProfile, String, ReplaceFilesProfile>
                  getParameterAction(String param) {
        return PARAMETER_KEYS.get(param);
    }

    public static ReplaceFilesProfile setPath(ReplaceFilesProfile profile, String path) {
        return profile.setPath(path);
    }

    public static ReplaceFilesProfile setToFind(ReplaceFilesProfile profile, String toFind) {
        return profile.setToFind(toFind);
    }

    public static ReplaceFilesProfile setReplaceWith(ReplaceFilesProfile profile, String replaceWith) {
        return profile.setReplaceWith(replaceWith);
    }

    public static ReplaceFilesProfile addExclusion(ReplaceFilesProfile profile, String exclusion) {
        return profile.addExclusion(exclusion);
    }

    public static ReplaceFilesProfile addIncludeNamePattern(ReplaceFilesProfile profile, String pattern) {
        return profile.addIncludeNamePattern(pattern);
    }

    public static ReplaceFilesProfile setFilenames(ReplaceFilesProfile profile, String setting) {
        return profile.setFilenames(setting);
    }

    public static ReplaceFilesProfile setSubfolders(ReplaceFilesProfile profile, String setting) {
        return profile.setSubfolders(setting);
    }

    public static ReplaceFilesProfile setCharset(ReplaceFilesProfile profile, String name) {
        return profile.setCharset(name);
    }

    public static ReplaceFilesProfile useProfile(ReplaceFilesProfile profile, String name) {
        return null;
    }

    public static ReplaceFilesProfile saveProfile(ReplaceFilesProfile profile, String name) {
        return null;
    }
    
    public static String describeResult(SearchResult result) {
        String filename = UNKNOWN_NAME;
        String modName = null;
        if (result.getModifiedName() != null && 
            result.getModifiedName().getFirst() != null) {
            filename = result.getModifiedName().getFirst().toString();
            if (result.getModifiedName().getLast() != null)
                modName = result.getModifiedName().getLast().toString();
        }
        StringBuilder builder = new StringBuilder(RESULTS_FOR + filename);
        if (modName != null)
            builder.append(MODIFIED_NAME)
                   .append(modName);
        else
            builder.append(NON_MODIFIED_NAME);
        builder.append(NEW_LINE);
        if (result.isExceptional())
            builder.append(EXCEPTION_CAUSE)
                   .append(result.getCause())
                   .append(NEW_LINE);
        else {
            builder.append(MOD_NUMBER)
                   .append(result.numberOfModificationsMade());
            result.getModifiedContent()
                  .stream()
                  .filter(tuple -> tuple.getLast() != null)
                  .forEach(tuple -> builder.append(ORIGINAL)
                                           .append(tuple.getFirst())
                                           .append(MODIFIED)
                                           .append(tuple.getLast()));
        }
        builder.append(NEW_LINE);
        return builder.toString();
    }
    
    public static String listResults(List<SearchResult> results) {
        StringBuilder resultsList = new StringBuilder(RESULTS_ARE);
        List<SearchResult> exceptionalResults = new ArrayList<>();
        int i = 1;
        for (SearchResult result : results) {
            if (result.isExceptional())
                exceptionalResults.add(result);
            else {
                resultsList.append(i++)
                            .append(COLON)
                            .append(result.getModifiedName().getFirst())
                            .append(RESULTS_WITH)
                            .append(result.numberOfModificationsMade())
                            .append(RESULTS_MOD);
            }
        }
        if (exceptionalResults.size() > 0) {
            resultsList.append(RESULTS_EXCEPT);
            for (SearchResult result : exceptionalResults)
                resultsList.append(i++)
                           .append(COLON)
                           .append(result.getCause())
                           .append(NEW_LINE);
        }
        return resultsList.toString();
    }

    private static Map<String, Consumer<ConsoleApplication>> fillMainCommands() {
        Map<String, Consumer<ConsoleApplication>> keys = new HashMap<>();
        EXIT_COMMANDS.forEach(key -> keys.put(key, CmdUtils::exit));
        HELP_COMMANDS.forEach(key -> keys.put(key, CmdUtils::showMainHelp));
        GO_BACK_COMMANDS.forEach(key -> keys.put(key, CmdUtils::cancel));
        return Collections.unmodifiableMap(keys);
    }

    private static Map<String, BiFunction<ReplaceFilesProfile, String, 
                                          ReplaceFilesProfile>> fillParamKeys() {
        Map<String, BiFunction<ReplaceFilesProfile, String, 
                               ReplaceFilesProfile>> pkeys = new HashMap<>();
        KEYS_PATH.forEach(key -> pkeys.put(key, CmdUtils::setPath));
        KEYS_FIND.forEach(key -> pkeys.put(key, CmdUtils::setToFind));
        KEYS_REPLACE.forEach(key -> pkeys.put(key, CmdUtils::setReplaceWith));
        KEYS_EXCLUDE.forEach(key -> pkeys.put(key, CmdUtils::addExclusion));
        KEYS_FILENAMES.forEach(key -> pkeys.put(key, CmdUtils::setFilenames));
        KEYS_SUBFOLDERS.forEach(key -> pkeys.put(key, CmdUtils::setSubfolders));
        KEYS_PATTERNS.forEach(key -> pkeys.put(key, CmdUtils::addIncludeNamePattern));
        KEYS_CHARSET.forEach(key -> pkeys.put(key, CmdUtils::setCharset));
        KEYS_USE_PROFILE.forEach(key -> pkeys.put(key, CmdUtils::useProfile));
        KEYS_SAVE_PROFILE.forEach(key -> pkeys.put(key, CmdUtils::saveProfile));
        return Collections.unmodifiableMap(pkeys);
    }
    
    
}
