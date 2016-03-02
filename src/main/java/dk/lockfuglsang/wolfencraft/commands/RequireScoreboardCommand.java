package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import org.bukkit.command.CommandSender;

import java.util.Map;

public abstract class RequireScoreboardCommand extends AbstractCommand {
    private final HolographicScoreboard plugin;

    public RequireScoreboardCommand(HolographicScoreboard plugin, String name, String parms, String description) {
        super(name, null, parms, description);
        this.plugin = plugin;
    }

    @Override
    public final boolean execute(CommandSender sender, String alias, Map<String, Object> map, String... args) {
        Object scoreboard = map.get("scoreboard");
        if (!(scoreboard instanceof Scoreboard)) {
            return false;
        }
        return doExecute(sender, alias, map, (Scoreboard) scoreboard, args);
    }

    protected abstract boolean doExecute(CommandSender sender, String alias, Map<String, Object> map, Scoreboard scoreboard, String... args);
}
