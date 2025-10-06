module net.mrpg.mrpglauncher {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires net.hycrafthd.minecraft_authenticator;

    opens net.mrpg.mrpglauncher to javafx.fxml;
    exports net.mrpg.mrpglauncher;
}