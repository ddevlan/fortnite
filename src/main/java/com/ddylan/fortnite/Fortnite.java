package com.ddylan.fortnite;

import com.ddylan.fortnite.chat.ChatManager;
import com.ddylan.fortnite.command.CommandImplementer;
import com.ddylan.fortnite.ores.OreListener;
import com.ddylan.fortnite.profile.Profile;
import com.ddylan.fortnite.profile.ProfileHandler;
import com.ddylan.fortnite.profile.PlayerListener;
import lombok.Getter;
import net.minecraft.server.v1_16_R1.Explosion;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Getter
public class Fortnite extends JavaPlugin {

    @Getter private static Fortnite instance;

    private ProfileHandler profileHandler;
    private CommandImplementer commandImplementer;

    @Override
    public void onEnable() {
        instance = this;

        this.commandImplementer = new CommandImplementer(this);
        this.profileHandler = new ProfileHandler(this);

        commandImplementer.preinit();
        commandImplementer.init();
        profileHandler.init();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new OreListener(), this);
        getServer().getPluginManager().registerEvents(new ChatManager(), this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = Profile.getProfiles().put(player.getUniqueId().toString(), new Profile(player.getUniqueId(), player.getName()));
        }



//        List<String> broadcasts = new ArrayList<>();
//        broadcasts.addAll(Arrays.asList(
//                "",
//                ""));
//
//        Random random = new Random();
//
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                Bukkit.broadcastMessage(broadcasts.get(random.nextInt(broadcasts.size())));
//            }
//        }.runTaskTimer(this, 20 * 60, 20 * 300);
    }

    @Override
    public void onDisable() {
        commandImplementer.onDisable();

        for (Profile profile : Profile.getProfiles().values()) {
            profile.save();
        }
    }

    public Location getSpawnLocation() {
        Location location = new Location(Bukkit.getWorld("world"), 0, 0, 0);

        for (int i = 55; i < 255; i++) {
            location = location.clone();
            location.setY(i);

            if (location.getBlock().isEmpty() && location.clone().add(0, 1, 0).getBlock().isEmpty()) {
                return location.add(0, 1.0, 0);
            }
        }

        location = location.clone();
        location.setY(70);

        return location;
    }
    
}
