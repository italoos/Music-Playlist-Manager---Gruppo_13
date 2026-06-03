package com.musicmanager.controller;

import java.io.File;
import java.io.IOException;

import com.musicmanager.model.Track;
import com.musicmanager.repository.TrackRepository;
import com.musicmanager.repository.TrackRepositoryImpl;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class MainController {

    

    private final CommandManager commandManager = new CommandManager();
    private final TrackRepository trackRepository = new TrackRepositoryImpl();

    private final ObservableList<Track> tracks = FXCollections.observableArrayList();

    @FXML
    private ListView<Track> tracksListView;

    @FXML
    private void loadPrimaryStage() throws IOException {
        App.setRoot("primary");
    }

    @FXML
    private void initialize() {
        tracksListView.setPlaceholder(new Label("I tuoi brani musicali saranno visualizzati qui"));
        tracksListView.setItems(tracks);
        tracksListView.setCellFactory(listView -> new TrackListCell());
        loadTracksFromDatabase();
    }

    private class TrackListCell extends ListCell<Track> {

        private final Label trackLabel = new Label();
        private final Region spacer = new Region();
        private final Button playButton = new Button("Play");
        private final HBox content = new HBox(16, trackLabel, spacer, playButton);

        private TrackListCell() {
            HBox.setHgrow(spacer, Priority.ALWAYS);
            trackLabel.setMaxWidth(Double.MAX_VALUE);
            playButton.visibleProperty().bind(hoverProperty());
            playButton.managedProperty().bind(playButton.visibleProperty());
            playButton.setOnAction(event -> playTrack(getItem()));
        }

        @Override
        protected void updateItem(Track track, boolean empty) {
            super.updateItem(track, empty);

            if (empty || track == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            trackLabel.setText(track.getTitle() + " - " + track.getAuthor() + " (" + track.getGenre() + ", " + track.getYear() + ")");
            setText(null);
            setGraphic(content);
        }
    }

    /**
     * Recupera tutti i brani dal database e li rende disponibili alla GUI.
     */

    public void loadTracksFromDatabase() {
        tracks.setAll(trackRepository.findAll());
        System.out.println("[MAIN CONTROLLER] INFO: Tracks loaded from database (" + tracks.size() + ").");
    }


    /** Gestisce la modifica di un brano musicale. */

    public void handleCreateTrack(File file) {

    try {

        TrackFileParser parser = new TrackFileParser();

        Track track = parser.parse(file);

        trackRepository.save(track);

    } catch (IOException e) {

        e.printStackTrace();

        // TO DO: mostrare messaggio di errore nella GUI
    }

    
}

    /** Gestisce la modifica di un brano musicale. */

    @FXML
    public void handleUpdateTrack(Track track, Track updatedTrack) {

        if (track == null || updatedTrack == null) {
            System.out.println("[MAIN CONTROLLER] WARNING: Cannot update track (invalid input).");
            return;
        }

        track.setTitle(updatedTrack.getTitle());
        track.setAuthor(updatedTrack.getAuthor());
        track.setLength(updatedTrack.getLength());
        track.setGenre(updatedTrack.getGenre());
        track.setYear(updatedTrack.getYear());

        int index = tracks.indexOf(track);

        if (index != -1) {
            tracks.set(index, track);
        }

        trackRepository.update(track);

        System.out.println("[MAIN CONTROLLER] INFO: Track updated successfully (ID: " + track.getId() + ").");

    }

    /** Gestisce la rimozione di un brano musicale. */

    @FXML
    public void handleDeleteTrack(Track track) {

        if (track == null) {
            System.out.println("[MAIN CONTROLLER] WARNING: No track selected for deletion.");
            return;
        }

        Command command = new RemoveTrackCommand(track, trackRepository, tracks);
        commandManager.executeCommand(command);

    }

    /** Ripristina lo stato precedente annullando l'ultima operazione. */

    @FXML
    public void handleUndo() {
        commandManager.undoLastCommand();
    }

    public ObservableList<Track> getTracks() {
        return tracks;
    }

    /*
    // TO DO:
    // Verificare se PlaybackEngine deve essere gestito come Singleton
    // oppure passato tramite dependency injection nel costruttore
    private PlaybackEngine playbackEngine;

    // TO DO:
    // Collegare la GUI principale al controller
    // e registrare eventuali listener/event handler
    private MediaPlayerUI view;

    /*
    // TO DO:
    // Gestire storico dei comandi per supportare Undo
    private CommandManager commandManager;

    // TO DO:
    // Implementare persistenza delle tracce nel database locale
    private TrackRepository trackRepository;

    // TO DO:
    // Implementare persistenza delle playlist nel database locale
    private PlaylistRepository playlistRepository;
    */

   
    public MainController(
            PlaybackEngine playbackEngine,
            MediaPlayerUI view
            /*
            ,
            CommandManager commandManager,
            TrackRepository trackRepo,
            PlaylistRepository playlistRepo
            */
    ) {

        // TO DO:
        // Validare eventuali dipendenze null
        // e inizializzare il controller

        this.playbackEngine = playbackEngine;
        this.view = view;

        /*
        this.commandManager = commandManager;
        this.trackRepository = trackRepo;
        this.playlistRepository = playlistRepo;
        */
    }

    public void handlePlay() {
        playTrack(tracksListView.getSelectionModel().getSelectedItem());
        
        Track currentTrack = playbackEngine.getCurrentTrack();

        if (currentTrack == null) {
            view.showMessage("Nessuna traccia selezionata");
            return;
        }

        playbackEngine.play();
    }

    private void playTrack(Track track) {

        if (track == null) {
            System.out.println("[MAIN CONTROLLER] WARNING: No track selected for playback.");
            return;
        }

        tracksListView.getSelectionModel().select(track);
        System.out.println("[MAIN CONTROLLER] INFO: Play requested for track: " + track.getTitle() + ".");
    }

    public void handlePause() {

        Track currentTrack = playbackEngine.getCurrentTrack();

        if (currentTrack == null) {
            view.showMessage("Nessuna traccia in riproduzione");
            return;
        }

        playbackEngine.pause();
    }

    public void handleSkip() {

        // TO DO:
        // Passare alla traccia successiva
        // in base alla PlaybackStrategy attiva

        // TO DO:
        // Aggiornare informazioni nella GUI
    }

    public void handleTogglePlayback() {

        if (playbackEngine.getCurrentTrack() == null) {
            view.showMessage("Nessuna traccia selezionata");
            return;
        }

        if (playbackEngine.isPlaying()) {
            playbackEngine.pause();
        } else {
            playbackEngine.play();
        }
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

    // public void handleUndo() {

        // TO DO:
        // Recuperare ultimo comando eseguito

        // TO DO:
        // Eseguire operazione di undo

        // TO DO:
        // Aggiornare GUI e stato applicazione
    // }
}
