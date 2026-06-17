package com.musicmanager.model.playback;

public class PlayingState implements PlayerState {

    /**
     * Non fa nulla perchè il brano è già in riproduzione.
     * @param engine Il motore di riproduzione.
     */
    @Override
    public void play(PlaybackEngine engine) {
        // già in riproduzione
    }

    /**
     * Mette in pausa la riproduzione del brano corrente.
     * @param engine Il motore di riproduzione.
     */
    @Override
    public void pause(PlaybackEngine engine) {

        engine.setState(new PausedState());

        engine.notifyObservers();
    }

    /**
     * Passa al brano successivo.
     * @param engine Il motore di riproduzione.
     */
    @Override
    public void skip(PlaybackEngine engine) {
        engine.nextTrack();
    }
}
