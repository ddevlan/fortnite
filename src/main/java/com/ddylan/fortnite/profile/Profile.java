package com.ddylan.fortnite.profile;

import com.ddylan.fortnite.Fortnite;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Profile {

    @Getter private static final Map<String, Profile> profiles;

    static {
        profiles = new HashMap<>();
    }

    private final UUID uuid;
    @Setter private String name;
    @Setter private UUID lastMessaged;
    @Setter private Location home;

    private File profileFile;

    public Profile(final UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.profileFile = new File(Fortnite.getInstance().getProfileHandler() + uuid.toString());

        if (!profileFile.exists()) {
            create();
            defaults();
            save();
        } else {
            load();
        }
    }

    private void defaults() {
        this.lastMessaged = UUID.fromString("26eb0954-40e0-47eb-8e82-f7828e2b5c8b");
        this.home = Fortnite.getInstance().getSpawnLocation();
    }

    private void create() {
        try {
            profileFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(profileFile);

        lastMessaged = UUID.fromString(Objects.requireNonNull(config.getString("lastMessaged")));
        home = config.getLocation("home");
    }

    public void save() {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(profileFile);

        configuration.set("lastMessaged", lastMessaged.toString());
        configuration.set("home", home);

        try {
            configuration.save(profileFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
