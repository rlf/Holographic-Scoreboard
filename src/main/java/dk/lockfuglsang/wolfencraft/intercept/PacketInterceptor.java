package dk.lockfuglsang.wolfencraft.intercept;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import dk.lockfuglsang.minecraft.util.JSONUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.reflection.ReflectionUtil.*;

public class PacketInterceptor {
    private static final Logger log = Logger.getLogger(PacketInterceptor.class.getName());
    private static final Map<UUID, ByteArrayOutputStream> interceptMap = new ConcurrentHashMap<>();
    private static final Map<String, Player> dummies = new ConcurrentHashMap<>();
    private volatile static List<Object> players = null;

    public PacketInterceptor() {
    }

    public void intercept(Player player, ByteArrayOutputStream baos) {
        interceptMap.put(player.getUniqueId(), baos);
        players.add(exec(player, "getHandle"));
    }

    public void stopIntercepting(Player player) {
        interceptMap.remove(player.getUniqueId());
        players.remove(exec(player, "getHandle"));
    }

    public Player createDummyPlayer(String id, Location location) {
        if (dummies.containsKey(id)) {
            return dummies.get(id);
        }
        try {
            Object craftServer = Bukkit.getServer();
            Object minecraftServer = exec(craftServer, "getServer");
            Object worldServer = exec(minecraftServer, "getWorldServer", new Class[]{Integer.TYPE}, 0);
            final UUID uuid = UUID.randomUUID();
            Object gameProfile = newInstance("com.mojang.authlib.GameProfile", new Class[]{UUID.class, String.class},
                    uuid, "\u00a7kHGS-" + id);
            Object playerInteractManager = newInstance(nms() + ".PlayerInteractManager",
                    new Class[]{Class.forName(nms() + ".World")}, worldServer);
            Object entityPlayer = newInstance(nms() + ".EntityPlayer",
                    new Class[]{Class.forName(nms() + ".MinecraftServer"), Class.forName(nms() + ".WorldServer"),
                            gameProfile.getClass(), playerInteractManager.getClass()},
                    minecraftServer, worldServer, gameProfile, playerInteractManager);
            World w = location.getWorld();
            Object dimension = getField(exec(w, "getHandle"), "dimension");
            setField(entityPlayer, "dimension", dimension);
            setField(entityPlayer, "locX", location.getX());
            setField(entityPlayer, "locY", location.getY());
            setField(entityPlayer, "locZ", location.getZ());
            Class<? extends Enum> protocolDirectionClass = (Class<? extends Enum>) Class.forName(nms() + ".EnumProtocolDirection");
            Object networkManager = newInstance(nms() + ".NetworkManager",
                    new Class[]{protocolDirectionClass},
                    Enum.valueOf(protocolDirectionClass, "CLIENTBOUND"));
            Channel channel = new EmbeddedChannel(new ChannelHandlerAdapter() {
            }) {
                @Override
                public ChannelFuture writeAndFlush(Object msg) {
                    if (msg != null && interceptMap.containsKey(uuid)) {
                        interceptPacket(uuid, msg);
                    }
                    return super.writeAndFlush(msg);
                }
            };
            setField(networkManager, "channel", channel);

            // We need to add the player to the list, otherwise it won't receive packets
            Object playerList = exec(minecraftServer, "getPlayerList");
            if (playerList != null && players == null) {
                players = (List) getField(playerList, "players");
            }
            // New instance will assign the connection to the entityPlayer
            newInstance(nms() + ".PlayerConnection", new Class[]{
                            Class.forName(nms() + ".MinecraftServer"), networkManager.getClass(), entityPlayer.getClass()},
                    minecraftServer, networkManager, entityPlayer);

            Object craftPlayer = newInstance(cb() + ".entity.CraftPlayer", new Class[]{
                    Class.forName(cb() + ".CraftServer"),
                    Class.forName(nms() + ".EntityPlayer")}, craftServer, entityPlayer);
            Player player = (Player) craftPlayer;
            player.setGameMode(GameMode.SPECTATOR);
            player.setDisplayName("\u00a7k");
            player.setPlayerListName("\u00a7k");
            player.setOp(true);
            dummies.put(id, player);
            return player;
        } catch (Exception e) {
            log.log(Level.INFO, "Unable to create dummy-player", e);
        }
        return null;
    }

    private void interceptPacket(UUID uuid, Object msg) {
        ByteArrayOutputStream baos = interceptMap.get(uuid);
        String packetName = msg.getClass().getSimpleName();
        if (packetName.equals("PacketPlayOutChat")) {
            interceptChat(msg, baos);
        }
    }

    private void interceptChat(Object msg, ByteArrayOutputStream baos) {
        Object handle = getField(msg, "a");
        if (handle == null) {
            return;
        }
        try {
            WrappedChatComponent chatComponent = WrappedChatComponent.fromHandle(handle);
            baos.write((JSONUtil.json2String(chatComponent.getJson()) + "\n").getBytes("UTF-8"));
        } catch (Throwable e) {
            // Ignore
        }
    }

    public static void shutdown() {
        interceptMap.clear();
        dummies.clear();
    }
}
