package dk.lockfuglsang.wolfencraft.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Proxy;

/**
 * Would be nice - currently broken (won't stay broken - hopefully)
 */
public class BufferedPlayerSender implements BufferedSender {
    private BufferedHandler handler;
    private Player proxy;

    public BufferedPlayerSender(Player player) {
        handler = new BufferedHandler(player);
        // If only this worked!
        // But it gives an IllegalArgumentException, due to the _INVALID_getLastDamage() on LivingEntity :(
        proxy = (Player) Proxy.newProxyInstance(BufferedPlayerSender.class.getClassLoader(), new Class<?>[] {HumanEntity.class, Player.class, LivingEntity.class}, handler);
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
