package dmv.desktop.searchandreplace.view.profile;

import static dmv.desktop.searchandreplace.view.profile.ReplaceFilesProfile.FILE_EXTENSION;
import static dmv.desktop.searchandreplace.view.profile.ReplaceFilesProfile.NAME_SIZE;
import static dmv.desktop.searchandreplace.view.profile.ReplaceFilesProfile.RECOGNIZED_BOOLEANS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import dmv.desktop.searchandreplace.exception.WrongProfileException;
import dmv.desktop.searchandreplace.model.SearchPath;
import dmv.desktop.searchandreplace.model.SearchProfile;
import dmv.desktop.searchandreplace.model.SearchResult;
import dmv.desktop.searchandreplace.service.SearchAndReplace;


public abstract class ReplaceFilesProfileTest {
    
    private String name;
    private Path path;
    private boolean subfolders;
    private String[] namePatterns;
    private String[] includedFiles;
    private String[] excludedFiles;
    private String toFind;
    private Charset charset;
    private String replaceWith;
    private boolean filenames;
    private String prefix;
    private String suffix;
    private Set<String> exclusions;
    
    private ReplaceFilesProfile target;
    
    abstract protected ReplaceFilesProfile getTarget();

    @Before
    public void setUp() throws Exception {
        name = "azAZ_09";
        path = Paths.get("src/");
        subfolders = true;
        namePatterns = new String[]{"**.txt", "**subfolder/*.bin"};
        includedFiles = new String[]{"somefolder/file1.txt", "file.txt", "folder/subfolder/file.bin"};
        excludedFiles = new String[]{"somefolder/file1.tst", "folder/otherfolder/file.bin"};
        toFind = "FindMe";
        charset = StandardCharsets.US_ASCII;
        replaceWith = "replaced";
        filenames = true;
        prefix = "prefix";
        suffix = "suffix";
        exclusions = new HashSet<>();
        exclusions.add(prefix + toFind);
        exclusions.add(toFind + suffix);
        target = getTarget();
    }

    @Test
    public void correctName() {
        assertThat(target.getName(), is(""));
        assertThat(target.setName(name)
                         .getName(), is(name));
        
        /* a bunch of strange but correct names */
        name = "_";
        assertThat(target.setName(name)
                         .getName(), is(name));
        name = "0";
        assertThat(target.setName(name)
                         .getName(), is(name));
        name = "Z";
        assertThat(target.setName(name)
                         .getName(), is(name));
        name = "9_";
        assertThat(target.setName(name)
                         .getName(), is(name));
        assertThat(target.setName(name + FILE_EXTENSION)
                         .getName(), is(name));
    }

