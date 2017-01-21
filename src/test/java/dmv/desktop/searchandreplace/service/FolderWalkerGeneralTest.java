package dmv.desktop.searchandreplace.service;

import static dmv.desktop.searchandreplace.model.SearchProfile.defaultRenameRule;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.AFTER_FOUND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.BEFORE_FIND;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.COMPUTED;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.INTERRUPTED;
import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.REPLACED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.junit.*;

import dmv.desktop.searchandreplace.exception.AccessResourceException;
import dmv.desktop.searchandreplace.exception.NothingToReplaceException;
import dmv.desktop.searchandreplace.model.*;
import dmv.desktop.searchandreplace.service.SearchAndReplace.State;

public class FolderWalkerGeneralTest extends FolderWalkerTest {
    
    private static String s1;
    private static String s11;
    private static String s2;
    private static Path subfolder1;
    private static Path subfolder11;
    private static Path subfolder2;
    private static int subfoldersNumber;
    private static int filesNumber;
    private static Set<String> folderNames;
    private static Set<String> fileNames;
    private static int filesCounter;
    
    @BeforeClass
    public static void init() {
        testRootFolder = Paths.get("src/test/resources");
        testFolder = testRootFolder.resolve("testfolder1");
        s1 = "subfolder1";
        s11 = "subfolder11";
        s2 = "subfolder2";
        subfolder1 = testFolder.resolve(s1);
        subfolder11 = subfolder1.resolve(s11);
        subfolder2 = testFolder.resolve(s2);
        charset = StandardCharsets.UTF_8;
        otherCharset = StandardCharsets.UTF_16;
        toFind = "Find me";
        replaceWith = "It's replaced";
        excludeAll = new ExclusionsTrie(Arrays.asList(prefixes), 
                                        Arrays.asList(suffixes), true);
    }

    @Before
    public void setUp() throws Exception {
        filesNumber = 3;
        subfoldersNumber = 3;
        removeFiles();
        writeTestFiles(testFolder, filesNumber);
        writeTestFiles(subfolder1, filesNumber);
        writeTestFiles(subfolder11, filesNumber);
        writeTestFiles(subfolder2, filesNumber);
        resetTarget();
        resetCounters();
    }

    private void resetTarget() {
        includePaths = new String[]{"**.txt", "**.bin"};
        rootFolder = SearchPathImpl.getBuilder(testFolder)
                                   .setNamePattern(includePaths)
                                   .build();
        profile = SearchProfileImpl.getBuilder(toFind)
                                   .setReplaceWith(replaceWith)
                                   .setCharset(charset)
                                   .build();
        target = new FolderWalker(rootFolder, profile);
    }

    private void resetCounters() {
        includePaths = new String[]{"**.txt", "**.bin"};
        rootFolder = rootFolder.setNamePattern(includePaths);
        target.setRootElement(rootFolder);
        folderNames = new HashSet<>();
        fileNames = new HashSet<>();
        filesCounter = 0;
    }
    
