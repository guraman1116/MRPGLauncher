package net.mrpg.mrpglauncher;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import net.mrpg.mrpglauncher.Minecraft.LaunchConfig;

public class ConfigCardController {

    @FXML
    private StackPane cardRoot;

    @FXML
    private ImageView configImageView;

    @FXML
    private Label configNameLabel;

    public void setData(LaunchConfig config) {
        configNameLabel.setText(config.getName());
        if (config.getEpisode().equals("1")) {
            configImageView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("episode1.png")));
        }else {
            configImageView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("default.png")));
        }

        // Here you can set the image based on the config name
        // Example:
        // if (config.getName().toLowerCase().contains("episode1")) {
        //     configImageView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("episode1.png")));
        // } else {
        //     // Set a default image
        //     configImageView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("default.png")));
        // }
    }

    public void setSelected(boolean selected) {
        if (selected) {
            if (!cardRoot.getStyleClass().contains("selected")) {
                cardRoot.getStyleClass().add("selected");
            }
        } else {
            cardRoot.getStyleClass().remove("selected");
        }
    }
}
