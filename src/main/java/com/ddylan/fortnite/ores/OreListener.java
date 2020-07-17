package com.ddylan.fortnite.ores;

import com.ddylan.fortnite.Fortnite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class OreListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getMetadata("found").isEmpty()) {
            if (event.getBlock().getType() == Material.DIAMOND_ORE) {
                if (!event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH) && event.getPlayer().getInventory().getItemInMainHand().getType().name().contains("PICKAXE")) {
                    int amountFound = 0;
                    for (int x = -3; x < 3; x++) {
                        for (int y = -3; y < 3; y++) {
                            for (int z = -3; z < 3; z++) {
                                Location checked = event.getBlock().getLocation().add(x, y, z);
                                if (checked.getBlock().getType() == Material.DIAMOND_ORE) {
                                    checked.getBlock().setMetadata("found", new FixedMetadataValue(Fortnite.getInstance(), true));
                                    amountFound++;
                                }
                            }
                        }
                    }
                    Bukkit.broadcastMessage("[FD] " + ChatColor.AQUA + event.getPlayer().getName() + " has found " + amountFound + " diamonds.");
                }
            }
        }
    }

}
