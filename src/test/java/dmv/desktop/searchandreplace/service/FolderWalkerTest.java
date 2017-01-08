package dmv.desktop.searchandreplace.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.junit.*;

import dmv.desktop.searchandreplace.model.*;


public class FolderWalkerTest {

    private static final int UNITS = Runtime.getRuntime().availableProcessors() * 3;
    private static final ExecutorService EXECS_POOL = Executors.newFixedThreadPool(UNITS);
    private static final ExecutorService SINGLE_EXEC = Executors.newSingleThreadExecutor();
    private static final String NULL = null;

    private static String dirName;
    private static String subDirName;
    private static Charset charset;
    
    private static String[] fileTypes = {"txt", "bin"};
    private static String[] includePaths = {"*.txt", "*.bin"};
    private static String[] prefixes = {"do not", "It won't"};
    private static String[] suffixes = {"leave this", "Stay"};
    private static String[] withPrefixes;
    private static String[] withSuffixes;
    private static String[] withPrefixesAndSuffixes;
    private static int pfxSize;
    private static int sfxSize;
    private static Exclusions excludeAll;
    private static Exclusions excludePfx;
    private static Exclusions excludeSfx;
    
    private static String toFind;
    private static String replaceWith;
    
    private List<String> fileContent;
    // L - lines per file; LL - line's length before toFind addition
    private int L = 10, LL = 10;
    private Random rand = new Random();
    
    private SearchAndReplace<SearchFolder, SearchProfile, FileSearchResult> target;
    private SearchFolder rootFolder;
    private SearchProfile profile;
    
    @BeforeClass
    public static void prepare() {
        dirName = "src/test/resources/testfolder/";
        subDirName = dirName + "subfolder/";
        charset = StandardCharsets.UTF_16;
        pfxSize = prefixes.length;
        sfxSize = suffixes.length;
        
        toFind = "Find me";
        replaceWith = "It's Replaced";
        excludeAll = new ExclusionsTrie(Arrays.asList(prefixes), 
                                        Arrays.asList(suffixes), true);
        excludePfx = new ExclusionsTrie(Arrays.asList(prefixes), 
                                        Collections.emptyList(), true);
        excludeSfx = new ExclusionsTrie(Collections.emptyList(), 
                                        Arrays.asList(suffixes), true);
        withPrefixes = Stream.of(prefixes)
                             .map(pfx -> pfx + toFind)
                             .toArray(size -> new String[size]);
        withSuffixes = Stream.of(suffixes)
                             .map(sfx -> toFind + sfx)
                             .toArray(size -> new String[size]);
        int length = Math.min(pfxSize, sfxSize);
        withPrefixesAndSuffixes = new String[length];
        for (int i = 0; i < length; i++) 
            withPrefixesAndSuffixes[i] = prefixes[i] + toFind + suffixes[i];
    }

    @Before
    public void setUp() throws Exception {
        writeTestFiles(dirName, UNITS * 2);
        writeTestFiles(subDirName, UNITS);
    }

    @Test
    public void multiFull() {
        target = createTarget(replaceWith, excludeAll, true, true);
        target.preview(EXECS_POOL)
              .forEach(result -> testResult(result, replaceWith, excludeAll, true, true));
    }

    private Object testResult(FileSearchResult result, 
                              String replaceWith,
                              Exclusions exclusions, 
                              boolean subfolders, 
                              boolean filenames) {
        System.out.println(result);
        String origName = result.getModifiedName().getFirst().toString();
        if (!subfolders)
            assertFalse(origName.contains(subDirName));
        if (filenames) {
            if (!origName.contains(toFind)) 
                assertThat(result.getModifiedName().getLast(), is(NULL));
            
        }
        return null;
    }

    @After
    public void removeFiles() throws IOException {
        Files.walk(rootFolder.getFolder())
             .filter(Files::isRegularFile)
             .forEach(t -> {
                try {
                    Files.deleteIfExists(t);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }
    
    @AfterClass
    public static void shutdown() throws InterruptedException {
        SINGLE_EXEC.shutdown();
        EXECS_POOL.shutdown();
    }

    private void prepareFileContent() {
        fileContent = new ArrayList<>();
        StringBuilder line = new StringBuilder(LL);
        for (int l = 0; l < L; l++) {
            for (int i = 0; i < LL; i++) {
                if (rand.nextInt(100) > 95) {
                    if (rand.nextInt(100) > 70)
                        line.append(prefixes[rand.nextInt(pfxSize)]);
                    line.append(toFind);
                    if (rand.nextInt(100) > 70)
                        line.append(suffixes[rand.nextInt(sfxSize)]);
                }
                line.append((char) rand.nextInt(Character.MAX_RADIX));
            }
            fileContent.add(line.toString());
            line = new StringBuilder(LL);
        }
    }

    private void writeTestFiles(String dir, int number) throws IOException {
        String name = "test";
        String finder = "";
        String otherExt = ".other";
        for (int i = 0; i < number; i++) {
            finder = name + i;
            if (rand.nextInt(100) > 70) {
                if (rand.nextInt(100) > 90)
                    finder += prefixes[rand.nextInt(pfxSize)];
                finder += toFind;
                if (rand.nextInt(100) > 90)
                    finder += suffixes[rand.nextInt(sfxSize)];
            }
            int ext = rand.nextInt(100);
            if (ext < 33)
                finder += "." + fileTypes[0];
            else if (ext > 66)
                finder += "." + fileTypes[1];
            else
                finder += otherExt;
            
            prepareFileContent();
            writeFile(Paths.get(dir + finder));
        }
    }
    
    private void writeFile(Path file) throws IOException {
        Files.createFile(file);
        Files.write(file, fileContent, charset);
    }

    private FolderWalker createTarget(String replaceWith, Exclusions exclusions,
                                      boolean subfolders, boolean filenames) {
        rootFolder = new SearchFolderImpl(Paths.get(dirName));
        rootFolder.setCharset(charset)
                  .setFileTypes(includePaths)
                  .setSubfolders(subfolders);
        
        profile = new SearchProfileImpl(toFind);
        profile.setReplaceWith(replaceWith)
               .setCharset(charset)
               .setExclusions(exclusions)
               .setFilename(filenames);

        return new FolderWalker(rootFolder, profile);
    }
}
