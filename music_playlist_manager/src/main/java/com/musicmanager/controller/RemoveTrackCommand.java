package com.musicmanager.controller;

import com.musicmanager.model.Track;
import com.musicmanager.repository.TrackRepository;
import javafx.collections.ObservableList;

/** Comando concreto responsabile della rimozione di un brano musicale. */

public class RemoveTrackCommand implements Command {

    private final Track track;
    private final TrackRepository trackRepository;
    private final ObservableList<Track> tracks;

    /** Indice della posizione originale del brano nella ObservableList<Track>. */

    private int index;

    /** Costruttore del comando di rimozione. */

    public RemoveTrackCommand(Track track, TrackRepository trackRepository, ObservableList<Track> tracks) {
        this.track = track;
        this.trackRepository = trackRepository;
        this.tracks = tracks;
        this.index = -1;
    }

    /**
     * Esegue la rimozione del brano musicale.
     * Rimuove il brano sia dalla lista della UI sia dal database,
     * salvando la posizione originale per l'eventuale undo.
     */

    @Override
    public void execute() {
        this.index = tracks.indexOf(track);
        tracks.remove(track);
        trackRepository.delete(track.getId());
        System.out.println("[COMMAND] INFO: RemoveTrackCommand executed for track: " + track.getTitle());
    }

    /**
     * Ripristina il brano musicale rimosso alla sua posizione originale.
     * Reinserisce il brano nella lista e lo salva nuovamente nel database.
     */

    @Override
    public void undo() {
        if (!tracks.contains(track)) {
            if (index >= 0 && index <= tracks.size()) {
                tracks.add(index, track);
            } else {
                tracks.add(track);
            }
        }
        trackRepository.save(track);
        System.out.println("[COMMAND] INFO: RemoveTrackCommand undone for track: " + track.getTitle());
    }

}