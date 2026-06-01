package com.musicmanager;

public class PausedState implements PlayerState {

    @Override
    public void play(PlaybackEngine engine) {

        if (engine.getCurrentTrack() == null) {
            return;
        }

        engine.setState(new PlayingState());

        engine.notifyObservers();
    }

    @Override
    public void pause(PlaybackEngine engine) {
        // già in pausa
    }

    @Override
    public void skip(PlaybackEngine engine) {

        
    }
}