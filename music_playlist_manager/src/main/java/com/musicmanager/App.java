package com.musicmanager;

import javafx.application.Application;
import javafx.stage.Stage;

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
    }
}