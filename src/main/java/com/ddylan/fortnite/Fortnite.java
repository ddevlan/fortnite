package com.ddylan.fortnite;

import com.ddylan.fortnite.command.CommandImplementer;
import com.ddylan.fortnite.profile.ProfileHandler;
import com.ddylan.fortnite.profile.ProfileListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Fortnite extends JavaPlugin {

    @Getter private static Fortnite instance;

    private ProfileHandler profileHandler;
    private CommandImplementer commandImplementer;

    @Override
    public void onEnable() {
        instance = this;

        this.profileHandler = new ProfileHandler(this);
        this.commandImplementer = new CommandImplementer(this);

        commandImplementer.preinit();
        commandImplementer.init();

        getServer().getPluginManager().registerEvents(new ProfileListener(), this);
    }

    @Override
    public void onDisable() {

    }

    public Location getSpawnLocation() {
        Location location = new Location(Bukkit.getWorld("world"), 0, 0, 0);

        for (int i = 55; i < 255; i++) {
            location = location.clone();
            location.setY(i);

            if (location.getBlock().isEmpty() && location.clone().add(0, 1, 0).getBlock().isEmpty()) {
                return location.add(0, 0.5, 0);
            }
        }

        location = location.clone();
        location.setY(70);

        return location;
    }
    
}
