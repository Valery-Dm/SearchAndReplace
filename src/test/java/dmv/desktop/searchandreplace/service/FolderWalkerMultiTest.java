package dmv.desktop.searchandreplace.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import dmv.desktop.searchandreplace.model.Exclusions;
import dmv.desktop.searchandreplace.model.SearchResult;


public class FolderWalkerMultiTest extends FolderWalkerTest {

    private static final int UNITS = Runtime.getRuntime().availableProcessors() * 2;
    private static final ExecutorService EXECS_POOL = Executors.newFixedThreadPool(UNITS);
    private static final ExecutorService SINGLE_EXEC = Executors.newSingleThreadExecutor();

    @Before
    public void setUp() throws Exception {
        removeFiles();
        writeTestFiles(testFolder, UNITS * 20);
        writeTestFiles(testSubFolder, UNITS / 2);
    }

    //@Ignore
    @Test
    public void excludeAll() {
        target = createTarget(replaceWith, excludeSfx, true, false);
        
        /* file reading loop */
        Random rand = new Random();
        List<SearchResult> modifications = null;
        String trackToFind = toFind;
        int T = 1, bound = toFind.length() - 1;
        while (T-- > 0) {
            int from = rand.nextInt(bound);
            toFind = trackToFind.substring(from, trackToFind.length());
            target.setProfile(profile.setToFind(toFind));
            modifications = target.preview(EXECS_POOL);
            modifications.forEach(result -> testResult(result, replaceWith, excludeSfx, true, false));
            //System.out.println(modifications.size());
        }
        toFind = trackToFind;
        target.setProfile(profile.setToFind(toFind));
        
        /* cache reading loop */
        Exclusions[] excl = new Exclusions[]{excludeAll, excludePfx, excludeSfx};
        T = 1;
        while (T-- > 0) {
            Exclusions e = excl[rand.nextInt(3)];
            target.setProfile(profile.setExclusions(e));
            modifications = target.preview(EXECS_POOL);
            modifications.forEach(result -> testResult(result, replaceWith, e, true, false));
            //System.out.println(modifications.size());
        }
        
        /* final replacing */
        target.setProfile(profile.setExclusions(excludeAll)
                                 .setFilename(true));
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
                              boolean inconsistent = false;
                              if (isFound(tuple.getFirst(), exclusions)) 
                                  inconsistent = tuple.getLast() == null;
                              else 
                                  inconsistent = tuple.getLast() != null;
                              if (inconsistent) {
                                  System.out.println(tuple.getFirst() + "\n");
                                  System.out.println(tuple.getLast());
                                  if (exclusions.equals(excludeAll))
                                      System.out.println("exclude all");
                                  if (exclusions.equals(excludePfx))
                                      System.out.println("exclude pfx");
                                  if (exclusions.equals(excludeSfx))
                                      System.out.println("exclude sfx");
                              }
                              return inconsistent;
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
}
