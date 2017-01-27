package dmv.desktop.searchandreplace.service;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;

import dmv.desktop.searchandreplace.model.*;

public abstract class FolderWalkerTest{

    protected static Path testRootFolder;
    protected static Path testFolder;
    protected static Path testSubFolder;
    protected static String dirName;
    protected static String subDirName;
    protected static Charset charset;
    protected static Charset otherCharset;
    protected static String[] fileTypes = {"txt", "bin"};
    protected static String[] includePaths = {"**.txt", "**.bin"};
    protected static String[] prefixes = {"do not", "It won't"};
    protected static String[] suffixes = {"leave this", "Stay"};
    protected static int pfxSize = prefixes.length;
    protected static int sfxSize = suffixes.length;
    protected static Exclusions excludeAll;
    protected static Exclusions excludePfx;
    protected static Exclusions excludeSfx;
    protected static String toFind;
    protected static String replaceWith;
    protected static int L = 30;
    protected static int LL = 200;
    protected Random rand = new Random();
    protected SearchAndReplace<SearchPath, SearchProfile, SearchResult> target;
    protected static SearchPath rootFolder;
    protected SearchProfile profile;

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

    protected static void removeFiles() throws IOException {
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

    protected FolderWalker createTarget(String replaceWith, 
            Exclusions exclusions, 
            boolean subfolders,
            boolean filenames) {
        rootFolder = SearchPathImpl.getBuilder(testFolder)
                .setNamePattern(includePaths)
                .setSubfolders(subfolders)
                .build();

        profile = SearchProfileImpl.getBuilder(toFind)
                .setReplaceWith(replaceWith)
                .setCharset(charset)
                .setExclusions(exclusions)
                .setFilename(filenames)
                .build();

        return new FolderWalker(rootFolder, profile);
    }

    protected void writeTestFiles(Path dir, int number) throws IOException {
        List<Path> paths = createPaths(dir, number);

        List<CompletableFuture<PreparedContent>> list = paths.stream()
                .map(path -> CompletableFuture.supplyAsync(() -> path))
                .map(future -> getContent(future))
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
    
    private CompletableFuture<PreparedContent> getContent(CompletableFuture<Path> future) {
        return future.thenApplyAsync(this::prepareFile);
    }

    protected List<Path> createPaths(Path dir, int number) {
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

    protected PreparedContent prepareFile(Path file) {
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
                    char ch = (char) rand.nextInt(Character.MAX_RADIX);
                    ch = (ch != '\\') ? ch : 'a';
                    line.append(ch);
                }
                fileContent.add(line.toString());
                line = new StringBuilder(LL);
            }
        }
        return new PreparedContent(file, fileContent);
    }

    protected void print(SearchResult result) {
        String name = result.getModifiedName() == null ? "unknown name\n" :
                      result.getModifiedName().getFirst().toString();
        StringBuilder print = new StringBuilder(name);
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

    protected void printStream(Stream<SearchResult> results) {
        System.out.println("===============Stream of results================\n");
        results.forEach(this::print);
        System.out.println("===============End of Stream================\n");
    }

    protected String printFile(SearchResult result, String string) {
        try {
            Path failedTest = testRootFolder.resolve("failedTest");
            if (!Files.exists(failedTest))
                Files.createFile(failedTest);
            StringBuilder sb = new StringBuilder();
            result.getModifiedContent()
                  .forEach(tuple -> {
                      sb.append(tuple.getFirst());
                      sb.append("\n");
                      sb.append(tuple.getLast());
                      sb.append("\n\n");
                  });
            Files.write(failedTest, sb.toString().getBytes(charset), 
                        StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string;
    }

    protected static class PreparedContent {
            Path file;
            List<String> content;
            
            public PreparedContent(Path file, List<String> content) {
                this.file = file;
                this.content = content;
            }
        
            void write() {
                try {
                    if (!Files.exists(file))
                        Files.createFile(file);
                    Files.write(file, content, charset, TRUNCATE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

}