package com.musicmanager.controller;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.musicmanager.MediaPlayerUI;
import com.musicmanager.PlaybackEngine;
import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.repository.TrackRepository;
import com.musicmanager.repository.TrackRepositoryImpl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainController { 

    private final CommandManager commandManager = new CommandManager();
    private final TrackRepository trackRepository;
    private final PlaybackEngine playbackEngine;

    private final ObservableList<Track> tracks = FXCollections.observableArrayList();
    private final ObservableList<Playlist> playlists = FXCollections.observableArrayList();
    @FXML
    private ListView<Playlist> playlistListView;
    @FXML
    private ListView<Track> playlistTracksListView;
    @FXML
    private Label detailsTitleLabel;
    @FXML
    private Button backButton;
    private Playlist selectedPlaylist;

    public MainController() {
        this(new TrackRepositoryImpl(), PlaybackEngine.getInstance());
    }

    MainController(TrackRepository trackRepository, PlaybackEngine playbackEngine) {
        this.trackRepository = trackRepository;
        this.playbackEngine = playbackEngine;
    }

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
        initializePlaylistSection();
        loadTracksFromDatabase();
    }

    private class TrackListCell extends ListCell<Track> {

        private final Label trackLabel = new Label();
        private final Region spacer = new Region();
        private final Button playButton = new Button("▶"); // Unicode per simbolo play
        private final Button deleteButton = new Button("🗑"); // Unicode per simbolo cestino 
        private final Button editButton = new Button("\u270E"); // Unicode per simbolo edit
        private final HBox content = new HBox(16, trackLabel, spacer, playButton, editButton, deleteButton);

        private TrackListCell() {
            HBox.setHgrow(spacer, Priority.ALWAYS);
            trackLabel.setMaxWidth(Double.MAX_VALUE);
            playButton.visibleProperty().bind(hoverProperty());
            playButton.managedProperty().bind(playButton.visibleProperty());
            editButton.visibleProperty().bind(hoverProperty());
            editButton.managedProperty().bind(editButton.visibleProperty());
            deleteButton.visibleProperty().bind(hoverProperty());
            deleteButton.managedProperty().bind(deleteButton.visibleProperty());
            playButton.setOnAction(event -> playTrack(getItem()));
            editButton.setOnAction(event -> showEditTrackDialog(getItem()));
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

        private void showEditTrackDialog(Track track) {
            if (track == null) {
                return;
            }

            Dialog<Track> dialog = new Dialog<>();
            dialog.setTitle("Modifica traccia");
            dialog.setHeaderText("Modifica i dati della traccia");

            ButtonType saveButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            TextField titleField = new TextField(track.getTitle());
            TextField authorField = new TextField(track.getAuthor());
            TextField lengthField = new TextField(String.valueOf(track.getLength()));
            TextField genreField = new TextField(track.getGenre());
            TextField yearField = new TextField(String.valueOf(track.getYear()));

            GridPane form = new GridPane();
            form.setHgap(10);
            form.setVgap(10);
            form.addRow(0, new Label("Titolo"), titleField);
            form.addRow(1, new Label("Autore"), authorField);
            form.addRow(2, new Label("Durata"), lengthField);
            form.addRow(3, new Label("Genere"), genreField);
            form.addRow(4, new Label("Anno"), yearField);

            dialog.getDialogPane().setContent(form);
            dialog.setResultConverter(button -> {
                if (button != saveButtonType) {
                    return null;
                }

                try {
                    return new Track(
                            track.getId(),
                            titleField.getText(),
                            authorField.getText(),
                            Integer.parseInt(lengthField.getText().trim()),
                            genreField.getText(),
                            Integer.parseInt(yearField.getText().trim())
                    );
                } catch (NumberFormatException e) {
                    showInvalidTrackEditAlert();
                    return null;
                }
            });

            Optional<Track> updatedTrack = dialog.showAndWait();
            updatedTrack.ifPresent(value -> handleUpdateTrack(track, value));
        }

        private void showInvalidTrackEditAlert() {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Dati non validi");
            alert.setContentText("Durata e anno devono essere numeri interi.");
            alert.showAndWait();
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

<<<<<<< HEAD
    public void handleSetStrategy() {
        // TO DO: Implementare strategia di riproduzione
        // playbackEngine.setStrategy(<nuova strategia>);
=======
    public ObservableList<Playlist> getPlaylists() {
        return playlists;
    }
    
    //metodo per la validazione del nome della Playlist
    private boolean isValidPlaylistName(String name) {

        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        return playlists.stream().noneMatch(p -> p.getName().equalsIgnoreCase(name.trim()));
    }


    //metodo per la creazione di una Playlist da form 
    @FXML
    public void handleCreatePlaylist() {

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Nuova Playlist");
        dialog.setHeaderText("Inserisci il nome della playlist");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String playlistName = result.get().trim();

        if (!isValidPlaylistName(playlistName)) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore");
            alert.setHeaderText("Nome non valido");
            alert.setContentText(
                    "Il nome è vuoto oppure esiste già una playlist con questo nome."
            );

            alert.showAndWait();
            return;
        }

        Playlist playlist = new Playlist(playlistName);

        playlists.add(playlist);
    }

    //metodo per la visualizzazione della sezione sulla playlist
    private void initializePlaylistSection() {

        this.playlistListView.setItems(playlists);

        playlistListView.setCellFactory(listView ->
            new ListCell<>() {
                @Override
                protected void updateItem(Playlist playlist, boolean empty) {
                    super.updateItem(playlist, empty);

                    if (empty || playlist == null) {
                        setText(null);
                    } else {
                        setText(playlist.getName());
                    }
                }
            }
        );

        playlistTracksListView.setCellFactory(listView ->
            new ListCell<>() {
                @Override
                protected void updateItem(Track track, boolean empty) {
                    super.updateItem(track, empty);

                    if (empty || track == null) {
                        setText(null);
                    } else {
                        setText(track.getTitle() + " - " + track.getAuthor());
                    }
                }
            }
        );

        playlistListView.setOnMouseClicked(event -> {

    Playlist selected = playlistListView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            showPlaylistDetails(selected);
        }
        });
    }

    private void showPlaylistsView() {

        selectedPlaylist = null;

        this.detailsTitleLabel.setText("Playlist");

        playlistListView.setVisible(true);
        playlistListView.setManaged(true);

        playlistTracksListView.setVisible(false);
        playlistTracksListView.setManaged(false);

        backButton.setVisible(false);
        backButton.setManaged(false);
    }

    private void showPlaylistDetails(Playlist playlist) {

        selectedPlaylist = playlist;

        this.detailsTitleLabel.setText(playlist.getName());

        playlistTracksListView.setItems(
                FXCollections.observableArrayList(
                        playlist.getTracks()
                )
        );

        playlistListView.setVisible(false);
        playlistListView.setManaged(false);

        playlistTracksListView.setVisible(true);
        playlistTracksListView.setManaged(true);

        backButton.setVisible(true);
        backButton.setManaged(true);
    }

    @FXML
    private void handleBackToPlaylists() {
        showPlaylistsView();
>>>>>>> 7ef21455c53d0ed29baf52daf9f8ef57018b1b49
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
