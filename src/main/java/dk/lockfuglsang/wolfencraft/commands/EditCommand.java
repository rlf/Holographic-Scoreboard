package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.CompositeCommand;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import dk.lockfuglsang.wolfencraft.util.LocationUtil;
import dk.lockfuglsang.wolfencraft.util.TimeUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class EditCommand extends CompositeCommand {
    private final HolographicScoreboard plugin;

    public EditCommand(final HolographicScoreboard plugin) {
        super("edit|e", null, "id", "edits a scoreboard");
        this.plugin = plugin;
        add(new RequireScoreboardCommand(plugin, "filter", "reg-ex", "sets the filter") {
            @Override
            protected boolean doExecute(CommandSender sender, String alias, Map<String, Object> map, Scoreboard scoreboard, String... args) {
                scoreboard.setFilter(args.length > 0 ? args[0] : null);
                return true;
            }
        });
        add(new RequireScoreboardCommand(plugin, "addfilter", "reg-ex", "adds a filter") {
            @Override
            protected boolean doExecute(CommandSender sender, String alias, Map<String, Object> map, Scoreboard scoreboard, String... args) {
                String value = args.length > 0 ? args[0] : null;
                if (value != null) {
                    List<String> filter = scoreboard.getFilter();
                    filter.add(value);
                    scoreboard.setFilter(filter);
                    return true;
                } else {
                    return false;
                }
            }
        });
        add(new RequireScoreboardCommand(plugin, "location", "location", "sets the location of the scoreboard") {
            @Override
            protected boolean doExecute(CommandSender sender, String alias, Map<String, Object> map, Scoreboard scoreboard, String... args) {
                String value = args.length > 0 ? args[0] : null;
                Location location = LocationUtil.getLocation(value);
                if (location == null) {
                    sender.sendMessage(plugin.getRM().format("error.wrong.location"));
                    return false;
                } else {
                    scoreboard.setLocation(location);
                }
                return true;
            }
        });
        add(new RequireScoreboardCommand(plugin, "delay|interval", "interval", "sets the delay or interval of the scoreboard") {
            @Override
            protected boolean doExecute(CommandSender sender, String alias, Map<String, Object> map, Scoreboard scoreboard, String... args) {
                String value = args.length > 0 ? args[0] : null;
                int time = TimeUtil.getTimeAsTicks(value);
                if (time == 0) {
                    sender.sendMessage(plugin.getRM().format("error.wrong." + alias));
                    return false;
                } else if (alias.equals("delay")) {
                    scoreboard.setDelay(value);
                } else if (alias.equals("interval")) {
                    scoreboard.setInterval(value);
                }
                return true;
            }
        });
    }

    @Override
    public boolean execute(CommandSender sender, String alias, Map<String, Object> data, String... args) {
        String scoreName = args.length > 0 ? args[0] : "";
        Scoreboard scoreboard = plugin.getScoreboard(scoreName);
        if (scoreboard != null) {
            data.put("scoreboard", scoreboard);
            if (super.execute(sender, alias, data, args) && args.length > 2) {
                sender.sendMessage(plugin.getRM().format("msg.scoreboard.edit", args[1], scoreName, args[2]));
                return true;
            }
            return false;
        }
        sender.sendMessage(plugin.getRM().format("error.scoreboard.notfound", scoreName));
        return false;
    }
}
