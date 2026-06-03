module com.musicmanager {

    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;

    opens com.musicmanager.controller to javafx.fxml;
    opens com.musicmanager to javafx.fxml;
    opens com.musicmanager.model to javafx.fxml;
    opens com.musicmanager.database to javafx.fxml;
    opens com.musicmanager.repository to javafx.fxml;

    exports com.musicmanager;
    exports com.musicmanager.controller;
    exports com.musicmanager.database;
    exports com.musicmanager.repository;

}
