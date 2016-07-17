package dk.lockfuglsang.wolfencraft.intercept;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;

/**
 * Intercepts messages passed from commands to "sender", so we can print them on scoreboards
 */
public class BufferedConsoleSender implements BufferedSender {
    private BufferedHandler handler;
    private ConsoleCommandSender proxy;
    private ByteArrayOutputStream baos;

    public BufferedConsoleSender(ConsoleCommandSender sender) {
        baos = new ByteArrayOutputStream();
        handler = new BufferedHandler(sender, baos);
        proxy = (ConsoleCommandSender) Proxy.newProxyInstance(BufferedConsoleSender.class.getClassLoader(), new Class<?>[]{ConsoleCommandSender.class}, handler);
    }

    @Override
    public String getStdout() {
        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Your server does not support UTF-8! Go cry in a corner!");
        }
    }

    @Override
    public CommandSender getSender() {
        return proxy;
    }

}
