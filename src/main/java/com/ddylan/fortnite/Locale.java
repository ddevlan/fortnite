package com.ddylan.fortnite;

import org.bukkit.ChatColor;

public class Locale {

    private static ChatColor PRIMARY_COLOR;

    public static final String PROFILE_CREATED;
    public static final String PROFILE_LOADED;
    public static final String PROFILE_SAVED;

    static {
        PRIMARY_COLOR = ChatColor.GREEN;

        PROFILE_CREATED = PRIMARY_COLOR + "Your profile has been created.";
        PROFILE_LOADED = PRIMARY_COLOR + "Your profile has been loaded.";
        PROFILE_SAVED = PRIMARY_COLOR + "Your profile has been saved.";
    }

}
