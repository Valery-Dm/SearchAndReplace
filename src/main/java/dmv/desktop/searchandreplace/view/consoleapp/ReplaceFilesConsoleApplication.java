package dmv.desktop.searchandreplace.view.consoleapp;

import java.nio.file.Paths;
import java.util.List;

import dmv.desktop.searchandreplace.model.FileReplacements;
import dmv.desktop.searchandreplace.model.SearchFolder;
import dmv.desktop.searchandreplace.model.SearchFolderImpl;
import dmv.desktop.searchandreplace.service.SearchAndReplace;

public class ReplaceFilesConsoleApplication {
    
    private SearchAndReplace<SearchFolder, List<FileReplacements>> replacer;

    public ReplaceFilesConsoleApplication() {
        
    }
    
    public static void main(String[] args) {
        SearchFolder folder = new SearchFolderImpl();
        folder.setFolder(Paths.get("res"))
              .setFileTypes("*.java", "*.project", "*.xml")
              .setFileNames(true)
              .setSubfolders(true);
        
        ReplaceFilesConsoleApplication app = new ReplaceFilesConsoleApplication();
    }
}
