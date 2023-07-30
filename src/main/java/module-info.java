module com.example.final_rev {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.example.final_rev to javafx.fxml;
    exports com.example.final_rev;
}