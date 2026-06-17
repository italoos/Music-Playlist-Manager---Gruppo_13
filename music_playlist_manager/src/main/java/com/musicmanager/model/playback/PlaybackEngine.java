package com.musicmanager.model.playback;
import java.util.ArrayList;
import java.util.List;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import com.musicmanager.repository.PlaylistRepository;
import com.musicmanager.repository.TrackRepository;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Motore centrale della riproduzione musicale.
 *
 * Coordina traccia corrente, playlist corrente, stato del player,
 * strategia di avanzamento e notifica degli osservatori della UI.
 */
public class PlaybackEngine {

    private static PlaybackEngine instance;

    private List<PlaybackObserver> observers = new ArrayList<>();

    private Playlist currentPlaylist;
    private Track currentTrack;
    private int currentTime;
    private Timeline timeline;
    private PlayerState currentState;
    private PlaybackStrategy strategy;
    private TrackRepository trackRepository;
    private PlaylistRepository playlistRepository;
    private boolean trackNeedsPlayCountIncrement;
    

    /**
     * Costruttore privato per creare un'istanza di PlaybackEngine.
     */
    private PlaybackEngine() {
        this(true);
    }

    /**
     * Costruttore privato per creare un'istanza di PlaybackEngine. Se initializeTimeline è true, viene inizializzato il timeline per aggiornare il tempo di riproduzione.
     * @param initializeTimeline Indica se inizializzare il timeline per aggiornare il tempo di riproduzione. Se false, il timeline non viene creato, utile per i test unitari.
     */
    private PlaybackEngine(boolean initializeTimeline) {
        currentState = new PausedState();
        currentTime = 0;

        // Modalità di riproduzione di default
        strategy = new SequentialStrategy();

        if (initializeTimeline) {
            initTimeline();
        }
    }

    /**
     * Inizializza il timeline per aggiornare il tempo di riproduzione.
     */
    private void initTimeline() {
        timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> {

                if (!(currentState instanceof PlayingState)) {
                    return;
                }

                if (currentTrack == null) {
                    return;
                }

                currentTime++;

                if (currentTime >= currentTrack.getLength()) {
                    nextTrack();
                    return;
                }

                notifyObservers();
            })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Restituisce l'istanza singleton di PlaybackEngine. Se l'istanza non esiste, viene creata una nuova istanza.
     * @return L'istanza singleton di PlaybackEngine.
     */
    public static PlaybackEngine getInstance() {
        if (instance == null) {
            instance = new PlaybackEngine();
        }
        return instance;
    }

    /**
     * Restituisce un'istanza di PlaybackEngine senza inizializzare il timeline, utile per i test unitari.
     * @return Un'istanza di PlaybackEngine senza inizializzare il timeline.
     */
    static PlaybackEngine getTestInstance() {
        instance = new PlaybackEngine(false);
        return instance;
    }

    /**
     * Registra un osservatore per ricevere aggiornamenti sullo stato di riproduzione. Se l'osservatore è già registrato, non viene aggiunto nuovamente.
     * @param o L'osservatore da registrare per ricevere aggiornamenti sullo stato di riproduzione.
     */
    public void registerObserver(PlaybackObserver o) {
        if (observers.contains(o)) {
            return;
        }

        observers.add(o);
    }

    /**
     * Notifica tutti gli osservatori dell'aggiornamento dello stato di riproduzione.
     */
    public void notifyObservers() {
        for (PlaybackObserver o : observers) {
            o.update(
                currentTrack,
                currentPlaylist,
                currentTime,
                isPlaying()
            );
        }
    }

    /**
     * Inizia la riproduzione della traccia corrente.
     */
    public void play() {
        if (currentTrack == null) {
            return;
        }

        boolean wasPlaying = isPlaying();
        boolean playCountIncremented = false;

        if (trackNeedsPlayCountIncrement) {
            currentTrack.incrementPlayCount();
            if (trackRepository != null) {
                trackRepository.update(currentTrack);
            }
            trackNeedsPlayCountIncrement = false;
            playCountIncremented = true;
        }

        currentState.play(this);

        // PausedState notifica gia' il cambio di stato. PlayingState invece non
        // notifica, quindi serve un aggiornamento esplicito per il nuovo conteggio.
        if (playCountIncremented && wasPlaying) {
            notifyObservers();
        }
    }

    /**
     * Mette in pausa la riproduzione della traccia corrente.
     */
    public void pause() {
        currentState.pause(this);
    }

    /**
     * Passa alla traccia successiva.
     */
    public void skip() {
        currentState.skip(this);
    }

