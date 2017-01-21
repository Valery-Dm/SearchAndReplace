package dmv.desktop.searchandreplace.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dmv.desktop.searchandreplace.collection.Tuple;


public class SearchResultImplTest extends SearchResultTest {
    
    private SearchResultImpl.SearchResultBuilder builder = SearchResultImpl.getBuilder();
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    @Before
    public void reCreateEmptyBuilder() {
        builder = SearchResultImpl.getBuilder();
    }

    @Override
    protected SearchResult getSearchResult(Tuple<Path, Path> modifiedPath, 
                                           List<Tuple<String, String>> modifiedContent,
                                           int numberOfModifications, boolean exceptional, 
                                           Throwable cause) {
//        return new SearchResultImpl(numberOfModifications, modifiedPath, modifiedContent, exceptional, cause);
        return builder.setModifiedName(modifiedPath)
                      .setModifiedContent(modifiedContent)
                      .setNumberOfModificationsMade(numberOfModifications)
                      .setExceptional(exceptional)
                      .setCause(cause)
                      .build();
    }
    
    @Test
    public void constructor() {
        SearchResultImpl result = 
                new SearchResultImpl(getNumberOfModifications(), 
                                     getModifiedPath(), getModifiedContent(), 
                                     false, null);
        assertThat(result.numberOfModificationsMade(), is(getNumberOfModifications()));
        assertThat(result.getModifiedName(), is(getModifiedPath()));
        assertThat(result.getModifiedContent(), is(getModifiedContent()));
        assertThat(result.getCause(), is(nullValue()));
        assertThat(result.isExceptional(), is(false));
        
        result = new SearchResultImpl(0, null, null, 
                                      getCause() != null, getCause());
        assertThat(result.numberOfModificationsMade(), is(0));
        assertThat(result.getModifiedName(), is(nullValue()));
        assertThat(result.getModifiedContent(), is(nullValue()));
        assertThat(result.getCause(), is(getCause()));
        assertThat(result.isExceptional(), is(getCause() != null));
    }
    
    @Test
    public void toStringNull() {
        SearchResultImpl result = 
                new SearchResultImpl(getNumberOfModifications(), 
                                     getModifiedPath(), getModifiedContent(), 
                                     false, null);
        result.toString();
        result = new SearchResultImpl(0, null, null, 
                                      getCause() != null, getCause());
        result.toString();
    }
    
    @Test
    public void constructorExceptions1() {
        exception.expect(IllegalArgumentException.class);
        new SearchResultImpl(-1, getModifiedPath(), getModifiedContent(), false, null);
    }
    
    @Test
    public void constructorExceptions2() {
        exception.expect(IllegalArgumentException.class);
        new SearchResultImpl(1, null, getModifiedContent(), false, null);
    }

    @Test
    public void constructorExceptions3() {
        exception.expect(IllegalArgumentException.class);
        new SearchResultImpl(1, getModifiedPath(), null, false, null);
    }

    @Test
    public void constructorExceptions4() {
        exception.expect(IllegalArgumentException.class);
        new SearchResultImpl(1, getModifiedPath(), getModifiedContent(), true, null);
    }

    @Test
    public void constructorExceptions5() {
        exception.expect(IllegalArgumentException.class);
        new SearchResultImpl(1, getModifiedPath(), getModifiedContent(), false, getCause());
    }

    @Test
    public void constructorExceptions6() {
        exception.expect(IllegalArgumentException.class);
        new SearchResultImpl(1, null, null, true, getCause());
    }

    @Test
    public void constructorExceptions7() {
        exception.expect(IllegalArgumentException.class);
        new SearchResultImpl(0, getModifiedPath(), null, true, getCause());
    }

    @Test
    public void constructorExceptions8() {
        exception.expect(IllegalArgumentException.class);
        new SearchResultImpl(0, null, getModifiedContent(), true, getCause());
    }

    @Test
    public void constructorExceptions9() {
        exception.expect(IllegalArgumentException.class);
        new SearchResultImpl(0, null, null, false, getCause());
    }

    @Test
    public void constructorExceptions10() {
        exception.expect(IllegalArgumentException.class);
        new SearchResultImpl(0, null, null, true, null);
    }
    
