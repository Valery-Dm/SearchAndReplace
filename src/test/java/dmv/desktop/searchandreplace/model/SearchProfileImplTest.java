package dmv.desktop.searchandreplace.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SearchProfileImplTest extends SearchProfileTest {

    @Override
    protected SearchProfile buildTarget() {
        return SearchProfileImpl.getBuilder(getToFind())
                                .build();
    }

    @Override
    protected String getToFind() {
        return "FindMe";
    }

    @Test
    public void immutabilityCheck() {
        SearchProfile target = buildTarget();
        target.toString();
        assertThat(target.setCharset(null), is(not(target)));
        assertThat(target.setFilename(true), is(not(target)));
        assertThat(target.setToFind(getToFind()), is(not(target)));
        assertThat(target.setReplaceWith(""), is(not(target)));
        assertThat(target.setExclusions(null), is(not(target)));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void builderNull() {
        SearchProfileImpl.getBuilder(null)
                         .build();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void builderEmpty() {
        SearchProfileImpl.getBuilder("")
                         .build();
    }
}
