package dmv.desktop.searchandreplace.view.consoleapp.menu;

import static dmv.desktop.searchandreplace.view.consoleapp.utility.CmdUtils.EXIT_COMMANDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;

import dmv.desktop.searchandreplace.view.consoleapp.ConsoleApplication;


public abstract class ConsoleMenuTest {
    
    private ByteArrayOutputStream output;
    private ConsoleApplication program;
    private ConsoleMenu previous;
    private ConsoleMenu target;
    
    abstract protected ConsoleMenu getTarget(ConsoleApplication app, ConsoleMenu from);
    
    abstract protected ConsoleApplication getApp();
    
    abstract protected boolean isMenuShown(String output);

    abstract protected boolean isMenuHelpShown(String output);

    @Before
    public void setUp() throws Exception {
        previous = mock(ConsoleMenu.class);
        program = getApp() != null ? getApp() : mock(ConsoleApplication.class);
        target = getTarget(program, previous);
    }

    @Test(expected=IllegalArgumentException.class)
    public void constructorNullMainProgram() {
        getTarget(null, previous);
    }
    
    @Test
    public void constructorNullMenu() {
        // first menu in a program case
        target = getTarget(program, null);
        target.cancel();
        // nowhere to go
        assertThat(target.next(), is(nullValue()));
    }

    @Test
    public void testShowMenu() throws UnsupportedEncodingException {
        catchOutput();
        target.showMenu();
        assertTrue(isMenuShown(output.toString()));
        resetOutput();
    }

    @Test
    public void testShowMenuHelp() throws UnsupportedEncodingException {
        catchOutput();
        target.showMenuHelp();
        assertTrue(isMenuHelpShown(output.toString()));
        resetOutput();
    }

    @Test
    public void testAcceptExitCommand() {
        target.accept(new String[]{EXIT_COMMANDS.get(0)});
        // this requires getApp() method return mocked object
        verify(program).exit();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAcceptEmpty() {
        target.accept(new String[]{});
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAcceptWrongKey() {
        target.accept(new String[]{"qwerty"});
    }

    @Test
    public void testNextAndCancel() {
        ConsoleMenu next = target.next();
        // HelpMenu may point to itself by default
        // others should not be set yet
        assertTrue(next == null || next == target);
        target.cancel();
        assertThat(target.next(), is(previous));
        target.clearNext();
        assertThat(target.next(), is(nullValue()));
    }

    @Test
    public void testShowMainHelp() {
        // Nothing will be shown right now.
        // In menu flow this method implies that
        // next menu should be of HelpMenu type
        target.showMainHelp();
        assertTrue(target.next() instanceof HelpMenu);
    }

    @Test
    public void testCancelAndExit() {
        target.cancel();
        assertThat(target.next(), is(previous));
        target.exit();
        // this requires getApp() method return mocked object
        verify(program).exit();
        assertThat(target.next(), is(nullValue()));
        
    }

    private void catchOutput() throws UnsupportedEncodingException {
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, "UTF-8"));
    }

    private void resetOutput() {
        System.setOut(System.out);
    }

}
