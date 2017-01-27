/**
 * 
 */
package dmv.desktop.searchandreplace.view.consoleapp.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import dmv.desktop.searchandreplace.exception.AccessResourceException;
import dmv.desktop.searchandreplace.exception.NothingToReplaceException;
import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.view.profile.ReplaceFilesProfile;

/**
 * Class <tt>CmdUtils.java</tt>
 * @author dmv
 * @since 2017 January 21
 */
public class CmdUtils {
    
    public static final int ATTEMPTS = 10;
    /* various menu texts */
    public static final String HELP;
    public static final String RESULTS;
    public static final String SINGLE_RESULT;
    /* command line keys and their commands (corresponding actions) */
    public static final Map<String, Runnable> MAIN_KEYS;
    public static final Map<String, 
                             BiFunction<ReplaceFilesProfile, 
                                        String, 
                                        ReplaceFilesProfile>> PARAMETER_KEYS;
    
    static {
        MAIN_KEYS = fillMainCommands();
        PARAMETER_KEYS = fillParamKeys();
        SINGLE_RESULT = makeFrom("Type 0 to go to previous menu or one of main commands.");
        RESULTS = makeFrom("Type file's number to see the details or " +
                           "Type word 'do' without quotes to make replacements or " + 
                           "Type 0 to go to previous menu or one of main commands.");
        HELP = makeFrom("Program usage: ", 
                        "Escape back slash '\\' characters with another one - like so '\\\\'",
                        "Put phrases with space characters or if they start with '-' in double quotes " +
                        "\"-like this one\" to be correctly recognized by a program.",
                        " ",
                        "nopreview (skip preview), help (show help), exit (exit program)",
                        "-p [path to file or folder] -f [what to find in it] " +
                        "-r [what to put in its place] -x [what to exclude] " +
                        "-fn [true/false (modify also file name)] " +
                        "-sf [true/false (include subfolders)] " +
                        "-np [naming pattern (file's path that will be included in search)]" +
                        "-cs [name of charset to use in read and write operations] " +
                        "-pf [name of profile to use] -sp [name of profile to save ] ",
                        "At least two parameters are required - 'path to the resource' where " +
                        "to search and 'what to find' in it, these parameters may also be " +
                        "included in a profile, so it is ok to specify just a profile with them.",
                        " ",
                        "Available keys:",
                        "help, h, /?:",
                        "       Shows this info. This key is expected to be alone on command line.",
                        "exit, q, /q:",
                        "       Quits the program. This key is expected to be alone on command line.",
                        "nopreview:",
                        "       If you are certain about results, you may skip 'preview' part, just\n" +
                        "       put this key at the very beggining of command line. This is a special\n" +
                        "       case and so this key cannot be saved in a profile.",
                        "-p -path:",
                        "       Specify a path to the file or folder where to search.\n" +
                        "       Expected to be a real path to existing resource. Required parameter.",
                        "-f -find:",
                        "       Specify a phrase that needed to be found in the resource,\n" +
                        "       must be at least one character long. Required parameter.",
                        "-r -replace:",
                        "       Specify a phrase that needed to be placed instead of found ones.\n" +
                        "       It is empty by default, but you may specify it as empty if\n" + 
                        "       you need to overwrite profile setting.",
                        "-x -exclude:",
                        "       Specify what need not be replaced, an exclusion that have to has\n" +
                        "       'what to find' phrase as a part of it. You can specify several exclusions, " +
                        "        just use a key (-x) before each of them. Again, you may write " +
                        "       just the key with the empty space after it if you want to overwrite " +
                        "       profile setting.",
                        "-fn -filename:",
                        "       Use word true or false to specify if you want to change also " +
                        "       file names with the same rule as for their content. False by default.",
                        "-sf -subfolders:",
                        "       Use word true or false to specify if you want subfolders to be " +
                        "       scanned through. It is false by default.",
                        "-np -namepattern:",
                        "       Specify what paths will be included in search and replace operation. " +
                        "       See java.nio.file.FileSystems#getPathMatcher method desciption " +
                        "       of what kind of patterns are supported by this program. " +
                        "       Specify just one pattern per key, there can be several keys in command.",
                        "-cs -charset:",
                        "       You can specify a Charset name (like UTF-8) that will be used for " +
                        "       reading and writing file contents. The defaut setting is UTF-16.",
                        "-pf -profile:",
                        "       Specify a name of existing profile with or without the file's extension. " +
                        "       If you will also specify other keys after it those keys parameters will " +
                        "       overwrite corresponding settings of a profile. Any key before the profile " +
                        "       will be overwritten by a corresponding profile's setting.",
                        "-sp -saveprofile:",
                        "       Specify a name under which current keys will be saved as a profile. " +
                        "       This command expected at the very end of command line. Any parameters " +
                        "       appeared after it won't be saved in a profile, it's may be your intention." +
                        "       File's extension will be added automatically. If this key has empty " +
                        "       space following it and there is a profile specified in command then " +
                        "       that profile will be overwritten. If no profile used in a command " +
                        "       then new profile will be saved with auto-generated name. If given " +
                        "       name is the same as some existing profile has (not specified in command) " +
                        "       then you'll be asked for overwritting operation or renaming.");
    }

