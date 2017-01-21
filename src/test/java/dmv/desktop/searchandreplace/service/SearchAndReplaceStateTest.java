package dmv.desktop.searchandreplace.service;

import static dmv.desktop.searchandreplace.service.SearchAndReplace.State.*;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class SearchAndReplaceStateTest {

   @Test
    public void testAdvance() {
        assertTrue(BEFORE_FIND.getAdvance()   < FIND_OTHER.getAdvance());
        assertTrue(FIND_OTHER.getAdvance()    < EXCLUDE_OTHER.getAdvance());
        assertTrue(EXCLUDE_OTHER.getAdvance() < AFTER_FOUND.getAdvance());
        assertTrue(AFTER_FOUND.getAdvance()   < COMPUTED.getAdvance());
        assertTrue(COMPUTED.getAdvance()      < REPLACED.getAdvance());
        assertTrue(REPLACED.getAdvance()      < INTERRUPTED.getAdvance());
    }

}
