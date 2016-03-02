package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class ReloadCommand extends AbstractCommand {
    private final HolographicScoreboard plugin;

    public ReloadCommand(HolographicScoreboard plugin) {
        super("reload", "reloads the config");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String s, Map<String, Object> map, String... strings) {
        plugin.reloadConfig();
        sender.sendMessage(plugin.getRM().format("msg.reload"));
        return true;
    }
}
