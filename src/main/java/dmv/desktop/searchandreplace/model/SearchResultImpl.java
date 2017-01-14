/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import static java.util.Collections.unmodifiableList;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import dmv.desktop.searchandreplace.collection.Tuple;
import dmv.desktop.searchandreplace.collection.UnmodifiableTuple;


/**
 * Immutable <tt>SearchResultImpl.java</tt> is a collection
 * of computed result of file searching and replacing operation.
 * Arguments will be copied at construction time into unmodifiable
 * collection types for safe return
 * @author dmv
 * @since 2017 January 03
 */
public class SearchResultImpl implements SearchResult {
    
    private final int numberOfModificationsMade;
    private final Tuple<Path, Path> modifiedName;
    private final List<Tuple<String, String>> modifiedContent;
    
    private final boolean exceptional;
    private final Throwable cause;
    
    /**
     * Create result with given parameters
     * @param numberOfModificationsMade How many replacements were done in total
     *                                  (including filename changes)
     * @param modifiedName Contains both original and modified name,
     *                     if name was not modified, last path should be null
     * @param modifiedContent Contains original and modified content lines,
     *                        should only contain lines with done replacements 
     * @param exceptional Is this result creation was interrupted. Usually
     *                    other parameters may not present in this case
     * @param cause The cause of interruption
     * @throws IllegalArgumentException if provided data is not consistent,
     *                                  see {@link SearchResult#NOT_CONSISTENT}
     */
    public SearchResultImpl(int numberOfModificationsMade,
                            Tuple<Path, Path> modifiedName,
                            List<Tuple<String, String>> modifiedContent, 
                            boolean exceptional,
                            Throwable cause) {
        checkConsistency(numberOfModificationsMade, modifiedName,
                         modifiedContent, exceptional, cause);
        this.numberOfModificationsMade = numberOfModificationsMade;
        this.modifiedName = copy(modifiedName);
        this.modifiedContent = copy(modifiedContent);
        this.exceptional = exceptional;
        this.cause = cause;
    }

    /*
     * Private constructors to be used with Builder:
     * will set either normal result's variables or create
     * exceptional result where is no content allowed.
     * Avoids another content copying operation.
     */
    private SearchResultImpl(int numberOfModificationsMade,
                            Tuple<Path, Path> modifiedName,
                            List<Tuple<String, String>> modifiedContent) {
        this.numberOfModificationsMade = numberOfModificationsMade;
        this.modifiedName = modifiedName;
        this.modifiedContent = modifiedContent;
        this.exceptional = false;
        this.cause = null;
    }
    
    private SearchResultImpl(boolean exceptional,
                             Throwable cause) {
        this.numberOfModificationsMade = 0;
        this.modifiedName = null;
        this.modifiedContent = null;
        this.exceptional = exceptional;
        this.cause = cause;
    }
    
    public static SearchResultBuilder getBuilder() {
        return new SearchResultBuilder();
    }

    private static void checkConsistency(int numberOfModificationsMade,
                                         Tuple<Path, Path> modifiedName,
                                         List<Tuple<String, String>> modifiedContent, 
                                         boolean exceptional,
                                         Throwable cause) {
        // start optimistic
        boolean consistent = true;
        if (exceptional) {
            // expect exceptional result only: the cause must exist,
            // content collections should be null, zero modifications
            if (cause == null || numberOfModificationsMade != 0 ||
                modifiedContent != null || modifiedName != null)
                consistent = false;
        } else {
            // non-exceptional result should contain non-null objects
            if (cause != null || numberOfModificationsMade < 0 ||
                modifiedContent == null || modifiedName == null)
                consistent = false;
        }
        if (!consistent) throw NOT_CONSISTENT;
    }

    private static Tuple<Path, Path> copy(Tuple<Path, Path> modifiedName) {
        return modifiedName == null ? null : new UnmodifiableTuple<>(modifiedName);
    }

    private static List<Tuple<String, String>> copy(List<Tuple<String, String>> modifiedContent) {
        return modifiedContent == null ? null :
               unmodifiableList(modifiedContent.stream()
                                               .map(tuple -> new UnmodifiableTuple<>(tuple))
                                               .collect(Collectors.toList()));
    }

    /**
     * @return {@link UnmodifiableTuple} object with original and 
     *         modified file name, second parameter may be null if 
     *         no modifications were made within the file name
     */
    @Override
    public Tuple<Path, Path> getModifiedName() {
        return modifiedName;
    }

