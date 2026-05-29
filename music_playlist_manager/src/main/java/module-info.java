module com.musicmanager {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.musicmanager to javafx.fxml;
    exports com.musicmanager;
}
