module com.musicmanager {

    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.base;
    requires transitive java.sql;

    opens com.musicmanager.controller to javafx.fxml;
    opens com.musicmanager.model to javafx.fxml;
    opens com.musicmanager.view to javafx.fxml;

    exports com.musicmanager;
    exports com.musicmanager.controller;
    exports com.musicmanager.database;
    exports com.musicmanager.model;
    exports com.musicmanager.model.generator;
    exports com.musicmanager.model.io;
    exports com.musicmanager.model.playback;
    exports com.musicmanager.repository;
    exports com.musicmanager.view;

}
