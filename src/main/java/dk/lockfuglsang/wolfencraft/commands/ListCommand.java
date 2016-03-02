package dk.lockfuglsang.wolfencraft.commands;

import dk.lockfuglsang.minecraft.command.AbstractCommand;
import dk.lockfuglsang.wolfencraft.HolographicScoreboard;
import dk.lockfuglsang.wolfencraft.config.Scoreboard;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.Set;

public class ListCommand extends AbstractCommand {
    private static final String[] STRIPES = {"\u00a77", "\u00a7f"};
    private final HolographicScoreboard plugin;

    public ListCommand(HolographicScoreboard plugin) {
        super("list", "lists the holograms");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String s, Map<String, Object> map, String... args) {
        StringBuilder sb = new StringBuilder();
        Set<Scoreboard> scoreboards = plugin.getScoreboards();
        if (scoreboards == null || scoreboards.isEmpty()) {
            sb.append(plugin.getRM().format("msg.scoreboards.empty"));
        } else {
            int cnt = 0;
            for (Scoreboard scoreboard : scoreboards) {
                sb.append(STRIPES[(cnt++ % 2)]);
                sb.append(scoreboard.getId());
                sb.append(" ");
            }
            sender.sendMessage(sb.toString().trim());
        }
        return true;
    }
}
