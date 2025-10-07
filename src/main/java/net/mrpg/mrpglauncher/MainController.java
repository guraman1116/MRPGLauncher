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
import javafx.scene.input.MouseEvent;
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
    private ProgressBar MCProgress;
    @FXML
    private Label DetailsLabel;
    @FXML
    private Button agreementButton;
    @FXML
    private StackPane black;
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
    @FXML
    private StackPane rootPane;

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
        bottomButtonBox.setVisible(false);
        // Add a click handler to the root pane to close the bottom bar
        rootPane.setOnMouseClicked(event -> {
        });
    }
    @FXML
    private void onMouseClicked(MouseEvent event) {
            if (isBarVisible) {
                // Check if the click was outside the bottom bar and not on the toggle buttons
                javafx.scene.Node target = (javafx.scene.Node) event.getTarget();
                boolean isClickOnBarOrToggle = false;

                if (target == toggleButton || target == toggleButtonUp) {
                    isClickOnBarOrToggle = true;
                }

                javafx.scene.Node parent = target;
                while (parent != null) {
                    if (parent == bottomButtonBox) {
                        isClickOnBarOrToggle = true;
                        break;
                    }
                    parent = parent.getParent();
                }

                if (!isClickOnBarOrToggle) {
                    hideBottomBar();
                }
            }

    }

    private void setupCarousel() {
        configContainer.getChildren().clear();
        if (configs == null) return;

        for (int i = 0; i < configs.size(); i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("config-card.fxml"));
                StackPane cardNode = loader.load();
                // Manually set the controller to the node's properties
                ConfigCardController controller = loader.getController();
                cardNode.getProperties().put("controller", controller);
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
                ConfigCardController controller = (ConfigCardController) cardNode.getProperties().get("controller");

                if (controller.getConfig().getEpisode().equals("1")){
                    rootPane.getStyleClass().remove("config-card-default");
                    rootPane.getStyleClass().add("config-card-background-1");
                }else {
                    rootPane.getStyleClass().remove("config-card-background-1");
                    rootPane.getStyleClass().add("config-card-default");
                }
                if (controller != null) {
                    controller.setSelected(i == currentConfigIndex);
                }
            } catch (Exception e) {
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

            javafx.scene.Node card = configContainer.getChildren().get(index);
            Bounds cardBoundsInParent = card.getBoundsInParent();
            double cardCenterX = cardBoundsInParent.getMinX() + (cardBoundsInParent.getWidth() / 2.0);

            // Smooth animation setup
            final int duration = 300; // ms
            final long startTime = System.currentTimeMillis();

            if (scrollableWidth <= 0) {
                // --- Case 1: All content fits, use translation to center ---
                configScrollPane.setHvalue(0); // Ensure no scrolling is active

                double targetTranslateX = (viewportWidth / 2.0) - cardCenterX;

                final double startTranslateX = configContainer.getTranslateX();
                final double deltaTranslateX = targetTranslateX - startTranslateX;

                new javafx.animation.AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        if (elapsed >= duration) {
                            configContainer.setTranslateX(targetTranslateX);
                            stop();
                            return;
                        }
                        double progress = (double) elapsed / duration;
                        progress = 1 - Math.pow(1 - progress, 3); // Ease-out
                        configContainer.setTranslateX(startTranslateX + deltaTranslateX * progress);
                    }
                }.start();

            } else {
                // --- Case 2: Content is scrollable, use hvalue ---
                configContainer.setTranslateX(0); // Ensure no translation is active

                double targetViewportLeftX = cardCenterX - (viewportWidth / 2.0);
                double clampedTargetX = Math.max(0, Math.min(targetViewportLeftX, scrollableWidth));
                double targetHValue = clampedTargetX / scrollableWidth;

                final double startHValue = configScrollPane.getHvalue();
                final double deltaHValue = targetHValue - startHValue;

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
                        progress = 1 - Math.pow(1 - progress, 3); // Ease-out
                        configScrollPane.setHvalue(startHValue + deltaHValue * progress);
                    }
                }.start();
            }
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
            hideBottomBar();
        } else {
            showBottomBar();
        }
    }

    private void showBottomBar() {
        if (!isBarVisible) {
            toggleButtonUp.setVisible(false);
            slideTransition.setToY(0);
            slideTransition.play();
            isBarVisible = true;
            bottomButtonBox.setVisible(true);
            rootPane.getStyleClass().add("blur");
            black.setVisible(true);
        }
    }

    private void hideBottomBar() {
        if (isBarVisible) {
            toggleButtonUp.setVisible(true);
            slideTransition.setToY(400);
            slideTransition.play();
            isBarVisible = false;
            bottomButtonBox.setVisible(false);
            rootPane.getStyleClass().remove("blur");
            black.setVisible(false);
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
            startButton.setText("開始");
            logoutButton.setVisible(true);
        } else {
            startButton.setText("ログイン");
            logoutButton.setVisible(false);
        }
    }
}
