package dmv.desktop.searchandreplace.view.profile;

import static java.util.Collections.unmodifiableMap;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import dmv.desktop.searchandreplace.collection.TupleImpl;
import dmv.desktop.searchandreplace.exception.WrongProfileException;
import dmv.desktop.searchandreplace.model.SearchPath;
import dmv.desktop.searchandreplace.model.SearchProfile;
import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.service.SearchAndReplace;
import dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils;

/**
 * Interface <tt>ReplaceFilesProfile.java</tt> describes methods for
 * creating and editing program profiles, and also saving and 
 * reading them from disk.
 * <p>
 * Its exceptional policy is a bit odd - it won't throw an exception
 * when incorrect parameter was set for the first time. 
 * That's done so because the main goal is to collect all parameters 
 * (good and bad) given at program start as simple strings and then, 
 * if their 'real' implementations that are needed for further processing 
 * could not be used due to some inconsistency, the whole profile 
 * will be shown to the user allowing to edit incorrect fields.
 * <p>
 * In edit mode, any parameter will be given and set separately, and
 * also validated at set time, immediately falling back in case of
 * incorrect input (via {@link WrongProfileException}).
 * <p>
 * Note, that exception is also possible on service update operation even
 * if all parameters were correct. It may happen if service is in exceptional
 * state already and given parameters are not sufficient enough to change it.
 * <p>
 * So, it's needed for smooth user experience. The only exception here
 * is a profile name - this one will be validated upon every set operation.
 * @author dmv
 * @since 2017 January 21
 */
public interface ReplaceFilesProfile {
    
    /**
     * Profile name should not exceed this length
     */
    static final int NAME_SIZE = 30;
    
    /**
     * File's extension of a profile
     */
    static final String FILE_EXTENSION = ".snrp";
    
    /**
     * Double hashtag at the very beginning of the line will
     * be recognized as a comment and the rest of the line
     * will not be parsed by a program. The one exception though
     * concerned the nature of string parameters (i.e. what to
     * find, replace, or what to exclude may contain presumably any 
     * characters) - this sign will be ignored and line content
     * will be parsed if it follows the one of string parameter keys.
     * <p>
     * In other words, it is safe to put comments at file's header
     * and not in between parameter keys.
     */
    static final String COMMENT_SIGN = "##";
    
    /**
     * Following words will be accepted as boolean parameters:
     * <pre>
     * Accepted as true:
     *      true
     *      yes
     *      y
     *      1
     * Accepted as false:      
     *      false
     *      no
     *      n
     *      0
     * </pre>
     */
    static final Map<String, Boolean> RECOGNIZED_BOOLEANS = unmodifiableMap(
            Arrays.asList(new TupleImpl<>("true",  true),
                          new TupleImpl<>("yes",   true),
                          new TupleImpl<>("y",     true),
                          new TupleImpl<>("1",     true),
                          new TupleImpl<>("false", false),
                          new TupleImpl<>("no",    false),
                          new TupleImpl<>("n",     false),
                          new TupleImpl<>("0",     false))
                  .stream()
                  .collect(Collectors.toMap(tuple -> tuple.getFirst(), 
                                            tuple -> tuple.getLast())));


    /**
     * Get current profile name or empty string if it was not set
     * @return current profile name
     */
    String getName();
    
    /**
     * Set profile name. It will be a name of a file,
     * it may contain English ASCII letters and/or numbers
     * and/or underscores. File extension will be added
     * automatically. Any existing file with the
     * same name may be overwritten if allowed.
     * <p>
     * Maximum length is {@value #NAME_SIZE} symbols
     * @param name Profile name
     * @return this object
     * @throws IllegalArgumentException if name contains disallowed symbols
     *                                  or its length is greater than
     *                                  {@value #NAME_SIZE} characters 
     *                                  or if it's null or empty
     */
    ReplaceFilesProfile setName(String name);

