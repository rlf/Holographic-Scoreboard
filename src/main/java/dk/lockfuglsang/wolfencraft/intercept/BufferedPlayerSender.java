package dk.lockfuglsang.wolfencraft.intercept;

import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Would be nice - currently broken (won't stay broken - hopefully)
 */
public class BufferedPlayerSender implements BufferedSender {
    private final ByteArrayOutputStream baos;
    private Player proxy;

    public BufferedPlayerSender(Player player) {
        baos = new ByteArrayOutputStream();
        // Use reflection to try to get the handle...
        interceptPlayerConnection(player);
        proxy = player;
    }

    private void interceptPlayerConnection(Player player) {
        HolographicScoreboard.interceptor.intercept(player, baos);
    }

    @Override
    public String getStdout() {
        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("You are out of luck, you still live in the 1980s");
        } finally {
            HolographicScoreboard.interceptor.stopIntercepting(proxy);
        }
    }

    @Override
    public CommandSender getSender() {
        return proxy;
    }

}
