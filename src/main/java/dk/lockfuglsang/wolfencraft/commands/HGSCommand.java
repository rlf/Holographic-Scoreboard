package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.AbstractCommandExecutor;
import dk.lockfuglsang.minecraft.command.completion.AbstractTabCompleter;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import dk.lockfuglsang.wolfencraft.util.ResourceManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Commands from the uSkyBlock common bukkit utils
 */
public class HGSCommand extends AbstractCommandExecutor {
    private final HolographicScoreboard plugin;

    public HGSCommand(final HolographicScoreboard plugin) {
        super("hgs", "holographicscoreboard.admin", "main hgs command");
        this.plugin = plugin;
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

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] args) {
        if (!plugin.isDependenciesFulfilled()) {
            if (commandSender.hasPermission(getPermission())) {
                commandSender.sendMessage(ResourceManager.getRM().format("log.missing.dependencies").split("\n"));
            }
            return true;
        }
        return super.onCommand(commandSender, command, alias, args);
    }
}
