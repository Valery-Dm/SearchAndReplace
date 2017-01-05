/**
 * 
 */
package dmv.desktop.searchandreplace.worker;

import java.util.function.Consumer;

import dmv.desktop.searchandreplace.model.FileReplacements;

/**
 * Class <tt>FileExcluder.java</tt>
 * @author dmv
 * @since 2017 January 03
 */
public class FileExcluder implements Consumer<FileReplacements> {

    @Override
    public void accept(FileReplacements replacements) {
        System.out.println(replacements.getFileName());
        replacements.getModifiedContent().forEach(System.out::println);
        replacements.addContentLine("Test line from FileExcluder");
    }

}
