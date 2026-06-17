package com.musicmanager.controller;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.musicmanager.MediaPlayerUI;
import com.musicmanager.PlaybackEngine;
import com.musicmanager.PlaybackObserver;
import com.musicmanager.PlaybackStrategy;
import com.musicmanager.PlaylistGenerator;
import com.musicmanager.PlaylistGeneratorFactory;
import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.model.Tag;
import com.musicmanager.repository.PlaylistRepository;
import com.musicmanager.repository.PlaylistRepositoryImpl;
import com.musicmanager.repository.TrackRepository;
import com.musicmanager.repository.TrackRepositoryImpl;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;

public class MainController implements PlaybackObserver { 

    private final CommandManager commandManager = new CommandManager();
    private final TrackRepository trackRepository;
    private final PlaybackEngine playbackEngine;
    private final PlaylistRepository playlistRepository;
    private final Map<Integer, Integer> displayedPlayCounts = new HashMap<>();
    private final Map<Integer, Integer> displayedPlaylistPlayCounts = new HashMap<>();

    private final ObservableList<Track> tracks = FXCollections.observableArrayList();
    private final ObservableList<Playlist> playlists = FXCollections.observableArrayList();

    @FXML
    private ListView<Playlist> playlistListView;
    @FXML
    private ListView<Track> playlistTracksListView;
    @FXML
    private ListView<Track> mostPlayedTracksListView;
    @FXML
    private ListView<Playlist> mostPlayedPlaylistsListView;
    @FXML
    private Label detailsTitleLabel;
    @FXML
    private Button undoButton;
    @FXML
    private Button backButton;
    @FXML
    private Button addTrackToPlaylistButton;
    @FXML
    private Button playPlaylistButton;

    @FXML
    private Button generatePlaylistButton;

    private Playlist selectedPlaylist;

    public MainController() {
        this(new TrackRepositoryImpl(),new PlaylistRepositoryImpl(), PlaybackEngine.getInstance());
    }

    MainController(TrackRepository trackRepository, PlaylistRepository playlistRepository, PlaybackEngine playbackEngine) {
        this.trackRepository = trackRepository;
        this.playlistRepository = playlistRepository;
        this.playbackEngine = playbackEngine;
        this.playbackEngine.setTrackRepository(trackRepository);
        this.playbackEngine.setPlaylistRepository(playlistRepository);
    }

    @FXML
    private ListView<Track> tracksListView;

    @FXML
    private Button addTrackButton;

    @FXML
    private MediaPlayerUI mediaPlayerUI;


    @FXML
    private Label titleLabel;

    @FXML
    private Label authorLabel;

    @FXML
    private Label genreLabel;

    @FXML
    private Label yearLabel;

    @FXML
    private Label lengthLabel;



    @FXML
    private void loadPrimaryStage() throws IOException {
        App.setRoot("primary");
    }

    /**
     * Inizializza il controller, impostando i listener e caricando i dati.
     */
    @FXML
    private void initialize() {
        undoButton.disableProperty().bind(commandManager.canUndoProperty().not());
        tracksListView.setPlaceholder(new Label("I tuoi brani musicali saranno visualizzati qui"));
        tracksListView.setItems(tracks);
        tracksListView.setCellFactory(listView -> new TrackListCell());

        tracksListView.getSelectionModel().selectedItemProperty().addListener((obs, oldTrack, newTrack) -> showTrackDetails(newTrack));
        
        playlistTracksListView.setCellFactory(listView -> new PlaylistTrackListCell());
        mediaPlayerUI.setController(this);
        playbackEngine.registerObserver(mediaPlayerUI);
        playbackEngine.registerObserver(this);
        playbackEngine.notifyObservers();
        initializePlaylistSection();
        loadTracksFromDatabase();
        initializeMostPlayedLists();
    }

