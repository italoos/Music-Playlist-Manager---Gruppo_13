package com.musicmanager.controller;

import com.musicmanager.PlaybackEngine;
import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.repository.PlaylistRepository;

/** Comando concreto che aggiunge un brano musicale a una playlist. */

public class AddTrackToPlaylistCommand implements Command {

    private final Playlist playlist;
    private final Track track;
    private final PlaylistRepository playlistRepository;

    public AddTrackToPlaylistCommand(Playlist playlist, Track track, PlaylistRepository playlistRepository) {
        this.playlist = playlist;
        this.track = track;
        this.playlistRepository = playlistRepository;
    }

    /** Aggiunge il brano alla playlist, aggiornando il database e la UI. */

    @Override
    public void execute() {

        if (playlist.getTracks().contains(track)) {
            return;
        }

        playlist.addTrack(track);
        playlistRepository.update(playlist);
        PlaybackEngine.getInstance().handlePlaylistModification(playlist);

    }

    /** Rimuove il brano dalla playlist, aggiornando il database e la UI. */

    @Override
    public void undo() {
        playlist.removeTrack(track);
        playlistRepository.update(playlist);
        PlaybackEngine.getInstance().handlePlaylistModification(playlist);
    }

}