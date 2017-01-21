/**
 * 
 */
package dmv.desktop.searchandreplace.model;

import java.nio.charset.Charset;

/**
 * Class <tt>SearchProfileImpl.java</tt> implements 
 * {@link SearchProfile} interface keeping its invariants
 * (like 'what to find should not be null or empty', defaults).
 * <p>
 * It provides immutability and therefore has no public constructor.
 * Use {@link #getBuilder} method with 'what to find' argument 
 * to obtain a builder for initial creation of object of this type.
 * <p>
 * Each setter method will return new instance of SearchProfile.
 * @author dmv
 * @since 2017 January 02
 */
public class SearchProfileImpl implements SearchProfile {
    
    private final Charset charset;
    private final boolean filename;
    private final String toFind;
    private final String replaceWith;
    private final Exclusions exclusions;
    
    /* to be used with builder */
    private SearchProfileImpl(Charset charset, boolean filename, 
                              String toFind, String replaceWith, 
                              Exclusions exclusions) {
        this.charset = charset;
        this.filename = filename;
        this.toFind = toFind;
        this.replaceWith = replaceWith;
        this.exclusions = exclusions;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public boolean isFileName() {
        return filename;
    }

    @Override
    public String getToFind() {
        return toFind;
    }

    @Override
    public String getReplaceWith() {
        return replaceWith;
    }

    @Override
    public Exclusions getExclusions() {
        return exclusions;
    }
    
    @Override
    public SearchProfile setCharset(Charset charset) {
        return new SearchProfileBuilder(this)
                        .setCharset(charset)
                        .build();
    }

    @Override
    public SearchProfile setFilename(boolean filename) {
        return new SearchProfileBuilder(this)
                        .setFilename(filename)
                        .build();
    }

    /**
     * @throws IllegalArgumentException if given string is null or empty
     */
    @Override
    public SearchProfile setToFind(String toFind) {
        return new SearchProfileBuilder(this)
                        .setToFind(toFind)
                        .build();
    }

    @Override
    public SearchProfile setReplaceWith(String replaceWith) {
        return new SearchProfileBuilder(this)
                        .setReplaceWith(replaceWith)
                        .build();
    }

    @Override
    public SearchProfile setExclusions(Exclusions exclusions) {
        return new SearchProfileBuilder(this)
                        .setExclusions(exclusions)
                        .build();
    }

    @Override
    public String toString() {
        return String.format(
                "SearchProfile [charset=%s, filename=%s, toFind=%s, replaceWith=%s, exclusions=%s]",
                charset, filename, toFind, replaceWith, exclusions);
    }

    /**
     * Use this builder setter methods to create a new instance
     * of immutable {@link SearchProfile} object
     * @return builder that creates the {@link SearchProfile} object
     * @throws IllegalArgumentException if given string is null or empty
     */
    public static SearchProfileBuilder getBuilder(String toFind) {
        return new SearchProfileBuilder(toFind);
    }
    
    /**
     * Use this builder setter methods to create a new instance
     * of immutable {@link SearchProfile} object
     */
    public static class SearchProfileBuilder {
        
        private Charset charset;
        private boolean filename;
        private String toFind;
        private String replaceWith;
        private Exclusions exclusions;
        
        private SearchProfileBuilder(String toFind) {
            checkToFind(toFind);
            this.toFind = toFind;
            charset = defaultCharset;
            filename = defaultRenameRule;
            replaceWith = EMPTY_REPLACE;
            exclusions = EMPTY_EXCLUSIONS;
        }
        
        private SearchProfileBuilder(SearchProfile profile) {
            charset = profile.getCharset();
            filename = profile.isFileName();
            toFind = profile.getToFind();
            replaceWith = profile.getReplaceWith();
            exclusions = profile.getExclusions();
        }

        /**
         * Create new instance of {@link SearchProfile}
         * which is immutable
         * @return immutable {@link SearchProfile} object
         */
        public SearchProfile build() {
            return new SearchProfileImpl(charset, filename, toFind, replaceWith, exclusions);
        }

        /**
         * Set Charset which will be used for reading and 
         * writing into a file. If the null is passed 
         * then the {@link SearchProfile#defaultCharset} will be used
         * @param charset {@link Charset} 
         * @return this builder
         */
        public SearchProfileBuilder setCharset(Charset charset) {
            this.charset = charset != null ? charset : defaultCharset;
            return this;
        }
        
        /**
         * Set it to true if you need to rename files
         * with the same 'search and replace' rule as 
         * for their content. The word given in method
         * {@link #setReplaceWith(String)} should not
         * contain symbols that are invalid for filenames,
         * it won't be enforced though.
         * @param filename true - rename file, false - skip file name
         * @return this builder
         */
        public SearchProfileBuilder setFilename(boolean filename) {
            this.filename = filename;
            return this;
        }
        
        /**
         * Set string to be found and replaced.
         * It is not appropriate to have a null pointer
         * or an empty string in this role.
         * It should contain at least one character.
         * @param toFind String to be found
         * @throws IllegalArgumentException if given argument is null or empty
         * @return this builder
         */
        public SearchProfileBuilder setToFind(String toFind) {
            checkToFind(toFind);
            this.toFind = toFind;
            return this;
        }
        
        /**
         * Set new string that will be placed instead 
         * of 'what to find'. If this object is null or empty
         * means that found strings will be replaced with nothing.
         * Note, that if {@link #setFilename(boolean)} is set to true
         * this string should not contain symbols that are invalid 
         * for filenames, it won't be enforced or checked though.
         * @param replaceWith String to be replaced with
         * @return this builder
         */
        public SearchProfileBuilder setReplaceWith(String replaceWith) {
            this.replaceWith = replaceWith != null ? replaceWith : EMPTY_REPLACE;
            return this;
        }

        /**
         * Set new exclusions: suffixes and reversed prefixes
         * of toFind word. Those combinations will not be replaced.
         * Current exclusion set will be overwritten by this one, and
         * if given argument is null or empty nothing will be excluded
         * during the next 'search and replace' operation
         * @param exclusions New set of exclusions
         * @return this builder
         */
        public SearchProfileBuilder setExclusions(Exclusions exclusions) {
            this.exclusions = exclusions != null ? exclusions : EMPTY_EXCLUSIONS;
            return this;
        }

        private void checkToFind(String toFind) {
            if (toFind == null || toFind.length() < 1)
                throw new IllegalArgumentException(
                        "'What to find' should be at least one character long");
        }
    }

}
