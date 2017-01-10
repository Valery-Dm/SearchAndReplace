package dmv.desktop.searchandreplace.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.Before;
import org.junit.Test;


public class ExclusionsTest {
    
    private Exclusions target;

    private List<String> prefixes;
    private List<String> suffixes;
    private Set<String> exclude;
    private String toFind;
    
    private String prefix;
    private String otherPrefix;
    private String reversedPrefix;
    private String reversedOtherPrefix;
    
    private String suffix;
    private String otherSuffix;
    
    
    @Before
    public void setUp() throws Exception {
        prefixes = new ArrayList<>();
        suffixes = new ArrayList<>();
        exclude = new HashSet<>();
        toFind = "FindMe";
        prefix = "somePrefix";
        otherPrefix = "1&5#$s";
        reversedPrefix = new StringBuilder(prefix).reverse().toString();
        reversedOtherPrefix = new StringBuilder(otherPrefix).reverse().toString();
        suffix = "someSuffix";
        otherSuffix = "^$#Ghhe#";
    }

    @Test
    public void containsSuffix1() {
        exclude.add(toFind + suffix);
        exclude.add(toFind + otherSuffix);
        
        target = new ExclusionsTrie(exclude, toFind, true);
        
        checkSuffixes();
    }

    @Test
    public void containsSuffix2() {
        suffixes.add(suffix);
        suffixes.add(otherSuffix);
        
        target = new ExclusionsTrie(prefixes, suffixes, true);

        assertFalse(target.isEmpty());
        checkSuffixes();
    }

    @Test
    public void checkAnySuffixes() {
        for (int i = 1; i < suffix.length() - 1; i++)
            suffixes.add(suffix.substring(0, suffix.length() - i));
        
        target = new ExclusionsTrie(prefixes, suffixes, true);
        
        for (int i = 1; i < suffix.length() - 1; i++)
            assertTrue(target.containsAnySuffixes(suffix.substring(0, suffix.length() - i)));
        
    }

    private void checkSuffixes() {
        assertThat(target.maxSuffixSize(), is(suffix.length()));
        
        assertTrue(target.containsSuffix(suffix));
        assertTrue(target.containsSuffix(otherSuffix));
        
        assertFalse(target.containsSuffix(suffix + " "));
        assertFalse(target.containsSuffix(" " + suffix));
        assertFalse(target.containsSuffix(suffix.substring(1, suffix.length() - 1)));
        assertFalse(target.containsSuffix(suffix.substring(0, suffix.length() - 2)));
    }

    @Test
    public void checkAnyPrefixes() {
        for (int i = 1; i < prefix.length() - 1; i++)
            prefixes.add(prefix.substring(0, prefix.length() - i));
        
        target = new ExclusionsTrie(prefixes, suffixes, true);
        
        for (int i = 1; i < prefix.length() - 1; i++)
            assertTrue(target.containsAnyPrefixes(prefix.substring(0, prefix.length() - i), true));
        
    }

    @Test
    public void containsPrefix1() {
        exclude.add(prefix + toFind);
        exclude.add(otherPrefix + toFind);
        
        target = new ExclusionsTrie(exclude, toFind, true);
        
        checkPrefixes();
    }

    private void checkPrefixes() {
        assertThat(target.maxPrefixSize(), is(prefix.length()));

        assertFalse(target.containsPrefix(null, true));
        assertFalse(target.containsPrefix("", true));
        
        assertTrue(target.containsPrefix(reversedPrefix, false));
        assertFalse(target.containsPrefix(reversedOtherPrefix, true));
        assertTrue(target.containsPrefix(prefix, true));
        assertFalse(target.containsPrefix(otherPrefix, false));

        assertFalse(target.containsPrefix(reversedPrefix + "1", false));
        assertFalse(target.containsPrefix("1" + reversedPrefix, false));
        assertFalse(target.containsPrefix(reversedPrefix.substring(1, reversedPrefix.length() - 1), false));
        assertFalse(target.containsPrefix(reversedPrefix.substring(0, reversedPrefix.length() - 2), false));
    }

    @Test
    public void nullAndEmpty() {
        prefixes.add("");
        prefixes.add(null);
        suffixes.add("");
        suffixes.add(null);
        
        target = new ExclusionsTrie(prefixes, suffixes, true);

        assertTrue(target.isEmpty());
        assertThat(target.maxPrefixSize(), is(0));
        assertThat(target.maxSuffixSize(), is(0));
        assertFalse(target.containsPrefix(null, true));
        assertFalse(target.containsPrefix("", true));

        target = new ExclusionsTrie(prefixes, null, false);

        assertTrue(target.isEmpty());
        assertThat(target.numberOfPrefixes(), is(0));
        assertThat(target.numberOfSuffixes(), is(0));
        assertFalse(target.containsPrefix(null, true));
        assertFalse(target.containsPrefix("", true));
    }

    @Test
    public void containsPrefix2() {
        prefixes.add(prefix);
        prefixes.add(otherPrefix);
        
        target = new ExclusionsTrie(prefixes, suffixes, true);

        assertFalse(target.isEmpty());
        checkPrefixes();
    }

    @Test
    public void containsPrefix3() {
        prefixes.add(reversedPrefix);
        prefixes.add(reversedOtherPrefix);
        
        target = new ExclusionsTrie(prefixes, suffixes, false);

        checkPrefixes();
        
    }
    
    @Test
    public void containsPrefix4() {
        exclude.add(prefix + toFind);
        exclude.add(otherPrefix + toFind);
        exclude.add(toFind);
        
        target = new ExclusionsTrie(exclude, toFind, false);

        assertTrue(target.containsPrefix(prefix, false));
        assertFalse(target.containsPrefix(otherPrefix, true));
        assertTrue(target.containsAnyPrefixes(prefix, false));
        assertFalse(target.containsAnyPrefixes(otherPrefix, true));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void illegalArg1() {
        target = new ExclusionsTrie(exclude, toFind, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void illegalArg2() {
        toFind = "";
        exclude.add("not");
        target = new ExclusionsTrie(exclude, toFind, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void illegalArg3() {
        exclude.add("notFindMe");
        exclude.add("notfindMe");
        target = new ExclusionsTrie(exclude, toFind, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void illegalArg4() {
        exclude.add("notFindMe");
        exclude.add(null);
        target = new ExclusionsTrie(exclude, toFind, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void illegalArg5() {
        target = new ExclusionsTrie(null, toFind, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void illegalArg6() {
        exclude.add("not");
        target = new ExclusionsTrie(exclude, null, true);
    }

    @Test
    public void emptyExclusions1() {
        target = new ExclusionsTrie(Collections.emptyList(), null, false);
        assertTrue(target.maxPrefixSize() == 0);
        assertTrue(target.maxSuffixSize() == 0);
    }

    @Test
    public void emptyExclusions2() {
        target = new ExclusionsTrie(null, Collections.emptyList(), false);
        assertTrue(target.maxPrefixSize() == 0);
        assertTrue(target.maxSuffixSize() == 0);
    }
}