    @Test
    public void stateChanges() throws IOException {
        /* check initial state */
        assertThat(target.getRootElement(), is(rootFolder));
        assertThat(target.getProfile(), is(profile));
        assertThat(target.getState(), is(BEFORE_FIND));
        
        /* state should remain the same */
        SearchPath tempRootFolder = rootFolder.setSubfolders(true);
        target.setRootElement(tempRootFolder);
        assertThat(target.getRootElement(), is(tempRootFolder));
        assertThat(target.getState(), is(BEFORE_FIND));
        target.setRootElement(rootFolder);
        
        SearchProfile tempProfile = profile.setExclusions(excludeAll);
        target.setProfile(tempProfile);
        assertThat(target.getProfile(), is(tempProfile));
        assertThat(target.getState(), is(BEFORE_FIND));
        target.setProfile(tempProfile = profile.setReplaceWith(replaceWith + "other"));
        assertThat(target.getProfile(), is(tempProfile));
        assertThat(target.getState(), is(BEFORE_FIND));
        target.setProfile(tempProfile = profile.setCharset(otherCharset));
        assertThat(target.getProfile(), is(tempProfile));
        assertThat(target.getState(), is(BEFORE_FIND));
        target.setProfile(tempProfile = profile.setFilename(!defaultRenameRule));
        assertThat(target.getProfile(), is(tempProfile));
        assertThat(target.getState(), is(BEFORE_FIND));
        target.setProfile(tempProfile = profile.setToFind(toFind + "other"));
        assertThat(target.getProfile(), is(tempProfile));
        assertThat(target.getState(), is(BEFORE_FIND));
        target.setProfile(profile.setToFind(toFind));

        /* check state reset after advancement */
        advanceToComputed();
        
        target.setRootElement(tempRootFolder);
        assertThat(target.getState(), is(BEFORE_FIND));
        target.setRootElement(rootFolder);
        
        advanceToComputed();

        target.setProfile(profile.setToFind(toFind + "other"));
        assertThat(target.getState(), is(BEFORE_FIND));
        target.setProfile(profile);

        advanceToComputed();

        target.setProfile(profile.setCharset(otherCharset));
        assertThat(target.getState(), is(BEFORE_FIND));
        target.setProfile(profile);

        advanceToComputed();
        
        target.setProfile(profile.setFilename(!defaultRenameRule));
        assertThat(target.getState(), is(AFTER_FOUND));
        target.setProfile(profile);
        
        advanceToComputed();
        
        target.setProfile(profile.setReplaceWith(replaceWith + "other"));
        assertThat(target.getState(), is(AFTER_FOUND));
        target.setProfile(profile);
        
        advanceToComputed();
        
        target.setProfile(profile.setExclusions(excludeAll));
        assertThat(target.getState(), is(AFTER_FOUND));
        target.setProfile(profile);
        
        advanceToComputed();
        
        advanceToReplaced();
        
        // replace back
        tempProfile = profile.setToFind(replaceWith)
                             .setReplaceWith(toFind);
        target.setProfile(tempProfile);
        
        advanceToComputed();
        
        // replace with other 
        target.setProfile(tempProfile.setReplaceWith(""));

        advanceToReplaced();
    }

    private void advanceToReplaced() {
        assertThat(target.replace().size(), is(filesNumber));
        assertThat(target.getState(), is(REPLACED));
    }

    private void advanceToComputed() {
        assertThat(target.preview().size(), is(filesNumber));
        assertThat(target.getState(), is(COMPUTED));
    }
    
    /* exceptional tests */
    
    @Test
    public void subfoldersEnabled() {
        target = new FolderWalker(rootFolder = rootFolder.setSubfolders(true), profile);
        target.preview()
              .forEach(result -> checkResult(result, rootFolder, profile));
        assertThat(folderNames.size(), is(subfoldersNumber));
        assertThat(fileNames.size(), is(filesNumber));
        assertThat(filesCounter, is(filesNumber * (subfoldersNumber + 1)));
        
        resetCounters();
        
        includePaths = new String[]{"**test?.*"};
        rootFolder = rootFolder.setNamePattern(includePaths);
        
        assertTrue(rootFolder.getNamePattern().matches(subfolder11.resolve("test1.txt")));
    
        target = new FolderWalker(rootFolder, profile);
        target.preview()
              .forEach(result -> checkResult(result, rootFolder, profile));
        assertThat(folderNames.size(), is(subfoldersNumber));
        assertThat(fileNames.size(), is(filesNumber));
        assertThat(filesCounter, is(filesNumber * (subfoldersNumber + 1)));
        
        resetCounters();
        
        includePaths = new String[]{"**"+s11+"/test?.*"};
        rootFolder = rootFolder.setNamePattern(includePaths);
        
        assertTrue(rootFolder.getNamePattern().matches(subfolder11.resolve("test1.txt")));
    
        target = new FolderWalker(rootFolder, profile);
        target.preview()
              .forEach(result -> checkResult(result, rootFolder, profile));
        assertThat(folderNames.size(), is(2));
        assertThat(fileNames.size(), is(filesNumber));
        assertThat(filesCounter, is(filesNumber));
        
        resetCounters();
        
        includePaths = new String[]{"**"+s1+"**test?.*"};
        rootFolder = rootFolder.setNamePattern(includePaths);
        
        assertTrue(rootFolder.getNamePattern().matches(subfolder11.resolve("test1.txt")));
    
        target = new FolderWalker(rootFolder, profile);
        target.preview()
              .forEach(result -> checkResult(result, rootFolder, profile));
        assertThat(folderNames.size(), is(2));
        assertThat(fileNames.size(), is(filesNumber));
        assertThat(filesCounter, is(filesNumber * 2));
    }

