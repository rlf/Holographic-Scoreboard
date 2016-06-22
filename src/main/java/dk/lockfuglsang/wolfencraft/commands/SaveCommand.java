package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class SaveCommand extends AbstractCommand {
    private final HolographicScoreboard plugin;

    public SaveCommand(HolographicScoreboard plugin) {
        super("save", "saves the scoreboards to the config");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String s, Map<String, Object> map, String... strings) {
        plugin.saveConfig();
        sender.sendMessage(plugin.getRM().format("msg.saved"));
        return true;
    }
}
