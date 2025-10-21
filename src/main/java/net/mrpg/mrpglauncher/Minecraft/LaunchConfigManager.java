package net.mrpg.mrpglauncher.Minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LaunchConfigManager {
    private static final URL lc;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static List<LaunchConfig> configs;

    static {
        try {
            lc = new URL("https://raw.githubusercontent.com/guraman1116/MRPGLauncher/refs/heads/master/launch_configs.json");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path configPath;

    public static void init(Path path) {
        configPath = path;
        loadConfigs();
    }

    private static void loadConfigs() {
        configs = new ArrayList<>();
        Path configFile = configPath.resolve("launch_configs.json");

        if (Files.exists(configFile)) {
            System.out.println("Loading configurations from local file: " + configFile);
            try (Reader reader = Files.newBufferedReader(configFile)) {
                configs = GSON.fromJson(reader, new TypeToken<List<LaunchConfig>>() {}.getType());
            } catch (IOException e) {
                System.err.println("Error reading local config file.");
                e.printStackTrace();
            }
        } else {
            System.out.println("Local config file not found. Fetching from URL: " + lc);
            try (InputStream is = lc.openStream();
                 Reader reader = new InputStreamReader(is)) {
                configs = GSON.fromJson(reader, new TypeToken<List<LaunchConfig>>() {}.getType());
            } catch (IOException e) {
                System.err.println("Error fetching or reading config from URL.");
                e.printStackTrace();
            }
        }
    }

    public static List<LaunchConfig> getConfigs() {
        return configs;
    }
}