    @Test(expected=IllegalArgumentException.class)
    public void nullName() {
        target.setName(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void emptyName() {
        target.setName("");
    }

    @Test(expected=IllegalArgumentException.class)
    public void extensionOnlyName() {
        target.setName(FILE_EXTENSION);
    }

    @Test
    public void notAllowedCharactersInName() {
        expectIllegalArgumentException('-' - 1);
        expectIllegalArgumentException('-' + 1);
        expectIllegalArgumentException('a' - 1);
        expectIllegalArgumentException('z' + 1);
        expectIllegalArgumentException('A' - 1);
        expectIllegalArgumentException('Z' + 1);
        expectIllegalArgumentException('0' - 1);
        expectIllegalArgumentException('9' + 1);
    }

    private void expectIllegalArgumentException(int illegalChar) {
        try {
            target.setName("nameWith" + (char) illegalChar);
            fail("IllegalArgumentException expected");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void tooManyCharactersName() {
        StringBuilder sb = new StringBuilder(NAME_SIZE + 1);
        for (int i = 0; i < NAME_SIZE + 1; i++)
            sb.append('a');
        target.setName(sb.toString());
    }

    @Test
    public void createCorrectService() {
        SearchAndReplace<SearchPath, SearchProfile, SearchResult> service;
        SearchPath searchPath;
        SearchProfile searchProfile;
        
        setParams();
        
        service = target.createService();
        searchPath = service.getRootElement();
        searchProfile = service.getProfile();
        
        assertThat(searchPath.getPath(), is(path));
        for (String pattern : includedFiles)
            assertTrue(searchPath.getNamePattern()
                                 .matches(Paths.get(pattern)));
        for (String pattern : excludedFiles)
            assertFalse(searchPath.getNamePattern()
                                  .matches(Paths.get(pattern)));
        assertThat(searchPath.isSubfolders(), is(subfolders));
        
        assertThat(searchProfile.getToFind(), is(toFind));
        assertThat(searchProfile.getCharset(), is(charset));
        assertThat(searchProfile.getReplaceWith(), is(replaceWith));
        assertThat(searchProfile.isFileName(), is(filenames));
        assertTrue(searchProfile.getExclusions().containsPrefix(prefix, true));
        assertTrue(searchProfile.getExclusions().containsSuffix(suffix));
        
        /* change settings */
        
        path = Paths.get("otherPath");
        subfolders = false;
        toFind = toFind + "1";
        replaceWith = "";
        filenames = false;
        target.setPath(path.toString());
        target.setSubfolders("" + subfolders);
        target.addIncludeNamePattern(null);
        target.setToFind(toFind);
        target.setCharset(null);
        target.setReplaceWith(replaceWith);
        target.setFilenames("" + filenames);
        target.addExclusion(null);
        
        service = target.createService();
        searchPath = service.getRootElement();
        searchProfile = service.getProfile();
        
        assertThat(searchPath.getPath(), is(path));
        // now the default pattern will accept all files
        for (String pattern : includedFiles)
            assertTrue(searchPath.getNamePattern()
                                 .matches(Paths.get(pattern)));
        for (String pattern : excludedFiles)
            assertTrue(searchPath.getNamePattern()
                                 .matches(Paths.get(pattern)));
        assertThat(searchPath.isSubfolders(), is(subfolders));
        
        assertThat(searchProfile.getToFind(), is(toFind));
        assertThat(searchProfile.getCharset(), is(SearchProfile.defaultCharset));
        assertThat(searchProfile.getReplaceWith(), is(replaceWith));
        assertThat(searchProfile.isFileName(), is(filenames));
        assertFalse(searchProfile.getExclusions().containsPrefix(prefix, true));
        assertFalse(searchProfile.getExclusions().containsSuffix(suffix));
        
        /* set the same parameters in sequence */
        target = getTarget();
        setParams();
        setParams();
        
        target.addIncludeNamePattern("");
        target.setCharset("");
        target.addExclusion("");
        service = target.createService();
        target.addIncludeNamePattern("");
        target.setCharset("");
        target.addExclusion("");
        service = target.createService();
        searchPath = service.getRootElement();
        searchProfile = service.getProfile();
        assertThat(searchPath.getNamePattern(), is(SearchPath.defaultPattern));
        assertThat(searchProfile.getCharset(), is(SearchProfile.defaultCharset));
        assertThat(searchProfile.getExclusions(), is(SearchProfile.EMPTY_EXCLUSIONS));
        
        
        for (String recognizedBoolean : RECOGNIZED_BOOLEANS.keySet()) {
            boolean setting = RECOGNIZED_BOOLEANS.get(recognizedBoolean);
            target.setFilenames(recognizedBoolean);
            target.setSubfolders(recognizedBoolean);
            
            service = target.createService();
            searchPath = service.getRootElement();
            searchProfile = service.getProfile();

            assertThat(searchPath.isSubfolders(), is(setting));
            assertThat(searchProfile.isFileName(), is(setting));
        }
    }
    
    @Test
    public void setParametersInSequence() {
        SearchAndReplace<SearchPath, SearchProfile, SearchResult> service;
        SearchPath searchPath;
        SearchProfile searchProfile;
        
        setParams();
        service = target.createService();
        setParams();
        
        target.addIncludeNamePattern("");
        target.setCharset("");
        target.addExclusion("");
        target.addIncludeNamePattern("");
        target.setCharset("");
        target.addExclusion("");
        service = target.createService();
        searchPath = service.getRootElement();
        searchProfile = service.getProfile();
        assertThat(searchPath.getNamePattern(), is(SearchPath.defaultPattern));
        assertThat(searchProfile.getCharset(), is(SearchProfile.defaultCharset));
        assertThat(searchProfile.getExclusions(), is(SearchProfile.EMPTY_EXCLUSIONS));
    }

    public void setParams() {
        target.setPath(path.toString());
        target.setSubfolders("" + subfolders);
        for (String pattern : namePatterns)
            target.addIncludeNamePattern(pattern);
        target.setToFind(toFind);
        target.setCharset(charset.name());
        target.setReplaceWith(replaceWith);
        target.setFilenames("" + filenames);
        for (String exclusion : exclusions)
            target.addExclusion(exclusion);
    }

    @Test(expected=WrongProfileException.class)
    public void createServiceNullPath() {
        setParams();
        target.setPath(null);
        target.createService();
    }

    @Test(expected=WrongProfileException.class)
    public void setServiceNullPath() {
        setParams();
        target.createService();
        target.setPath(null);
    }

    @Test(expected=WrongProfileException.class)
    public void createServiceWrongSubfolders() {
        setParams();
        target.setSubfolders("some wrong word");
        target.createService();
    }

    @Test(expected=WrongProfileException.class)
    public void setServiceWrongSubfolders() {
        setParams();
        target.createService();
        target.setSubfolders("some wrong word");
    }

    @Test(expected=WrongProfileException.class)
    public void createServiceIllegalPattern() {
        setParams();
        target.addIncludeNamePattern("{{}}");
        target.createService();
    }

    @Test(expected=WrongProfileException.class)
    public void setServiceIllegalPattern() {
        setParams();
        target.createService();
        target.addIncludeNamePattern("{{}}");
    }

    @Test(expected=WrongProfileException.class)
    public void createServiceEmptyToFind() {
        setParams();
        target.setToFind("");
        target.createService();
    }

    @Test(expected=WrongProfileException.class)
    public void setServiceEmptyToFind() {
        setParams();
        target.createService();
        target.setToFind("");
    }

    @Test(expected=WrongProfileException.class)
    public void createServiceWrongCharset() {
        setParams();
        target.setCharset("UFT_61");
        target.createService();
    }

    @Test(expected=WrongProfileException.class)
    public void setServiceWrongCharset() {
        setParams();
        target.createService();
        target.setCharset("UFT_61");
    }

    @Test(expected=WrongProfileException.class)
    public void createServiceWrongFilnames() {
        setParams();
        target.setFilenames("");
        target.createService();
    }

    @Test(expected=WrongProfileException.class)
    public void setServiceWrongFilnames() {
        setParams();
        target.createService();
        target.setFilenames("");
    }

    @Test(expected=WrongProfileException.class)
    public void createServiceWrongExclusion() {
        setParams();
        target.addExclusion("some exclusion");
        target.createService();
    }

    @Test(expected=WrongProfileException.class)
    public void setServiceWrongExclusion() {
        setParams();
        target.createService();
        target.addExclusion("some exclusion");
    }

    @Test
    public void testUseProfile() {
        //fail("Not yet implemented");
    }

    @Test
    public void testSaveProfie() {
        //fail("Not yet implemented");
    }

    @Test
    public void testToString() {
        String defaultSettings = createStringFrom(
                                   "Name of a profile",
                                    "Currently not set",
                                    "Overwrite profile with the same name",
                                    "false",
                                    "",
                                    "-path ## required",
                                    "Currently not set",
                                    "-find ## required",
                                    "Currently not set",
                                    "-replace",
                                    "",
                                    "-charset",
                                    "Currently not set",
                                    "-filenames",
                                    "false",
                                    "-subfolders",
                                    "false",
                                    "-namepattern",
                                    "Currently not set",
                                    "-exclude",
                                    "Currently not set");
        assertThat(target.toString(), is(defaultSettings));
        
        target.setName(name);
        target.setOverwrite();
        target.setPath(path.toString());
        target.setSubfolders("" + subfolders);
        for (String pattern : namePatterns)
            target.addIncludeNamePattern(pattern);
        target.setToFind(toFind);
        target.setCharset(charset.name());
        target.setReplaceWith(replaceWith);
        target.setFilenames("" + filenames);
        target.addExclusion("prefixFindMe");
        target.addExclusion("FindMesuffix");
        
        String appliedSettings = createStringFrom(
                                    "Name of a profile",
                                    name,
                                    "Overwrite profile with the same name",
                                    "true",
                                    "",
                                    "-path ## required",
                                    path.toString(),
                                    "-find ## required",
                                    toFind,
                                    "-replace",
                                    replaceWith,
                                    "-charset",
                                    charset.name(),
                                    "-filenames",
                                    "" + filenames,
                                    "-subfolders",
                                    "" + subfolders,
                                    "-namepattern",
                                    String.join(", ", namePatterns),
                                    "-exclude",
                                    "prefixFindMe",
                                    "-exclude",
                                    "FindMesuffix");
        assertThat(target.toString(), is(appliedSettings));
    }

    private String createStringFrom(String...lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines)
            builder.append(line)
                   .append("\n");
        builder.delete(builder.length() - 1, builder.length());
        return builder.toString();
    }

}
