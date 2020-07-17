package com.ddylan.fortnite.command;

import com.ddylan.fortnite.Fortnite;
import com.ddylan.fortnite.profile.Profile;
import com.ddylan.fortnite.util.CollectionUtil;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CommandImplementer implements Listener {

    private JavaPlugin plugin;
    private Map<UUID, UUID> teleportRequests = new HashMap<>();

    public CommandImplementer(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void onDisable() {
        teleportRequests.clear();
    }

    public void preinit() {
        CommandAPI.unregister("msg", true);
        CommandAPI.unregister("tell", true);
        CommandAPI.unregister("whisper", true);
        CommandAPI.unregister("me", true);
    }

    public void init() {
        //  Help
        registerHelpCommand();

        //  Messaging
        registerMessageCommand();
        registerReplyCommand();

        //  Homes
        registerHomeCommand();
        registerSetHomeCommand();
        registerDeleteHomeCommand();

        //  Temp home commands
        registerTemporaryCommand();
        registerSetTemporary();

        //  Teleports
        registerTeleportCommand();
        registerTeleportAcceptCommand();
        registerTeleportDenyCommand();

        //  Chat commands
        registerDisplayNameCommand();
        registerPrefixCommand();
    }

    private void registerPrefixCommand() {
        LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
        arguments.put("prefix", new GreedyStringArgument());

        new CommandAPICommand("prefix")
                .withPermission(CommandPermission.NONE)
                .withArguments(arguments)
                .executesPlayer((Player sender, Object[] args) -> {
                    Profile senderProfile = Profile.getProfiles().get(sender.getUniqueId().toString());
                    String prefix = (String) args[0];

                    senderProfile.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
                    sender.sendMessage(ChatColor.GOLD + "Your prefix has been set to: " + ChatColor.GRAY + "[" + ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.GRAY + "]");
                    return 1;
                })
                .register();
    }

    private void registerDisplayNameCommand() {
        LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
        arguments.put("color", new ChatColorArgument());

        new CommandAPICommand("color")
                .withPermission(CommandPermission.NONE)
                .withArguments(arguments)
                .executesPlayer((Player sender, Object[] args) -> {
                    Profile senderProfile = Profile.getProfiles().get(sender.getUniqueId().toString());
                    ChatColor color = (ChatColor) args[0];

                    senderProfile.setDisplayColor(color);
                    sender.setDisplayName(color + sender.getName());
                    sender.sendMessage(ChatColor.GOLD + "Your display name has been set to: " + ChatColor.RESET + sender.getDisplayName());
                    return 1;
                })
                .register();
    }

    /*
        Help command
     */

    private void registerHelpCommand() {
        List<String> help = new ArrayList<>();

        help.add(ChatColor.DARK_PURPLE + "" + ChatColor.STRIKETHROUGH + "------------------------------------------------");
        help.add(ChatColor.GOLD + "" + ChatColor.BOLD + "wizard hut discord smp help menu:");
        help.add("");
        help.add(ChatColor.GOLD + "general information:");
        help.add(ChatColor.GRAY + "spawn:" + ChatColor.YELLOW +" 0, 0");
        help.add(ChatColor.GRAY + "difficulty:" + ChatColor.YELLOW + " hard");
        help.add(ChatColor.GRAY + "pvp:" + ChatColor.YELLOW + " up to you");
        help.add(ChatColor.GRAY + "break other ppl house:" + ChatColor.YELLOW + " ban from internet");
        help.add(ChatColor.GRAY + "discord:" + ChatColor.YELLOW + " https://discord.gg/aueb6vc");
        help.add("");
        help.add(ChatColor.GOLD + "social commands:");
        help.add(ChatColor.GRAY + "/msg <player> <msg>" + ChatColor.YELLOW + " - msg a player something!");
        help.add(ChatColor.GRAY + "/reply" + ChatColor.YELLOW + " - reply to your latest messaged player");
        help.add(ChatColor.GRAY + "/color <color>" + ChatColor.YELLOW + " - change your display name color!");
        help.add(ChatColor.GRAY + "/prefix <character>" + ChatColor.YELLOW + " - change your tag prefix!");
        help.add("");
        help.add(ChatColor.GOLD + "teleport commands:");
        help.add(ChatColor.GRAY + "/tpa <player>" + ChatColor.YELLOW + " - send a teleport request to a player");
        help.add(ChatColor.GRAY + "/tpaccept" + ChatColor.YELLOW + " - accept a teleport request");
        help.add(ChatColor.GRAY + "/tpdeny" + ChatColor.YELLOW + " - deny a teleport request");
        help.add("");
        help.add(ChatColor.GOLD + "home commands:");
        help.add(ChatColor.GRAY + "/sethome" + ChatColor.YELLOW + " - sets your home to your current location");
        help.add(ChatColor.GRAY + "/home" + ChatColor.YELLOW + " - teleport to your home (or spawn)");
        help.add(ChatColor.GRAY + "/settemp" + ChatColor.YELLOW + " - save a temporary location");
        help.add(ChatColor.GRAY + "/temp" + ChatColor.YELLOW + " - teleport to your temporary location");
        help.add(ChatColor.DARK_PURPLE + "" + ChatColor.STRIKETHROUGH + "------------------------------------------------");
        help.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "wanna see this menu again? type '/helpme'!");
        help.add(ChatColor.DARK_PURPLE + "" + ChatColor.STRIKETHROUGH + "------------------------------------------------");


        new CommandAPICommand("helpme")
                .withPermission(CommandPermission.NONE)
                .executesPlayer((Player sender, Object[] args) -> {
                    help.forEach(sender::sendMessage);
                    return 1;
                })
                .register();
    }


    /*
        Teleport commands start.
     */

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        teleportRequests.remove(event.getPlayer().getUniqueId());
        teleportRequests.remove(CollectionUtil.getKeyByValue(teleportRequests, event.getPlayer().getUniqueId()));
    }

    private void registerTeleportAcceptCommand() {
        new CommandAPICommand("tpaccept")
                .withPermission(CommandPermission.NONE)
                .executesPlayer((Player sender, Object[] args) -> {
                    acceptTeleportRequest(sender);
                    return 1;
                })
                .register();
    }

    private void registerTeleportDenyCommand() {
        new CommandAPICommand("tpdeny")
                .withPermission(CommandPermission.NONE)
                .executesPlayer((Player sender, Object[] args) -> {
                    denyTeleportRequest(sender);
                    return 1;
                })
                .register();
    }

    private void registerTeleportCommand() {
        LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
        arguments.put("player", new PlayerArgument());

        new CommandAPICommand("tpa")
                .withArguments(arguments)
                .withPermission(CommandPermission.NONE)
                .executesPlayer((Player sender, Object[] args) -> {
                    Player receiver = (Player) args[0];
                    sendTeleportRequest(receiver, sender);
                    return 1;
                })
                .register();
    }

    private void denyTeleportRequest(Player receiver) {
        UUID senderId = CollectionUtil.getKeyByValue(teleportRequests, receiver.getUniqueId());

        if (senderId == null) {
            receiver.sendMessage(ChatColor.RED + "You do not have any pending teleport requests.");
            return;
        }

        teleportRequests.remove(senderId);
        teleportRequests.remove(receiver.getUniqueId());

        Player sender = Bukkit.getPlayer(senderId);

        if (sender == null) {
            receiver.sendMessage(ChatColor.RED + "The player you had a request for logged out.");
            return;
        }

        receiver.sendMessage(ChatColor.GOLD + "Your teleport request was " + ChatColor.RED + "denied" + ChatColor.GOLD + ".");
        sender.sendMessage(ChatColor.GOLD + "Teleport request " + ChatColor.RED + "denied" + ChatColor.GOLD + ".");
    }

    private void acceptTeleportRequest(Player receiver) {
        UUID senderId = CollectionUtil.getKeyByValue(teleportRequests, receiver.getUniqueId());

        if (senderId == null) {
            receiver.sendMessage(ChatColor.RED + "You do not have any pending teleport requests.");
            return;
        }

        teleportRequests.remove(senderId);

        Player sender = Bukkit.getPlayer(senderId);

        if (sender == null) {
            receiver.sendMessage(ChatColor.RED + "The player you had a request for logged out.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "Teleport commencing in 5 seconds...");
        receiver.sendMessage(ChatColor.GOLD + "Teleport request " + ChatColor.GREEN + "accepted.");

        new BukkitRunnable() {
            @Override
            public void run() {
                sender.getWorld().playSound(sender.getLocation(), Sound.ENTITY_ENDER_PEARL_THROW, 1, 1);
                sender.teleport(receiver);
                sender.getWorld().playEffect(sender.getLocation(), Effect.ENDEREYE_LAUNCH, 1, 1);
                sender.getWorld().playSound(sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            }
        }.runTaskLater(Fortnite.getInstance(), 20 * 5);
    }

    private void sendTeleportRequest(Player receiver, Player sender) {
        if (!teleportRequests.containsKey(sender.getUniqueId()) || !teleportRequests.containsValue(CollectionUtil.getKeyByValue(teleportRequests, receiver.getUniqueId()))) {
            teleportRequests.put(sender.getUniqueId(), receiver.getUniqueId());
            receiver.sendMessage(ChatColor.RESET + sender.getDisplayName() + ChatColor.GOLD + " would like to teleport to you.");
            BaseComponent component = new TextComponent("Click here to accept the request.");
            component.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"));
            BaseComponent hoverComponent = new TextComponent("Click to accept!");
            hoverComponent.setColor(net.md_5.bungee.api.ChatColor.GRAY);
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {hoverComponent}));
            receiver.spigot().sendMessage(component);
            receiver.sendMessage(ChatColor.GRAY + "This teleport request will expire in 30 seconds.");
            sender.sendMessage(ChatColor.GRAY + "Your teleport request will expire in 30 seconds.");

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (teleportRequests.containsKey(sender.getUniqueId())) {
                        teleportRequests.remove(sender.getUniqueId());
                        receiver.sendMessage(ChatColor.GRAY + "The teleport request has expired.");
                        sender.sendMessage(ChatColor.GRAY + "Your teleport request has expired.");
                    }
                }
            }.runTaskLater(Fortnite.getInstance(), 20 * 30);
        } else {
            sender.sendMessage(ChatColor.RED + "There is currently a pending teleport request for you or the specified player.");
        }
    }

    /*
        Teleport commands end.
     */

    private void registerMessageCommand() {
        final String messageFormat = ChatColor.GRAY + "({context}" + ChatColor.GRAY +" {player}" + ChatColor.GRAY +") {message}";

        LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
        arguments.put("player", new PlayerArgument());
        arguments.put("message", new GreedyStringArgument());

        new CommandAPICommand("msg")
                .withArguments(arguments)
                .withAliases("m", "t", "tell", "w", "whisper", "message", "pm", "privatemessage", "dm", "directmessage")
                .withPermission(CommandPermission.NONE)
                .executesPlayer((Player sender, Object[] args) -> {
                    Player receiver = (Player) args[0];
                    String message = (String) args[1];

                    //  update reply fields
                    Profile senderProfile = Profile.getProfiles().get(sender.getUniqueId().toString());
                    senderProfile.setLastMessaged(receiver.getUniqueId());

                    //  do the command
                    if (message.contains("[item]")) {
                        if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                            String senderText = messageFormat.replace("{context}", "To").replace("[item]", "%s").replace("{player}", ChatColor.RESET + receiver.getDisplayName()).replace("{message}", message);
                            String receiverText = messageFormat.replace("{context}", "From").replace("[item]", "%s").replace("{player}", ChatColor.RESET + sender.getDisplayName()).replace("{message}", message);

                            sender.spigot().sendMessage(formatItemLink(senderText, sender.getInventory().getItemInMainHand()));
                            receiver.spigot().sendMessage(formatItemLink(receiverText, sender.getInventory().getItemInMainHand()));
                        } else {
                            sender.sendMessage(ChatColor.DARK_RED + "Silly! You don't have an item in your hand!");
                            return 0 ;
                        }
                    } else {
                        sender.sendMessage(messageFormat.replace("{context}", "To").replace("{player}", ChatColor.RESET + receiver.getDisplayName()).replace("{message}", message));
                        receiver.sendMessage(messageFormat.replace("{context}", "From").replace("{player}", ChatColor.RESET + sender.getDisplayName()).replace("{message}", message));
                    }

                    //  do noises
                    sender.playSound(sender.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 0.8f);
                    receiver.playSound(sender.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.0f);
                    return 1;
                })
                .register();
    }

    private void registerReplyCommand() {
        final String messageFormat = ChatColor.GRAY + "({context}" + ChatColor.GRAY +" {player}" + ChatColor.GRAY +") {message}";

        LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
        arguments.put("message", new GreedyStringArgument());

        new CommandAPICommand("r")
                .withArguments(arguments)
                .withAliases("reply")
                .withPermission(CommandPermission.NONE)
                .executesPlayer((Player sender, Object[] args) -> {
                    String message = (String) args[0];
                    Profile senderProfile = Profile.getProfiles().get(sender.getUniqueId().toString());

                    Player receiver = Bukkit.getPlayer(senderProfile.getLastMessaged());

                    if (receiver == null) {
                        CommandAPI.fail(ChatColor.RED + "That player is not online.");
                        return 0;
                    }

                    //  do the command
                    if (message.contains("[item]")) {
                        if (sender.getInventory().getItemInMainHand().getType() != Material.AIR) {
                            String senderText = messageFormat.replace("{context}", "To").replace("{player}", ChatColor.RESET + receiver.getDisplayName()).replace("{message}", message);
                            String receiverText = messageFormat.replace("{context}", "From").replace("{player}", ChatColor.RESET + sender.getDisplayName()).replace("{message}", message);

                            sender.spigot().sendMessage(formatItemLink(senderText, sender.getInventory().getItemInMainHand()));
                            receiver.spigot().sendMessage(formatItemLink(receiverText, sender.getInventory().getItemInMainHand()));
                        } else {
                            sender.sendMessage(ChatColor.DARK_RED + "Silly! You don't have an item in your hand!");
                            return 0 ;
                        }
                    } else {
                        sender.sendMessage(messageFormat.replace("{context}", "To").replace("{player}", ChatColor.RESET + receiver.getDisplayName()).replace("{message}", message));
                        receiver.sendMessage(messageFormat.replace("{context}", "From").replace("{player}", ChatColor.RESET + sender.getDisplayName()).replace("{message}", message));
                    }

                    //  do noises
                    sender.playSound(sender.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 0.8f);
                    receiver.playSound(sender.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.0f);
                    return 1;
                })
                .register();
    }

    /*
        Home commands start.
     */

    private void registerTemporaryCommand() {
        new CommandAPICommand("temp")
                .withPermission(CommandPermission.NONE)
                .executesPlayer((Player player, Object[] objects) -> {
                    Profile senderProfile = Profile.getProfiles().get(player.getUniqueId().toString());
                    if (senderProfile.getTemporary() != null) {
                        player.teleport(senderProfile.getTemporary());
                        player.sendMessage(ChatColor.GOLD + "Teleported to your temporary location.");
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have a temporary location set.");
                        player.sendMessage(ChatColor.GRAY + "Type '/settemp' to save a temporary location.");
                    }
                    return 1;
                })
                .register();
    }

    private void registerSetTemporary() {
        new CommandAPICommand("settemp")
                .withPermission(CommandPermission.NONE)
                .executesPlayer((Player player, Object[] objects) -> {
                    Profile senderProfile = Profile.getProfiles().get(player.getUniqueId().toString());
                    senderProfile.setTemporary(player.getLocation());
                    player.sendMessage(ChatColor.GOLD + "Your temporary location has been set.");
                    return 1;
                })
                .register();
    }

    private void registerHomeCommand() {
        new CommandAPICommand("home")
                .withAliases("h")
                .withPermission(CommandPermission.NONE)
                .executesPlayer((Player player, Object[] objects) -> {
                    Profile senderProfile = Profile.getProfiles().get(player.getUniqueId().toString());
                    player.teleport(senderProfile.getHome());
                    player.sendMessage(ChatColor.GOLD + "Teleported to your home.");
                    return 1;
                })
                .register();
    }

    private void registerSetHomeCommand() {
        new CommandAPICommand("sethome")
                .withPermission(CommandPermission.NONE)
                .executesPlayer((Player player, Object[] objects) -> {
                    Profile senderProfile = Profile.getProfiles().get(player.getUniqueId().toString());
                    senderProfile.setHome(player.getLocation());
                    player.sendMessage(ChatColor.GOLD + "Your home has been set.");
                    return 1;
                })
                .register();
    }

    private void registerDeleteHomeCommand() {
        new CommandAPICommand("delhome")
                .withPermission(CommandPermission.NONE)
                .executesPlayer((Player player, Object[] objects) -> {
                    Profile senderProfile = Profile.getProfiles().get(player.getUniqueId().toString());
                    senderProfile.setHome(Fortnite.getInstance().getSpawnLocation());
                    player.sendMessage(ChatColor.GOLD + "Your home has been removed.");
                    player.sendMessage(ChatColor.GRAY + "You will now be teleported to spawn when using '/home'.");
                    return 1;
                })
                .register();
    }

    /*
        Home commands end.
     */

    private BaseComponent formatItemLink(String source, ItemStack itemStack) {
        String linkDisplayName = (itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : WordUtils.capitalizeFully(itemStack.getType().name().toLowerCase().replace("_", " ")));
        String linkFormat = ChatColor.GRAY + "[" + ChatColor.AQUA + linkDisplayName + ChatColor.GRAY + "]";
        BaseComponent component = new TextComponent(source.replace("[item]", linkFormat));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()).toString())}));

        return component;
    }

}
