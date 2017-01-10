package dmv.desktop.searchandreplace.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dmv.desktop.searchandreplace.collection.Tuple;


public class SearchResultImplTest extends SearchResultTest {

    @Override
    protected SearchResult getSearchResult(Path originalPath, Path modifiedPath,
            List<String> originalContent, List<String> modifiedContent,
            int numberOfModifications, boolean exceptional, Throwable cause) {
        Tuple<Path, Path> name = originalPath == null ? null : new Tuple<>(originalPath, modifiedPath);
        List<Tuple<String, String>> content = new ArrayList<>();
        if (originalContent != null && modifiedContent != null) {
            Iterator<String> origIterator = originalContent.iterator();
            Iterator<String> modIterator = modifiedContent.iterator();
            while (origIterator.hasNext() && modIterator.hasNext())
                content.add(new Tuple<>(origIterator.next(), modIterator.next()));
        }
        return new SearchResultImpl(numberOfModifications, name, content, exceptional, cause);
    }

}
