package com.musicmanager;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MediaPlayerUI implements PlaybackObserver {

    private MainController controller;

    private Button trackButton;
    private Label timeLabel;
    private Button playButton;

    private ProgressBar progressBar;

    private double displayedProgress = 0;
    private double targetProgress = 0;

    private Stage stage;

    public MediaPlayerUI(Stage stage) {

        this.stage = stage;

        initComponents();
        initActions();

        VBox root = createRoot();

        initProgressBar(root);
        initSmoothAnimation();

        Scene scene = new Scene(root, 300, 200);

        stage.setTitle("Music Manager");
        stage.setScene(scene);
        stage.show();

        initDefaultTrack();
    }

    public void setController(MainController controller) {
        this.controller = controller;
    }

    // Inizializzazione dei componenti grafici
    private void initComponents() {

        trackButton = new Button("🎵 Nessuna traccia selezionata");
        timeLabel = new Label("00:00");
        playButton = new Button("Play");
    }

    // Registrazione degli event handler
    private void initActions() {

        trackButton.setOnAction(e -> handlePlaybackAction());

        playButton.setOnAction(e -> handlePlaybackAction());
    }

    // Gestione Play/Pause tramite Controller
    private void handlePlaybackAction() {

        if (controller == null) {
            return;
        }

        controller.handleTogglePlayback();
    }

    // Creazione del layout principale
    private VBox createRoot() {

        VBox root = new VBox(10);

        root.getChildren().addAll(
                trackButton,
                timeLabel,
                playButton
        );

        return root;
    }

    // Inizializzazione della traccia di default
    private void initDefaultTrack() {

        // TEST TRACK
        Track defaultTrack = new Track(
                "Default Song",
                "Test Artist",
                180,
                "Pop",
                2024
        );

        trackButton.setText("🎵 " + defaultTrack.getTitle());
    }

    //Update sulla base del pattern Observer con aggiornamento del MediaPlayer
    //sulla base delle operazioni effettuate dall'utente
    @Override
    public void update(
            Track currentTrack,
            Playlist currentPlaylist,
            int currentTime,
            boolean isPlaying) {

        playButton.setText(isPlaying ? "Pause" : "Play");

        if (currentTrack != null) {
            trackButton.setText("🎵 " + currentTrack.getTitle());
        } else {
            trackButton.setText("🎵 Nessuna traccia selezionata");
        }

        // Tempo
        int currentMin = currentTime / 60;
        int currentSec = currentTime % 60;
        String currentTimeStr =
                String.format("%02d:%02d", currentMin, currentSec);

        if (currentTrack != null && currentTrack.getLength() > 0) {

            int totalMin = currentTrack.getLength() / 60;
            int totalSec = currentTrack.getLength() % 60;

            String totalTimeStr =
                    String.format("%02d:%02d", totalMin, totalSec);

            timeLabel.setText(
                    currentTimeStr + " / " + totalTimeStr
            );

            // Logica di aggiornamento della progress bar
            targetProgress =
                    (double) currentTime / currentTrack.getLength();

            targetProgress =
                    Math.max(0, Math.min(1, targetProgress));

        } else {

            timeLabel.setText(currentTimeStr + " / 00:00");

            targetProgress = 0;
        }
    }

    //Inizializzazione Progress Bar
    private void initProgressBar(VBox root) {

        progressBar = new ProgressBar(0);

        progressBar.setPrefWidth(260);

        progressBar.setPrefHeight(20);
        progressBar.setMinHeight(20);
        progressBar.setMaxHeight(20);

        // Stile minimale
        progressBar.setStyle(
                "-fx-accent: #1DB954;"
        );

        root.getChildren().add(progressBar);
    }

    //Smoothing sulla progress bar
    private void initSmoothAnimation() {

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {

                displayedProgress +=
                        (targetProgress - displayedProgress) * 0.12;

                progressBar.setProgress(displayedProgress);
            }
        };

        timer.start();
    }

    //Messaggio di Alert per notificare l'utente su vari problemi
    public void showMessage(String message) {

        Alert alert = new Alert(AlertType.INFORMATION);

        alert.setTitle("Music Manager");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}