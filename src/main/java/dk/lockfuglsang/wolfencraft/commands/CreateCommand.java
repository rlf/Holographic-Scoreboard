package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import dk.lockfuglsang.wolfencraft.config.ConfigWriter;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import dk.lockfuglsang.wolfencraft.util.LocationUtil;
import dk.lockfuglsang.wolfencraft.util.ResourceManager;
import dk.lockfuglsang.wolfencraft.util.TimeUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;

public class CreateCommand extends AbstractCommand {

    private final HolographicScoreboard plugin;
    private final ResourceManager rm;

    public CreateCommand(HolographicScoreboard plugin) {
        super("create|c", null, "id interval player|console cmd", "creates a hologram");
        this.plugin = plugin;
        rm = plugin.getRM();
    }

    @Override
    public boolean execute(CommandSender sender, String s, Map<String, Object> map, String... args) {
        if (args.length < 4) {
            sender.sendMessage(rm.format("msg.usage.create"));
            return true;
        }
        String id = args[0];
        String refresh = args[1];
        int interval = TimeUtil.getTimeAsTicks(refresh);
        if (interval < 200) {
            sender.sendMessage(rm.format("error.wrong.interval"));
            return false;
        }
        Scoreboard.Sender senderType;
        try {
            senderType = Scoreboard.Sender.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException ignored) {
            sender.sendMessage(rm.format("error.wrong.sender"));
            return false;
        }
        int ix = 3;
        Location location = LocationUtil.getLocation(args[ix]);
        if (location == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(rm.format("msg.usage.create.console"));
                return true;
            } else {
                Player player = (Player) sender;
                location = player.getEyeLocation();
            }
        } else {
            ix++;
        }
        String cmd = args[ix++];
        for (int i = ix; i < args.length; i++) {
            cmd += " " + args[i];
        }
        Scoreboard scoreboard = plugin.getScoreboard(id);
        if (scoreboard != null) {
            plugin.removeScoreboard(scoreboard);
        }
        scoreboard = new Scoreboard(id, refresh, senderType, cmd, location);
        plugin.addScoreboard(scoreboard);
        YamlConfiguration config = new YamlConfiguration();
        ConfigWriter.save(config, scoreboard);
        sender.sendMessage(rm.format("msg.scoreboard.created", id, config.saveToString()).split("\n"));
        plugin.scheduleUpdater(scoreboard);
        plugin.saveConfig();
        return true;
    }
}
