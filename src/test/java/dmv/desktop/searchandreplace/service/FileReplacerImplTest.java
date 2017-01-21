package dmv.desktop.searchandreplace.service;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import dmv.desktop.searchandreplace.model.SearchProfile;
import dmv.desktop.searchandreplace.model.SearchProfileImpl;


public class FileReplacerImplTest extends FileReplacerTest {

    @Override
    protected FileReplacer createTarget(Path file, SearchProfile profile) {
        return new FileReplacerImpl(file, profile);
    }
    
    @Override
    protected boolean isContentCached() {
        return true;
    }

    @Test(expected=NullPointerException.class)
    public void constructorNullFile() {
        new FileReplacerImpl(null, SearchProfileImpl.getBuilder("res").build());
    }

    @Test(expected=NullPointerException.class)
    public void constructorNullProfile() {
        new FileReplacerImpl(Paths.get("file"), null);
    }
}
