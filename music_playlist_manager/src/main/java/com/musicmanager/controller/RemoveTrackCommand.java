package com.musicmanager.controller;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.model.playback.PlaybackEngine;
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

    private final List<Integer> affectedPlaylistIndices = new ArrayList<>();

    /**
     * Crea il comando per rimuovere una traccia dal catalogo generale.
     * @param track Traccia da rimuovere.
     * @param trackRepository Repository usato per eliminare o ripristinare la traccia.
     * @param playlistRepository Repository usato per aggiornare le playlist coinvolte.
     * @param tracks Lista osservabile mostrata dalla GUI.
     */
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

        index = tracks.indexOf(track);
        affectedPlaylists.clear();
        affectedPlaylistIndices.clear();

        PlaybackEngine engine = PlaybackEngine.getInstance();

        if (engine.getCurrentTrack() != null && engine.getCurrentTrack().getId() == track.getId()) {
            engine.nextTrack();
        }

        List<Playlist> playlists = playlistRepository.findAll();

        for (Playlist p : playlists) {
            int trackIndex = p.getTracks().indexOf(track);
            if (trackIndex != -1) {
                affectedPlaylists.add(p);
                affectedPlaylistIndices.add(trackIndex);
                p.removeTrack(track);
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

        PlaybackEngine engine = PlaybackEngine.getInstance();

        for (int i = 0; i < affectedPlaylists.size(); i++) {

            Playlist p = affectedPlaylists.get(i);
            int playlistTrackIndex = affectedPlaylistIndices.get(i);

            if (playlistTrackIndex >= 0 && playlistTrackIndex <= p.getTracks().size()) {
                p.getTracks().add(playlistTrackIndex, track);
            } else {
                p.addTrack(track);
            }

            engine.handlePlaylistModification(p);

            playlistRepository.update(p);

        }

        engine.notifyObservers();
        
        System.out.println("[COMMAND] INFO: RemoveTrackCommand undone for track: " + track.getTitle());
        
    }

}
