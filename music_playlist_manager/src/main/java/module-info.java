module com.musicmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.musicmanager to javafx.fxml;
    opens com.musicmanager.database to javafx.fxml;
    exports com.musicmanager;
    exports com.musicmanager.database;
}
