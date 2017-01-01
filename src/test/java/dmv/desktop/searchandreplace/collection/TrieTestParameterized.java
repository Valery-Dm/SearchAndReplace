package dmv.desktop.searchandreplace.collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class TrieTestParameterized extends TrieTest {
    
    @Parameter
    public TestCase testCase;
    
    @Parameters(name="{index}: {0}")
    public static List<TestCase> buildTests() {
        return Arrays.asList(new TestCase[] {
                    new TestCase("abba", "abba", "true"),
                    new TestCase("cb!iO", "cb!iO", "true"),
                    new TestCase("cb!iO", "cb!io", "false"),
                    new TestCase("abba", "Abba", "false"),
                    new TestCase("asldSDalkFjlkdsfje", "asldSDalkFjlkdsfje", "true"),
                    new TestCase("asldSDalkFjlkdsfje", "asldSDalkFjlkdsfj", "false"),
                    new TestCase("abba", "abbb", "false")
                });
    }
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        target.add(testCase.add);
    }

    @Test
    public void parameterized() {
        assertThat(target.contains(testCase.contains), is(testCase.result));
    }
    
    private static class TestCase {
        String add, contains;
        boolean result;

        TestCase(String add, String contains, String result) {
            this.add = add;
            this.contains = contains;
            this.result = Boolean.parseBoolean(result);
        }

        @Override
        public String toString() {
            return String.format("TestCase [add=%s, contains=%s, result=%s]",
                    add, contains, result);
        }
    }
}
