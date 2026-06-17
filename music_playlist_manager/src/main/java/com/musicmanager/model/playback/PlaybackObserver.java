package com.musicmanager.model.playback;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;

/**
 * Observer notificato dal PlaybackEngine a ogni variazione della riproduzione.
 */
public interface PlaybackObserver{

    /**
     * Aggiorna l'osservatore con lo stato corrente della riproduzione.
     * @param CurrentTrack Traccia corrente.
     * @param CurrentPlaylist Playlist corrente.
     * @param currentTime Tempo di riproduzione corrente in secondi.
     * @param isPlaying true se il player e in riproduzione, false altrimenti.
     */
    void update(Track CurrentTrack, Playlist CurrentPlaylist, int currentTime, boolean isPlaying);
}