    @Test
    public void subfoldersEnabledTXT() {
        includePaths = new String[]{"**test?.txt"};
        rootFolder = rootFolder.setNamePattern(includePaths)
                               .setSubfolders(true);
        target = new FolderWalker(rootFolder, profile);
        target.preview()
              .forEach(result -> checkResult(result, rootFolder, profile));
        assertThat(folderNames.size(), is(subfoldersNumber));
        assertThat(fileNames.size(), is(filesNumber - 1));
        assertThat(filesCounter, is((filesNumber - 1) * (subfoldersNumber + 1)));
        
        resetCounters();
        
        includePaths = new String[]{"**test1.txt"};
        rootFolder = rootFolder.setNamePattern(includePaths);
        target = new FolderWalker(rootFolder, profile);
        target.preview()
              .forEach(result -> checkResult(result, rootFolder, profile));
        assertThat(folderNames.size(), is(subfoldersNumber));
        assertThat(fileNames.size(), is(filesNumber - 2));
        assertThat(filesCounter, is((filesNumber - 2) * (subfoldersNumber + 1)));
        
        resetCounters();
        
        includePaths = new String[]{"**"+s1+"**test?.txt"};
        rootFolder = rootFolder.setNamePattern(includePaths);
    
        assertTrue(rootFolder.getNamePattern().matches(subfolder1.resolve("test1.txt")));
        assertTrue(rootFolder.getNamePattern().matches(subfolder11.resolve("test2.txt")));
    
        target = new FolderWalker(rootFolder, profile);
        target.preview()
              .forEach(result -> checkResult(result, rootFolder, profile));
        assertThat(folderNames.size(), is(2));
        assertThat(fileNames.size(), is(filesNumber - 1));
        assertThat(filesCounter, is((filesNumber - 1) * 2));
    }

    @Test
    public void subfoldersEnabledTXTorBIN() {
        includePaths = new String[]{"**test?.bin"};
        rootFolder = rootFolder.setNamePattern(includePaths)
                               .setSubfolders(true);
        target = new FolderWalker(rootFolder, profile);
        target.preview()
              .forEach(result -> checkResult(result, rootFolder, profile));
        assertThat(folderNames.size(), is(subfoldersNumber));
        assertThat(fileNames.size(), is(filesNumber - 2));
        assertThat(filesCounter, is((filesNumber - 2) * (subfoldersNumber + 1)));
        
        resetCounters();
        
        includePaths = new String[]{"**test2.*"};
        rootFolder = rootFolder.setNamePattern(includePaths);
        target = new FolderWalker(rootFolder, profile);
        target.preview()
              .forEach(result -> checkResult(result, rootFolder, profile));
        assertThat(folderNames.size(), is(subfoldersNumber));
        assertThat(fileNames.size(), is(filesNumber - 1));
        assertThat(filesCounter, is((filesNumber - 1) * (subfoldersNumber + 1)));
    }

    @Test
    public void subfoldersDisabled() {
        target = new FolderWalker(rootFolder = rootFolder.setSubfolders(false), profile);
        target.preview()
              .forEach(result -> checkResult(result, rootFolder, profile));
        assertThat(folderNames.size(), is(0));
        assertThat(filesCounter, is(filesNumber));
        
        resetCounters();
        
        includePaths = new String[]{"**test?.*"};
        rootFolder = rootFolder.setNamePattern(includePaths);
        target = new FolderWalker(rootFolder, profile);
        target.preview()
              .forEach(result -> checkResult(result, rootFolder, profile));
        assertThat(folderNames.size(), is(0));
        assertThat(filesCounter, is(filesNumber));
    }

    @Test
    public void nothingToReplace() {
        target.setProfile(profile.setToFind(toFind + "other"));
        checkPreviewException(NothingToReplaceException.class);
        
        target.setProfile(profile.setToFind(toFind + "!!"));
        checkReplaceException(NothingToReplaceException.class);
    }
    
    @Test(expected=IllegalStateException.class)
    public void walkWhenInterrupted() {
        target.setRootElement(rootFolder.setPath(Paths.get("res")));
        // go into INTERRUPTED state
        checkReplaceException(AccessResourceException.class);
        // try to do something
        checkReplaceException(IllegalStateException.class);
        checkPreviewException(IllegalStateException.class);
        
        // reset to BEFORE_FIND
        target.setProfile(profile.setToFind(toFind + "other"));
        // will just reset exception
        checkPreviewException(AccessResourceException.class);
        checkPreviewException(IllegalStateException.class);
        checkReplaceException(IllegalStateException.class);
        
        // reset to AFTER_FOUND is insufficient to clear INTERRUPTED
        // state, so it will throw IllegalStateException exception 
        target.setProfile(profile.setToFind(toFind + "other")
                                 .setReplaceWith(""));
    }
    
