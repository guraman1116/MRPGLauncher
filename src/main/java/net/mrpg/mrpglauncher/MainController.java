package net.mrpg.mrpglauncher;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import net.mrpg.mrpglauncher.Minecraft.LaunchConfig;
import net.mrpg.mrpglauncher.Minecraft.LaunchConfigManager;
import net.mrpg.mrpglauncher.Minecraft.MinecraftLaunchHelper;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private final Properties settings = new Properties();
    private final Path settingsPath = Paths.get(System.getProperty("user.home"), ".mrpg-launcher", "settings.properties");

    // FXML UI Components
    @FXML private BorderPane rootPane;
    @FXML private VBox sidebar;
    @FXML private ScrollPane configScrollPane;
    @FXML private VBox configContainer;
    @FXML private BorderPane mainContent;
    @FXML private Label configNameLabel;
    @FXML private Label configDescriptionLabel;
    @FXML private Button startButton;
    @FXML private Button logoutButton;
    @FXML private Button agreementButton;
    @FXML private ProgressBar progressBar;
    @FXML private Button sidebarToggleButton;


    private Auth auth;
    private List<LaunchConfig> configs;
    private int currentConfigIndex = -1;
    private boolean isLaunching = false;
    private boolean isSidebarVisible = true;
    private Process minecraftProcess;
    private Timeline buttonLoadingAnimation;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSettings();
        updateButtonStates();
        auth = new Auth();
        updateUiForLoginState();
        setupHoverAnimations();
    }

    private void setupHoverAnimations() {
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(200), sidebarToggleButton);
        rotateTransition.setCycleCount(1);
        rotateTransition.setAutoReverse(false);

        sidebarToggleButton.setOnMouseEntered(event -> {
            rotateTransition.setToAngle(90);
            rotateTransition.play();
        });

        sidebarToggleButton.setOnMouseExited(event -> {
            rotateTransition.setToAngle(0);
            rotateTransition.play();
        });
    }

    public void postInit(Path configPath) {
        LaunchConfigManager.init(configPath);
        configs = LaunchConfigManager.getConfigs();

        populateSidebar();

        if (!configs.isEmpty()) {
            // Select the first config by default
            currentConfigIndex = 0;
            updateSidebarSelection();
            updateMainContent(configs.get(currentConfigIndex));
        } else {
            // Handle case with no configs
            configNameLabel.setText("利用可能な構成がありません");
            configDescriptionLabel.setText("構成ファイルを確認してください。");
            startButton.setDisable(true);
        }
    }

    private void populateSidebar() {
        configContainer.getChildren().clear();
        if (configs == null) return;

        for (int i = 0; i < configs.size(); i++) {
            LaunchConfig config = configs.get(i);
            Button configButton = new Button(config.getName());
            configButton.getStyleClass().add("nav-button");
            final int index = i;
            configButton.setOnAction(event -> {
                if (!isLaunching) {
                    currentConfigIndex = index;
                    updateSidebarSelection();
                    updateMainContent(configs.get(index));
                }
            });
            configContainer.getChildren().add(configButton);
        }
    }

    private void updateSidebarSelection() {
        for (int i = 0; i < configContainer.getChildren().size(); i++) {
            javafx.scene.Node node = configContainer.getChildren().get(i);
            node.getStyleClass().remove("selected");
            if (i == currentConfigIndex) {
                node.getStyleClass().add("selected");
            }
        }
    }

    private void playButtonLoadingAnimation() {
        startButton.setDisable(true);
        DoubleProperty x = new SimpleDoubleProperty(0);
        x.addListener((obs, oldX, newX) -> {
            double pos = newX.doubleValue();
            String style = String.format( "-fx-background-color: linear-gradient(to right, #5865f2,#5865f2 %.0f%%, #81C784 %.0f%%, #4CAF50 %.0f%%, #4CAF50);",
                    (pos*100) -10,
                    pos*100,
                    (pos*100) + 10
            );
            startButton.setStyle(style);
        });
        buttonLoadingAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(x, 0)),
                new KeyFrame(Duration.seconds(1.0), new KeyValue(x, 1.0))
        );
        buttonLoadingAnimation.setCycleCount(Timeline.INDEFINITE);
        buttonLoadingAnimation.play();
    }

    private void stopButtonLoadingAnimation() {
        if (buttonLoadingAnimation != null) {
            buttonLoadingAnimation.stop();
            buttonLoadingAnimation.setCycleCount(0);
        }
        startButton.setStyle(" ");
        startButton.setDisable(false);
    }

    private void updateMainContent(LaunchConfig config) {
        if (config != null) {
            playButtonLoadingAnimation();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(1000));
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event ->{
                    configNameLabel.setText(config.getName());
                    configDescriptionLabel.setText(config.getDescription());
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), mainContent);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1.0);
                    fadeIn.setOnFinished(e -> stopButtonLoadingAnimation());
                    fadeIn.play();
            });
            fadeOut.play();
        }
    }

    @FXML
    protected void onStartButtonClick() throws IOException {
        if (isLaunching) {
            // Cancel logic
            cancelLaunch();
            return;
        }

        if (auth.isLoggedIn()) {
            startLaunch();
        } else {
            openLoginWindow();
        }
    }

    private void startLaunch() throws IOException {
        isLaunching = true;
        startButton.setText("キャンセル");
        startButton.getStyleClass().add("cancel");
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS); // Indeterminate progress
        setSidebarVisible(false); // Collapse sidebar

        // Disable sidebar buttons
        configContainer.getChildren().forEach(node -> node.setDisable(true));

        System.out.println("Starting Minecraft...");
        MinecraftLaunchHelper.launch( SettingsManager.getInstance().getProperty("config_path", "\\"), configs.get(currentConfigIndex), auth);
        //TODO: Add actual Minecraft Launch method here
    }

    private void cancelLaunch() {
        isLaunching = false;
        startButton.setText("開始");
        startButton.getStyleClass().remove("cancel");
        progressBar.setVisible(false);
        progressBar.setProgress(0);
        setSidebarVisible(true); // Expand sidebar

        // Enable sidebar buttons
        configContainer.getChildren().forEach(node -> node.setDisable(false));

        System.out.println("Launch cancelled.");
        //TODO: Add any necessary cancellation logic for the launch process
    }

    @FXML
    protected void toggleSidebar() {
        setSidebarVisible(!isSidebarVisible);
    }

    private void setSidebarVisible(boolean visible) {
        if (isSidebarVisible == visible) {
            return; // Already in the desired state
        }
        isSidebarVisible = visible;

        final double targetWidth = visible ? 280.0 : 0.0;

        sidebar.setPrefWidth(sidebar.getWidth());

        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(sidebar.prefWidthProperty(), targetWidth);
        KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        if (visible) {
            sidebar.setManaged(true);
            sidebar.setVisible(true);
        } else {
            timeline.setOnFinished(event -> {
                sidebar.setManaged(false);
                sidebar.setVisible(false);
            });
        }

        timeline.play();
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

    private void openLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();
            loginController.setAuth(auth);
            loginController.setOnLoginSuccess(() -> {
                updateUiForLoginState();
                // Automatically start launch after successful login
                try {
                    startLaunch();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Microsoft アカウントでログイン");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
        } else {
            startButton.setText("ログイン");
        }
        logoutButton.setVisible(auth.isLoggedIn());
    }
}