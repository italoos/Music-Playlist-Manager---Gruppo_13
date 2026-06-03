package com.musicmanager.controller;

import java.io.File;
import java.io.IOException;

import com.musicmanager.MediaPlayerUI;
import com.musicmanager.PlaybackEngine;
import com.musicmanager.model.Track;
import com.musicmanager.repository.TrackRepository;
import com.musicmanager.repository.TrackRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class MainController { 

    private final CommandManager commandManager = new CommandManager();
    private final TrackRepository trackRepository = new TrackRepositoryImpl();
    private final PlaybackEngine playbackEngine = PlaybackEngine.getInstance();

    private final ObservableList<Track> tracks = FXCollections.observableArrayList();

    @FXML
    private ListView<Track> tracksListView;

    @FXML
    private Button addTrackButton;

    @FXML
    private MediaPlayerUI mediaPlayerUI;

    @FXML
    private void loadPrimaryStage() throws IOException {
        App.setRoot("primary");
    }

    @FXML
    private void initialize() {
        tracksListView.setPlaceholder(new Label("I tuoi brani musicali saranno visualizzati qui"));
        tracksListView.setItems(tracks);
        tracksListView.setCellFactory(listView -> new TrackListCell());
        mediaPlayerUI.setController(this);
        playbackEngine.registerObserver(mediaPlayerUI);
        playbackEngine.notifyObservers();
        /*Track track = new Track(0, "Short", "Antonio", 10, "Metal", 2010);
        trackRepository.save(track);
        tracks.add(track);*/
        loadTracksFromDatabase();
    }

    private class TrackListCell extends ListCell<Track> {

        private final Label trackLabel = new Label();
        private final Region spacer = new Region();
        private final Button playButton = new Button("Play");
        private final Button deleteButton = new Button("Delete");
        private final HBox content = new HBox(16, trackLabel, spacer, playButton, deleteButton);

        private TrackListCell() {
            HBox.setHgrow(spacer, Priority.ALWAYS);
            trackLabel.setMaxWidth(Double.MAX_VALUE);
            playButton.visibleProperty().bind(hoverProperty());
            playButton.managedProperty().bind(playButton.visibleProperty());
            deleteButton.visibleProperty().bind(hoverProperty());
            deleteButton.managedProperty().bind(deleteButton.visibleProperty());
            playButton.setOnAction(event -> playTrack(getItem()));
            deleteButton.setOnAction(event -> confirmAndDeleteTrack(getItem()));
        }

        @Override
        protected void updateItem(Track track, boolean empty) {
            super.updateItem(track, empty);

            if (empty || track == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            trackLabel.setText(track.getTitle() + " - " + track.getAuthor() + "  (" + track.getGenre() + ", " + track.getYear() + ")");
            setText(null);
            setGraphic(content);
        }

        private void confirmAndDeleteTrack(Track track) {
            if (track == null) {
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Conferma eliminazione");
            confirmAlert.setHeaderText("Eliminare la traccia?");
            confirmAlert.setContentText("Sei sicuro di voler eliminare \"" + track.getTitle() + "\"?\nQuesta azione rimuoverà la traccia dal sistema.");

            java.util.Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handleDeleteTrack(track);
            }
        }
    }

    /**
     * Recupera tutti i brani dal database e li rende disponibili alla GUI.
     */

    public void loadTracksFromDatabase() {
        tracks.setAll(trackRepository.findAll());
        System.out.println("[MAIN CONTROLLER] INFO: Tracks loaded from database (" + tracks.size() + ").");
    }

    /**
     * Apre un FileChooser per selezionare un file di traccia e crea una nuova traccia.
     */
    @FXML
    public void handleAddTrackButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona un file di traccia");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("File di testo", "*.txt")
        );

        Stage stage = (Stage) addTrackButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            handleCreateTrack(selectedFile);
        }
    }


    /** Gestisce la creazione di un nuovo brano musicale a partire da un file. */

    public void handleCreateTrack(File file) {

    try {

        TrackFileParser parser = new TrackFileParser();

        Track track = parser.parse(file);

        if (isDuplicateTrack(track)) {
            showDuplicateTrackAlert(track);
            return;
        }

        trackRepository.save(track);
        tracks.add(track);

    } catch (Exception e) {

        e.printStackTrace();

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("File non valido");
        alert.setContentText("Il file selezionato non è valido o non può essere letto. Assicurati che il file abbia il formato corretto.");
        alert.showAndWait();
    }

    
}

    private boolean isDuplicateTrack(Track newTrack) {
        return tracks.stream().anyMatch(existingTrack -> hasSameFields(existingTrack, newTrack));
    }

    private boolean hasSameFields(Track firstTrack, Track secondTrack) {
        return sameText(firstTrack.getTitle(), secondTrack.getTitle())
                && sameText(firstTrack.getAuthor(), secondTrack.getAuthor())
                && firstTrack.getLength() == secondTrack.getLength()
                && sameText(firstTrack.getGenre(), secondTrack.getGenre())
                && firstTrack.getYear() == secondTrack.getYear();
    }

    private boolean sameText(String firstText, String secondText) {
        if (firstText == null || secondText == null) {
            return firstText == secondText;
        }

        return firstText.trim().equalsIgnoreCase(secondText.trim());
    }

    private void showDuplicateTrackAlert(Track track) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Traccia duplicata");
        alert.setHeaderText("Traccia gia presente");
        alert.setContentText("La traccia \"" + track.getTitle() + "\" ha gli stessi campi di una traccia gia presente nella lista.");
        alert.showAndWait();
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

    @FXML
    public void handlePlay() {
        Track selectedTrack = tracksListView == null ? null : tracksListView.getSelectionModel().getSelectedItem();
        playTrack(selectedTrack);
    }

    private void playTrack(Track track) {

        if (track == null) {
            System.out.println("[MAIN CONTROLLER] WARNING: No track selected for playback.");
            return;
        }

        tracksListView.getSelectionModel().select(track);
        playbackEngine.setCurrentTrack(track);
        playbackEngine.play();
        System.out.println("[MAIN CONTROLLER] INFO: Play requested for track: " + track.getTitle() + ".");
    }

    public void handlePause() {
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
        if (playbackEngine.isPlaying()) {
            handlePause();
            return;
        }

        Track currentTrack = playbackEngine.getCurrentTrack();

        if (currentTrack != null) {
            playbackEngine.play();
            return;
        }

        handlePlay();
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