    /**
     * @return Unmodifiable List with {@link UnmodifiableTuple} objects
     *         with file lines before and after modifications
     */
    @Override
    public List<Tuple<String, String>> getModifiedContent() {
        return modifiedContent;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchResult#numberOfModificationsMade()
     */
    @Override
    public int numberOfModificationsMade() {
        return numberOfModificationsMade;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchResult#isExceptional()
     */
    @Override
    public boolean isExceptional() {
        return exceptional;
    }

    /* (non-Javadoc)
     * @see dmv.desktop.searchandreplace.model.SearchResult#getCause()
     */
    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        StringBuilder modContent = new StringBuilder();
        if (modifiedName != null) {
            String modName = modifiedName.getLast() == null ?
                    "name was not modified" : "new file name is " +
                    modifiedName.getLast().getFileName();
            modContent.append("\nResults for a file ")
                      .append(modifiedName.getFirst())
                      .append(":\n")
                      .append(modName)
                      .append("\nNumber Of modifications = ")
                      .append(numberOfModificationsMade);
        }
        if (modifiedContent != null) {
            int maxLen = 10;
            for (Tuple<String, String> line : modifiedContent) {
                if (maxLen-- == 0) break;
                modContent.append("\noriginal: ")
                          .append(line.getFirst())
                          .append("\nmodified: ")
                          .append(line.getLast());
            }
        }
        if (exceptional && cause != null)
            modContent.append("\nprocess was interrupted because:\n")
                      .append(cause.getMessage());
        return modContent.toString();
    }

    /**
     * Class <tt>SearchResultBuilder</tt> offers methods
     * for convenient creation {@link SearchResult} objects,
     * or changing existing ones while keeping their integrity.
     */
    public static class SearchResultBuilder {
        
        private int numberOfModificationsMade;
        private Tuple<Path, Path> modifiedName;
        private List<Tuple<String, String>> modifiedContent;
        
        private boolean exceptional;
        private Throwable cause;
        
        private SearchResultBuilder() {}
        
        /**
         * Builds either normal or exceptional result, depends
         * on provided data or throws {@link SearchResult#NOT_CONSISTENT}
         * exception if data is not consistent
         * @return SearchResult object
         * @throws IllegalArgumentException if data provided earlier is
         *                                  not consistent
         */
        public SearchResult build() {
            checkConsistency(numberOfModificationsMade, 
                             modifiedName, modifiedContent, 
                             exceptional, cause);
            return exceptional ? new SearchResultImpl(exceptional, cause) :
                                 new SearchResultImpl(numberOfModificationsMade, 
                                                      modifiedName, modifiedContent);
        }
        
        /**
         * Can be used to reset some parameters of existing result.
         * Avoids another defensive copying for old collections.
         * For each new one copying will be done in set...() methods.
         * @param result Existing result to play with
         * @return this builder
         * @throws NullPointerException if argument is null
         */
        public SearchResultBuilder setResult(SearchResult result) {
            Objects.requireNonNull(result);
            numberOfModificationsMade = result.numberOfModificationsMade();
            modifiedName = result.getModifiedName();
            modifiedContent = result.getModifiedContent();
            exceptional = result.isExceptional();
            cause = result.getCause();
            return this;
        }
        
        /**
         * Set new number of modifications
         * @param numberOfModificationsMade new number
         * @return this builder
         */
        public SearchResultBuilder setNumberOfModificationsMade(int numberOfModificationsMade) {
            this.numberOfModificationsMade = numberOfModificationsMade;
            return this;
        }
        
        /**
         * Set new modified name
         * @param modifiedName new modified name
         * @return this builder
         */
        public SearchResultBuilder setModifiedName(Tuple<Path, Path> modifiedName) {
            this.modifiedName = copy(modifiedName);
            return this;
        }
        
        /**
         * Set new modified content
         * @param modifiedContent new modified content
         * @return this builder
         */
        public SearchResultBuilder setModifiedContent(List<Tuple<String, String>> modifiedContent) {
            this.modifiedContent = copy(modifiedContent);
            return this;
        }
        
        /**
         * Mark result as exceptional (true), or normal (false)
         * @param exceptional exceptional (true), or normal (false) result
         * @return this builder
         */
        public SearchResultBuilder setExceptional(boolean exceptional) {
            this.exceptional = exceptional;
            return this;
        }
        
        /**
         * Set new cause of exception
         * @param cause new cause of exception
         * @return this builder
         */
        public SearchResultBuilder setCause(Throwable cause) {
            this.cause = cause;
            return this;
        }
    }
}
