package dmv.desktop.searchandreplace.service;

import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.AFTER_FOUND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.BEFORE_FIND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.COMPUTED;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.EXCLUDE_OTHER;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.FIND_OTHER;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.*;
import org.junit.rules.ExpectedException;

import dmv.desktop.searchandreplace.collection.Tuple;
import dmv.desktop.searchandreplace.collection.TupleImpl;
import dmv.desktop.searchandreplace.model.*;


public abstract class FileReplacerTest {
    
    @Rule
    public ExpectedException expected = ExpectedException.none();
    
    /* These are just defaults, they may be overridden, but will be restored */
    private static String toFind = "FindMe";
    private static String replaceWith = "Replaced";
    private static Charset charset = StandardCharsets.UTF_8;
    /* Test files */
    private static Path file1 = Paths.get("src/test/resources/replacertest/file1.txt");
    private static Path file2 = Paths.get("src/test/resources/replacertest/file2"+toFind+".txt");
    private static Path file2Renamed = Paths.get("src/test/resources/replacertest/file2.txt");
    private static Path readOnly = Paths.get("src/test/resources/replacertest/readOnly.txt");
    private static Path nonReadable = Paths.get("src/test/resources/replacertest/nonReadable.txt");
    private static Path notExisting = Paths.get("src/test/resources/replacertest/notExisting.txt");
    /* new random filename in case of collisions (is not yet known) */
    private static Path newName;
    /* This block of variables is not for changes */
    private static List<String> origContent1;
    private static List<Tuple<String, String>> modContent1;
    private static List<String> origContent2;
    private static List<Tuple<String, String>> modContent2;
    private static int modifications1;
    private static int modifications2;
    
    /* Main changeable instances */
    private FileReplacer target1;
    private FileReplacer target2;
    private SearchProfile profile;
    

    /* Get actual implementation */
    protected abstract FileReplacer createTarget(Path file, SearchProfile profile);
    /* Do we need to reset after file's content changes */
    protected abstract boolean isContentCached();
    
    /* prepare BeforeClass and Before tests */
    
    @BeforeClass
    public static void createContent() throws IOException {
        modContent1  = getConstantContent(toFind, replaceWith);
        origContent1 = getFileContent(modContent1);
        modContent2  = getConstantContent(toFind, "");
        origContent2 = getFileContent(modContent2);
        modifications1 = 4;
        modifications2 = 5;
    }

    @Before
    public void setUp() throws Exception {
        /* restore default static variables */
        toFind = "FindMe";
        replaceWith = "Replaced";
        charset = StandardCharsets.UTF_8;
        file1 = Paths.get("src/test/resources/replacertest/file1.txt");
        file2 = Paths.get("src/test/resources/replacertest/file2"+toFind+".txt");
        file2Renamed = Paths.get("src/test/resources/replacertest/file2.txt");
        nonReadable = Paths.get("src/test/resources/replacertest/nonReadable.txt");
        
        /* restore default file contents */
        Files.deleteIfExists(file1);
        Files.deleteIfExists(file2);
        Files.write(file1, origContent1);
        Files.write(file2, origContent2);
        
        /* create default targets */
        profile = SearchProfileImpl.getBuilder(toFind)
                                  .setReplaceWith(replaceWith)
                                  .setCharset(charset)
                                  .build();
        target1 = createTarget(file1, profile);
        
        target2 = createTarget(file2, profile.setReplaceWith("")
                                             .setFilename(true));
    }
    
    /* Basic API methods tests */
    
    @Test
    public void getInitialState() {
        assertThat(target1.getState(), is(BEFORE_FIND));
    }

    @Test
    public void setNewFile() throws Throwable {
        /* advance state */
        target1.hasReplacements();
        assertThat(target1.getState(), is(AFTER_FOUND));
        
        /* check reset */
        target1.setFile(file1);
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        /* non-existing path */
        target1.setFile(Paths.get(",l"));
        checkResultExpect(target1.getResult(), null, null, 0, true, NoSuchFileException.class);
        
        expected.expect(NullPointerException.class);
        target1.setFile(null);
    }

