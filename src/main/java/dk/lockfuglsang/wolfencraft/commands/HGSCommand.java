package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.AbstractCommandExecutor;
import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Commands from the uSkyBlock common bukkit utils
 */
public class HGSCommand extends AbstractCommandExecutor {
    public HGSCommand(final HolographicScoreboard plugin) {
        super("hgs", "holographicscoreboard.admin", "main hgs command");
        addTab("id", new AbstractTabCompleter() {
            @Override
            protected List<String> getTabList(CommandSender commandSender, String s) {
                List<String> ids = new ArrayList<>();
                for (Scoreboard scoreboard : plugin.getScoreboards()) {
                    ids.add(scoreboard.getId());
                }
                Collections.sort(ids);
                return ids;
            }
        });
        addTab("player|console", new AbstractTabCompleter() {
            @Override
            protected List<String> getTabList(CommandSender commandSender, String s) {
                return Arrays.asList("console", "player");
            }
        });
        addTab("interval", new AbstractTabCompleter() {
            @Override
            protected List<String> getTabList(CommandSender commandSender, String s) {
                return Arrays.asList("10s", "30s", "5m", "10m", "30m", "1h");
            }
        });
        add(new CreateCommand(plugin));
        add(new ListCommand(plugin));
        add(new RemoveCommand(plugin));
        add(new MoveCommand(plugin));
        add(new RefreshCommand(plugin));
        add(new InfoCommand(plugin));
        add(new SaveCommand(plugin));
        add(new ReloadCommand(plugin));
        add(new EditCommand(plugin));
    }
}
