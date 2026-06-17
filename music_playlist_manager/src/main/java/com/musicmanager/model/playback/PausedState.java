package com.musicmanager.model.playback;

/**
 * Stato del player quando la riproduzione e ferma o in pausa.
 */
public class PausedState implements PlayerState {

    /**
     * Fa ripartire la riproduzione del brano corrente.
     * @param engine Il motore di riproduzione.
     */
    @Override
    public void play(PlaybackEngine engine) {

        if (engine.getCurrentTrack() == null) {
            return;
        }

        engine.setState(new PlayingState());

        engine.notifyObservers();
    }

    /**
     * Mette in pausa la riproduzione del brano corrente.
     * @param engine Il motore di riproduzione.
     */
    @Override
    public void pause(PlaybackEngine engine) {
        // già in pausa
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
