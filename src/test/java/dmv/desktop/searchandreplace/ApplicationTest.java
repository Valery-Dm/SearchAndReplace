package dmv.desktop.searchandreplace;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class ApplicationTest {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);
    private static Charset charSet = StandardCharsets.UTF_8;
    
    private static String rootDirectoryName;
    private static String subfolder1;
    private static String subfolder2;
    private static String subfolder11;

    @BeforeClass
    public static void init() throws Exception {
        rootDirectoryName = "src/test/resources/testfolder/";
        createIfNotExist(rootDirectoryName);
        subfolder1 = rootDirectoryName + "subfolder1/";
        createIfNotExist(subfolder1);
        subfolder2 = rootDirectoryName + "subfolder2/";
        createIfNotExist(subfolder2);
        // Inside subfolder1
        subfolder11 = subfolder1 + "subfolder11";
        createIfNotExist(subfolder11);
    }
    
    private static void createIfNotExist(String dirName) throws IOException {
        Path dirPath = Paths.get(dirName);
        if (!Files.exists(dirPath))
            Files.createDirectory(dirPath);
    }
    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
        fail("Not yet implemented");
    }

}
