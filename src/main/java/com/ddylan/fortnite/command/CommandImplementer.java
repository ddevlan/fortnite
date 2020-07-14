package com.ddylan.fortnite.command;

import com.ddylan.fortnite.profile.Profile;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;

public class CommandImplementer {

    private JavaPlugin plugin;

    public CommandImplementer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        registerMessageCommand();
        registerReplyCommand();
        registerHomeCommand();
    }

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
                    sender.sendMessage(messageFormat.replace("{context}", "To").replace("{player}", ChatColor.RESET + receiver.getDisplayName()).replace("{message}", message));
                    receiver.sendMessage(messageFormat.replace("{context}", "From").replace("{player}", ChatColor.RESET + sender.getDisplayName()).replace("{message}", message));

                    //  do noises
                    sender.playSound(sender.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 0.8f);
                    receiver.playSound(sender.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 0.85f);
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
                        sender.sendMessage(ChatColor.RED + "That player is not online.");
                        return;
                    }

                    //  do the command
                    sender.sendMessage(messageFormat.replace("{context}", "To").replace("{player}", ChatColor.RESET + receiver.getDisplayName()).replace("{message}", message));
                    receiver.sendMessage(messageFormat.replace("{context}", "From").replace("{player}", ChatColor.RESET + sender.getDisplayName()).replace("{message}", message));

                    //  do noises
                    sender.playSound(sender.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 0.8f);
                    receiver.playSound(sender.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 0.85f);
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
                })
                .register();
    }

}
