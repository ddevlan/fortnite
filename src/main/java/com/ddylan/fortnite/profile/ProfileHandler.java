package com.ddylan.fortnite.profile;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ProfileHandler {

    @Getter private final File mainDirectory;

    public ProfileHandler(JavaPlugin plugin) {
        this.mainDirectory = new File(plugin.getDataFolder() + "/profiles/");

        if (!mainDirectory.exists()) {
            if (mainDirectory.mkdirs()) {
                plugin.getLogger().info("Success: Created 'profiles' directory in the plugin data folder.");
            } else {
                plugin.getLogger().severe("Err: Could not create 'profiles' directory in the plugin data folder.");
            }
        }
    }

}
