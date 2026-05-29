module com.musicmanager {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.musicmanager to javafx.fxml;
    opens com.musicmanager.controller to javafx.fxml;
    opens com.musicmanager.model to javafx.fxml;
    exports com.musicmanager;
}