    /**
     * Set new path to a file or folder where program
     * will run its search. It won't be validated right
     * away, but when the {@link SearchPath} is constructed,
     * malformed or empty paths will trigger an exception. 
     * @param path File or folder path
     * @return this object
     * @throws WrongProfileException in {@code edit mode} 
     *                               (i.e. when service is already created)
     */
    ReplaceFilesProfile setPath(String path);
     
    /**
     * Add new file naming pattern to existing ones which will
     * be included in {@code search and replace} operation.
     * These patterns won't be validated until creation of
     * {@link SearchPath} object for the first time. 
     * Malformed patterns may trigger an exception later. 
     * <p>
     * Any existing patterns will be removed if argument is null or empty.
     * @param pattern Single naming pattern
     * @return this object
     * @throws WrongProfileException in {@code edit mode} 
     *                               (i.e. when service is already created)
     */
    ReplaceFilesProfile addIncludeNamePattern(String pattern);
    
    /**
     * Set whether subfolders should be scanned or not using
     * {@link ReplaceFilesProfile#RECOGNIZED_BOOLEANS allowed} words.
     * @param subfolders {@code true} - include subfolders, {@code false} - skip them
     * @return this object
     * @throws WrongProfileException in {@code edit mode} 
     *                               (i.e. when service is already created)
     */
    ReplaceFilesProfile setSubfolders(String subfolders);
    
    /**
     * Set charset name as described {@link Charset here}.
     * It won't be validated until {@link SearchProfile}
     * creation time where unrecognizable charset name
     * will trigger an exception.
     * @param charset Charset name
     * @return this object
     * @throws WrongProfileException in {@code edit mode} 
     *                               (i.e. when service is already created)
     */
    ReplaceFilesProfile setCharset(String charset);
    
    /**
     * Set if file names should be searched and replaced with the
     * same rule as for their content. Use one of
     * {@link ReplaceFilesProfile#RECOGNIZED_BOOLEANS allowed} words
     * for this setting.
     * @param filenames {@code true} if file names should be modified 
     *                  and {@code false} if not.
     * @return this object
     * @throws WrongProfileException in {@code edit mode} 
     *                               (i.e. when service is already created)
     */
    ReplaceFilesProfile setFilenames(String filenames);
    
    /**
     * Set {@code what to find} string. It should contain at least
     * one character. It won't be validated until {@link SearchProfile}
     * creation though and empty string will trigger an exception.
     * @param toFind {@code what to find} string
     * @return this object
     * @throws WrongProfileException in {@code edit mode} 
     *                               (i.e. when service is already created)
     */
    ReplaceFilesProfile setToFind(String toFind);
    
    /**
     * Set string that will be placed instead of found one.
     * If it's null or empty - found phrases will be simply removed.
     * @param replaceWith what to place instead of found string
     * @return this object
     */
    ReplaceFilesProfile setReplaceWith(String replaceWith);
    
    /**
     * Add new exclusion to existing exclusions (strings that are contain
     * {@code what to find} string in them plus some prefix or suffix
     * and if found such a combination in a file it won't be replaced).
     * They won't be checked until creation of {@link SearchProfile}
     * and set with wrong exclusions may trigger an exception.
     * <p>
     * If given parameter is null or empty the existing set will be removed.
     * @param exclusion Single exclusion
     * @return this object
     * @throws WrongProfileException in {@code edit mode} 
     *                               (i.e. when service is already created)
     */
    ReplaceFilesProfile addExclusion(String exclusion);
    
    /**
     * The profile will be read and its settings will overwrite any existing.
     * Its name will overwrite previously given name. The {@code overwriteExisting}
     * setting will be set to true allowing this profile to be overwritten.
     * @param profileName the name of {@link ReplaceFilesProfile profile}
     * @return this object
     * @throws IllegalArgumentException if name contains disallowed symbols
     *                                  or its length is greater than
     *                                  {@value #NAME_SIZE} characters 
     *                                  or if it's null or empty
     * @throws IllegalStateException if profile with given name does not exist or
     *                               it's incorrectly formatted (cannot be parsed)
     */
    ReplaceFilesProfile useProfile(String profileName);
    