    @Test
    public void setNewProfileInitially() {
        /* initial state must not be changed */
        profile = profile.setToFind(toFind + "other");
        target1.setProfile(profile);
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        target1.setProfile(profile.setCharset(StandardCharsets.US_ASCII));
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        target1.setProfile(profile.setToFind("other" + toFind));
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        target1.setProfile(profile.setExclusions(new ExclusionsTrie(Arrays.asList("pre"), null, false)));
        assertThat(target1.getState(), is(BEFORE_FIND));

        target1.setProfile(profile.setReplaceWith(replaceWith + "other"));
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        target1.setProfile(profile.setFilename(true));
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
        profile = SearchProfileImpl.getBuilder(toFind).build();
        
        profile = profile.setCharset(StandardCharsets.US_ASCII);
        target1.setProfile(profile);
        assertThat(target1.getState(), is(BEFORE_FIND));
        
        target1.getResult();
        
        profile = profile.setToFind("other" + toFind);
        target1.setProfile(profile);
        assertThat(target1.getState(), is(FIND_OTHER)); 
        
        target1.getResult();
        
        profile = profile.setExclusions(
                new ExclusionsTrie(Arrays.asList("pre"), null, false));
        target1.setProfile(profile);
        assertThat(target1.getState(), is(EXCLUDE_OTHER));

        target1.getResult();
        
        profile = profile.setReplaceWith(replaceWith + "other");
        target1.setProfile(profile);
        assertThat(target1.getState(), is(AFTER_FOUND));
        
        target1.getResult();
        
        target1.setProfile(profile.setFilename(true));
        assertThat(target1.getState(), is(COMPUTED));
    }
    
    @Test
    public void resetProfileInnerCheck() {
        /* check renaming rule changes */
        profile = profile.setReplaceWith("")
                         .setFilename(true);
        target2.getResult();
        
        //cancel renaming
        target2.setProfile(profile.setFilename(false));
        SearchResult result = target2.getResult();
        assertThat(result.getModifiedName().getLast(), is(nullValue()));
        assertThat(result.numberOfModificationsMade(), is(modifications2 - 1));
        
        // repeat
        target2.setProfile(profile.setFilename(false));
        result = target2.getResult();
        assertThat(result.getModifiedName().getLast(), is(nullValue()));
        assertThat(result.numberOfModificationsMade(), is(modifications2 - 1));
        
        // revert back
        target2.setProfile(profile.setFilename(true));
        result = target2.getResult();
        assertThat(result.getModifiedName().getLast(), is(file2Renamed));
        assertThat(result.numberOfModificationsMade(), is(modifications2));
        
        /* exclude filename marker */
        
        profile = profile.setExclusions(
                new ExclusionsTrie(Arrays.asList("2"), null, false));
        target2.setProfile(profile);
        result = target2.getResult();
        assertThat(result.getModifiedName().getLast(), is(nullValue()));
        assertThat(result.numberOfModificationsMade(), is(modifications2 - 1));
        
        /* reset filename after exception */
        
        target2.setFile(nonReadable);
        target2.getResult();
        
        // interrupted state is not canceled by this profile change
        target2.setProfile(profile.setFilename(false));
        result = target2.getResult();
        assertTrue(result.isExceptional());
        
        target2.setProfile(profile.setFilename(true));
        result = target2.getResult();
        assertTrue(result.isExceptional());
    }

    @Test
    public void hasReplacements() throws IOException {
        /* first TestFile content has replacements with default profile set */
        assertTrue(target1.hasReplacements());

        /* change second TestFile so it will have toFind word in filename only */
        List<Tuple<String, String>> modContent = 
                getConstantContent(toFind.substring(0, toFind.length() - 1), "");
        List<String> origContent = getFileContent(modContent);
        Files.write(file2, origContent, TRUNCATE_EXISTING);
        /* filename replacements required */
        assertTrue(target2.hasReplacements());
        
        /* cancel renaming (but replacements are still possible) */
        target2.setProfile(profile.setFilename(false));
        assertThat(target2.getResult().getModifiedName().getLast(), is(nullValue()));
        assertTrue(target2.hasReplacements());
        
        /* return renaming */
        target2.setProfile(profile.setFilename(true).setReplaceWith(""));
        assertThat(target2.getResult().getModifiedName().getLast(), is(file2Renamed));
        assertTrue(target2.hasReplacements());
        
        /* change toFind word so nothing will be found in files */
        profile = profile.setToFind("other" + toFind);
        target1.setProfile(profile);
        assertFalse(target1.hasReplacements());
        target2.setProfile(profile);
        assertFalse(target2.hasReplacements());
        
        /* after exception */
        target1.setFile(nonReadable);
        assertTrue(target1.hasReplacements());
    }
    
