package dk.lockfuglsang.wolfencraft.util;

import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

/**
 * TODO: Rasmus javadoc
 */
public class BufferedConsoleSender implements ConsoleCommandSender {
    private ConsoleCommandSender sender;
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private PrintStream ps = new PrintStream(baos);

    public BufferedConsoleSender(ConsoleCommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(String s) {
        sender.sendMessage(s);
        ps.println(s);
    }

    @Override
    public void sendMessage(String[] strings) {
        sender.sendMessage(strings);
        for (String s : strings) {
            ps.println(s);
        }
    }

    @Override
    public Server getServer() {
        return sender.getServer();
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public boolean isConversing() {
        return sender.isConversing();
    }

    @Override
    public void acceptConversationInput(String s) {
        sender.acceptConversationInput(s);
    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        return sender.beginConversation(conversation);
    }

    @Override
    public void abandonConversation(Conversation conversation) {
        sender.abandonConversation(conversation);
    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent conversationAbandonedEvent) {
        sender.abandonConversation(conversation, conversationAbandonedEvent);
    }

    @Override
    public void sendRawMessage(String s) {
        sender.sendRawMessage(s);
        ps.println(s);
    }

    @Override
    public boolean isPermissionSet(String s) {
        return sender.isPermissionSet(s);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return sender.isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(String s) {
        return sender.hasPermission(s);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        return sender.addAttachment(plugin, s, b);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        return addAttachment(plugin, s, b, i);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return sender.addAttachment(plugin, i);
    }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment) {
        sender.removeAttachment(permissionAttachment);
    }

    @Override
    public void recalculatePermissions() {
        sender.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return sender.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return sender.isOp();
    }

    @Override
    public void setOp(boolean b) {
        sender.setOp(b);
    }

    public String getStdout() {
        return baos.toString();
    }
}
