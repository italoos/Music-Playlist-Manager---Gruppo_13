package com.musicmanager;

public class MainController {

    /*
    // TO DO:
    // Verificare se PlaybackEngine deve essere gestito come Singleton
    // oppure passato tramite dependency injection nel costruttore
    private PlaybackEngine playbackEngine;

    // TO DO:
    // Collegare la GUI principale al controller
    // e registrare eventuali listener/event handler
    private MediaPlayerUI view;

    // TO DO:
    // Gestire storico dei comandi per supportare Undo
    private CommandManager commandManager;

    // TO DO:
    // Implementare persistenza delle tracce nel database locale
    private TrackRepository trackRepository;

    // TO DO:
    // Implementare persistenza delle playlist nel database locale
    private PlaylistRepository playlistRepository;

    public MainController(
            PlaybackEngine playbackEngine,
            MediaPlayerUI view,
            CommandManager commandManager,
            TrackRepository trackRepo,
            PlaylistRepository playlistRepo
    ) {

        // TO DO:
        // Validare eventuali dipendenze null
        // e inizializzare il controller

        this.playbackEngine = playbackEngine;
        this.view = view;
        this.commandManager = commandManager;
        this.trackRepository = trackRepo;
        this.playlistRepository = playlistRepo;
    }
    */

    public void handlePlay() {

        // TO DO:
        // Avviare la riproduzione della traccia corrente
        // tramite PlaybackEngine

        // TO DO:
        // Aggiornare la GUI dopo l'avvio del playback

        // TO DO:
        // Gestire eventuali casi limite
        // (nessuna traccia selezionata, playlist vuota, ecc.)
    }

    public void handlePause() {

        // TO DO:
        // Mettere in pausa la riproduzione corrente

        // TO DO:
        // Aggiornare lo stato della GUI
    }

    public void handleSkip() {

        // TO DO:
        // Passare alla traccia successiva
        // in base alla PlaybackStrategy attiva

        // TO DO:
        // Aggiornare informazioni nella GUI
    }

    /*
    public void handleAddTrackToPlaylist(Track t, Playlist p) {

        // TO DO:
        // Creare comando AddTrackCommand

        // TO DO:
        // Eseguire il comando tramite CommandManager

        // TO DO:
        // Salvare modifiche nel repository

        // TO DO:
        // Aggiornare la GUI
    }

    public void handleRemoveTrackFromPlaylist(Track t, Playlist p) {

        // TO DO:
        // Creare comando RemoveTrackCommand

        // TO DO:
        // Eseguire il comando tramite CommandManager

        // TO DO:
        // Aggiornare persistenza e GUI
    }

    public void handleUpdateTrack(
            Track t,
            String title,
            String author,
            int length,
            String genre,
            int year
    ) {

        // TO DO:
        // Validare input utente

        // TO DO:
        // Aggiornare dati della traccia

        // TO DO:
        // Salvare modifiche nel database

        // TO DO:
        // Aggiornare la visualizzazione
    }

    public void handleUpdatePlaylistName(Playlist p, String newName) {

        // TO DO:
        // Validare nuovo nome playlist

        // TO DO:
        // Aggiornare nome playlist

        // TO DO:
        // Salvare modifiche nel repository

        // TO DO:
        // Aggiornare GUI
    }
    */

    public void handleUndo() {

        // TO DO:
        // Recuperare ultimo comando eseguito

        // TO DO:
        // Eseguire operazione di undo

        // TO DO:
        // Aggiornare GUI e stato applicazione
    }
}