    @Test
    public void getResult() throws Throwable {
        
        /* default results */
        
        checkResultExpect(target1.getResult(), modContent1, null, modifications1, false, null);
        
        checkResultExpect(target2.getResult(), modContent2, file2Renamed, modifications2, false, null);
        
        // should be readable
        target1.setFile(readOnly);
        
        /* exceptional results */
        
        target1.setFile(notExisting);
        checkResultExpect(target1.getResult(), null, null, 0, true, NoSuchFileException.class);
        
        target1.setFile(nonReadable);
        checkResultExpect(target1.getResult(), null, null, 0, true, AccessDeniedException.class);
    }
    
    @Test
    public void writeResult() throws Throwable {
        
        /* default results */
        
        // three calls in a row should yield the same result
        checkResultExpect(target1.writeResult(), modContent1, null, modifications1, false, null);
        checkResultExpect(target1.writeResult(), modContent1, null, modifications1, false, null);
        checkResultExpect(target1.writeResult(), modContent1, null, modifications1, false, null);
        readAndCheckContent(file1, target1, true);
        
        // change replaceWith
        profile = profile.setReplaceWith("");
        target1.setProfile(profile);
        checkResultExpect(target1.writeResult(), modContent2, null, modifications1, false, null);

        // first try without renaming
        profile = profile.setFilename(false);
        target2.setProfile(profile);
        checkResultExpect(target2.writeResult(), modContent2, null, modifications2 - 1, false, null);
        checkResultExpect(target2.writeResult(), modContent2, null, modifications2 - 1, false, null);
        readAndCheckContent(file2, target2, true);
        
        // now with rename:
        
        // recreate original file
        Files.deleteIfExists(file2);
        Files.write(file2, origContent2);
        
        // reset state and file renaming rule
        profile = profile.setReplaceWith("1").setFilename(true);
        target2.setProfile(profile);
        // reset replaceWith back to check results 
        profile = profile.setReplaceWith("");
        target2.setProfile(profile);
        checkResultExpect(target2.writeResult(), modContent2, file2Renamed, modifications2, false, null);
        readAndCheckContent(file2Renamed, target2, true);

        // recreate original file
        Files.deleteIfExists(file2);
        Files.write(file2, origContent2);
        
        // create file with expected modified name (to check collisions)
        Files.write(file2Renamed, origContent2);
        
        // renaming rule won't change previous result
        profile = profile.setFilename(true);
        target2.setProfile(profile);
        checkResultExpect(target2.writeResult(), modContent2, file2Renamed, modifications2, false, null);
        
        // reset status (we have original file recreated)
        target2.setFile(file2);
        SearchResult result = target2.writeResult();
        // I don't know what new random name will be created in this case
        newName = result.getModifiedName().getLast();
        checkResultExpect(result, modContent2, newName, modifications2, false, null);
        readAndCheckContent(newName, target2, true);
        // try to change profile 
        target2.setProfile(profile.setToFind("a"));
        target2.writeResult();
        
        /* exceptional results */
        
        target1.setProfile(profile.setCharset(StandardCharsets.UTF_16BE));
        Files.setAttribute(file1, "dos:readonly", true);
        checkResultExpect(target1.writeResult(), null, null, 0, true, AccessDeniedException.class);
        Files.setAttribute(file1, "dos:readonly", false);
        // without exception now
        target1.setProfile(profile.setCharset(charset));
        target1.writeResult();
        
        target1.setFile(notExisting);
        checkResultExpect(target1.writeResult(), null, null, 0, true, NoSuchFileException.class);
        
        target1.setFile(readOnly);
        checkResultExpect(target1.writeResult(), null, null, 0, true, AccessDeniedException.class);
        
        target1.setFile(nonReadable);
        checkResultExpect(target1.writeResult(), null, null, 0, true, AccessDeniedException.class);
    }

    /* correctness tests */