    @Test
    public void builder() {
        SearchResult normalResult = 
                 builder.setNumberOfModificationsMade(getNumberOfModifications())
                        .setModifiedName(getModifiedPath())
                        .setModifiedContent(getModifiedContent())
                        .build();
        assertThat(normalResult.numberOfModificationsMade(), is(getNumberOfModifications()));
        assertThat(normalResult.getModifiedName(), is(getModifiedPath()));
        assertThat(normalResult.getModifiedContent(), is(getModifiedContent()));
        assertThat(normalResult.getCause(), is(nullValue()));
        assertThat(normalResult.isExceptional(), is(false));
        
        builder = SearchResultImpl.getBuilder();
        SearchResult exceptionalResult = 
                 builder.setCause(getCause())
                        .setExceptional(getCause() != null)
                        .build();
        assertThat(exceptionalResult.numberOfModificationsMade(), is(0));
        assertThat(exceptionalResult.getModifiedName(), is(nullValue()));
        assertThat(exceptionalResult.getModifiedContent(), is(nullValue()));
        assertThat(exceptionalResult.getCause(), is(getCause()));
        assertThat(exceptionalResult.isExceptional(), is(getCause() != null));
        
        /* Reset old results */
        
        normalResult = builder.setResult(normalResult)
                              .build();
        assertThat(normalResult.numberOfModificationsMade(), is(getNumberOfModifications()));
        assertThat(normalResult.getModifiedName(), is(getModifiedPath()));
        assertThat(normalResult.getModifiedContent(), is(getModifiedContent()));
        assertThat(normalResult.getCause(), is(nullValue()));
        assertThat(normalResult.isExceptional(), is(false));
        
        exceptionalResult = builder.setResult(exceptionalResult)
                                   .build();
        assertThat(exceptionalResult.numberOfModificationsMade(), is(0));
        assertThat(exceptionalResult.getModifiedName(), is(nullValue()));
        assertThat(exceptionalResult.getModifiedContent(), is(nullValue()));
        assertThat(exceptionalResult.getCause(), is(getCause()));
        assertThat(exceptionalResult.isExceptional(), is(getCause() != null));
    }

    @Test
    public void builderExceptions1() {
        exception.expect(IllegalArgumentException.class);
        builder.setNumberOfModificationsMade(-1)
               .setModifiedName(getModifiedPath())
               .setModifiedContent(getModifiedContent())
               .build();
    }
    
    @Test
    public void builderExceptions2() {
        exception.expect(IllegalArgumentException.class);
        builder.setNumberOfModificationsMade(1)
               .setModifiedName(null)
               .setModifiedContent(getModifiedContent())
               .build();
    }

    @Test
    public void builderExceptions3() {
        exception.expect(IllegalArgumentException.class);
        builder.setNumberOfModificationsMade(1)
               .setModifiedName(getModifiedPath())
               .setModifiedContent(null)
               .build();
    }

    @Test
    public void builderExceptions4() {
        exception.expect(IllegalArgumentException.class);
        builder.setNumberOfModificationsMade(1)
               .setModifiedName(getModifiedPath())
               .setModifiedContent(getModifiedContent())
               .setExceptional(true)
               .build();
    }

    @Test
    public void builderExceptions5() {
        exception.expect(IllegalArgumentException.class);
        builder.setNumberOfModificationsMade(1)
               .setModifiedName(getModifiedPath())
               .setModifiedContent(getModifiedContent())
               .setCause(getCause())
               .build();
    }

    @Test
    public void builderExceptions6() {
        exception.expect(IllegalArgumentException.class);
        builder.setNumberOfModificationsMade(1)
               .setExceptional(true)
               .setCause(getCause())
               .build();
    }

    @Test
    public void builderExceptions7() {
        exception.expect(IllegalArgumentException.class);
        builder.setModifiedName(getModifiedPath())
               .setExceptional(true)
               .setCause(getCause())
               .build();
    }

    @Test
    public void builderExceptions8() {
        exception.expect(IllegalArgumentException.class);
        builder.setModifiedContent(getModifiedContent())
               .setExceptional(true)
               .setCause(getCause())
               .build();
    }

    @Test
    public void builderExceptions9() {
        exception.expect(IllegalArgumentException.class);
        builder.setExceptional(false)
               .setCause(getCause())
               .build();
    }

    @Test
    public void builderExceptions10() {
        exception.expect(IllegalArgumentException.class);
        builder.setExceptional(true)
               .setCause(null)
               .build();
    }
    
}
