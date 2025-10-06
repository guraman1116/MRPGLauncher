package net.mrpg.mrpglauncher;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import net.mrpg.mrpglauncher.Minecraft.Auth;

import java.awt.*;
import java.net.URI;

public class LoginController {

    @FXML
    private Label statusLabel;

    @FXML
    private Label instructionLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    private Auth auth;
    private Runnable onLoginSuccess;

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    @FXML
    protected void onLoginButtonClick() {
        loginButton.setDisable(true);
        errorLabel.setText("");
        statusLabel.setText("認証中...");
        instructionLabel.setText("ブラウザで認証を完了してください");

        // Run authentication in a background thread to avoid blocking the UI
        new Thread(() -> {
            try {
                boolean success = auth.auth(deviceCode -> {
                    // Update UI with device code information
                    Platform.runLater(() -> {
                        statusLabel.setText("デバイスコード: " + deviceCode.getUserCode());
                        instructionLabel.setText("ブラウザで上記のコードを入力してください");
                    });

                    // Open browser with direct verification URL
                    try {
                        Desktop.getDesktop().browse(new URI(deviceCode.getDirectVerificationUri()));
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            errorLabel.setText("ブラウザを開けませんでした。手動で以下のURLにアクセスしてください:\n" +
                                             deviceCode.getVerificationUri());
                        });
                    }
                });

                Platform.runLater(() -> {
                    if (success) {
                        statusLabel.setText("ログイン成功");
                        instructionLabel.setText("ようこそ、" + auth.getUsername() + " さん");

                        // Call the success callback
                        if (onLoginSuccess != null) {
                            onLoginSuccess.run();
                        }

                        // Close the login window after a short delay
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> {
                                    Stage stage = (Stage) loginButton.getScene().getWindow();
                                    stage.close();
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    } else {
                        statusLabel.setText("ログイン失敗");
                        instructionLabel.setText("もう一度お試しください");
                        errorLabel.setText("認証に失敗しました。ブラウザで正しくログインできたか確認してください。");
                        loginButton.setDisable(false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("エラー");
                    instructionLabel.setText("もう一度お試しください");
                    errorLabel.setText("エラーが発生しました: " + e.getMessage());
                    loginButton.setDisable(false);
                    e.printStackTrace();
                });
            }
        }).start();
    }
}