    @Test
    public void correctFullWords() throws IOException {
        toFind = "FindMe";
        replaceWith = "Replaced";
        prepareProfile(target1, toFind, replaceWith);
        
        Tuple<String, String> tuple = new TupleImpl<>("FindMeljdlfFindMeklkFFFindMek;kpoFindMeklklFindMe", 
                                                      "ReplacedljdlfReplacedklkFFReplacedk;kpoReplacedklklReplaced");
        Files.write(file1, Arrays.asList(tuple.getFirst()), TRUNCATE_EXISTING);
        assertThat(target1.getResult().getModifiedContent(), is(Arrays.asList(tuple)));
        
        /* Rescan cached content */
        if (isContentCached()) {
            toFind = "FindMe";
            replaceWith = "";
            prepareProfile(target1, toFind, replaceWith);
            tuple = new TupleImpl<>("FindMeljdlfFindMeklkFFFindMek;kpoFindMeklklFindMe", 
                                    "ljdlfklkFFk;kpoklkl");
            assertThat(target1.getResult().getModifiedContent(), is(Arrays.asList(tuple)));
            
            toFind = "F";
            replaceWith = "Replaced";
            prepareProfile(target1, toFind, replaceWith);
            tuple = new TupleImpl<>("FindMeljdlfFindMeklkFFFindMek;kpoFindMeklklFindMe", 
                                    "ReplacedindMeljdlfReplacedindMeklkReplacedReplaced" +
                                    "ReplacedindMek;kpoReplacedindMeklklReplacedindMe");
            assertThat(target1.getResult().getModifiedContent(), is(Arrays.asList(tuple)));
        }
    }

    @Test
    public void correctFullWordsWithRepeatitions() throws IOException {
        toFind = "FindMe";
        replaceWith = "Replaced";
        prepareProfile(target1, toFind, replaceWith);

        Tuple<String, String> tuple = new TupleImpl<>("FFindMeljdlfFindMeFindMeklkFFFndMek;kpoFindMeFeeklklFindMee", 
                                                      "FReplacedljdlfReplacedReplacedklkFFFndMek;kpoReplacedFeeklklReplacede");
        Files.write(file1, Arrays.asList(tuple.getFirst()), TRUNCATE_EXISTING);
        assertThat(target1.getResult().getModifiedContent(), is(Arrays.asList(tuple)));
    }
    
    @Test
    public void correctEmptyReplace() throws IOException {
        toFind = "FindMe";
        replaceWith = "";
        prepareProfile(target1, toFind, replaceWith);
        Tuple<String, String> tuple = new TupleImpl<>("FindMeljdlfFindMeklkFFFindMek;kpoFindMeklklFindMe", 
                                                      "ljdlfklkFFk;kpoklkl");
        Files.write(file1, Arrays.asList(tuple.getFirst()), TRUNCATE_EXISTING);
        assertThat(target1.getResult().getModifiedContent(), is(Arrays.asList(tuple)));
    }
    
    @Test
    public void correctOneLetterToFind() throws IOException {
        toFind = "F";
        replaceWith = "Replaced";
        prepareProfile(target1, toFind, replaceWith);
        Tuple<String, String> tuple = new TupleImpl<>("FFashjdFjklFFaFaF;lkFFF;l;oFF", 
                                                      "ReplacedReplacedashjdReplacedjklReplacedReplaceda" +
                                                      "ReplacedaReplaced;lkReplacedReplacedReplaced;l;oReplacedReplaced");
        Files.write(file1, Arrays.asList(tuple.getFirst()), TRUNCATE_EXISTING);
        assertThat(target1.getResult().getModifiedContent(), is(Arrays.asList(tuple)));
    }
    
    @Test
    public void getRandomResult() throws IOException {
        List<Tuple<String, String>> randomContent;
        List<String> fileContent;
        String[] exclude = prepareProfile(target1, toFind, replaceWith);;
        
        int T = 100;
        while (T-- > 0) {
            randomContent = getRandomContent(exclude, toFind, replaceWith);
            fileContent = getFileContent(randomContent);
            
            //printContent(randomContent);
            
            Files.write(file1, fileContent, TRUNCATE_EXISTING);
            
            // remove cached result
            if (isContentCached()) target1.setFile(file1);
            
            assertThat(target1.getResult().getModifiedContent(), is(randomContent));
        }
        
    }

    /* After and AfterClass cleanups */
    
    @After
    public void reset() throws IOException {
        Files.setAttribute(file1, "dos:readonly", false);
        Files.setAttribute(file1, "dos:hidden", false);
        Files.setAttribute(file1, "dos:system", false);
    }
    
    @AfterClass
    public static void deleteFiles() throws IOException {
        Files.deleteIfExists(file1);
        Files.deleteIfExists(file2);
        Files.deleteIfExists(file2Renamed);
        Files.deleteIfExists(newName);
    }

