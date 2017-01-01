package dmv.desktop.searchandreplace.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

import dmv.desktop.searchandreplace.model.Exclusions;


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
        
        target = new Exclusions(exclude, toFind);
        
        checkSuffixes();
    }

    @Test
    public void containsSuffix2() {
        suffixes.add(suffix);
        suffixes.add(otherSuffix);
        
        target = new Exclusions(prefixes, suffixes, true);
        
        checkSuffixes();
    }

    @Test
    public void checkAnySuffixes() {
        for (int i = 1; i < suffix.length() - 1; i++)
            suffixes.add(suffix.substring(0, suffix.length() - i));
        
        target = new Exclusions(prefixes, suffixes, true);
        
        for (int i = 1; i < suffix.length() - 1; i++)
            assertTrue(target.containsAnySuffixes(suffix.substring(0, suffix.length() - i)));
        
    }

    private void checkSuffixes() {
        assertThat(target.maxSuffix(), is(suffix.length()));
        
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
        
        target = new Exclusions(prefixes, suffixes, true);
        
        for (int i = 1; i < prefix.length() - 1; i++)
            assertTrue(target.containsAnyPrefixes(prefix.substring(0, prefix.length() - i)));
        
    }

    @Test
    public void containsPrefix1() {
        exclude.add(prefix + toFind);
        exclude.add(otherPrefix + toFind);
        
        target = new Exclusions(exclude, toFind);
        
        checkPrefixes();
    }

    private void checkPrefixes() {
        assertThat(target.maxPrefix(), is(prefix.length()));
        
        assertTrue(target.containsPrefix(reversedPrefix));
        assertTrue(target.containsPrefix(reversedOtherPrefix));
        assertFalse(target.containsPrefix(prefix));
        assertFalse(target.containsPrefix(otherPrefix));

        assertFalse(target.containsPrefix(reversedPrefix + "1"));
        assertFalse(target.containsPrefix("1" + reversedPrefix));
        assertFalse(target.containsPrefix(reversedPrefix.substring(1, reversedPrefix.length() - 1)));
        assertFalse(target.containsPrefix(reversedPrefix.substring(0, reversedPrefix.length() - 2)));
    }

    @Test
    public void containsPrefix2() {
        prefixes.add(prefix);
        prefixes.add(otherPrefix);
        
        target = new Exclusions(prefixes, suffixes, true);

        checkPrefixes();
    }

    @Test
    public void containsPrefix3() {
        prefixes.add(reversedPrefix);
        prefixes.add(reversedOtherPrefix);
        
        target = new Exclusions(prefixes, suffixes, false);

        checkPrefixes();
        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void illegalArg1() {
        target = new Exclusions(exclude, toFind);
    }

    @Test(expected=IllegalArgumentException.class)
    public void illegalArg2() {
        toFind = "";
        exclude.add("not");
        target = new Exclusions(exclude, toFind);
    }

    @Test(expected=IllegalArgumentException.class)
    public void illegalArg3() {
        exclude.add("notFindMe");
        exclude.add("notfindMe");
        target = new Exclusions(exclude, toFind);
    }

    @Test(expected=IllegalArgumentException.class)
    public void illegalArg4() {
        exclude.add("notFindMe");
        exclude.add(null);
        target = new Exclusions(exclude, toFind);
    }

    @Test(expected=NullPointerException.class)
    public void nullArg1() {
        target = new Exclusions(null, toFind);
    }

    @Test(expected=NullPointerException.class)
    public void nullArg2() {
        target = new Exclusions(exclude, null);
    }

    @Test(expected=NullPointerException.class)
    public void nullArg3() {
        target = new Exclusions(prefixes, null, false);
    }

    @Test(expected=NullPointerException.class)
    public void nullArg4() {
        target = new Exclusions(null, suffixes, false);
    }
}
