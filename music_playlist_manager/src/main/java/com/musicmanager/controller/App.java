package com.musicmanager.controller;

import javafx.application.Application;
import javafx.stage.Stage;
import com.musicmanager.model;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        PlaybackEngine playbackEngine = PlaybackEngine.getInstance();

        Track defaultTrack = new Track(
        "Default Song",
        "Test Artist",
        180,
        "Pop",
        2024
);

       playbackEngine.setCurrentTrack(defaultTrack);

        MediaPlayerUI view = new MediaPlayerUI(primaryStage);

        MainController controller =
                new MainController(playbackEngine, view);

        view.setController(controller);

        playbackEngine.registerObserver(view);
    }

    public static void main(String[] args) {
        launch(args);
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/musicmanager/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}