    /* Helper methods */
    
    @SuppressWarnings("unused")
    private void printContent(List<Tuple<String, String>> content) {
        content.stream()
               .map(tuple -> tuple.getFirst() + "\n" + tuple.getLast() + "\n")
               .forEach(System.out::println);;
    }

    private String[] prepareProfile(FileReplacer target, String toFind, String replaceWith) {
        String[] exclude = new String[]{"somePrefix" + toFind, 
                                        "otherPrefix" + toFind,
                                        "  !!!  " + toFind,
                                        toFind + "someSuffix",
                                        toFind + "otherSuffix",
                                        toFind + "  ???  ", 
                                        "somePrefix" + toFind + "someSuffix", 
                                        "otherPrefix" + toFind + "otherSuffix"};

        profile = profile.setToFind(toFind)
                         .setReplaceWith(replaceWith)
                         .setExclusions(new ExclusionsTrie(
                                 new TreeSet<>(Arrays.asList(exclude)), toFind, true));
        target.setProfile(profile);
        return exclude;
    }
    
    private void readAndCheckContent(Path file, FileReplacer replacer, boolean equals) throws IOException {
        List<String> resultLines = replacer.getResult()
                        .getModifiedContent()
                        .stream()
                        .map(tuple -> tuple.getLast() != null ? tuple.getLast() : tuple.getFirst())
                        .collect(Collectors.toList());
        List<String> fileLines = Files.readAllLines(file, profile.getCharset());
        if (equals)
            assertThat(fileLines, is(resultLines));
        else 
            assertThat(fileLines, is(not(resultLines)));
    }
    
    private void checkResultExpect(SearchResult result, 
                                   List<Tuple<String, String>> modContent, 
                                   Path modName, 
                                   int modifications, 
                                   boolean exceptional, 
                                   Class<? extends Throwable> cause) throws Throwable {
        if (result.isExceptional()) { 
            assertTrue("unexpected exception " + result.getCause(), exceptional);
            assertThat(result.getModifiedName(), is(nullValue()));
            assertTrue(result.getCause().getClass() == cause);
        } else {
            assertFalse("missing exception", exceptional);
            assertThat(result.getModifiedName().getLast(), is(modName));
            assertThat(result.getCause(), is(nullValue()));
        }
        assertThat(result.getModifiedContent(), is(modContent));
        assertThat(result.numberOfModificationsMade(), is(modifications));
        assertTrue(result.isExceptional() == exceptional);
    }

    private static List<String> getFileContent(List<Tuple<String, String>> content) {
        return content.stream()
                      .map(tuple -> tuple.getFirst())
                      .collect(toList());
    }

    private static List<Tuple<String, String>> getConstantContent(String toFind, String replaceWith) {
        return Arrays.asList(new TupleImpl<>("some text without *that* word", null),
                             new TupleImpl<>("some text without *that* word", null),
                             new TupleImpl<>("some text with " + toFind + " word",
                                             "some text with " + replaceWith + " word"),
                             new TupleImpl<>("another text that contains at the end " + toFind,
                                             "another text that contains at the end " + replaceWith),
                             new TupleImpl<>("some text without *that* word", null),
                             new TupleImpl<>(toFind + " at the beginning and " + toFind + " again", 
                                             replaceWith + " at the beginning and " + replaceWith + " again"),
                             new TupleImpl<>("some text without *that* word", null));
    }
    
    private static List<Tuple<String, String>> getRandomContent(String[] notReplace, 
                                                               String toFind, String replaceWith) {
        List<Tuple<String, String>> content = new ArrayList<>();
        int lines = 10, lineLength = 10, charBound = 100, charShift = 50;
        Random rand = new Random();
        StringBuilder origLine = new StringBuilder(lineLength);
        StringBuilder modLine = new StringBuilder(lineLength);
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
                    appendBoth(origLine, modLine, String.valueOf((char) (rand.nextInt(charBound) + charShift)));
            }
            content.add(new TupleImpl<>(origLine.toString(), replaced ? modLine.toString() : null));
            origLine = new StringBuilder(lineLength);
            modLine = new StringBuilder(lineLength);
            replaced = false;
        }
        
        return content;
    }

    private static void appendBoth(StringBuilder origLine,
                                   StringBuilder modLine, 
                                   String s) {
        origLine.append(s);
        modLine.append(s);
    }
    
}
