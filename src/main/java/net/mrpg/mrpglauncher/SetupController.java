package net.mrpg.mrpglauncher;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class SetupController {

    @FXML
    private TextField pathTextField;
    @FXML
    private Button saveButton;

    private Stage stage;
    private final Properties settings = new Properties();
    private final Path settingsPath = Paths.get(System.getProperty("user.home"), ".mrpg-launcher", "settings.properties");

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        loadSettings();
    }

    @FXML
    private void onBrowseButtonClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("起動構成フォルダの選択");
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            pathTextField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void onSaveButtonClick() {
        String path = pathTextField.getText();
        if (path != null && !path.isEmpty()) {
            settings.setProperty("config_path", path);
            saveSettings();
            stage.close();
        }
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

    private void saveSettings() {
        try {
            Files.createDirectories(settingsPath.getParent());
            try (OutputStream output = Files.newOutputStream(settingsPath)) {
                settings.store(output, "MRPGLauncher Settings");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
