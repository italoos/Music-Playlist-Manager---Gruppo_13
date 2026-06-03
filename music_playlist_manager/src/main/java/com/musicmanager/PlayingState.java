package com.musicmanager;

public class PlayingState implements PlayerState {

    @Override
    public void play(PlaybackEngine engine) {
        // già in riproduzione
    }

    @Override
    public void pause(PlaybackEngine engine) {

        engine.setState(new PausedState());

        engine.notifyObservers();
    }

    @Override
    public void skip(PlaybackEngine engine) {

       
    }
}