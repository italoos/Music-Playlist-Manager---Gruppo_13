package com.musicmanager;

public class MainController {
    
    private PlaybackEngine playbackEngine;
    private MediaPlayerUI view;

    private CommandManager commandmanager;

    private TrackRepository trackRepository;
    private PlaylistRepository playlistRepository;

    public MainController(
        PlaybackEngine playbackEngine,
        MediaPlayerUI view, 
        CommandManager commandManager,
        TrackRepository trackRepo,
        PlaylistRepository playlistRepo
    ){   
    this.playbackEngine = playbackEngine;
    this.view = view;
    this.commandManager = commandManager;
    this.trackRepository = trackRepo;
    this.playlistRepository = playlistRepo;
    }

    public void handlePlay() {

    }

    public void handlePause() {

    }

    public void handleSkip() {

    }

    public void handleAddTrackToPlaylist(Track t, Playlist p) {

    }

    public void handleRemoveTrackFromPlaylist(Track t, Playlist p) {

    }

    public void handleUpdateTrack(Track t, String title, String author, int length, String genre, int year) {

    }

    public void handleUpdatePlaylistName(Playlist p, String newName) {

    }

    public void handleUndo() {

    }
}