    @Override
    public void update(Track currentTrack, Playlist currentPlaylist, int currentTime, boolean isPlaying) {
        boolean playCountChanged = syncDisplayedPlaylistPlayCount(currentPlaylist);

        if (currentTrack != null) {
            if (isPlaying) {
                syncTrackPlayCount(currentTrack);
            }

            Integer displayedPlayCount = displayedPlayCounts.put(
                currentTrack.getId(),
                currentTrack.getPlayCount()
            );

            playCountChanged = playCountChanged
                || displayedPlayCount == null
                || displayedPlayCount.intValue() != currentTrack.getPlayCount();
        }

        if (!playCountChanged) {
            return;
        }

        if (tracksListView != null) {
            tracksListView.refresh();
        }

        if (playlistTracksListView != null) {
            playlistTracksListView.refresh();
        }

        refreshMostPlayedLists();
    }

    private boolean syncDisplayedPlaylistPlayCount(Playlist currentPlaylist) {
        if (currentPlaylist == null || currentPlaylist.getId() <= 0) {
            return false;
        }

        playlists.stream()
            .filter(playlist -> playlist.getId() == currentPlaylist.getId())
            .forEach(playlist -> playlist.setPlayCount(currentPlaylist.getPlayCount()));

        Integer displayedPlayCount = displayedPlaylistPlayCounts.put(
            currentPlaylist.getId(),
            currentPlaylist.getPlayCount()
        );

        return displayedPlayCount == null
            || displayedPlayCount.intValue() != currentPlaylist.getPlayCount();
    }

    private void syncTrackPlayCount(Track currentTrack) {
        tracks.stream()
            .filter(track -> track.getId() == currentTrack.getId())
            .forEach(track -> track.setPlayCount(currentTrack.getPlayCount()));

        playlists.stream()
            .flatMap(playlist -> playlist.getTracks().stream())
            .filter(track -> track.getId() == currentTrack.getId())
            .forEach(track -> track.setPlayCount(currentTrack.getPlayCount()));
    }

