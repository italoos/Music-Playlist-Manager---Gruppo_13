package com.musicmanager;
import java.util.ArrayList;
import java.util.List;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class PlaybackEngine {

    private static PlaybackEngine instance;

    private List<PlaybackObserver> observers = new ArrayList<>();

    private Playlist currentPlaylist;
    private Track currentTrack;
    private int currentTime;
    private Timeline timeline;
    private PlayerState currentState;

    private PlaybackEngine() {
        this(true);
    }

    private PlaybackEngine(boolean initializeTimeline) {
        currentState = new PausedState();
        currentTime = 0;

        if (initializeTimeline) {
            initTimeline();
        }
    }

    private void initTimeline() {
        timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {

                if (!(currentState instanceof PlayingState)) {
                    return;
                }

                if (currentTrack == null) {
                    return;
                }

                currentTime++;

                if (currentTime >= currentTrack.getLength()) {
                    currentTime = currentTrack.getLength();
                    currentState = new PausedState();
                }

                notifyObservers();
            })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public static PlaybackEngine getInstance() {
        if (instance == null) {
            instance = new PlaybackEngine();
        }
        return instance;
    }

    static PlaybackEngine getTestInstance() {
        instance = new PlaybackEngine(false);
        return instance;
    }

    public void registerObserver(PlaybackObserver o) {
        if (observers.contains(o)) {
            return;
        }

        observers.add(o);
    }

    public void notifyObservers() {
        for (PlaybackObserver o : observers) {
            o.update(
                currentTrack,
                currentPlaylist,
                currentTime,
                isPlaying()
            );
        }
    }

    public void play() {
        currentState.play(this);
    }

    public void pause() {
        currentState.pause(this);
    }

    public void skip() {
        currentState.skip(this);
    }

    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
        this.currentTime = 0;
        notifyObservers();
    }

    public void setCurrentPlaylist(Playlist currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void setState(PlayerState state) {
        this.currentState = state;
    }

    //Controlla se il MediaPlayer è attualmente in riproduzione
    public boolean isPlaying() {
        return currentState instanceof PlayingState;
    }
}
