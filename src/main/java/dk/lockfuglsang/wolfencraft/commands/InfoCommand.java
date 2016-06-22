package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import dk.lockfuglsang.wolfencraft.config.ConfigWriter;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;

public class InfoCommand extends AbstractCommand {
    private final HolographicScoreboard plugin;

    public InfoCommand(HolographicScoreboard plugin) {
        super("info|i", null, "id", "shows information about a scoreboard");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String s, Map<String, Object> map, String... args) {
        Scoreboard scoreboard = plugin.getScoreboard(args != null && args.length > 0 ? args[0] : null);
        if (scoreboard != null) {
            YamlConfiguration c = new YamlConfiguration();
            ConfigWriter.save(c, scoreboard);
            sender.sendMessage(c.saveToString());
            return true;
        }
        return false;
    }
}
