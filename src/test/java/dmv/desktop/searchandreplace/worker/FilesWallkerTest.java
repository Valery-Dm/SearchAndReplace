package dmv.desktop.searchandreplace.worker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.*;


public class FilesWallkerTest {
    
    private static final int UNITS = Runtime.getRuntime().availableProcessors() * 3;
    private static final ExecutorService POOL = Executors.newFixedThreadPool(UNITS);
    private static final ExecutorService ONE_ES = Executors.newSingleThreadExecutor();
    
    private FilesWallker target;
    
    private String dirName = "src/test/resources/testfolder/";
    private String subDirName = dirName + "subfolder/";
    
    private static String[] fileTypes = {"txt", "bin"};
    private static String[] prefixes = {"do not", "It won't"};
    private static String[] suffixes = {"leave this", "Stay"};
    private static String[] exclusions;
    private static int pfxSize = prefixes.length;
    private static int sfxSize = suffixes.length;
    
    private static String toFind = "Find me";
    private static String replaceWith = "It was Replaced";
    
    private List<String> filesContent;
    // L - lines per file; LL - line's length before toFind addition
    private int L = 20, LL = 50;
    private Random rand = new Random();
    
    @BeforeClass
    public static void prepare() {
        exclusions = new String[prefixes.length + suffixes.length];
        int i = 0;
        for (String p : prefixes)
            exclusions[i++] = p + toFind;
        for (String s : suffixes)
            exclusions[i++] = toFind + s;
    }

    @Before
    public void setUp() throws Exception {
        writeTestFiles(dirName, UNITS * 2);
        writeTestFiles(subDirName, UNITS);
    }
    
    @Test
    public void testSingle() throws IOException {
        target = createTarget(dirName, false, false);
        //target.preview(ONE_ES);
    }

    @Test
    public void testSingleFileNamesSubDirs() throws IOException {
        target = createTarget(dirName, true, true);
        //target.preview(ONE_ES);
    }

    @After
    public void removeFiles() throws IOException {
        Files.walk(Paths.get(dirName))
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
        ONE_ES.shutdown();
        POOL.shutdown();
    }

    private void prepareFilesContent() {
        filesContent = new ArrayList<>();
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
            filesContent.add(line.toString());
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
            prepareFilesContent();
            writeFile(Paths.get(dir + finder));
        }
    }
    
    private void writeFile(Path file) throws IOException {
        Files.createFile(file);
        Files.write(file, filesContent, StandardCharsets.UTF_16);
    }

    private FilesWallker createTarget(String dir, boolean isFileNames, boolean isSubfolders) {
        return  FilesWallker.getBuilder(dir)
                            .setToFind(toFind)
                            .setReplaceWith(replaceWith)
                            .setFileTypes(fileTypes)
                            .setExclusions(exclusions)
                            .setFileNames(isFileNames)
                            .setSubfolders(isSubfolders)
                            .build();
    }
}
