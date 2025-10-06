package net.mrpg.mrpglauncher;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import net.hycrafthd.minecraft_authenticator.login.Authenticator;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private final Properties settings = new Properties();
    private final Path settingsPath = Paths.get(System.getProperty("user.home"), ".mrpglauncher", "settings.properties");

    @FXML
    private Label welcomeText;
    @FXML
    private ProgressBar MCProgress;
    @FXML
    private Label DetailsLabel;
    @FXML
    private Button agreementButton;
    @FXML
    private Button startButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSettings();
        updateButtonStates();
    }

    private void loadSettings() {
        try {
            if (Files.exists(settingsPath)) {
                try (InputStream input = Files.newInputStream(settingsPath)) {
                    settings.load(input);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle error appropriately
        }
    }

    private void saveSettings() {
        try {
            Files.createDirectories(settingsPath.getParent());
            try (OutputStream output = Files.newOutputStream(settingsPath)) {
                settings.store(output, "MRPGLauncher Settings");
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle error appropriately
        }
    }

    private void updateButtonStates() {
        boolean hasAgreed = Boolean.parseBoolean(settings.getProperty("agreed", "false"));
        startButton.setDisable(!hasAgreed);
    }

    @FXML
    protected void onAgreementButtonClick() {
        try (InputStream is = getClass().getResourceAsStream("terms.txt")) {
            if (is == null) {
                System.err.println("terms.txt not found!");
                return;
            }

            StringBuilder termsTextBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    termsTextBuilder.append(line).append(System.lineSeparator());
                }
            }

            Stage stage = new Stage();
            stage.setTitle("利用規約");

            TextArea textArea = new TextArea(termsTextBuilder.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);

            Button agreeButton = new Button("同意する");
            agreeButton.setOnAction(e -> {
                settings.setProperty("agreed", "true");
                saveSettings();
                updateButtonStates();
                stage.close();
            });

            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(textArea);
            BorderPane.setMargin(agreeButton, new Insets(10));
            borderPane.setBottom(agreeButton);
            BorderPane.setAlignment(agreeButton, Pos.CENTER);

            Scene scene = new Scene(borderPane, 600, 400);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    final Authenticator authenticator = Authenticator.ofMicrosoft(a);
    @FXML
    protected void onStartButtonClick() {
        //TODO: Minecraft Launch method
        boolean firstRun = settings.getProperty("firstRun", "true").equals("true");
        if (firstRun) {

        }
    }
}
