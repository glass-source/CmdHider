package com.me.asunder.cmdhider.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Config {

    private static final String CONFIG_FILE_NAME = "cmdhider.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Config instance;

    private List<String> hiddenRootCommands = new ArrayList<>();

    public Config() {
    }

    public static void init() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(CONFIG_FILE_NAME);

        if (Files.notExists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException ignored) {
            }
        }

        if (Files.exists(configFile)) {
            try (BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                Config loaded = GSON.fromJson(reader, Config.class);
                if (loaded == null) {
                    loaded = defaultConfig();
                } else if (loaded.hiddenRootCommands == null) {
                    loaded.hiddenRootCommands = new ArrayList<>();
                }
                instance = loaded;
                return;
            } catch (Exception e) {
            }
        }

        instance = defaultConfig();
        save();
    }

    private static Config defaultConfig() {
        Config config = new Config();
        //example
        config.hiddenRootCommands.add("video");
        return config;
    }

    public static Config get() {
        if (instance == null) {
            instance = defaultConfig();
        }
        return instance;
    }

    public static void save() {
        if (instance == null) {
            return;
        }

        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(CONFIG_FILE_NAME);

        try {
            if (Files.notExists(configDir)) {
                Files.createDirectories(configDir);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
        }
    }

    public static boolean reload() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(CONFIG_FILE_NAME);

        try {
            if (Files.notExists(configDir)) {
                Files.createDirectories(configDir);
            }

            if (Files.exists(configFile)) {
                try (BufferedReader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
                    Config loaded = GSON.fromJson(reader, Config.class);
                    if (loaded == null) {
                        loaded = defaultConfig();
                    } else if (loaded.hiddenRootCommands == null) {
                        loaded.hiddenRootCommands = new ArrayList<>();
                    }
                    instance = loaded;
                    return true;
                }
            } else {
                instance = defaultConfig();
                save();
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean shouldHideCommand(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        List<String> list = hiddenRootCommands;
        return list != null && list.contains(name);
    }
}