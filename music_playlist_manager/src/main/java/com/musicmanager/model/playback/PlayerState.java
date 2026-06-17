package com.musicmanager.model.playback;

/**
 * Stato operativo del player nel pattern State.
 */
public interface PlayerState {

    /**
     * Fa ripartire la riproduzione del brano corrente.
     * @param engine Il motore di riproduzione.
     */
    void play(PlaybackEngine engine);

    /**
     * Mette in pausa la riproduzione del brano corrente.
     * @param engine Il motore di riproduzione.
     */
    void pause(PlaybackEngine engine);

    /**
     * Passa al brano successivo.
     * @param engine Il motore di riproduzione.
     */
    void skip(PlaybackEngine engine);
}
