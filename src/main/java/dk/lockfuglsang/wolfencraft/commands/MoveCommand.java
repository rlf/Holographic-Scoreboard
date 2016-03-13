package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import dk.lockfuglsang.wolfencraft.util.LocationUtil;
import dk.lockfuglsang.wolfencraft.util.ResourceManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class MoveCommand extends AbstractCommand {
    private final HolographicScoreboard plugin;
    private final ResourceManager rm;

    public MoveCommand(HolographicScoreboard plugin) {
        super("move|mv", null, "id ?location", "moves a scoreboard");
        this.plugin = plugin;
        rm = plugin.getRM();
    }

    @Override
    public boolean execute(CommandSender sender, String s, Map<String, Object> map, String... args) {
        if (args == null || args.length == 0) {
            sender.sendMessage(rm.format("msg.usage.move"));
            return true;
        }
        Scoreboard scoreboard = plugin.getScoreboard(args[0]);
        if (scoreboard != null) {
            Location location = null;
            try {
                if (args.length == 1 && sender instanceof Player) {
                    location = ((Player) sender).getEyeLocation();
                } else if (args.length == 2) {
                    location = LocationUtil.getLocation(args[1]);
                }
                if (location == null) {
                    throw new IllegalArgumentException("No valid location was found");
                }
            } catch (IllegalArgumentException e) {
                sender.sendMessage(rm.format("msg.usage.move"));
                return true;
            }
            scoreboard.setLocation(location);
            scoreboard.refreshView(plugin);
            sender.sendMessage(rm.format("msg.scoreboard.moved", args[1], location.getX(), location.getY(), location.getZ(), location.getWorld().getName()));
            return true;
        }
        sender.sendMessage(rm.format("error.scoreboard.notfound", args[1]));
        return false;
    }
}