    @Test
    public void walkWhenReplaced() {
        target.replace();
        assertThat(target.getState(), is(REPLACED));
        
        checkReplaceException(IllegalStateException.class);
        
        target.setProfile(profile.setToFind(replaceWith)
                                 .setReplaceWith(""));
        target.replace();
        checkPreviewException(IllegalStateException.class);
    }

    private void checkReplaceException(Class<? extends Throwable> expectedException) {
        State expectedState = INTERRUPTED;
        try {
            target.replace();
            fail(expectedException.getSimpleName() + " expected");
        } catch (Exception e) {
            assertTrue("\nexpected exception is " + 
                       expectedException.getSimpleName() +
                       "\nbut was " + e.getClass().getSimpleName(), 
                       e.getClass() == expectedException);
            assertThat("expected state is " + expectedState +
                       "\nbut was " + target.getState(), 
                       target.getState(), is(expectedState));
        }
    }
    
    private void checkPreviewException(Class<? extends Throwable> expectedException) {
        State expectedState = INTERRUPTED;
        try {
            target.preview();
            fail(expectedException.getSimpleName() + " expected");
        } catch (Exception e) {
            assertTrue("\nexpected exception is " + 
                        expectedException.getSimpleName() +
                        "\nbut was " + e.getClass().getSimpleName(), 
                        e.getClass() == expectedException);
             assertThat("expected state is " + expectedState +
                        "\nbut was " + target.getState(), 
                        target.getState(), is(expectedState));
        }
    }

    @Test
    public void resultException() {
        includePaths = new String[]{"**readOnly.txt"};
        Path path = testRootFolder.resolve("replacertest");
        target.setRootElement(rootFolder.setPath(path)
                                        .setNamePattern(includePaths));
        target.setProfile(profile.setToFind("FindMe"));
        
        List<SearchResult> result = target.preview();
        //printStream(result.stream());
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getModifiedContent(), is(not(nullValue())));
        assertFalse(result.get(0).isExceptional());
        
        result = target.replace();
        //printStream(result.stream());
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getModifiedContent(), is(nullValue()));
        assertTrue(result.get(0).isExceptional());
        
        includePaths = new String[]{"**nonReadable.txt"};
        target.setRootElement(rootFolder.setPath(path)
                                        .setNamePattern(includePaths));
        result = target.preview();
        //printStream(result.stream());
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getModifiedContent(), is(nullValue()));
        assertTrue(result.get(0).isExceptional());
    }
    
    @Test(expected=NullPointerException.class)
    public void nullConstructorRootFolder() {
        target = new FolderWalker(null, profile);
    }
    
    @Test(expected=NullPointerException.class)
    public void nullSetRootFolder() {
        target.setRootElement(null);
    }
    
    @Test(expected=NullPointerException.class)
    public void nullConstructorProfile() {
        target = new FolderWalker(rootFolder, null);
    }
    
    @Test(expected=NullPointerException.class)
    public void nullSetProfile() {
        target.setProfile(null);
    }
    
    @Test(expected=NullPointerException.class)
    public void nullPreviewExec() {
        target.preview(null);
    }
    
    @Test(expected=NullPointerException.class)
    public void nullReplaceExec() {
        target.replace(null);
    }
    
    @AfterClass
    public static void cleanUp() throws Exception {
        removeFiles();
    }

    @Override
    protected List<Path> createPaths(Path dir, int number) {
        assert(number == 3) : "these tests depend on number 3";
        /* strictly three files per folder */
        return Arrays.asList(dir.resolve("test1.txt"), 
                             dir.resolve("test2.txt"), 
                             dir.resolve("test2.bin"));
    }

    @Override
    protected PreparedContent prepareFile(Path file) {
        /* all files must be found */
        List<String> fileContent = Arrays.asList(
                "some text content with " + toFind + " word",
                "some text content with " + toFind + " word");
        return new PreparedContent(file, fileContent);
    }

    private void checkResult(SearchResult result, 
                             SearchPath rootFolder,
                             SearchProfile profile) {
        Path path = result.getModifiedName().getFirst();
        addSubfolder(path, s1);
        addSubfolder(path, s11);
        addSubfolder(path, s2);
        fileNames.add(path.getFileName().toString());
        filesCounter++;
    }
    
    private void addSubfolder(Path path, String subfolder) {
        if (path.toString().contains(subfolder)) folderNames.add(subfolder);
    }
}
