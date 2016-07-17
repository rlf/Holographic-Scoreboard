package dk.lockfuglsang.wolfencraft.util;

import dk.lockfuglsang.wolfencraft.intercept.BufferedConsoleSender;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.Test;

import java.beans.PropertyChangeListener;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class BufferedHandlerTest {
    public static final String NL = System.lineSeparator();

    @Test
    public void testInvokeOnString() {
        // Arrange
        ConsoleCommandSender sender = mock(ConsoleCommandSender.class);
        BufferedConsoleSender consoleSender = new BufferedConsoleSender(sender);

        // Act
        consoleSender.getSender().sendMessage("Test");

        // Assert
        //verify(sender).sendMessage("Test");
        assertThat(consoleSender.getStdout(), is("Test" + NL));
    }

    @Test
    public void testInvokeOnStringArray() {
        // Arrange
        ConsoleCommandSender sender = mock(ConsoleCommandSender.class);
        BufferedConsoleSender consoleSender = new BufferedConsoleSender(sender);
        String[] expected = new String[] { "TEST", "ABE"};

        // Act
        consoleSender.getSender().sendMessage(expected);

        // Assert
        //verify(sender).sendMessage(expected);
        assertThat(consoleSender.getStdout(), is("TEST" + NL + "ABE" + NL));
    }

    @Test
    public void testMultipleInvocations() {
        // Arrange
        ConsoleCommandSender sender = mock(ConsoleCommandSender.class);
        BufferedConsoleSender consoleSender = new BufferedConsoleSender(sender);
        PropertyChangeListener listener = mock(PropertyChangeListener.class);

        // Act
        consoleSender.getSender().sendMessage("Hello World");
        consoleSender.getSender().sendMessage(new String[] {"A", "B", "C"});

        // Assert
        assertThat(consoleSender.getStdout(), is("Hello World" + NL + "A" + NL + "B" + NL + "C" + NL));
    }

}