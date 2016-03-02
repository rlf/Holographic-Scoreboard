package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import dk.lockfuglsang.wolfencraft.util.ResourceManager;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class RefreshCommand extends AbstractCommand {
    private final HolographicScoreboard plugin;
    private final ResourceManager rm;

    public RefreshCommand(HolographicScoreboard plugin) {
        super("refresh", null, "?id", "refreshes all or specific scoreboards");
        this.plugin = plugin;
        rm = plugin.getRM();
    }

    @Override
    public boolean execute(CommandSender sender, String s, Map<String, Object> map, String... args) {
        if (args.length == 1) {
            if (args[0].equals("*")) {
                return plugin.refreshAll(sender);
            } else {
                Scoreboard scoreboard = plugin.getScoreboard(args[0]);
                if (scoreboard != null) {
                    scoreboard.refreshView(plugin);
                    sender.sendMessage(rm.format("msg.scoreboard.refresh", args[0]));
                    return true;
                } else {
                    sender.sendMessage(rm.format("error.scoreboard.notfound", args[0]));
                    return false;
                }
            }
        } else if (args.length == 0) {
            return plugin.refreshAll(sender);
        }
        return false;
    }
}