    private void initializeMostPlayedLists() {
        tracks.addListener((ListChangeListener<Track>) change -> refreshMostPlayedLists());
        playlists.addListener((ListChangeListener<Playlist>) change -> refreshMostPlayedLists());

        mostPlayedTracksListView.setPlaceholder(new Label("Nessun brano disponibile"));
        mostPlayedTracksListView.setCellFactory(listView -> new ListCell<Track>() {

            private final Label trackLabel = new Label();
            private final Region spacer = new Region();
            private final Button playButton = new Button("▶");
            private final HBox content = new HBox(16, trackLabel, spacer, playButton);

            {
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

                String playCountText = track.getPlayCount() == 1 ? " ascolto" : " ascolti";
                trackLabel.setText(
                    track.getTitle() + " - " + track.getAuthor()
                        + "   [" + track.getPlayCount() + playCountText + "]"
                );
                setText(null);
                setGraphic(content);
            }
        });

        mostPlayedPlaylistsListView.setPlaceholder(new Label("Nessuna playlist disponibile"));
        mostPlayedPlaylistsListView.setCellFactory(listView -> new ListCell<Playlist>() {

            private final Label playlistLabel = new Label();
            private final Region spacer = new Region();
            private final Button playButton = new Button("\u25B6");
            private final HBox content = new HBox(16, playlistLabel, spacer, playButton);

            {
                HBox.setHgrow(spacer, Priority.ALWAYS);
                playlistLabel.setMaxWidth(Double.MAX_VALUE);
                playButton.visibleProperty().bind(hoverProperty());
                playButton.managedProperty().bind(playButton.visibleProperty());
                playButton.setOnAction(event -> handlePlayPlaylist(getItem()));
            }

            @Override
            protected void updateItem(Playlist playlist, boolean empty) {
                super.updateItem(playlist, empty);

                if (empty || playlist == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String playCountText = getPlaylistPlayCount(playlist) == 1 ? " ascolto" : " ascolti";
                playlistLabel.setText(
                    playlist.getName() + "   [" + getPlaylistPlayCount(playlist) + playCountText + "]"
                );
                setText(null);
                setGraphic(content);
            }
        });

        refreshMostPlayedLists();
    }

    private void refreshMostPlayedLists() {
        if (mostPlayedTracksListView == null || mostPlayedPlaylistsListView == null) {
            return;
        }

        mostPlayedTracksListView.getItems().setAll(
            tracks.stream()
                .sorted(Comparator.<Track>comparingInt(Track::getPlayCount).reversed()
                    .thenComparingInt(Track::getId))
                .limit(10)
                .toList()
        );

        mostPlayedPlaylistsListView.getItems().setAll(
            playlists.stream()
                .sorted(Comparator.<Playlist>comparingInt(this::getPlaylistPlayCount).reversed()
                    .thenComparingInt(Playlist::getId))
                .limit(10)
                .toList()
        );
    }

    private int getPlaylistPlayCount(Playlist playlist) {
        return playlist.getPlayCount();
    }

    private void showTrackDetails(Track track) {

        if (track == null) {
            return;
        }
    
        titleLabel.setText("Titolo: " + track.getTitle());
        authorLabel.setText("Autore: " + track.getAuthor());
        genreLabel.setText("Genere: " + track.getGenre());
        yearLabel.setText("Anno: " + track.getYear());
        lengthLabel.setText("Durata: " + track.getLength() + " sec");
    
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

            StringBuilder tags = new StringBuilder();

            if (track.hasTag(Tag.FAVOURITE)) {
                tags.append(" ❤");
            }

            if (track.hasTag(Tag.EXPLICIT)) {
                tags.append(" E");
            }

            if (track.hasTag(Tag.NEW_RELEASE)) {
                tags.append(" N");
            }

            trackLabel.setText(track.getTitle() + " - " + track.getAuthor()+ " (" + track.getGenre() + ", " + track.getYear() + ")" + "\t" + tags);

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
            TextField playCountField = new TextField(String.valueOf(track.getPlayCount()));

            CheckBox favouriteCheckBox = new CheckBox("Favourite");
            favouriteCheckBox.setSelected(track.hasTag(Tag.FAVOURITE));

            CheckBox explicitCheckBox = new CheckBox("Explicit");
            explicitCheckBox.setSelected(track.hasTag(Tag.EXPLICIT));

            CheckBox newReleaseCheckBox = new CheckBox("New Release");
            newReleaseCheckBox.setSelected(track.hasTag(Tag.NEW_RELEASE));

            GridPane form = new GridPane();
            form.setHgap(10);
            form.setVgap(10);
            form.addRow(0, new Label("Titolo"), titleField);
            form.addRow(1, new Label("Autore"), authorField);
            form.addRow(2, new Label("Durata"), lengthField);
            form.addRow(3, new Label("Genere"), genreField);
            form.addRow(4, new Label("Anno"), yearField);
            form.addRow(4, new Label("Numero di riproduzioni"), playCountField);

            form.add(new Separator(), 0, 5, 2, 1);
            
            form.add(favouriteCheckBox, 0, 6, 2, 1);
            form.add(explicitCheckBox, 0, 7, 2, 1);
            form.add(newReleaseCheckBox, 0, 8, 2, 1);

            dialog.getDialogPane().setContent(form);
            dialog.setResultConverter(button -> {
                if (button != saveButtonType) {
                    return null;
                }

                try {
                    Track updatedTrack = new Track(
                        track.getId(),
                        titleField.getText(),
                        authorField.getText(),
                        Integer.parseInt(lengthField.getText().trim()),
                        genreField.getText(),
                        Integer.parseInt(yearField.getText().trim()),
                        Integer.parseInt(playCountField.getText().trim())   
                    );
        
                if (favouriteCheckBox.isSelected()) {
                    updatedTrack.addTag(Tag.FAVOURITE);
                }
        
                if (explicitCheckBox.isSelected()) {
                    updatedTrack.addTag(Tag.EXPLICIT);
                }
        
                if (newReleaseCheckBox.isSelected()) {
                    updatedTrack.addTag(Tag.NEW_RELEASE);
                }
        
                return updatedTrack;
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

    private class PlaylistTrackListCell extends ListCell<Track> {

        private final Label trackLabel = new Label();
        private final Region spacer = new Region();
        private final Button removeButton = new Button("- Rimuovi");
        private final HBox content = new HBox(16, trackLabel, spacer, removeButton);

        private PlaylistTrackListCell() {
            HBox.setHgrow(spacer, Priority.ALWAYS);
            trackLabel.setMaxWidth(Double.MAX_VALUE);

            removeButton.visibleProperty().bind(hoverProperty());
            removeButton.managedProperty().bind(removeButton.visibleProperty());

            removeButton.setOnAction(event -> {
                Track track = getItem();

                if (track != null) {
                    handleRemoveTrackFromSelectedPlaylist(track);
                }
            });
        }

        @Override
        protected void updateItem(Track track, boolean empty) {
            super.updateItem(track, empty);

            if (empty || track == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            trackLabel.setText(track.getTitle() + " - " + track.getAuthor()
                    + "  (" + track.getGenre() + ", " + track.getYear() + ")");

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

            Command command = new AddTrackCommand(track, trackRepository, tracks);
            commandManager.executeCommand(command);

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
            System.out.println("[MAIN CONTROLLER] WARNING: Invalid input parameters.");
            return;
        }

        track.setTitle(updatedTrack.getTitle());
        track.setAuthor(updatedTrack.getAuthor());
        track.setLength(updatedTrack.getLength());
        track.setGenre(updatedTrack.getGenre());
        track.setYear(updatedTrack.getYear());
        track.setTags(updatedTrack.getTags());
        track.setPlayCount(updatedTrack.getPlayCount());

        int index = tracks.indexOf(track);

        if (index != -1) {
            tracks.set(index, track);
        }

        trackRepository.update(track);

        if (selectedPlaylist != null && selectedPlaylist.getTracks().contains(track)) {
            refreshPlaylistDetailsUI();
        }

        if (playbackEngine.getCurrentTrack() != null && playbackEngine.getCurrentTrack().getId() == track.getId()) {
            playbackEngine.notifyObservers();
        }

        System.out.println("[MAIN CONTROLLER] INFO: Track updated successfully (ID: " + track.getId() + ").");

    }

    /**
     * Gestisce la rimozione di una traccia musicale. Riceve la traccia da rimuovere, elimina la traccia dal repository e dalla lista dei brani per riflettere le modifiche nella GUI. Prima di eseguire l'eliminazione, mostra un dialog di conferma per evitare eliminazioni accidentali.
     * @param track La traccia da rimuovere. Deve essere una traccia già presente nella lista e nel repository.
     */
    @FXML
    public void handleDeleteTrack(Track track) {

        if (track == null) {
            System.out.println("[MAIN CONTROLLER] WARNING: Invalid input parameters.");
            return;
        }

        Command command = new RemoveTrackCommand(track, trackRepository, playlistRepository, tracks);
        commandManager.executeCommand(command);

        if (selectedPlaylist != null) {
            refreshPlaylistDetailsUI();
        }

        System.out.println("[MAIN CONTROLLER] INFO: Track deleted successfully (ID: " + track.getId() + ").");

    }

    /** Aggiunge un brano musicale a una playlist. */

    public void handleAddTrackToPlaylist(Track track, Playlist playlist) {

        if (track == null || playlist == null) {
            System.out.println("[MAIN CONTROLLER] WARNING: Invalid input parameters.");
            return;
        }

        Command command = new AddTrackToPlaylistCommand(playlist, track, playlistRepository);
        commandManager.executeCommand(command);

        if (selectedPlaylist != null && selectedPlaylist.getId() == playlist.getId()) {
            refreshPlaylistDetailsUI();
        }

        System.out.println("[MAIN CONTROLLER] INFO: Track added to playlist successfully (Track ID: " + track.getId() + ", Playlist ID: " + playlist.getId() + ").");

    }

    @FXML
    private void handleDetailedAddTrack() {

        Track track = tracksListView.getSelectionModel().getSelectedItem();

        if (selectedPlaylist == null) {
            showPlaylistOperationAlert("Nessuna playlist selezionata", "Seleziona una playlist prima di aggiungere una traccia.");
            return;
        }

        if (track == null) {
            showPlaylistOperationAlert("Nessuna traccia selezionata", "Seleziona una traccia dal catalogo generale.");
            return;
        }

        if (selectedPlaylist.getTracks().contains(track)) {
            showPlaylistOperationAlert("Traccia già presente", "La traccia selezionata è già presente nella playlist.");
            return;
        }

        handleAddTrackToPlaylist(track, selectedPlaylist);

    }

    /** Rimuove un brano musicale da una playlist. */

    public void handleRemoveTrackFromPlaylist(Track track, Playlist playlist) {

        if (track == null || playlist == null) {
            System.out.println("[MAIN CONTROLLER] WARNING: Invalid input parameters.");
            return;
        }

        Command command = new RemoveTrackFromPlaylistCommand(playlist, track, playlistRepository);
        commandManager.executeCommand(command);

        if (selectedPlaylist != null && selectedPlaylist.getId() == playlist.getId()) {
            refreshPlaylistDetailsUI();
        }

        System.out.println("[MAIN CONTROLLER] INFO: Track removed from playlist successfully (Track ID: " + track.getId() + ", Playlist ID: " + playlist.getId() + ").");

    }

    private void handleRemoveTrackFromSelectedPlaylist(Track track) {

        if (selectedPlaylist == null) {
            showPlaylistOperationAlert("Nessuna playlist selezionata", "Seleziona una playlist prima di rimuovere una traccia.");
            return;
        }

        if (track == null) {
            showPlaylistOperationAlert("Nessuna traccia selezionata", "Seleziona una traccia dalla playlist.");
            return;
        }

        handleRemoveTrackFromPlaylist(track, selectedPlaylist);

    }

    /** Ripristina lo stato precedente annullando l'ultima operazione. */

    @FXML
    public void handleUndo() {
        commandManager.undoLastCommand();
        if (selectedPlaylist != null) {
            refreshPlaylistDetailsUI();
        }
    }

    /** Aggiorna la UI della playlist con i dati aggiornati presenti nel repository. */

    private void refreshPlaylistDetailsUI() {

        if (selectedPlaylist == null) {
            return;
        }

        playlistTracksListView.setItems(
            FXCollections.observableArrayList(
                selectedPlaylist.getTracks()
            )
        );

        playlistTracksListView.refresh();
        refreshMostPlayedLists();
    }

    private void showPlaylistOperationAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Playlist");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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
        playbackEngine.startPlaylist(createPlaylistFromTracks(), track);
        System.out.println("[MAIN CONTROLLER] INFO: Play requested for track: " + track.getTitle() + ".");
    }

    /**
     * Gestisce la pausa della riproduzione. Mette in pausa la traccia corrente.
     */
    public void handlePause() {
        playbackEngine.pause();
    }

    /**
     * Passa alla riproduzione del brano successivo.
     */
    public void handleSkip() {

        playbackEngine.skip();
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

    /**
     * Imposta la strategia di riproduzione. Riceve una strategia di riproduzione e la passa al motore di riproduzione per modificare il comportamento di riproduzione. Ad esempio, se si riceve una TrackLoopStrategy, il motore di riproduzione dovrebbe essere configurato per ripetere la traccia corrente indefinitamente finché non viene cambiata la traccia o la strategia.
     * @param strategy La strategia di riproduzione da impostare. Deve essere una strategia valida che implementa l'interfaccia PlaybackStrategy. Se è null, non viene eseguita alcuna operazione e viene mostrato un messaggio di avviso.
     */
    public void handleSetStrategy(PlaybackStrategy strategy) {

        playbackEngine.setStrategy(strategy);
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

    /**
     * Gestisce l riproduzione della sezione "Tutti i brani".
     */
    @FXML
    public void handlePlayAllTracks() {
        Playlist playlist = createPlaylistFromTracks();
        handlePlayPlaylist(playlist);
    }

    /**
     * Genera una playlist per la compatibilità con il playback engine nell'inizializzazione della coda di riproduzione.
     * @return Plylist contenente tutte le tracce musicali.
     */
    private Playlist createPlaylistFromTracks() {
        Playlist playlist = new Playlist("Tutti i brani");
        playlist.getTracks().addAll(tracks);
        return playlist;
    }

    /**
     * Gestisce il click del tasto play associato alle playlist avviando la riproduzione delle traccie contenute nella playlist coerentemente con la Playback Strategy.
     * @param playlist La Playlist di cui iniziare la riproduzione.
     */
    private void handlePlayPlaylist(Playlist playlist) {
        if (playlist == null || playlist.getTracks().isEmpty()) {
            System.out.println("[MAIN CONTROLLER] WARNING: No playlist selected or playlist is empty.");
            return;
        }

        selectedPlaylist = playlist;
        playbackEngine.startPlaylist(selectedPlaylist);
        syncDisplayedPlaylistPlayCount(selectedPlaylist);
        if (playlistListView != null) {
            playlistListView.refresh();
        }
        refreshMostPlayedLists();

        System.out.println("[MAIN CONTROLLER] INFO: Play requested for playlist: " + playlist.getName() + ".");
    }

    /**
     * Metodo pubblico richiamato dal bottone play nella sezione di dettaglio della playlist.
     */
    @FXML
    public void handlePlayPlaylistButton() {
        handlePlayPlaylist(selectedPlaylist);
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
            refreshMostPlayedLists();
            playbackEngine.setCurrentTrack(null);
            playbackEngine.setCurrentPlaylist(null);
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

            playlist.setName(trimmedName);

            playlistRepository.update(playlist);

            playlistListView.refresh();
            refreshMostPlayedLists();
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

        addTrackToPlaylistButton.setVisible(false);
        addTrackToPlaylistButton.setManaged(false);

        playPlaylistButton.setVisible(false);
        playPlaylistButton.setManaged(false);

    }

    /**
     * Mostra i dettagli di una playlist selezionata, inclusa la lista delle tracce presenti nella playlist. Aggiorna la GUI per visualizzare le informazioni della playlist e nascondere la lista delle playlist.
     * @param playlist La playlist di cui mostrare i dettagli. Deve essere una playlist già presente nella lista delle playlist. Viene utilizzata per aggiornare la GUI con le informazioni della playlist e la lista delle tracce contenute nella playlist.
     */
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

        addTrackToPlaylistButton.setVisible(true);
        addTrackToPlaylistButton.setManaged(true);

        playPlaylistButton.setVisible(true);
        playPlaylistButton.setManaged(true);

    }

    /**
     * Gestisce il clic sul pulsante per tornare alla visualizzazione delle playlist.
     */
    @FXML
    private void handleBackToPlaylists() {
        showPlaylistsView();
    }

    @FXML
    public void handleGeneratePlaylist() {
        // Se non ci sono brani nel catalogo, non possiamo generare nulla
        if (tracks.isEmpty()) {
            showInvalidPlaylistNameAlert("Non ci sono brani disponibili nel sistema per generare una playlist.");
            return;
        }

        // Creazione del Dialog personalizzato
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Generazione Automatica Playlist");
        dialog.setHeaderText("Configura i criteri per la generazione automatica");

        ButtonType generateButtonType = new ButtonType("Genera", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(generateButtonType, ButtonType.CANCEL);

        // Componenti del Form
        TextField nameField = new TextField();
        nameField.setPromptText("Nome della playlist");

        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Genere", "Anno");
        typeComboBox.setValue("Genere"); // Valore di default

        ComboBox<String> criteriaComboBox = new ComboBox<>();
        
        // Funzione interna per ripopolare la ComboBox dei criteri in base alla scelta (Genere o Anno)
        Runnable updateCriteriaOptions = () -> {
            criteriaComboBox.getItems().clear();
            if ("Genere".equals(typeComboBox.getValue())) {
                // Estrae tutti i generi unici e non nulli, ordinati alfabeticamente
                java.util.List<String> uniqueGenres = tracks.stream()
                        .map(Track::getGenre)
                        .filter(g -> g != null && !g.trim().isEmpty())
                        .distinct()
                        .sorted()
                        .toList();
                criteriaComboBox.getItems().addAll(uniqueGenres);
            } else {
                // Estrae tutti gli anni unici convertiti in stringa, ordinati
                java.util.List<String> uniqueYears = tracks.stream()
                        .map(Track::getYear)
                        .filter(y -> y > 0)
                        .distinct()
                        .sorted()
                        .map(String::valueOf)
                        .toList();
                criteriaComboBox.getItems().addAll(uniqueYears);
            }
            if (!criteriaComboBox.getItems().isEmpty()) {
                criteriaComboBox.setValue(criteriaComboBox.getItems().get(0));
            }
        };

        // Aggiorna le opzioni la prima volta e ogni volta che cambia il tipo di filtro
        updateCriteriaOptions.run();
        typeComboBox.setOnAction(e -> updateCriteriaOptions.run());

        // Disposizione grafica nel Dialog via GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");
        grid.addRow(0, new Label("Nome Playlist:"), nameField);
        grid.addRow(1, new Label("Filtra per:"), typeComboBox);
        grid.addRow(2, new Label("Seleziona Valore:"), criteriaComboBox);

        dialog.getDialogPane().setContent(grid);

        // Mostra il dialog e attende la conferma dell'utente
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == generateButtonType) {
            String playlistName = nameField.getText().trim();
            String selectedType = typeComboBox.getValue();
            String selectedCriteria = criteriaComboBox.getValue();

            // Validazione del nome
            if (!isValidPlaylistName(playlistName)) {
                showInvalidPlaylistNameAlert("Il nome della playlist è vuoto o esiste già una playlist con questo nome.");
                return;
            }

            //  Validazione del criterio selezionato
            if (selectedCriteria == null || selectedCriteria.isEmpty()) {
                showInvalidPlaylistNameAlert("Seleziona un valore valido per il genere o l'anno.");
                return;
            }

            // Mappiamo la scelta della GUI alla stringa attesa dalla Factory ("GENRE" o "YEAR")
            String factoryType = "Genere".equals(selectedType) ? "GENRE" : "YEAR";

            try {
                // Otteniamo il generatore corretto dalla Factory
                PlaylistGenerator generator = PlaylistGeneratorFactory.getGenerator(factoryType, selectedCriteria);

                // Generiamo la playlist passando la lista completa di tracce presenti in memoria
                Playlist generatedPlaylist = generator.generate(playlistName, new java.util.ArrayList<>(tracks));

                // Controllo di sicurezza: se nessuna traccia rispetta il criterio
                if (generatedPlaylist.getTracks().isEmpty()) {
                    showInvalidPlaylistNameAlert("Nessun brano corrisponde al criterio selezionato.");
                    return;
                }

                // salvataggio definitivo nel Database
                playlistRepository.save(generatedPlaylist);

                // Ricarica la lista sulla barra laterale per mostrare la nuova playlist generata
                playlists.setAll(playlistRepository.findAll());

                System.out.println("[MAIN CONTROLLER] INFO: Playlist '" + playlistName + "' generata automaticamente con successo.");

            } catch (Exception e) {
                System.err.println("[MAIN CONTROLLER] ERROR durante la generazione: " + e.getMessage());
                showInvalidPlaylistNameAlert("Errore durante la generazione automatica: " + e.getMessage());
            }
        }
    }
}
