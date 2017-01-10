package dmv.desktop.searchandreplace.service;

import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.AFTER_FOUND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.BEFORE_FIND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.COMPUTED;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.EXCLUDE_OTHER;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.FIND_OTHER;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import dmv.desktop.searchandreplace.collection.Tuple;
import dmv.desktop.searchandreplace.model.*;


public abstract class FileReplacerTest {
    
    @Rule
    public ExpectedException expected = ExpectedException.none();
    
    private static String toFind = "FindMe";
    private static String replaceWith = "Replaced";
    private static Path file1 = Paths.get("src/test/resources/replacertest/file1.txt");
    private static Path file2 = Paths.get("src/test/resources/replacertest/file2"+toFind+".txt");
    private static Path file2Renamed = Paths.get("src/test/resources/replacertest/file2.txt");
    private static Path nonReadable = Paths.get("src/test/resources/replacertest/nonReadable.txt");
    private static List<String> origContent1;
    private static List<Tuple<String, String>> modContent1;
    private static List<String> origContent2;
    private static List<Tuple<String, String>> modContent2;
    private static int modifications1;
    private static int modifications2;
    
    private FileReplacer target1;
    private FileReplacer target2;
    private SearchProfile profile;
    private Charset charset = StandardCharsets.UTF_8;

    protected abstract FileReplacer createTarget(Path file, SearchProfile profile);
    
    @BeforeClass
    public static void createContent() throws IOException {
        modContent1 = getModContent(toFind, replaceWith);
        origContent1 = getContentFrom(modContent1);
        origContent1.add("some text without *that* word");
        modContent2 = getModContent(toFind, "");
        origContent2 = getContentFrom(modContent2);
        modifications1 = 4;
        modifications2 = 5;
    }

    @Before
    public void setUp() throws Exception {
        /* restore static variables */
        toFind = "FindMe";
        replaceWith = "Replaced";
        file1 = Paths.get("src/test/resources/replacertest/file1.txt");
        file2 = Paths.get("src/test/resources/replacertest/file2"+toFind+".txt");
        
        /* restore default file contents */
        Files.deleteIfExists(file1);
        Files.deleteIfExists(file2);
        Files.write(file1, origContent1);
        Files.write(file2, origContent2);
        
        /* create default targets */
        profile = new SearchProfileImpl(toFind)
                           .setReplaceWith(replaceWith)
                           .setCharset(charset);
        target1 = createTarget(file1, profile);
        profile.setReplaceWith("")
               .setFilename(true);
        target2 = createTarget(file2, profile);
    }
    
    @Test
    public void getInitialState() {
        assertThat(target1.getState(), is(BEFORE_FIND));
    }

    @Test
    public void setNewFile() {
        target1.hasReplacements();
        assertThat(target1.getState(), is(AFTER_FOUND));
        
        target1.setFile(file1);
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        /* non-existing path */
        target1.setFile(Paths.get(",l"));
        assertTrue(target1.getResult().isExceptional());
        
        expected.expect(NullPointerException.class);
        target1.setFile(null);
    }

    @Test
    public void setNewProfileInitially() {
        /* initial state must not be changed */
        profile = new SearchProfileImpl(toFind + "other");
        target1.setProfile(profile);
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        profile.setCharset(StandardCharsets.US_ASCII);
        target1.setProfile(profile);
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        profile.setToFind("other" + toFind);
        target1.setProfile(profile);
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        profile.setExclusions(new ExclusionsTrie(Arrays.asList("pre"), null, false));
        target1.setProfile(profile);
        assertThat(target1.getState(), is(BEFORE_FIND));

        profile.setReplaceWith(replaceWith + "other");
        target1.setProfile(profile);
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        profile.setFilename(true);
        target1.setProfile(profile);
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        expected.expect(NullPointerException.class);
        target1.setProfile(null);
    }

    @Test
    public void setNewProfileAfterComputed() {
        /* change state */
        target1.getResult();
        assertThat(target1.getState(), is(COMPUTED));
        
        /* create new profile */
        profile = new SearchProfileImpl(toFind);
        
        profile.setCharset(StandardCharsets.US_ASCII);
        target1.setProfile(profile);
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        target1.getResult();
        
        profile.setToFind("other" + toFind);
        target1.setProfile(profile);
        assertThat(target1.getState(), is(FIND_OTHER));
        
        target1.getResult();
        
        profile.setExclusions(new ExclusionsTrie(Arrays.asList("pre"), null, false));
        target1.setProfile(profile);
        assertThat(target1.getState(), is(EXCLUDE_OTHER));

        target1.getResult();
        
        profile.setReplaceWith(replaceWith + "other");
        target1.setProfile(profile);
        assertThat(target1.getState(), is(AFTER_FOUND));
        
        target1.getResult();
        
        profile.setFilename(true);
        target1.setProfile(profile);
        assertThat(target1.getState(), is(COMPUTED));
    }

