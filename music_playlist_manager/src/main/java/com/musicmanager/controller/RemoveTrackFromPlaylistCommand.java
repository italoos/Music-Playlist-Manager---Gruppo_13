package com.musicmanager.controller;

import com.musicmanager.PlaybackEngine;
import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.repository.PlaylistRepository;

/** Comando concreto che rimuove un brano musicale da una playlist. */

public class RemoveTrackFromPlaylistCommand implements Command {

    private final Playlist playlist;
    private final Track track;
    private final PlaylistRepository playlistRepository;

    private int index;

    /** Indica se il brano era in riproduzione nella playlist al momento della rimozione. */

    private boolean wasPlayingInThisPlaylist = false;

    public RemoveTrackFromPlaylistCommand(Playlist playlist, Track track, PlaylistRepository playlistRepository) {
        this.playlist = playlist;
        this.track = track;
        this.playlistRepository = playlistRepository;
        this.index = -1;
    }

    /** Rimuove il brano dalla playlist, aggiornando il database, la UI e la riproduzione corrente quando necessario. */

    @Override
    public void execute() {

        PlaybackEngine engine = PlaybackEngine.getInstance();

        if (engine.getCurrentPlaylist() != null && engine.getCurrentPlaylist().getId() == playlist.getId() && engine.getCurrentTrack() != null && engine.getCurrentTrack().getId() == track.getId()) {
            wasPlayingInThisPlaylist = true;
            engine.nextTrack();
        }

        index = playlist.getTracks().indexOf(track);

        playlist.removeTrack(track);
        playlistRepository.update(playlist);
        
        engine.handlePlaylistModification(playlist);

    }

    /** Ripristina il brano nella playlist, aggiornando il database, la UI e la riproduzione corrente quando necessario. */

    @Override
    public void undo() {

        if (index >= 0 && index <= playlist.getTracks().size()) {
            playlist.getTracks().add(index, track);
        } else {
            playlist.addTrack(track);
        }

        playlistRepository.update(playlist);
        
        PlaybackEngine engine = PlaybackEngine.getInstance();
        engine.handlePlaylistModification(playlist);
        
        if (wasPlayingInThisPlaylist) {
            engine.setCurrentTrack(track);
            wasPlayingInThisPlaylist = false;
        }

    }

}