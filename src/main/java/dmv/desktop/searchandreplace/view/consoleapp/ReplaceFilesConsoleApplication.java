package dmv.desktop.searchandreplace.view.consoleapp;

import java.nio.file.Paths;
import java.util.List;

import dmv.desktop.searchandreplace.model.*;
import dmv.desktop.searchandreplace.service.SearchAndReplace;

public class ReplaceFilesConsoleApplication {
    
    private SearchAndReplace<SearchPath, SearchProfile, List<SearchResult>> replacer;

    public ReplaceFilesConsoleApplication() {
        
    }
    
    public static void main(String[] args) {
        SearchPath folder = new SearchPathImpl(Paths.get("src/test/resources"));
        folder.setNamePattern("*.java", "*.project", "*.xml")
              .setSubfolders(true);
        SearchProfile profile = new SearchProfileImpl("test");
        
        ReplaceFilesConsoleApplication app = new ReplaceFilesConsoleApplication();
        //app.replacer = new FolderWalker(folder, profile);
        
        app.replacer.preview();
    }
}