    public static void exit() {
        System.exit(0);
    }
    
    public static void printHelp() {
        System.out.print(HELP);
        System.out.close();
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
    
    public static void printResult(List<SearchResult> result) {
        StringBuilder builder = new StringBuilder("Results are:\n");
        int i = 1;
        List<SearchResult> exceptionals = new ArrayList<>();
        for (SearchResult r : result) {
            if (r.isExceptional())
                exceptionals.add(r);
            else
                builder.append(i++)
                       .append(": ")
                       .append(r.getModifiedName().getFirst())
                       .append(" with ")
                       .append(r.numberOfModificationsMade())
                       .append(" modifications\n");
        }
        if (exceptionals.size() > 0) {
            builder.append("\nNext results were exceptional:\n");
            for (SearchResult r : exceptionals)
                builder.append(i++)
                       .append(": ")
                       .append(r.getCause())
                       .append("\n");
        }
        System.out.println(builder);          
    }

    public static void tellAbout(Exception e) {
        if (e instanceof AccessResourceException)
            System.out.println("Something wrong with the resource provided, check if it exists and readable");
        else if (e instanceof NothingToReplaceException)
            System.out.println("There is nothing found with given parameters");
    }

    private static void getProperInput(BufferedReader reader) throws IOException {
        System.out.println("choose from 1 or 2");
        String[] args = reader.readLine().split("\\s+");
        System.out.println(Arrays.deepToString(args));
        String first = args.length > 0 ? args[0] : "";
        System.out.println(first);
        if (first.equals("1"))
            System.out.println("you have choosen 1");
        else if (first.equals("2"))
            System.out.println("you have choosen 2");
        else
            getProperInput(reader);
    }

    private static String makeFrom(String... lines) {
        StringBuilder result = new StringBuilder();
        Stream.of(lines)
              .forEach(line -> result.append(line).append("\n"));
        return result.toString();
    }

    private static Map<String, Runnable> fillMainCommands() {
        Map<String, Runnable> keys = new HashMap<>();
        keys.put("exit", CmdUtils::exit);
        keys.put("q",    CmdUtils::exit);
        keys.put("/q",   CmdUtils::exit);
        keys.put("help", CmdUtils::printHelp);
        keys.put("h",    CmdUtils::printHelp);
        keys.put("/?",   CmdUtils::printHelp);
        return keys;
    }

    private static Map<String, BiFunction<ReplaceFilesProfile, String, 
                                          ReplaceFilesProfile>> fillParamKeys() {
        Map<String, BiFunction<ReplaceFilesProfile, String, 
                               ReplaceFilesProfile>> pkeys = new HashMap<>();
        pkeys.put("-p",           CmdUtils::setPath);
        pkeys.put("-path",        CmdUtils::setPath);
        pkeys.put("-f",           CmdUtils::setToFind);
        pkeys.put("-find",        CmdUtils::setToFind);
        pkeys.put("-r",           CmdUtils::setReplaceWith);
        pkeys.put("-replace",     CmdUtils::setReplaceWith);
        pkeys.put("-x",           CmdUtils::addExclusion);
        pkeys.put("-exclude",     CmdUtils::addExclusion);
        pkeys.put("-fn",          CmdUtils::setFilenames);
        pkeys.put("-filenames",   CmdUtils::setFilenames);
        pkeys.put("-sf",          CmdUtils::setSubfolders);
        pkeys.put("-subfolders",  CmdUtils::setSubfolders);
        pkeys.put("-np",          CmdUtils::addIncludeNamePattern);
        pkeys.put("-namepattern", CmdUtils::addIncludeNamePattern);
        pkeys.put("-cs",          CmdUtils::setCharset);
        pkeys.put("-charset",     CmdUtils::setCharset);
        pkeys.put("-pf",          CmdUtils::useProfile);
        pkeys.put("-replaceProfile",     CmdUtils::useProfile);
        pkeys.put("-sp",          CmdUtils::saveProfile);
        pkeys.put("-saveprofile", CmdUtils::saveProfile);
        return pkeys;
    }
    
    
}
