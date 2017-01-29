package dmv.desktop.searchandreplace.view.profile;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import dmv.desktop.searchandreplace.collection.TupleImpl;
import dmv.desktop.searchandreplace.model.Exclusions;
import dmv.desktop.searchandreplace.model.SearchPath;
import dmv.desktop.searchandreplace.model.SearchProfile;

/**
 * Class <tt>ReplaceFilesProfile.java</tt> describes methods for
 * creating and editing program profiles, and also saving and 
 * reading them from disk.
 * <p>
 * Its exceptional policy is a bit odd - it would throw an 
 * exception on parameter's get method call if that parameter is
 * wrong. That's done so because instance variables are stored
 * as simple strings for easier reading/writing file operations,
 * and the main goal is to collect them all (good and bad) first
 * and then, if their 'real' implementations that are needed for
 * further processing could not be created because of some
 * inconsistency, the whole profile will be shown to the user.
 * <p>
 * So it's needed for smooth user experience. The only exception
 * is a profile name - this will be validated upon set.
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
     * Given names are expected to appear in a profile as parameter keys:
     * <pre>
     * Where to search, path (required)
     * What to find (required)
     * What to put in replace
     * Charset to use
     * Modify filenames
     * Scan subfolders
     * Include file name patterns
     * exclusion
     * </pre>
     */
    static final Set<String> PARAMETER_NAMES = unmodifiableSet(
                 new HashSet<>(Arrays.asList("Where to search, path (required)",
                                             "What to find (required)",
                                             "What to put in replace",
                                             "Charset to use",
                                             "Modify filenames",
                                             "Scan subfolders",
                                             "Include file name patterns",
                                             "exclusion")));

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
     * Get current path to a file or 
     * to a folder where program will do its search.
     * <p>
     * This is a required setting.
     * <p>
     * If this parameter was not set or if it contains malformed path
     * the exception will be thrown (it won't check if path exists).
     * @return current path saved in a profile
     * @throws WrongProfileException if current path is malformed or absent
     */
    Path getPath() throws WrongProfileException;
    
    /**
     * Set new path to a file or folder where program
     * will run its search. It won't be validated upon
     * storage, but when the {@link SearchPath} is constructed,
     * malformed or empty paths will trigger an exception. 
     * @param path File or folder path
     * @return this object
     */
    ReplaceFilesProfile setPath(String path);
    
    /**
     * Get current file naming patterns which will
     * be included in {@code search and replace} operation.
     * @return current Array of file naming patterns
     */
    String[] getIncludeNamePatterns();
     
    /**
     * Add new file naming pattern to existing ones which will
     * be included in {@code search and replace} operation.
     * These patterns won't be validated until creation of
     * {@link SearchPath} object. Malformed patterns may trigger
     * an exception later. 
     * <p>
     * Any existing patterns will be removed if argument is null or empty.
     * @param pattern Single naming pattern
     * @return this object
     */
    ReplaceFilesProfile addIncludeNamePattern(String pattern);
    
    /**
     * Is subfolders should be scanned {@code true}
     * or not {@code false}.
     * <p>
     * Because this setting constructed from certain words
     * given in a profile or through a setter method,
     * if those words were not expected ones the exception
     * will be thrown now on this method call.
     * @return current subfolders setting
     * @throws WrongProfileException if current setting is not one of
     *                               {@link ReplaceFilesProfile#RECOGNIZED_BOOLEANS 
     *                               expected} words
     */
    boolean getSubfolders() throws WrongProfileException;
    
    /**
     * Set whether subfolders should be scanned or not using
     * {@link ReplaceFilesProfile#RECOGNIZED_BOOLEANS allowed} words.
     * @param subfolders {@code true} - include subfolders, {@code false} - skip them
     * @return this object
     */
    ReplaceFilesProfile setSubfolders(String subfolders);
    
    /**
     * Get current charset or null if nothing set.
     * If current setting is non-empty string but it is
     * unrecognized as a valid charset name the exception
     * will be thrown
     * @return current charset or null
     * @throws WrongProfileException if given charset is unrecognized
     */
    Charset getCharset() throws WrongProfileException;
    
    /**
     * Set charset name as described {@link Charset here}.
     * It won't be validated until {@link SearchProfile}
     * creation time where unrecognizable charset name
     * will trigger an exception.
     * @param charset Charset name
     * @return this object
     */
    ReplaceFilesProfile setCharset(String charset);
    
    /**
     * If file names should be searched and replaced with the
     * same rule as for their content. 
     * @return {@code true} if names will be modified and {@code false} if not
     * @throws WrongProfileException if current setting is not one of
     *                               {@link ReplaceFilesProfile#RECOGNIZED_BOOLEANS 
     *                               expected} words
     */
    boolean getFilenames() throws WrongProfileException;
    
    /**
     * Set if file names should be searched and replaced with the
     * same rule as for their content. Use one of
     * {@link ReplaceFilesProfile#RECOGNIZED_BOOLEANS allowed} words
     * for this setting.
     * @param filenames {@code true} if file names should be modified 
     *                  and {@code false} if not.
     * @return this object
     */
    ReplaceFilesProfile setFilenames(String filenames);
    
    /**
     * Get current {@code what to find} string, which is
     * at least one character long.
     * @return current {@code what to find} string
     * @throws WrongProfileException if this parameter is empty
     */
    String getToFind() throws WrongProfileException;
    
    /**
     * Set {@code what to find} string. It should contain at least
     * one character. It won't be validated until {@link SearchProfile}
     * creation though and empty string will trigger an exception.
     * @param toFind {@code what to find} string
     * @return this object
     */
    ReplaceFilesProfile setToFind(String toFind);
    
    /**
     * Get current string that will be placed instead of found one,
     * this could be an empty string
     * @return current {@code replace with} string
     */
    String getReplaceWith();
    
    /**
     * Set string that will be placed instead of found one
     * @param replaceWith what to place instead of found string
     * @return this object
     */
    ReplaceFilesProfile setReplaceWith(String replaceWith);
    
    /**
     * Get current exclusions in a form of
     * {@link Exclusions} type, because this object creation
     * uses a {@code what to find} setting it may throw an exception
     * if latter was not provided.
     * @return current set of exclusions
     * @throws WrongProfileException if creation of required type fails
     */
    Exclusions getExclusions() throws WrongProfileException;
    
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
     */
    ReplaceFilesProfile addExclusion(String exclusion);
    
    /**
     * The profile will be read and its settings will overwrite any existing.
     * Its name will overwrite previously given name. The {@code overwriteExisting}
     * setting will be set to true allowing this profile to be overwritten.
     * @param profileName the name of {@link ReplaceFilesProfile profile}
     * @return this object
     * @throws IllegalArgumentException if profile with given name does not exist or
     *                                  it's incorrectly formatted (cannot be parsed)
     */
    ReplaceFilesProfile useProfile(String profileName);
    
    /**
     * Profile with current settings will be saved on disk under specified name
     * or with name previously set if this {@code profileName} is null or empty,
     * or, if inner parameter is also absent, name will be auto-generated.
     * If profile with given name is already exist and {@code overwriteExisting}
     * option is not set to true this method will throw an exception
     * @param profileName The name under which the profile will be saved
     * @return this object
     * @throws IllegalArgumentException if profile with given name is already exist
     *                                  and {@code overwriteExisting} setting is false
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
     * This method is a part of API and should provide output as follows:
     * <p>
     * (for each parameter that is absent there should be a phrase {@code Currently not set})
     * <pre>
     * Name of a profile:           
     * Current_name
     * Overwrite profile with the same name: 
     * true or false
     * Where to search, path (required):             
     * Current_path
     * What to find (required):    
     * Current_to_find
     * What to put in replace:      
     * Current_replace_with (it is always set, even when it's empty)
     * Charset to use:              
     * Current_charset
     * Modify filenames:            
     * (If properly set) true or false
     * Scan subfolders:             
     * (If properly set) true or false
     * Include file name patterns:  
     * Comma_separated_list_of_patterns 
     * (List of exclusions)  
     * exclusion: One_of_exclusions
     * exclusion: Another_one
     * etc...           
     * 
     * </pre>
     * All parameters may not be validated yet (except for the profile name)
     * and shown as they were provided. {@code Overwrite} setting is
     * not a part of a profile and won't be saved in a file, it is shown
     * here just for information. Other settings are shown exactly as they 
     * would be saved in a profile.
     * <p>
     * Note that string parameters ({@code what to find}, {@code what to put in replace},
     * and any of {@code exclusions}) may contain special characters like {@code new line}
     * \n character of {@code tab} \t or else, so to be correctly recognized they are
     * expected to start exactly at the new line after corresponding key and ends before
     * the next key (on separate line) or the end of a file mark.
     * @return Formatted string representation of a profile
     */
    @Override
    String toString();
}