    @Test
    public void hasReplacements() throws IOException {
        /* first TestFile content has replacements with default profile set */
        assertTrue(target1.hasReplacements());

        /* change second TestFile so it will have toFind word in filename only */
        modContent2 = getModContent(toFind.substring(0, toFind.length() - 1), "");
        origContent2 = getContentFrom(modContent2);
        Files.write(file2, origContent2, TRUNCATE_EXISTING);
        /* filename replacements */
        assertTrue(target2.hasReplacements());
        
        /* change profile so nothing will be found in file */
        profile.setToFind("other" + toFind);
        target1.setProfile(profile);
        assertFalse(target1.hasReplacements());
        target2.setProfile(profile);
        assertFalse(target2.hasReplacements());
    }
    
    @Test
    public void getResult() throws Throwable {
        
        checkResultExpect(target1.getResult(), modContent1, null, modifications1, false, null);
        
        checkResultExpect(target2.getResult(), modContent2, file2Renamed, modifications2, false, null);
        
        /* exceptional results */

        profile.setCharset(StandardCharsets.UTF_16LE);
        target1.setProfile(profile);
        checkResultExpect(target1.getResult(), null, null, 0, true, MalformedInputException.class);
        
        profile.setCharset(charset);
        target1.setProfile(profile);
        
        target1.setFile(nonReadable);
        checkResultExpect(target1.getResult(), null, null, 0, true, AccessDeniedException.class);
        
    }

    @After
    public void reset() throws IOException {
//        Files.setAttribute(file1, "dos:readonly", false);
//        Files.setAttribute(file1, "dos:hidden", false);
//        Files.setAttribute(file1, "dos:system", false);
    }
    
    @AfterClass
    public static void deleteFiles() throws IOException {
        Files.deleteIfExists(file1);
        Files.deleteIfExists(file2);
    }

    private void checkResultExpect(SearchResult result, 
                                 List<Tuple<String, String>> modContent, 
                                 Path modName, 
                                 int modifications, 
                                 boolean exceptional, 
                                 Class<? extends Throwable> cause) throws Throwable {
        if (exceptional) {
            assertThat(result.getModifiedName(), is(nullValue()));
            assertTrue(result.getCause().getClass() == cause);
        } else {
            assertThat(result.getModifiedName().getLast(), is(modName));
            assertThat(result.getCause(), is(nullValue()));
        }
        assertThat(result.getModifiedContent(), is(modContent));
        assertThat(result.numberOfModificationsMade(), is(modifications));
        assertTrue(result.isExceptional() == exceptional);
    }
    
    private static List<String> getContentFrom(List<Tuple<String, String>> modContent) {
        return modContent.stream()
                         .map(tuple -> tuple.getFirst())
                         .collect(toList());
    }

    private static List<Tuple<String, String>> getModContent(String toFind, String replaceWith) {
        return Arrays.asList(new Tuple<>("some text with " + toFind + " word",
                                         "some text with " + replaceWith + " word"),
                             new Tuple<>("another text that contains at the end " + toFind,
                                         "another text that contains at the end " + replaceWith),
                             new Tuple<>(toFind + " at the beginning and " + toFind + " again", 
                                         replaceWith + " at the beginning and " + replaceWith + " again"));
    }
    
    @SuppressWarnings("unused")
    private static List<Tuple<String, String>> getRandomContent(String[] notReplace, 
                                                                   String toFind, String replaceWith) {
        List<Tuple<String, String>> fileContent = new ArrayList<>();
        Tuple<String, String> tuple = new Tuple<>();
        int lines = 20, lineLength = 30, charBound = 100, charShift = 30;
        Random rand = new Random();
        StringBuilder origLine = new StringBuilder(lineLength);
        StringBuilder modLine = new StringBuilder(lineLength);
        String excluded = null;
        boolean replaced = false;
        while (lines-- > 0) {
            for (int i = 0; i < lineLength; i++) {
                if (rand.nextInt(100) > 95) 
                    appendBoth(origLine, modLine, notReplace[rand.nextInt(notReplace.length)]);
                else if (rand.nextInt(100) > 95) {
                    origLine.append(toFind);
                    modLine.append(replaceWith);
                    replaced = true;
                } else 
                    appendBoth(origLine, modLine, String.valueOf(rand.nextInt(charBound) + charShift));
            }
            tuple.setFirst(origLine.toString());
            tuple.setLast(replaced ? modLine.toString() : null);
            fileContent.add(tuple);
            origLine = new StringBuilder(lineLength);
            modLine = new StringBuilder(lineLength);
            replaced = false;
        }
        
        return fileContent;
    }

    private static void appendBoth(StringBuilder origLine,
                                   StringBuilder modLine, 
                                   String s) {
        origLine.append(s);
        modLine.append(s);
    }
    
}
