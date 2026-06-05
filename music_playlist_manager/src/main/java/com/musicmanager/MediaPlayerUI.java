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

    public void setController(MainController controller) {
        this.controller = controller;
    }

    private void initComponents() {
        trackLabel = new Label("Nessuna traccia selezionata");
        timeLabel = new Label("00:00 / 00:00");
        playButton = new Button("▶"); // Unicode per simbolo play
        trackLoopButton = new Button("🔁"); // Unicode per simbolo loop
        progressBar = new ProgressBar(0);

        trackLabel.setMaxWidth(Double.MAX_VALUE);
        trackLabel.setMinWidth(180);

        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(12);
        progressBar.setMinHeight(12);
        progressBar.setMaxHeight(12);
        progressBar.setStyle("-fx-accent: #1DB954;");
    }

    private void initActions() {
        playButton.setOnAction(e -> handlePlaybackAction());
        trackLoopButton.setOnAction(e -> handleTrackLoopAction());
    }

    private void handlePlaybackAction() {
        if (controller == null) {
            return;
        }

        controller.handleTogglePlayback();
    }

    private void handleTrackLoopAction() {
        if (controller == null) {
            return;
        }

        //controller.handleToggleTrackLoop();
    }

    private void createLayout() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        setPadding(new Insets(8, 12, 8, 12));
        setMinHeight(56);
        setStyle("-fx-border-color: #d8d8d8 transparent transparent transparent;");

        HBox.setHgrow(trackLabel, Priority.ALWAYS);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        getChildren().addAll(playButton, trackLabel, progressBar, timeLabel);
    }

    @Override
    public void update(Track currentTrack, Playlist currentPlaylist, int currentTime, boolean isPlaying) {
        playButton.setText(isPlaying ? "⏸" : "▶"); // Unicode per simbolo pause e play

        if (currentTrack != null) {
            trackLabel.setText(currentTrack.getTitle() + " - " + currentTrack.getAuthor() + "  (" + currentTrack.getGenre() + ", " + currentTrack.getYear() + ")");
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

    public void showMessage(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);

        alert.setTitle("Music Manager");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
