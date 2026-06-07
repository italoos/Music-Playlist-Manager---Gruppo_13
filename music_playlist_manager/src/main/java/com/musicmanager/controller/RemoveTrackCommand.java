package com.musicmanager.controller;

import com.musicmanager.PlaybackEngine;
import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.repository.PlaylistRepository;
import com.musicmanager.repository.TrackRepository;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;

/** Comando concreto che rimuove un brano musicale. */

public class RemoveTrackCommand implements Command {

    private final Track track;
    private final TrackRepository trackRepository;
    private final PlaylistRepository playlistRepository;
    private final ObservableList<Track> tracks;

    /** Indice della posizione originale del brano nella lista dei brani. */

    private int index;

    /** Playlist che contenevano il brano prima della rimozione. */

    private final List<Playlist> affectedPlaylists = new ArrayList<>();

    public RemoveTrackCommand(Track track, TrackRepository trackRepository, PlaylistRepository playlistRepository, ObservableList<Track> tracks) {
        this.track = track;
        this.trackRepository = trackRepository;
        this.playlistRepository = playlistRepository;
        this.tracks = tracks;
        this.index = -1;
    }

    /** Rimuove il brano, aggiornando la UI, il database e le playlist che lo contenevano. */

    @Override
    public void execute() {

        this.index = tracks.indexOf(track);
        affectedPlaylists.clear();

        PlaybackEngine engine = PlaybackEngine.getInstance();

        if (engine.getCurrentTrack() != null && engine.getCurrentTrack().getId() == track.getId()) {
            engine.nextTrack();
        }

        List<Playlist> playlists = playlistRepository.findAll();

        for (Playlist p : playlists) {
            boolean containsTrack = p.getTracks().contains(track);
            if (containsTrack) {
                p.removeTrack(track);
                affectedPlaylists.add(p);
            }
        }

        tracks.remove(track);
        trackRepository.delete(track.getId());

        for (Playlist p : affectedPlaylists) {
            engine.handlePlaylistModification(p);
        }

        System.out.println("[COMMAND] INFO: RemoveTrackCommand executed for track: " + track.getTitle());

    }

    /** Ripristina il brano, reinserendolo nella UI, nel database e nelle playlist che lo contenevano. */

    @Override
    public void undo() {

        trackRepository.save(track);

        if (index >= 0 && index <= tracks.size()) {
            tracks.add(index, track);
        } else {
            tracks.add(track);
        }

        for (Playlist p : affectedPlaylists) {
            p.addTrack(track);
            playlistRepository.update(p);
        }

        PlaybackEngine.getInstance().notifyObservers();
        
        System.out.println("[COMMAND] INFO: RemoveTrackCommand undone for track: " + track.getTitle());
        
    }

}