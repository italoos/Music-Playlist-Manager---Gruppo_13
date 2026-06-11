package com.musicmanager.controller;

import com.musicmanager.PlaybackEngine;
import com.musicmanager.model.Track;
import com.musicmanager.repository.TrackRepository;
import javafx.collections.ObservableList;

/** Comando concreto che aggiunge un brano musicale. */

public class AddTrackCommand implements Command {

    private final Track track;
    private final TrackRepository trackRepository;
    private final ObservableList<Track> tracks;

    public AddTrackCommand(Track track, TrackRepository trackRepository, ObservableList<Track> tracks) {
        this.track = track;
        this.trackRepository = trackRepository;
        this.tracks = tracks;
    }

    /** Aggiunge il brano, aggiornando il database e la UI. */

    @Override
    public void execute() {
        trackRepository.save(track);
        tracks.add(track);
        System.out.println("[COMMAND] INFO: AddTrackCommand executed for track: " + track.getTitle());
    }

    /** Rimuove il brano, aggiornando il database e la UI. */

    @Override
    public void undo() {

        PlaybackEngine engine = PlaybackEngine.getInstance();

        if (engine.getCurrentTrack() != null && engine.getCurrentTrack().getId() == track.getId()) {
            engine.nextTrack();
        }

        tracks.remove(track);
        trackRepository.delete(track.getId());

        System.out.println("[COMMAND] INFO: AddTrackCommand undone for track: " + track.getTitle());

    }

}