    /**
     * Profile with current settings will be saved on disk under specified name
     * or with name previously set if this {@code profileName} is null or empty,
     * or, if inner parameter is also absent, name will be auto-generated.
     * If profile with given name already exists and {@code overwriteExisting}
     * option is not set to true this method will throw an exception
     * @param profileName The name under which the profile will be saved
     * @return this object
     * @throws IllegalArgumentException if name contains disallowed symbols
     *                                  or its length is greater than
     *                                  {@value #NAME_SIZE} characters 
     * @throws IllegalStateException if profile with given name already exists
     *                               and {@code overwriteExisting} setting is false
     */
    ReplaceFilesProfile saveProfie(String profileName); 
    
    /**
     * Could be used for second {@code write profile} attempt with 
     * overwrite existing file option set to true.
     * <p>
     * This setting is not part of a profile and won't be saved in file
     * as a parameter (it also can't be recognized).
     * @return this object
     */
    ReplaceFilesProfile setOverwrite();

    /**
     * Create (for the first time) or update and then return the main 
     * {@link SearchAndReplace} interface implementation with searching
     * and replacing files functionality. If object creation would
     * fail because of incorrect setting in current profile then the
     * exception will be thrown.
     * @return {@link SearchAndReplace} service implementation
     * @throws WrongProfileException when some settings are incorrect or they are not
     *                               enough to reset exceptional state of existing
     *                               service
     */
    SearchAndReplace<SearchPath, SearchProfile, SearchResult> createService();

    /**
     * This method is a part of API and should provide output as follows:
     * <p>
     * Each parameter key is a full version of command line keys, for
     * example {@link CmdUtils#KEYS_FIND these} are for {@code What to find} parameter.
     * The parameter itself is expected on the next line.
     * <p>
     * Comments are allowed on the same line as the key (behind double hashtag).
     * <p>
     * Note, that for string parameters (like find, exclude etc) any character counts
     * including new line characters, several spaces etc.
     * <p>
     * For each parameter that is absent there should be a phrase {@code Currently not set}
     * or {@code false} for boolean parameters. {@code Replace with} parameter is an
     * empty string by default, and it is always considered to be set.
     * <p>
     * {@code Included name patterns} will be combined in one parameter as comma-separated list.
     * {@code Exclusions} are given separately in the end of file.
     * <pre>
     * Name of a profile
     * Current_name
     * Overwrite profile with the same name
     * true or false
     * 
     * -path ## required
     * Current_Where_to_search_path
     * -find ## required
     * Current_What_to_find_phrase
     * -replace
     * Current_replace_with
     * -charset
     * Current_charset
     * -filenames
     * Current_boolean_word
     * -subfolders
     * Current_boolean_word
     * -namepattern
     * Comma_separated_list_of_patterns
     * -exclude
     * One_of_exclusions
     * -exclude
     * Another_one
     * ...
     * </pre>
     * All parameters stored before validation (except for the profile name)
     * and shown as they were provided. {@code Name} and {@code Overwrite} settings
     * are not a part of a profile and won't be saved in a file. Other parameters 
     * are shown exactly as they would be saved in a profile.
     * <p>
     * When creating your own profiles you are allowed to use either version of 
     * parameter's key (long or short). But the toString method will always print 
     * long version.
     * <p>
     * {@code Name patterns} could be typed in a profile separately (i.e. each
     * pattern after {@code -namepattern} key), but in toString output they
     * always will be combined into one comma-separated list.
     * <p>
     * Note, that string parameters ({@code what to find}, {@code what to put in replace},
     * and any of {@code exclusions}) may contain special characters like {@code new line}
     * \n character of {@code tab} \t or else, so to be correctly recognized they are
     * expected to start exactly at the new line after corresponding key and ends before
     * the next key (on separate line) or the end of a file mark. Any spaces or new line
     * characters will be consumed exactly as they are given in a profile.
     * <p>
     * Other parameters will be trimmed out of surrounding spaces.
     * @return Formatted string representation of a profile
     */
    @Override
    String toString();
}
