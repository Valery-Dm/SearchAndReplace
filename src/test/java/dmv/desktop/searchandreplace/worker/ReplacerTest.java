package dmv.desktop.searchandreplace.worker;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.junit.*;

import dmv.desktop.searchandreplace.model.Exclusions;


public class ReplacerTest {
    
    private static Charset charSet;
    private static String rootDirectory;
    private static Path testFile;
    private static Path resultFile;
    private static boolean isPreview;
    private static String toFind;
    private static String replaceWith;
    
    private boolean isFileNames;
    private Exclusions exclusions;
    private Replacer target;
    
    @BeforeClass
    public static void prepareTest() throws Exception {
        charSet = StandardCharsets.UTF_8;
        rootDirectory = "src/test/resources/replacertest/";
        isPreview = false;
    }
    
    @AfterClass
    public static void cleanUp() throws Exception {
        Files.deleteIfExists(testFile);
        Files.deleteIfExists(resultFile);
        Files.deleteIfExists(Paths.get(rootDirectory + "single"+replaceWith+"Test.txt"));
    }

    @Before
    public void setUp() throws Exception {
        toFind = "FindMe";
        replaceWith = "Replaced";

        List<String> input = new ArrayList<>();
        input.add("00000oooooooSimpleLineoo000000000");
        input.add("00000oooooooot"+toFind+"otoooooo000000000");
        input.add("00000ooooooooo"+toFind+""+toFind+"ooooooo000000000");
        input.add("00000ooooooNot"+toFind+"Notooooooo000000000");
        input.add("00000ooooooNot"+toFind+"oooooooooo000000000");
        input.add("00000oooooo"+toFind+"Notoooooooooo000000000");
        input.add("eeoo"+toFind+"oooo"+toFind+"Notoo00000"+toFind+"0000"+toFind+"0000");
        input.add(toFind+"ooooooo000000000");
        input.add(toFind+"Notooooooo000000000");
        input.add("Not"+toFind+"oooooooooo000000000");
        input.add("00000ooooooooo"+toFind);
        input.add("00000ooooooNot"+toFind);
        input.add("00000oooooo"+toFind+"Not");

        testFile = Paths.get(rootDirectory + "single"+toFind+"Test.txt");
        Files.deleteIfExists(testFile);
        Files.createFile(testFile);
        Files.write(testFile, input, charSet);
    }

    @Test
    public void allExclusions() throws Exception {
        List<String> result = new ArrayList<>();
        result.add("00000oooooooSimpleLineoo000000000");
        result.add("00000oooooooot"+replaceWith+"otoooooo000000000");
        result.add("00000ooooooooo"+replaceWith+""+replaceWith+"ooooooo000000000");
        result.add("00000ooooooNot"+toFind+"Notooooooo000000000");
        result.add("00000ooooooNot"+toFind+"oooooooooo000000000");
        result.add("00000oooooo"+toFind+"Notoooooooooo000000000");
        result.add("eeoo"+replaceWith+"oooo"+toFind+"Notoo00000"+replaceWith+"0000"+replaceWith+"0000");
        result.add(replaceWith+"ooooooo000000000");
        result.add(toFind+"Notooooooo000000000");
        result.add("Not"+toFind+"oooooooooo000000000");
        result.add("00000ooooooooo"+replaceWith);
        result.add("00000ooooooNot"+toFind);
        result.add("00000oooooo"+toFind+"Not");
        
        writeResult(result);
        
        addExclusions("Not"+toFind, toFind+"Not");
        
        runTest();
    }

    @Test
    public void excludePrefixes() throws Exception {
        List<String> result = new ArrayList<>();
        result.add("00000oooooooSimpleLineoo000000000");
        result.add("00000oooooooot"+replaceWith+"otoooooo000000000");
        result.add("00000ooooooooo"+replaceWith+""+replaceWith+"ooooooo000000000");
        result.add("00000ooooooNot"+toFind+"Notooooooo000000000");
        result.add("00000ooooooNot"+toFind+"oooooooooo000000000");
        result.add("00000oooooo"+replaceWith+"Notoooooooooo000000000");
        result.add("eeoo"+replaceWith+"oooo"+replaceWith+"Notoo00000"+replaceWith+"0000"+replaceWith+"0000");
        result.add(replaceWith+"ooooooo000000000");
        result.add(replaceWith+"Notooooooo000000000");
        result.add("Not"+toFind+"oooooooooo000000000");
        result.add("00000ooooooooo"+replaceWith);
        result.add("00000ooooooNot"+toFind);
        result.add("00000oooooo"+replaceWith+"Not");
        
        writeResult(result);
        
        addExclusions("Not"+toFind);
        
        runTest();
    }
    
    @Test
    public void excludeSuffixes() throws Exception {
        List<String> result = new ArrayList<>();
        result.add("00000oooooooSimpleLineoo000000000");
        result.add("00000oooooooot"+replaceWith+"otoooooo000000000");
        result.add("00000ooooooooo"+replaceWith+""+replaceWith+"ooooooo000000000");
        result.add("00000ooooooNot"+toFind+"Notooooooo000000000");
        result.add("00000ooooooNot"+replaceWith+"oooooooooo000000000");
        result.add("00000oooooo"+toFind+"Notoooooooooo000000000");
        result.add("eeoo"+replaceWith+"oooo"+toFind+"Notoo00000"+replaceWith+"0000"+replaceWith+"0000");
        result.add(replaceWith+"ooooooo000000000");
        result.add(toFind+"Notooooooo000000000");
        result.add("Not"+replaceWith+"oooooooooo000000000");
        result.add("00000ooooooooo"+replaceWith);
        result.add("00000ooooooNot"+replaceWith);
        result.add("00000oooooo"+toFind+"Not");
        
        writeResult(result);
        
        addExclusions(toFind+"Not");
        
        runTest();
    }
    
    private void addExclusions(String ... list) {
        Set<String> set = new HashSet<>();
        for (String exclusion : list)
            set.add(exclusion);
        exclusions = new Exclusions(set, toFind, true);
    }
    
    private void writeResult(List<String> result) throws IOException {
        resultFile = Paths.get(rootDirectory + "singleResult.txt");
        Files.deleteIfExists(resultFile);
        Files.createFile(resultFile);
        Files.write(resultFile, result, charSet);
    }

    private void runTest() throws IOException {
        target = new Replacer(testFile, toFind, replaceWith, exclusions, charSet, isFileNames, isPreview);
        target.run();
        
        List<String> actual = Files.readAllLines(testFile, charSet);
        List<String> expected = Files.readAllLines(resultFile, charSet);
        assertThat(actual, is(expected));
    }

}