    /**
     * Imposta la traccia corrente.
     * @param currentTrack La traccia da impostare come corrente.
     */
    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
        this.currentTime = 0;
        this.trackNeedsPlayCountIncrement = currentTrack != null;
        notifyObservers();
    }

    /**
     * Restituisce la playlist attualmente in riproduzione.
     * @return La playlist corrente, oppure null se non e attiva alcuna playlist.
     */
    public Playlist getCurrentPlaylist() {
        return this.currentPlaylist;
    }

    /**
     * Imposta la playlist corrente.
     * @param currentPlaylist La playlist da impostare come corrente.
     */
    public void setCurrentPlaylist(Playlist currentPlaylist) {
        this.currentPlaylist = currentPlaylist;
    }

    /**
     * Avvia una nuova coda di riproduzione usando la strategia corrente.
     * Se preferredTrack e' null, la strategia sceglie la prima traccia.
     * @param playlist La playlist da cui iniziare la riproduzione.
     * @param preferredTrack La traccia preferita da riprodurre per prima, se presente nella playlist. Se null, la strategia sceglie la prima traccia.
     */
    public void startPlaylist(Playlist playlist, Track preferredTrack) {
        if (playlist == null || playlist.getTracks().isEmpty()) {
            return;
        }

        currentPlaylist = playlist;
        currentPlaylist.incrementPlayCount();
        if (playlistRepository != null && currentPlaylist.getId() > 0) {
            playlistRepository.update(currentPlaylist);
        }
        Track firstTrack = strategy.getFirst(playlist.getTracks(), preferredTrack);
        setCurrentTrack(firstTrack);
        play();
    }

    /**
     * Avvia una playlist scegliendo automaticamente la prima traccia tramite la strategia corrente.
     * @param playlist La playlist da riprodurre.
     */
    public void startPlaylist(Playlist playlist) {
        startPlaylist(playlist, null);
    }

    /**
     * Restituisce la traccia attualmente in riproduzione.
     * @return La traccia attualmente in riproduzione.
     */
    public Track getCurrentTrack() {
        return currentTrack;
    }

    /**
     * Restituisce la playlist attualmente in riproduzione.
     * @param state Lo stato da impostare per il player.
     */
    public void setState(PlayerState state) {
        this.currentState = state;
    }

    /**
     * Imposta la strategia di riproduzione da utilizzare per la riproduzione delle tracce.
     * @param strategy La strategia di riproduzione da utilizzare per la riproduzione delle tracce.
     */
    public void setStrategy(PlaybackStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Imposta il repository usato per aggiornare il contatore di riproduzione delle tracce.
     * @param trackRepository Repository delle tracce.
     */
    public void setTrackRepository(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    /**
     * Imposta il repository usato per aggiornare il contatore di riproduzione delle playlist.
     * @param playlistRepository Repository delle playlist.
     */
    public void setPlaylistRepository(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    /**
     * Restituisce true se la traccia è attualmente in riproduzione, false altrimenti.
     * @return true se la traccia è attualmente in riproduzione, false altrimenti.
     */
    public boolean isPlaying() {
        return currentState instanceof PlayingState;
    }

    /**
     * Determina e avvia la prossima traccia della playlist
     * utilizzando la PlaybackStrategy attualmente selezionata.
     *
     * Se non esiste una traccia successiva, la riproduzione
     * viene interrotta e il player passa allo stato Paused.
     */
    public void nextTrack() {
        
        if (currentTrack == null) {
            return;
        }

        if (currentPlaylist == null) {
            setCurrentTrack(null);
            currentTime = 0;
            setState(new PausedState());
            notifyObservers();
            return;
        }

    
        int currentIndex = currentPlaylist.indexOf(currentTrack);
    
        Track nextTrack = strategy.getNext(currentPlaylist.getTracks(), currentIndex);
    
        if (nextTrack == null) {
            currentTime = 0;
            setCurrentTrack(null); 
            setState(new PausedState());
            notifyObservers();
            return;
        }

        // incremento playCount quando ricomincia la playlist
        if (strategy instanceof PlaylistLoopStrategy || ( strategy instanceof TrackLoopStrategy && currentPlaylist.getTracks().size() == 1 )) {
            if (currentPlaylist.indexOf(nextTrack) == 0) {
                currentPlaylist.incrementPlayCount();
                if (playlistRepository != null && currentPlaylist.getId() > 0) {
                    playlistRepository.update(currentPlaylist);
                }
            }
        }
        
        setCurrentTrack(nextTrack);
        play();


    }

    /**
     * Sincronizza le modifiche della playlist corrente con la UI.
     * @param playlist Playlist aggiornata dal controller o da un comando.
     */
    public void handlePlaylistModification(Playlist playlist) {
        if (this.currentPlaylist != null && this.currentPlaylist.getId() == playlist.getId()) {
            this.currentPlaylist = playlist;
            notifyObservers();
        }
    }

}
