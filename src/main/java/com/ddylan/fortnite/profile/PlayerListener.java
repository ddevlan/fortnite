package com.ddylan.fortnite.profile;

import com.ddylan.fortnite.Fortnite;
import com.ddylan.fortnite.Locale;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProfileLoad(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPlayedBefore()) {
            player.teleport(Fortnite.getInstance().getSpawnLocation());
        }

        Profile profile = Profile.getProfiles().get(player.getUniqueId().toString());

        if (profile != null) {
            player.sendMessage(Locale.PROFILE_LOADED);
        } else {
            profile = new Profile(player.getUniqueId(), player.getName());
            Profile.getProfiles().put(player.getUniqueId().toString(), profile);
            player.sendMessage(Locale.PROFILE_CREATED);
            profile.save();
            player.sendMessage(Locale.PROFILE_SAVED);
        }

        profile.setName(player.getName());
        player.setDisplayName(profile.getDisplayColor() + player.getName());
        player.performCommand("helpme");
    }

    @EventHandler
    public void onPlayerDeath(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getProfiles().get(player.getUniqueId().toString());

        if (profile.getHome() != Fortnite.getInstance().getSpawnLocation()) {
            event.setRespawnLocation(profile.getHome());
            return;
        }

        if (player.getBedSpawnLocation() == null) {
            event.setRespawnLocation(Fortnite.getInstance().getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getProfiles().get(player.getUniqueId().toString());

        profile.save();
    }

}
