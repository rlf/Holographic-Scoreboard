package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import dk.lockfuglsang.wolfencraft.util.ResourceManager;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class RemoveCommand extends AbstractCommand {
    private final HolographicScoreboard plugin;
    private final ResourceManager rm;

    public RemoveCommand(HolographicScoreboard plugin) {
        super("remove|rm", null, "id", "removes a scoreboard");
        this.plugin = plugin;
        rm = plugin.getRM();
    }

    @Override
    public boolean execute(CommandSender sender, String s, Map<String, Object> map, String... args) {
        if (args.length != 1) {
            sender.sendMessage(rm.format("msg.usage.remove"));
            return true;
        }
        String scoreName = args[0];
        Scoreboard scoreboard = plugin.getScoreboard(scoreName);
        if (scoreboard == null) {
            sender.sendMessage(rm.format("error.scoreboard.notfound", scoreName));
            return false;
        }
        if (plugin.removeScoreboard(scoreboard)) {
            sender.sendMessage(rm.format("msg.scoreboard.removed", scoreName));
            plugin.saveConfig();
            return true;
        }
        return false;
    }
}
