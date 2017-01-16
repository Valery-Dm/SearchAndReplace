package dmv.desktop.searchandreplace.service;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.junit.*;

import dmv.desktop.searchandreplace.model.*;


public class FolderWalkerTest {

    private static final int UNITS = Runtime.getRuntime().availableProcessors() * 3;
    private static final ExecutorService EXECS_POOL = Executors.newFixedThreadPool(UNITS);
    private static final ExecutorService SINGLE_EXEC = Executors.newSingleThreadExecutor();

    private static Path testRootFolder;
    private static Path testFolder;
    private static Path testSubFolder;
    private static String dirName;
    private static String subDirName;
    private static Charset charset;
    
    private static String[] fileTypes = {"txt", "bin"};
    private static String[] includePaths = {"*.txt", "*.bin"};
    private static String[] prefixes = {"do not", "It won't"};
    private static String[] suffixes = {"leave this", "Stay"};
    private static int pfxSize;
    private static int sfxSize;
    private static Exclusions excludeAll;
    private static Exclusions excludePfx;
    private static Exclusions excludeSfx;
    
    private static String toFind;
    private static String replaceWith;
    
    // L - lines per file; LL - line's length before toFind addition
    private int L = 30, LL = 200;
    private Random rand = new Random();
    
    private SearchAndReplace<SearchPath, SearchProfile, SearchResult> target;
    private static SearchPath rootFolder;
    private SearchProfile profile;
    
    @BeforeClass
    public static void prepare() {
        dirName = "testfolder";
        subDirName = "subfolder";
        testRootFolder = Paths.get("src/test/resources");
        testFolder = testRootFolder.resolve(dirName);
        testSubFolder = testFolder.resolve(subDirName);
        charset = StandardCharsets.UTF_16;
        pfxSize = prefixes.length;
        sfxSize = suffixes.length;
        
        toFind = "Find me";
        replaceWith = "It's replaced";
        excludeAll = new ExclusionsTrie(Arrays.asList(prefixes), 
                                        Arrays.asList(suffixes), true);
        excludePfx = new ExclusionsTrie(Arrays.asList(prefixes), 
                                        Collections.emptyList(), true);
        excludeSfx = new ExclusionsTrie(Collections.emptyList(), 
                                        Arrays.asList(suffixes), true);
    }

    @Before
    public void setUp() throws Exception {
        removeFiles();
        writeTestFiles(testFolder, UNITS * 2);
        writeTestFiles(testSubFolder, UNITS / 2);
    }
    
    @Test
    public void setGet() {
        rootFolder = new SearchPathImpl(testFolder)
                .setCharset(charset)
                .setNamePattern(includePaths);
        profile = new SearchProfileImpl(toFind)
                .setReplaceWith(replaceWith)
                .setCharset(charset);

        target = new FolderWalker(rootFolder, profile);
        assertThat(target.getRootElement(), is(rootFolder));
        assertThat(target.getProfile(), is(profile));
        
        target.setRootElement(rootFolder.setPath(Paths.get("res")));
        assertThat(target.getRootElement(), is(rootFolder));
        
        target.setProfile(profile.setCharset(StandardCharsets.ISO_8859_1));
        assertThat(target.getProfile(), is(profile));
    }
    
    @Test(expected=NullPointerException.class)
    public void nullConstructorRootFolder() {
        profile = new SearchProfileImpl(toFind)
                            .setReplaceWith(replaceWith)
                            .setCharset(charset);

        target = new FolderWalker(null, profile);
    }
    
    @Test(expected=NullPointerException.class)
    public void nullSetRootFolder() {
        target = createTarget(replaceWith, excludeAll, true, true);
        target.setRootElement(null);
    }
    
    @Test(expected=NullPointerException.class)
    public void nullConstructorProfile() {
        rootFolder = new SearchPathImpl(testFolder)
                            .setCharset(charset)
                            .setNamePattern(includePaths);

        target = new FolderWalker(rootFolder, null);
    }
    
    @Test(expected=NullPointerException.class)
    public void nullSetProfile() {
        target = createTarget(replaceWith, excludeAll, true, true);
        target.setProfile(null);
    }

    //@Ignore
    @Test
    public void excludeAll() {
//        int T = 300;
//        while (T-- > 0) {
            target = createTarget(replaceWith, excludeAll, true, true);
            target.preview(EXECS_POOL)
                  .forEach(result -> testResult(result, replaceWith, excludeAll, true, true));
//        }
        target.replace(EXECS_POOL)
              .forEach(result -> testResult(result, replaceWith, excludeAll, true, true));
    }
    
    //@Ignore
    @Test
    public void excludePrefixes() {
        target = createTarget(replaceWith, excludePfx, false, true);
        target.preview(SINGLE_EXEC)
              .forEach(result -> testResult(result, replaceWith, excludePfx, false, true));
    }

    //@Ignore
    @Test
    public void excludeSuffixes() {
        target = createTarget(replaceWith, excludeSfx, false, false);
        target.preview()
              .forEach(result -> testResult(result, replaceWith, excludeSfx, false, false));
        target.replace()
              .forEach(result -> testResult(result, replaceWith, excludeSfx, false, false));
    }

    @AfterClass
    public static void shutdown() throws InterruptedException, IOException {
        SINGLE_EXEC.shutdown();
        EXECS_POOL.shutdown();
        removeFiles();
    }

