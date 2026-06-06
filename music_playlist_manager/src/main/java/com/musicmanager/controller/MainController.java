package com.musicmanager.controller;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.musicmanager.MediaPlayerUI;
import com.musicmanager.PlaybackEngine;
import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.repository.PlaylistRepository;
import com.musicmanager.repository.PlaylistRepositoryImpl;
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
    private final PlaylistRepository playlistRepository;

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
        this(new TrackRepositoryImpl(),new PlaylistRepositoryImpl(), PlaybackEngine.getInstance());
    }

    MainController(TrackRepository trackRepository, PlaylistRepository playlistRepository, PlaybackEngine playbackEngine) {
        this.trackRepository = trackRepository;
        this.playlistRepository = playlistRepository;
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

    /**
     * Inizializza il controller, impostando i listener e caricando i dati.
     */
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

    /**
     * Classe per la gestione delle celle della lista delle tracce.
     */
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

        /**
         * Aggiorna l'elemento della lista con le informazioni della traccia.
         * @param track La traccia da visualizzare.
         * @param empty Indica se l'elemento è vuoto.
         */
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

        /**
         * Mostra un dialog di conferma prima di eliminare una traccia. Se l'utente conferma, chiama il metodo handleDeleteTrack per eseguire l'eliminazione.
         * @param track
         */
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


        /**
         * Mostra un dialog per l'editing di una traccia.
         * @param track La traccia da modificare.
         */
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

        /**
         * Mostra un alert di errore quando i dati della traccia da modificare non sono validi.
         */
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


    /**
     * Crea una nuova traccia a partire da un file selezionato. Il file deve essere in un formato specifico che viene interpretato da TrackFileParser. Se la traccia ha gli stessi campi di una traccia già presente, mostra un alert di avviso e impedisce la creazione della traccia duplicata.
     * @param file Il file da cui creare la traccia. Deve essere in un formato specifico che viene interpretato da TrackFileParser.
     */
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

    /**
     * Controlla se esiste già una traccia con gli stessi campi di quella da creare. Se sì, mostra un alert di avviso e impedisce la creazione della traccia duplicata.
     * @param newTrack La traccia da verificare.
     * @return true se esiste già una traccia con gli stessi campi, false altrimenti.
     */
    private boolean isDuplicateTrack(Track newTrack) {
        return tracks.stream().anyMatch(existingTrack -> hasSameFields(existingTrack, newTrack));
    }

    /**
     * Controlla se due tracce hanno gli stessi campi (titolo, autore, durata, genere, anno). Utilizzato per identificare tracce duplicate.
     * @param firstTrack La prima traccia da confrontare.
     * @param secondTrack La seconda traccia da confrontare.
     * @return true se le tracce hanno gli stessi campi, false altrimenti.
     */
    private boolean hasSameFields(Track firstTrack, Track secondTrack) {
        return sameText(firstTrack.getTitle(), secondTrack.getTitle())
                && sameText(firstTrack.getAuthor(), secondTrack.getAuthor())
                && firstTrack.getLength() == secondTrack.getLength()
                && sameText(firstTrack.getGenre(), secondTrack.getGenre())
                && firstTrack.getYear() == secondTrack.getYear();
    }

    /**
     * Controlla se due stringhe sono uguali ignorando spazi bianchi e differenze tra maiuscole e minuscole. Utilizzato per confrontare i campi testuali delle tracce.
     * @param firstText La prima stringa da confrontare.
     * @param secondText La seconda stringa da confrontare.
     * @return true se le stringhe sono uguali ignorando spazi bianchi e differenze tra maiuscole e minuscole, false altrimenti.
     */
    private boolean sameText(String firstText, String secondText) {
        if (firstText == null || secondText == null) {
            return firstText == secondText;
        }

        return firstText.trim().equalsIgnoreCase(secondText.trim());
    }

    /**
     * Mostra un alert di avviso quando si tenta di creare una traccia che ha gli stessi campi di una traccia già presente nella lista. Informa l'utente che la traccia è duplicata e non può essere aggiunta.
     * @param track La traccia che si è tentato di creare ma è risultata duplicata. Viene utilizzata per mostrare le informazioni della traccia nell'alert.
     */
    private void showDuplicateTrackAlert(Track track) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Traccia duplicata");
        alert.setHeaderText("Traccia gia presente");
        alert.setContentText("La traccia \"" + track.getTitle() + "\" ha gli stessi campi di una traccia gia presente nella lista.");
        alert.showAndWait();
    }

    /**
     * Gestisce l'aggiornamento di una traccia musicale. Riceve la traccia da aggiornare e la traccia con i nuovi dati. Aggiorna i campi della traccia originale con quelli della traccia aggiornata, salva le modifiche nel repository e aggiorna la lista dei brani per riflettere le modifiche nella GUI.
     * @param track La traccia da aggiornare. Deve essere una traccia già presente nella lista e nel repository.
     * @param updatedTrack La traccia con i nuovi dati. I campi di questa traccia verranno copiati nella traccia originale. Non deve essere presente nella lista o nel repository, altrimenti si rischia di creare una traccia duplicata.
     */
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

    /**
     * Gestisce la rimozione di una traccia musicale. Riceve la traccia da rimuovere, elimina la traccia dal repository e dalla lista dei brani per riflettere le modifiche nella GUI. Prima di eseguire l'eliminazione, mostra un dialog di conferma per evitare eliminazioni accidentali.
     * @param track La traccia da rimuovere. Deve essere una traccia già presente nella lista e nel repository.
     */
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

    /**
     * Utilizzata principalmente per consentire ad altre classi, come MediaPlayerUI, di accedere alla lista dei brani e visualizzarli nella GUI.
     * @return La lista dei brani musicali attualmente presenti. Ogni elemento della lista è un oggetto Track che rappresenta un brano musicale con i suoi campi (titolo, autore, durata, genere, anno). La lista è osservabile, quindi eventuali modifiche alla lista (aggiunta, rimozione, aggiornamento di tracce) saranno automaticamente riflesse nella GUI.
     */
    public ObservableList<Track> getTracks() {
        return tracks;
    }

    /**
     * Gestisce la riproduzione di una traccia musicale. Riceve la traccia da riprodurre e la passa al motore di riproduzione.
     * @param track La traccia da riprodurre. Deve essere una traccia già presente nella lista e nel repository.
     */
    @FXML
    public void handlePlay() {
        Track selectedTrack = tracksListView == null ? null : tracksListView.getSelectionModel().getSelectedItem();
        playTrack(selectedTrack);
    }

    /**
     * Riproduce una traccia musicale specifica. Se la traccia è null, mostra un messaggio di avviso e non esegue alcuna operazione. Altrimenti, seleziona la traccia nella lista dei brani, imposta la traccia corrente nel motore di riproduzione e avvia la riproduzione.
     * @param track La traccia da riprodurre. Deve essere una traccia già presente nella lista e nel repository. Se è null, non viene eseguita alcuna operazione e viene mostrato un messaggio di avviso.
     */
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

    /**
     * Gestisce la pausa della riproduzione. Mette in pausa la traccia corrente.
     */
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

    /**
     * Alterna tra la pausa e la riproduzione della traccia corrente.
     */
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

    public void handleSetStrategy() {
        // TO DO: Implementare strategia di riproduzione
        // playbackEngine.setStrategy(<nuova strategia>);
    }

    /**
     * Utilizzata principalmente per consentire ad altre classi, come MediaPlayerUI, di accedere alla lista delle playlist e visualizzarle nella GUI.
     * @return La lista delle playlist attualmente presenti. Ogni elemento della lista è un oggetto Playlist che rappresenta una playlist con i suoi campi (nome, lista di tracce). La lista è osservabile, quindi eventuali modifiche alla lista (aggiunta, rimozione, aggiornamento di playlist) saranno automaticamente riflesse nella GUI.
     */
    public ObservableList<Playlist> getPlaylists() {
        return playlists;
    }
    
    /**
     * Controlla se esiste già una playlist con lo stesso nome di quella da creare. Se sì, mostra un alert di avviso e impedisce la creazione della playlist duplicata.
     * @param name Il nome della playlist da verificare. Deve essere una stringa non nulla e non vuota. Viene confrontato con i nomi delle playlist già presenti nella lista, ignorando spazi bianchi e differenze tra maiuscole e minuscole.
     * @return true se esiste già una playlist con lo stesso nome, false altrimenti.
     */
    private boolean isValidPlaylistName(String name) {

        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        return playlists.stream().noneMatch(p -> p.getName().equalsIgnoreCase(name.trim()));
    }


    /**
     * Gestisce la creazione di una nuova playlist.
     */
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

        playlistRepository.save(playlist);

        playlistRepository.save(playlist);
        playlists.setAll(playlistRepository.findAll());
    }

    /**
     * Inizializza la sezione delle playlist.
     */
    private void initializePlaylistSection() {

        playlists.setAll(playlistRepository.findAll());
        this.playlistListView.setItems(playlists);

        playlistListView.setCellFactory(listView -> new ListCell<Playlist>() {

            private final Label nameLabel = new Label();
            private final Region spacer = new Region();

            private final Button playButton = new Button("▶");
            private final Button editButton = new Button("\u270E");
            private final Button deleteButton = new Button("🗑");

            private final HBox content = new HBox(10, nameLabel, spacer, playButton, editButton, deleteButton);

            {
                HBox.setHgrow(spacer, Priority.ALWAYS);

                
                playButton.visibleProperty().bind(hoverProperty());
                playButton.managedProperty().bind(playButton.visibleProperty());

                editButton.visibleProperty().bind(hoverProperty());
                editButton.managedProperty().bind(editButton.visibleProperty());

                deleteButton.visibleProperty().bind(hoverProperty());
                deleteButton.managedProperty().bind(deleteButton.visibleProperty());

                
                playButton.setOnAction(e -> {
                    Playlist p = getItem();
                    if (p != null) {
                        handlePlayPlaylist(p);
                    }
                });

                editButton.setOnAction(e -> {
                    Playlist p = getItem();
                    if (p != null) {
                        showEditPlaylistDialog(p);
                    }
                });

                deleteButton.setOnAction(e -> {
                    Playlist p = getItem();
                    if (p != null) {
                        confirmAndDeletePlaylist(p);
                    }
                });
            }

            @Override
            protected void updateItem(Playlist playlist, boolean empty) {
                super.updateItem(playlist, empty);

                if (empty || playlist == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                nameLabel.setText(playlist.getName());

                setText(null);
                setGraphic(content);
            }
        });

        playlistListView.setOnMouseClicked(event -> {

            Playlist selected = playlistListView.getSelectionModel().getSelectedItem();

            if (selected != null) {
                showPlaylistDetails(selected);
            }
        });
    }

    private void handlePlayPlaylist(Playlist playlist) {

    }

    /**
     * Mostra un dialog di conferma prima di eliminare una playlist. Se l'utente conferma, chiama il metodo handleRemovePlaylist per eseguire l'eliminazione.
     * @param playlist La playlist da eliminare. Deve essere una playlist già presente nella lista e nel repository. Se è null, non viene eseguita alcuna operazione e viene mostrato un messaggio di avviso.
     */
    private void confirmAndDeletePlaylist(Playlist playlist) {
        if (playlist == null) {
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma eliminazione");
        confirmAlert.setHeaderText("Eliminare la playlist?");
        confirmAlert.setContentText("Sei sicuro di voler eliminare la playlist \"" + playlist.getName() + "\"?\nQuesta azione rimuoverà la playlist dal sistema.");

        java.util.Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            handleDeletePlaylist(playlist);
        }
    }

    /**
     * Gestisce la rimozione di una playlist selezionata.
     */
    @FXML
    public void handleDeletePlaylist(Playlist playlist) {
        Playlist target = playlist != null ? playlist : playlistListView.getSelectionModel().getSelectedItem();

        if (target != null) {
            playlistRepository.delete(target.getId());
            playlistListView.getItems().remove(target);
            playlistListView.refresh();
        }
    }

    /**
     * Mostra un dialog per modificare il nome di una playlist. Riceve la playlist da modificare, mostra un dialog con un campo di testo precompilato con il nome attuale della playlist e consente all'utente di inserire un nuovo nome. Se l'utente conferma, valida il nuovo nome (non vuoto, non duplicato) e aggiorna la playlist con il nuovo nome, salvando le modifiche nel repository e aggiornando la GUI. Se il nuovo nome non è valido, mostra un alert di errore.
     * @param playlist La playlist da modificare. Deve essere una playlist già presente nella lista e nel repository. Viene utilizzata per precompilare il campo di testo del dialog con il nome attuale della playlist e per aggiornare il nome della playlist se l'utente conferma la modifica.
     */
    private void showEditPlaylistDialog(Playlist playlist) {

        TextInputDialog dialog = new TextInputDialog(playlist.getName());
        dialog.setTitle("Modifica playlist");
        dialog.setHeaderText("Inserisci il nuovo nome della playlist");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newName -> {

            String trimmedName = newName.trim();

            
            if (trimmedName.isEmpty()) {
                showInvalidPlaylistNameAlert("Il nome non può essere vuoto.");
                return;
            }

            
            boolean alreadyExists = playlists.stream()
                    .anyMatch(p ->
                            p.getName().equalsIgnoreCase(trimmedName)
                            && p != playlist
                    );

            if (alreadyExists) {
                showInvalidPlaylistNameAlert("Esiste già una playlist con questo nome.");
                return;
            }

            String oldName = playlist.getName();
            playlist.setName(trimmedName);

            playlistRepository.update(playlist);

            playlistListView.refresh();
        });
    }

    /**
     * Mostra un alert di errore quando si tenta di modificare il nome di una playlist con un nome non valido (vuoto o duplicato). Informa l'utente che il nome inserito non è valido e deve essere modificato.
     * @param message Il messaggio da visualizzare nell'alert. Deve essere una stringa che spiega il motivo per cui il nome della playlist non è valido (ad esempio, "Il nome non può essere vuoto" o "Esiste già una playlist con questo nome"). Viene mostrato nel contenuto dell'alert per informare l'utente del problema specifico con il nome inserito.
     */
    private void showInvalidPlaylistNameAlert(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText("Nome playlist non valido");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mostra la vista delle playlist.
     */
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

    /**
     * Mostra i dettagli di una playlist selezionata, inclusa la lista delle tracce presenti nella playlist. Aggiorna la GUI per visualizzare le informazioni della playlist e nascondere la lista delle playlist.
     * @param playlist La playlist di cui mostrare i dettagli. Deve essere una playlist già presente nella lista delle playlist. Viene utilizzata per aggiornare la GUI con le informazioni della playlist e la lista delle tracce contenute nella playlist.
     */
    private void showPlaylistDetails(Playlist playlist) {

        Playlist fullPlaylist = playlistRepository.findById(playlist.getId());

        if (fullPlaylist == null) return;

        selectedPlaylist = fullPlaylist;

        this.detailsTitleLabel.setText(fullPlaylist.getName());

        playlistTracksListView.setItems(
                FXCollections.observableArrayList(
                        fullPlaylist.getTracks()
                )
        );

        playlistListView.setVisible(false);
        playlistListView.setManaged(false);

        playlistTracksListView.setVisible(true);
        playlistTracksListView.setManaged(true);

        backButton.setVisible(true);
        backButton.setManaged(true);
    }

    /**
     * Gestisce il clic sul pulsante per tornare alla visualizzazione delle playlist.
     */
    @FXML
    private void handleBackToPlaylists() {
        showPlaylistsView();
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
