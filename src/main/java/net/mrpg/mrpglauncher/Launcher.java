package net.mrpg.mrpglauncher;

import javafx.application.Application;
import net.mrpg.mrpglauncher.Minecraft.Auth;

public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
        new Auth();
    }
}
