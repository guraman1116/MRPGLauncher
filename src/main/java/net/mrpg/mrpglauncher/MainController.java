package net.mrpg.mrpglauncher;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.mrpg.mrpglauncher.Minecraft.Auth;
import net.mrpg.mrpglauncher.Minecraft.LaunchConfig;
import net.mrpg.mrpglauncher.Minecraft.LaunchConfigManager;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Stack;

public class MainController implements Initializable {

    private final Properties settings = new Properties();
    private final Path settingsPath = Paths.get(System.getProperty("user.home"), ".mrpg-launcher", "settings.properties");
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
    private Button toggleButtonUp;
    @FXML
    private ButtonBar bottomButtonBar;
    @FXML
    private VBox bottomButtonBox;
    @FXML
    private HBox configContainer;
    @FXML
    private ScrollPane configScrollPane;
    @FXML
    private Button scrollLeftButton;
    @FXML
    private Button scrollRightButton;

    private Auth auth;
    private boolean isBarVisible = false;
    private TranslateTransition slideTransition;
    private int currentConfigIndex = 0;
    private List<LaunchConfig> configs;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSettings();
        updateButtonStates();
        auth = new Auth();
        LaunchConfigManager.init();
        updateUiForLoginState();
        setupSlideAnimation();
        configs = LaunchConfigManager.getConfigs();
        setupCarousel();
    }

    private void setupCarousel() {
        configContainer.getChildren().clear();
        if (configs == null) return;

        for (int i = 0; i < configs.size(); i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("config-card.fxml"));
                StackPane cardNode = loader.load();
                ConfigCardController controller = loader.getController();
                controller.setData(configs.get(i));

                final int index = i;
                cardNode.setOnMouseClicked(event -> {
                    currentConfigIndex = index;
                    updateCarouselSelection();
                });

                configContainer.getChildren().add(cardNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!configs.isEmpty()) {
            updateCarouselSelection();
        }
    }

    private void updateCarouselSelection() {
        for (int i = 0; i < configContainer.getChildren().size(); i++) {
            try {
                StackPane cardNode = (StackPane) configContainer.getChildren().get(i);
                // The controller is stored as a property of the node when loaded via FXML
                ConfigCardController controller = (ConfigCardController) cardNode.getProperties().get("controller");
                if (controller != null) {
                    controller.setSelected(i == currentConfigIndex);
                }
            } catch (Exception e) {
                // It's possible the node or controller isn't what we expect.
                // In a real app, you might want to log this.
                System.err.println("Could not update selection for card at index " + i);
            }
        }
        scrollToIndex(currentConfigIndex);
    }

    @FXML
    protected void onScrollLeft() {
        if (currentConfigIndex > 0) {
            currentConfigIndex--;
            updateCarouselSelection();
        }
    }

    @FXML
    protected void onScrollRight() {
        if (currentConfigIndex < configs.size() - 1) {
            currentConfigIndex++;
            updateCarouselSelection();
        }
    }

    private void scrollToIndex(int index) {
        if (configContainer.getChildren().isEmpty() || index < 0 || index >= configContainer.getChildren().size()) {
            return;
        }

        // We need to wait for the layout pass to get accurate widths.
        Platform.runLater(() -> {
            double viewportWidth = configScrollPane.getViewportBounds().getWidth();
            if (viewportWidth <= 0) {
                return; // Avoid calculations if the UI is not ready
            }

            double contentWidth = configContainer.getWidth();
            double scrollableWidth = contentWidth - viewportWidth;

            // If content is not wider than the viewport, no scrolling is needed.
            if (scrollableWidth <= 0) {
                return;
            }

            javafx.scene.Node card = configContainer.getChildren().get(index);
            Bounds cardBoundsInParent = card.getBoundsInParent();
            double cardCenterX = cardBoundsInParent.getMinX() + (cardBoundsInParent.getWidth() / 2.0);
            double targetViewportLeftX = cardCenterX - (viewportWidth / 2.0);
            double clampedTargetX = Math.max(0, Math.min(targetViewportLeftX, scrollableWidth));
            double targetHValue = clampedTargetX / scrollableWidth;

            // Smooth scroll animation
            final double startHValue = configScrollPane.getHvalue();
            final double deltaHValue = targetHValue - startHValue;
            final int duration = 300; // ms
            final long startTime = System.currentTimeMillis();

            new javafx.animation.AnimationTimer() {
                @Override
                public void handle(long now) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    if (elapsed >= duration) {
                        configScrollPane.setHvalue(targetHValue);
                        stop();
                        return;
                    }
                    double progress = (double) elapsed / duration;
                    // Ease-out interpolation
                    progress = 1 - Math.pow(1 - progress, 3);
                    configScrollPane.setHvalue(startHValue + deltaHValue * progress);
                }
            }.start();
        });
    }

    private void setupSlideAnimation() {
        // Initially hide the button bar by translating it down
        double barHeight = 400; // Approximate height to hide the bar
        bottomButtonBox.setTranslateY(barHeight);

        // Create the slide animation
        slideTransition = new TranslateTransition(Duration.millis(300), bottomButtonBox);
    }

    @FXML
    protected void onToggleButtonClick() {
        if (isBarVisible) {
            // Hide the bar
            toggleButtonUp.setVisible(true);
            slideTransition.setToY(400);
        } else {
            // Show the bar
            toggleButtonUp.setVisible(false);
            slideTransition.setToY(0);
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
