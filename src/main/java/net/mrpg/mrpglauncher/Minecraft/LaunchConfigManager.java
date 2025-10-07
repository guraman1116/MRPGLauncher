package net.mrpg.mrpglauncher.Minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class LaunchConfigManager {
    private static final URL lc;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static {
        try {
            lc = new URL("https://raw.githubusercontent.com/guraman1116/MRPGLauncher/refs/heads/master/launch_configs.json");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init(){
        try {
            URLConnection connection = lc.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            try (InputStream is = connection.getInputStream()) {
                if (Files.exists(Paths.get(System.getProperty("user.home"), ".mrpg-launcher", "launcher_config.json"))) {
                    Files.delete(Paths.get(System.getProperty("user.home"), ".mrpg-launcher", "launcher_config.json"));
                }
                Files.copy(is, Paths.get(System.getProperty("user.home"), ".mrpg-launcher", "launcher_config.json"));
                System.out.println("Downloaded launch config successfully");
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static List<LaunchConfig> getConfigs() {
        Path path = Paths.get(System.getProperty("user.home"), ".mrpg-launcher", "launcher_config.json");
        if (Files.exists(path) ){
            try  {
                String json = Files.readString(path);
                Type type = new TypeToken<List<LaunchConfig>>() {}.getType();
                List<LaunchConfig> configs = GSON.fromJson(json, type);
                if (configs != null) return configs;
                else throw new RuntimeException("Failed to parse launch configs");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Collections.emptyList();
    }

}
