package net.mrpg.mrpglauncher;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.mrpg.mrpglauncher.MainController;
import net.mrpg.mrpglauncher.Minecraft.LaunchConfigManager;
import net.mrpg.mrpglauncher.SetupController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class MainApplication extends javafx.application.Application {
    private final Properties settings = new Properties();
    private final Path settingsPath = Paths.get(System.getProperty("user.home"), ".mrpg-launcher", "settings.properties");

    @Override
    public void start(Stage stage) throws IOException {
        loadSettings();
        String configPathStr = settings.getProperty("config_path");

        if (configPathStr == null || configPathStr.isEmpty() || !Files.exists(Paths.get(configPathStr))) {
            if (!showSetupScreen()) {
                Platform.exit();
                return;
            }
            // Reload settings after setup
            loadSettings();
            configPathStr = settings.getProperty("config_path");
        }

        // Proceed to main application
        Path configPath = Paths.get(configPathStr);
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();
        MainController mainController = fxmlLoader.getController();
        mainController.postInit(configPath); // Pass the path to the controller

        Scene scene = new Scene(root);
        stage.setTitle("MRPG Launcher");
        stage.setScene(scene);
        stage.show();
    }

    private boolean showSetupScreen() throws IOException {
        FXMLLoader setupLoader = new FXMLLoader(MainApplication.class.getResource("setup-view.fxml"));
        Parent setupRoot = setupLoader.load();
        Stage setupStage = new Stage();
        setupStage.initModality(Modality.APPLICATION_MODAL);
        setupStage.setTitle("初回セットアップ");
        setupStage.setScene(new Scene(setupRoot));

        SetupController setupController = setupLoader.getController();
        setupController.setStage(setupStage);

        setupStage.showAndWait();

        // Check if the setting was saved
        loadSettings();
        return settings.getProperty("config_path") != null;
    }

    private void loadSettings() {
        try {
            if (Files.exists(settingsPath)) {
                try (InputStream input = Files.newInputStream(settingsPath)) {
                    settings.load(input);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
