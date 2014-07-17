package dk.lockfuglsang.wolfencraft.util;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.lang.reflect.Proxy;

/**
 * Intercepts messages passed from commands to "sender", so we can print them on scoreboards
 */
public class BufferedConsoleSender implements BufferedSender {
    private BufferedHandler handler;
    private ConsoleCommandSender proxy;

    public BufferedConsoleSender(ConsoleCommandSender player) {
        handler = new BufferedHandler(player);
        proxy = (ConsoleCommandSender) Proxy.newProxyInstance(BufferedPlayerSender.class.getClassLoader(), new Class<?>[]{ConsoleCommandSender.class}, handler);
    }

    @Override
    public String getStdout() {
        return handler.getStdout();
    }

    @Override
    public CommandSender getSender() {
        return proxy;
    }

}
