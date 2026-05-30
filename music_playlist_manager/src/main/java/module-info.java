module com.musicmanager {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.musicmanager.controller;
    opens com.musicmanager.controller to javafx.fxml;
    opens com.musicmanager.model to javafx.fxml;
}
