package dmv.desktop.searchandreplace;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dmv.desktop.searchandreplace.model.Exclusions;
import dmv.desktop.searchandreplace.model.ExclusionsTrie;
import dmv.desktop.searchandreplace.worker.Replacer;

public class Application {
    
    private static final ExecutorService POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);
    private static Charset charSet = StandardCharsets.UTF_8;
    
    private static boolean isPreview;
    
    private static String rootDirectoryName;
    private static boolean isFileNames;
    private static boolean isSubfolders;
    private static PathMatcher fileTypes;
    private static String toFind;
    private static String replaceWith;
    private static List<String> exclude;
    private static Exclusions exclusions;
    
    public static void renameIn(Path dir) {
        try {
            Files.walk(dir, isSubfolders ? Integer.MAX_VALUE : 1)
                 .forEach(path -> {
                     //System.out.println("#################" + path);
                     if (!Files.isDirectory(path) && 
                          Files.isWritable(path) &&
                          fileTypes.matches(path.getFileName())) {
                             process(path);
                     }
                 });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            POOL.shutdown();
        }
    }

    private static void process(Path path) {
        POOL.execute(new Replacer(path, toFind, replaceWith, exclusions, charSet, isFileNames, isPreview));
    }

    public static void main(String[] args) {
        
        isPreview = true;
        
        rootDirectoryName = "src/test/resources/apptest";
        // Only file's content will be changed by default (false)
        isFileNames = true;
        // Only 1 level without subfolders will be scanned by default (false)
        isSubfolders = true;
        fileTypes = FileSystems.getDefault().getPathMatcher("glob:*.{java,xml,project}");
        toFind = "HamiltonianCycle";
        replaceWith = "HamiltonianPath";
        exclude = Arrays.asList(new String[] {"isHamiltonianCycleExist", "getHamiltonianCycle"});
        exclusions = new ExclusionsTrie(new HashSet<>(exclude), toFind, true);
        
        renameIn(Paths.get(rootDirectoryName));
        
    }

}
