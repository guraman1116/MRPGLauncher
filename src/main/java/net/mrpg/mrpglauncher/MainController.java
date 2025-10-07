package net.mrpg.mrpglauncher;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.mrpg.mrpglauncher.Minecraft.Auth;

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
    private ChoiceBox<String> episode_select;
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
    @FXML
    private Button logoutButton;
    @FXML
    private Button toggleButton;
    @FXML
    private ButtonBar bottomButtonBar;
    @FXML
    private VBox bottomButtonBox;

    private Auth auth;
    private boolean isBarVisible = false;
    private TranslateTransition slideTransition;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSettings();
        updateButtonStates();
        auth = new Auth();
        updateUiForLoginState();
        setupSlideAnimation();
    }

    private void setupSlideAnimation() {
        // Initially hide the button bar by translating it down
        double barHeight = 100; // Approximate height to hide the bar
        bottomButtonBox.setTranslateY(barHeight);

        // Create the slide animation
        slideTransition = new TranslateTransition(Duration.millis(300), bottomButtonBox);
    }

    @FXML
    protected void onToggleButtonClick() {
        if (isBarVisible) {
            // Hide the bar
            slideTransition.setToY(100);
            toggleButton.setText("▲");
        } else {
            // Show the bar
            slideTransition.setToY(0);
            toggleButton.setText("▼");
        }
        slideTransition.play();
        isBarVisible = !isBarVisible;
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

    @FXML
    protected void onStartButtonClick() {
        if (auth.isLoggedIn()) {
            System.out.println("Already logged in. Starting Minecraft...");
            //TODO: Minecraft Launch method
            return;
        }

        // Open login window
        openLoginWindow();
    }

    private void openLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();
            loginController.setAuth(auth);
            loginController.setOnLoginSuccess(() -> {
                // Update UI after successful login
                updateUiForLoginState();
            });

            Stage stage = new Stage();
            stage.setTitle("Microsoft アカウントでログイン");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error to user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("エラー");
            alert.setHeaderText("ログインウィンドウを開けませんでした");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    protected void onLogoutButtonClick() {
        auth.logout();
        updateUiForLoginState();
    }

    private void updateUiForLoginState() {
        if (auth.isLoggedIn()) {
            welcomeText.setText("ようこそ、 " + auth.getUsername() + " さん");
            startButton.setText("開始");
            logoutButton.setVisible(true);
        } else {
            welcomeText.setText("MRPG Launcher");
            startButton.setText("ログイン");
            logoutButton.setVisible(false);
        }
    }
}
