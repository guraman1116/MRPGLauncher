module net.mrpg.mrpglauncher {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires MinecraftAuth;
    requires httpclient;
    requires java.datatransfer;
    requires java.desktop;

    opens net.mrpg.mrpglauncher to javafx.fxml;
    exports net.mrpg.mrpglauncher;
}