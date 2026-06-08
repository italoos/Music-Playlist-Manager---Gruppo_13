package com.musicmanager;

import com.musicmanager.controller.MainController;
import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class MediaPlayerUI extends HBox implements PlaybackObserver {

    private MainController controller;

    private Label trackLabel;
    private Label timeLabel;
    private Button playButton;
    private Button skipButton;
    private Button sequentialButton;
    private Button shuffleButton;
    private Button playlistLoopButton;
    private Button trackLoopButton;
    private ProgressBar progressBar;

    private double displayedProgress = 0;
    private double targetProgress = 0;

    public MediaPlayerUI() {
        initComponents();
        initActions();
        createLayout();
        initSmoothAnimation();
    }

    /**
     * Imposta il controller per gestire le azioni dell'interfaccia utente.
     * @param controller Il controller da associare all'interfaccia utente.
     */
    public void setController(MainController controller) {
        this.controller = controller;
    }

    /**
     * Inizializza i componenti dell'interfaccia utente.
     */
    private void initComponents() {
        trackLabel = new Label("Nessuna traccia selezionata");
        timeLabel = new Label("00:00 / 00:00");
        playButton = new Button("▶"); // Unicode per simbolo play
        skipButton = new Button("▶|");
        sequentialButton = new Button("▶▶");
        sequentialButton.setStyle("-fx-background-color: #3498db;"); // Attivo di default
        shuffleButton = new Button("🔀");
        playlistLoopButton = new Button("Playlist🔁");
        trackLoopButton = new Button("🔁¹"); // Unicode per simbolo loop
        progressBar = new ProgressBar(0);

        trackLabel.setMaxWidth(Double.MAX_VALUE);
        trackLabel.setMinWidth(180);

        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(12);
        progressBar.setMinHeight(12);
        progressBar.setMaxHeight(12);
        progressBar.setStyle("-fx-accent: #1DB954;");
    }

    /**
     * Inizializza le azioni per i componenti dell'interfaccia utente.
     */
    private void initActions() {
        playButton.setOnAction(e -> handlePlaybackAction());
        skipButton.setOnAction(e -> handleSkipAction());
        sequentialButton.setOnAction(e -> handleSequentialAction());
        shuffleButton.setOnAction(e -> handleShuffleAction());
        playlistLoopButton.setOnAction(e -> handlePlaylistLoopAction());
        trackLoopButton.setOnAction(e -> handleTrackLoopAction());
    }

    /**
     * Gestisce l'azione di riproduzione/pausa.
     */
    private void handlePlaybackAction() {
        if (controller == null) {
            return;
        }

        controller.handleTogglePlayback();
    }

    /**
     * Gestisce lo skip della traccia corrente.
     */
    private void handleSkipAction() {
        if (controller == null) {
            return;
        }

        controller.handleSkip();
    }


    private void handleSequentialAction(){
        if(controller == null){
            return;
        }
        resetStrategyButtons();

        sequentialButton.setStyle("-fx-background-color: #3498db;");

        controller.handleSetStrategy(new SequentialStrategy());
    }

     /**
     * Gestisce la riproduzione casuale della playlist.
     */
    private void handleShuffleAction(){
        if(controller == null){
            return;

        }
        resetStrategyButtons();

        shuffleButton.setStyle("-fx-background-color: #3498db;");

        controller.handleSetStrategy(new ShuffleStrategy());
    }

     /**
     * Gestisce l'azione di loop della playlist.
     */
    private void handlePlaylistLoopAction(){
        if(controller == null){
            return;
        }
        resetStrategyButtons();

        playlistLoopButton.setStyle("-fx-background-color: #3498db;");
        controller.handleSetStrategy(new PlaylistLoopStrategy());
    }

    /**
     * Gestisce l'azione di loop della traccia.
     */
    private void handleTrackLoopAction() {
        if (controller == null) {
            return;
        }
        resetStrategyButtons();

        trackLoopButton.setStyle("-fx-background-color: #3498db;");
         
        controller.handleSetStrategy(new TrackLoopStrategy());
    }

    /**
     * Resetta il pulsante della modalità selezionata.
     */
    private void resetStrategyButtons() {

        sequentialButton.setStyle("");
        shuffleButton.setStyle("");
        playlistLoopButton.setStyle("");
        trackLoopButton.setStyle("");
    }

    /**
     * Crea il layout dell'interfaccia utente.
     */
    private void createLayout() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        setPadding(new Insets(8, 12, 8, 12));
        setMinHeight(56);
        setStyle("-fx-border-color: #d8d8d8 transparent transparent transparent;");

        HBox.setHgrow(trackLabel, Priority.ALWAYS);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        getChildren().addAll(playButton, skipButton, trackLabel, shuffleButton, sequentialButton, playlistLoopButton, trackLoopButton, progressBar, timeLabel);
    }

    /**
     * Aggiorna l'interfaccia utente in base allo stato attuale del MediaPlayer.
     * @param currentTrack La traccia attualmente in riproduzione.
     * @param currentPlaylist La playlist attualmente in riproduzione.
     * @param currentTime Il tempo attuale della traccia.
     * @param isPlaying Indica se la traccia è in riproduzione.
     */
    @Override
    public void update(Track currentTrack, Playlist currentPlaylist, int currentTime, boolean isPlaying) {
        playButton.setText(isPlaying ? "⏸" : "▶"); // Unicode per simbolo pause e play

        if (currentTrack != null) {
            trackLabel.setText(currentPlaylist.getName() + "\n" + currentTrack.getTitle() + " - " + currentTrack.getAuthor() + "  (" + currentTrack.getGenre() + ", " + currentTrack.getYear() + ")");
        } else {
            trackLabel.setText("Nessuna traccia selezionata");
        }

        int currentMin = currentTime / 60;
        int currentSec = currentTime % 60;
        String currentTimeStr = String.format("%02d:%02d", currentMin, currentSec);

        if (currentTrack != null && currentTrack.getLength() > 0) {
            int totalMin = currentTrack.getLength() / 60;
            int totalSec = currentTrack.getLength() % 60;
            String totalTimeStr = String.format("%02d:%02d", totalMin, totalSec);

            timeLabel.setText(currentTimeStr + " / " + totalTimeStr);

            targetProgress = (double) currentTime / currentTrack.getLength();
            targetProgress = Math.max(0, Math.min(1, targetProgress));
        } else {
            timeLabel.setText(currentTimeStr + " / 00:00");
            targetProgress = 0;
        }
    }

    /**
     * Inizializza l'animazione fluida per la barra di avanzamento.
     */
    private void initSmoothAnimation() {
        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                displayedProgress += (targetProgress - displayedProgress) * 0.12;
                progressBar.setProgress(displayedProgress);
            }
        };

        timer.start();
    }

    /**
     * Mostra un messaggio informativo all'utente.
     * @param message Il messaggio da visualizzare nell'alert.
     */
    public void showMessage(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);

        alert.setTitle("Music Manager");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
