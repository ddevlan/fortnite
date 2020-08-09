package com.ddylan.fortnite.chat;

import com.ddylan.fortnite.profile.Profile;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

public class ChatManager implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Profile profile = Profile.getProfiles().get(event.getPlayer().getUniqueId().toString());

        if (event.getMessage().contains("[item]")) {
            event.setCancelled(true);
            if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR) {
                ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
                String chatFormat;

                if (profile.hasPrefix()) {
                    chatFormat = ChatColor.DARK_GRAY + "[" + ChatColor.RESET + profile.getPrefix() + ChatColor.DARK_GRAY + "] " + event.getPlayer().getDisplayName() + ChatColor.RESET + ": " + event.getMessage();
                } else {
                    chatFormat = event.getPlayer().getDisplayName() + ChatColor.RESET + ": " + event.getMessage();
                }
                BaseComponent component = formatItemLink(chatFormat, itemStack);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.spigot().sendMessage(component);
                }
                return;
            } else {
                event.getPlayer().sendMessage(ChatColor.DARK_RED + "Silly! You don't have an item in your hand!");
            }
            return;
        }

        if (profile.hasPrefix()) {
            event.setFormat(ChatColor.DARK_GRAY + "[" + ChatColor.RESET + profile.getPrefix() + ChatColor.DARK_GRAY + "] " + event.getPlayer().getDisplayName() + ChatColor.RESET + ": " + event.getMessage());
        } else {
            event.setFormat(event.getPlayer().getDisplayName() + ChatColor.RESET + ": " + event.getMessage());
        }

        if (event.getMessage().equalsIgnoreCase("xd")) {
            Bukkit.broadcastMessage("[Server]: xD");
        }
    }

    private BaseComponent formatItemLink(String source, ItemStack itemStack) {
        String linkDisplayName = (itemStack.getItemMeta() != null && itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : WordUtils.capitalizeFully(itemStack.getType().name().toLowerCase().replace("_", " ")));
        String linkFormat = ChatColor.GRAY + "[" + ChatColor.AQUA + linkDisplayName + ChatColor.GRAY + "]";
        BaseComponent component = new TextComponent(source.replace("[item]", linkFormat));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()).toString())}));

        return component;
    }

}