    private void testResult(SearchResult result, 
                            String replaceWith,
                            Exclusions exclusions, 
                            boolean subfolders, 
                            boolean filenames) {
        //print(result);
        checkNumberOfModifications(result);
        
        String origName = result.getModifiedName().getFirst().toString();
        Path modifiedName = result.getModifiedName().getLast();
        if (!subfolders)
            assertFalse("subfolders must be excluded", origName.contains(subDirName));
        
        if (filenames) {
            if (isFound(origName, exclusions)) 
                assertThat("file renaming should be done " + origName, 
                           modifiedName, is(notNullValue()));
            else
                assertThat("file should not be renamed " + origName, 
                           modifiedName, is(nullValue()));
        } else {
            if (isFound(origName, exclusions)) 
                assertThat("renaming is not allowed " + origName, 
                           modifiedName, is(nullValue()));
        }
        
        assertFalse("inconsistent result " + origName, 
                    result.getModifiedContent()
                          .stream()
                          .anyMatch(tuple -> {
                              if (isFound(tuple.getFirst(), exclusions))
                                  return tuple.getLast() == null;
                              return tuple.getLast() != null;
                          }));
    }
    
    private void checkNumberOfModifications(SearchResult result) {
        if (replaceWith.length() == 0) return;
        int realModifications = 0;
        if (result.getModifiedName().getLast() != null)
            realModifications++;
        List<String> collect = result.getModifiedContent()
              .stream()
              .filter(tuple -> tuple.getLast() != null)
              .map(tuple -> tuple.getLast())
              .collect(Collectors.toList());
        
        for (String line : collect) {
            int idx = line.indexOf(replaceWith);
            while (idx >= 0) {
                realModifications++;
                idx = line.indexOf(replaceWith, ++idx);
            }
        }
        
        assertThat(result.getModifiedName().getFirst().toString(), 
                   realModifications, is(result.numberOfModificationsMade()));
    }

    private boolean isFound(String line, Exclusions exclusions) {
        // at least one toFind excluding exclusions
        int idx = line.indexOf(toFind);
        int maxPrefixSize = exclusions.maxPrefixSize();
        int maxSuffixSize = exclusions.maxSuffixSize();
        while (idx >= 0) {
            int start = idx - maxPrefixSize;
            start = start < 0 ? 0 : start;
            if (!exclusions.containsAnyPrefixes(line.substring(start, idx), true)) {
                start = idx + toFind.length();
                int end = start + maxSuffixSize;
                end = end > line.length() ? line.length() : end;
                if (!exclusions.containsAnySuffixes(line.substring(start, end)))
                    return true;
            }
            idx = line.indexOf(toFind, ++idx);
        }
        return false;
    }
    
    @SuppressWarnings("unused")
    private void print(SearchResult result) {
        StringBuilder print = new StringBuilder(result.getModifiedName().getFirst().toString());
        if (result.isExceptional())
            print.append(result.getCause());
        else {
            print.append("\nmodifications done: ")
                 .append(result.numberOfModificationsMade());
            if (result.getModifiedName().getLast() != null) 
                print.append("\nnew name is: ")
                     .append(result.getModifiedName().getLast().toString());
            else
                print.append("\nname was not modified");
            result.getModifiedContent()
                  .stream()
                  .filter(tuple -> tuple.getLast() != null)
                  .forEach(tuple -> {
                      print.append("\norginal:  ")
                           .append(tuple.getFirst())
                           .append("\nmodified: ")
                           .append(tuple.getLast());
                  });
        }
        System.out.println(print);
        System.out.println();
    }

    private FolderWalker createTarget(String replaceWith, Exclusions exclusions,
                                      boolean subfolders, boolean filenames) {
        rootFolder = new SearchPathImpl(testFolder)
                           .setCharset(charset)
                           .setNamePattern(includePaths)
                           .setSubfolders(subfolders);
        
        profile = new SearchProfileImpl(toFind)
                           .setReplaceWith(replaceWith)
                           .setCharset(charset)
                           .setExclusions(exclusions)
                           .setFilename(filenames);
    
        return new FolderWalker(rootFolder, profile);
    }

    private void writeTestFiles(Path dir, int number) throws IOException {
        List<Path> paths = createPaths(dir, number);
        
        List<CompletableFuture<PreparedContent>> list = paths.stream()
             .map(path -> CompletableFuture.supplyAsync(() -> path))
             .map(future -> future.thenApplyAsync(this::prepareFile))
             .collect(Collectors.<CompletableFuture<PreparedContent>>toList());
             
        list.stream()
            .map(future -> {
                try {
                    return future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            })
            .forEach(content -> content.write());
             
    }

    private List<Path> createPaths(Path dir, int number) {
        List<Path> files = new ArrayList<>();
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
            files.add(dir.resolve(finder));
        }
        return files;
    }
    
    private PreparedContent prepareFile(Path file) {
        List<String> fileContent;
        if (rand.nextInt(100) > 80) {
            fileContent = Arrays.asList("some file's content without ",
                                        "words that need to be found");
        } else {
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
        return new PreparedContent(file, fileContent);
    }

    private static void removeFiles() throws IOException {
        Files.walk(testFolder)
             .filter(Files::isRegularFile)
             .forEach(t -> {
                try {
                    Files.deleteIfExists(t);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    private static class PreparedContent {
        Path file;
        List<String> content;
        
        public PreparedContent(Path file, List<String> content) {
            this.file = file;
            this.content = content;
        }
    
        void write() {
            try {
                Files.createFile(file);
                Files.write(file, content, charset, TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
