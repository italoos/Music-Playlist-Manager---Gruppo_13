package com.musicmanager;
import java.util.ArrayList;
import java.util.List;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class PlaybackEngine {

    private static PlaybackEngine instance;

    private List<PlaybackObserver> observers = new ArrayList<>();

    private Playlist currentPlaylist;
    private Track currentTrack;
    private int currentTime;
    private Timeline timeline;
    private PlayerState currentState;
    private PlaybackStrategy strategy;
    

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
        currentState.play(this);
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
        notifyObservers();
    }

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
    
        setCurrentTrack(nextTrack);
        play();

    }

    /** Sincronizza le modifiche della playlist corrente con la UI. */

    public void handlePlaylistModification(Playlist playlist) {
        if (this.currentPlaylist != null && this.currentPlaylist.getId() == playlist.getId()) {
            this.currentPlaylist = playlist;
            notifyObservers();
        }
    }

}
