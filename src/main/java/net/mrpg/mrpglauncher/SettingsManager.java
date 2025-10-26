package net.mrpg.mrpglauncher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class SettingsManager {

    private static final SettingsManager INSTANCE = new SettingsManager();

    private final Properties settings = new Properties();
    private final Path settingsPath = Paths.get(System.getProperty("user.home"), ".mrpg-launcher", "settings.properties");

    private SettingsManager() {
        loadSettings();
    }

    public static SettingsManager getInstance() {
        return INSTANCE;
    }

    public String getProperty(String key, String defaultValue) {
        return settings.getProperty(key, defaultValue);
    }

    public void setProperty(String key, String value) {
        settings.setProperty(key, value);
        saveSettings();
    }

    private void loadSettings() {
        try {
            if (Files.exists(settingsPath)) {
                try (InputStream input = Files.newInputStream(settingsPath)) {
                    settings.load(input);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveSettings() {
        try {
            Files.createDirectories(settingsPath.getParent());
            try (OutputStream output = Files.newOutputStream(settingsPath)) {
                settings.store(output, "MRPGLauncher Settings");
            }
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
