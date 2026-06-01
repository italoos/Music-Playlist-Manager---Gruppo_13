module com.musicmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.musicmanager.controller;
    opens com.musicmanager.controller to javafx.fxml;
    opens com.musicmanager.database to javafx.fxml;
    opens com.musicmanager.model to javafx.fxml;
    exports com.musicmanager.database;
}
