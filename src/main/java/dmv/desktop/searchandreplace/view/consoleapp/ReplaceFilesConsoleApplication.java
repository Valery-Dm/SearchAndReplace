package dmv.desktop.searchandreplace.view.consoleapp;

import java.nio.file.Paths;
import java.util.List;

import dmv.desktop.searchandreplace.model.*;
import dmv.desktop.searchandreplace.service.SearchAndReplace;

public class ReplaceFilesConsoleApplication {
    
    private SearchAndReplace<SearchFolder, SearchFile, List<FileSearchResult>> replacer;

    public ReplaceFilesConsoleApplication() {
        
    }
    
    public static void main(String[] args) {
        SearchFolder folder = new SearchFolderImpl();
        folder.setFolder(Paths.get("src/test/resources"))
              .setFileTypes("*.java", "*.project", "*.xml")
              .setSubfolders(true);
        SearchFile profile = new SearchFileImpl("test");
        
        ReplaceFilesConsoleApplication app = new ReplaceFilesConsoleApplication();
        //app.replacer = new FolderWalker(folder, profile);
        
        app.replacer.preview();
    }
}
