package com.musicmanager.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.musicmanager.PlaylistGenerator;
import com.musicmanager.PlaylistGeneratorFactory;
/**
 * Classe di test JUnit per verificare il corretto funzionamento dei generatori 
 * di playlist automatici e della PlaylistGeneratorFactory.
 */
class PlaylistGenerationTest {

    private List<Track> catalogoBrani;

    @BeforeEach
    void setUp() {
        catalogoBrani = new ArrayList<>();
        
        // Prepariamo un catalogo di brani simulato per il test
        catalogoBrani.add(new Track(1, "Bohemian Rhapsody", "Queen", 354, "Rock", 1975, 0));
        catalogoBrani.add(new Track(2, "Hotel California", "Eagles", 390, "Rock", 1976, 0));
        catalogoBrani.add(new Track(3, "Bad Guy", "Billie Eilish", 194, "Pop", 2019, 0));
        catalogoBrani.add(new Track(4, "Blinding Lights", "The Weeknd", 200, "Pop", 2019, 0));
        catalogoBrani.add(new Track(5, "Master of Puppets", "Metallica", 515, "Metal", 1986, 0));
    }

    /**
     * Verifica che la Factory istanzi il generatore corretto per i Generi
     * e che la playlist generata contenga solo i brani di quel genere.
     */
    @Test
    void testGenerazionePlaylistPerGenere() {
        // Chiediamo alla factory il generatore per il genere "Rock"
        PlaylistGenerator generator = PlaylistGeneratorFactory.getGenerator("GENRE", "Rock");
        
        assertNotNull(generator, "Il generatore non dovrebbe essere null");
        
        // Generiamo la playlist
        Playlist playlistRock = generator.generate("I Miei Brani Rock", catalogoBrani);
        
        // Verifichiamo i risultati
        assertEquals("I Miei Brani Rock", playlistRock.getName());
        assertEquals(2, playlistRock.getTracks().size(), "La playlist Rock dovrebbe contenere esattamente 2 brani");
        
        // Verifichiamo che contenga i brani corretti
        assertTrue(playlistRock.getTracks().stream().allMatch(t -> t.getGenre().equalsIgnoreCase("Rock")),
                "Tutti i brani della playlist devono essere di genere Rock");
    }

    /**
     * Verifica che la Factory istanzi il generatore corretto per gli Anni
     * e che la playlist generata contenga solo i brani pubblicati in quell'anno.
     */
    @Test
    void testGenerazionePlaylistPerAnno() {
        // Chiediamo alla factory il generatore per l'anno "2019"
        PlaylistGenerator generator = PlaylistGeneratorFactory.getGenerator("YEAR", "2019");
        
        assertNotNull(generator, "Il generatore non dovrebbe essere null");
        
        // Generiamo la playlist
        Playlist playlist2019 = generator.generate("Hits del 2019", catalogoBrani);
        
        // Verifichiamo i risultati
        assertEquals("Hits del 2019", playlist2019.getName());
        assertEquals(2, playlist2019.getTracks().size(), "La playlist del 2019 dovrebbe contenere esattamente 2 brani");
        
        // Verifichiamo che contenga i brani dell'anno corretto
        assertTrue(playlist2019.getTracks().stream().allMatch(t -> t.getYear() == 2019),
                "Tutti i brani della playlist devono essere del 2019");
    }

    /**
     * Verifica che la Factory gestisca correttamente l'insensibilità alle maiuscole/minuscole
     * sia per i tipi di comando ("genre", "Genre", "GENRE") sia per i criteri.
     */
    @Test
    void testFactoryCaseInsensitivity() {
        // Testiamo con stringhe minuscole nella factory e nel criterio
        PlaylistGenerator generator = PlaylistGeneratorFactory.getGenerator("genre", "pop");
        Playlist playlistPop = generator.generate("Pop Hits", catalogoBrani);
        
        assertEquals(2, playlistPop.getTracks().size());
        assertTrue(playlistPop.getTracks().stream().allMatch(t -> t.getGenre().equalsIgnoreCase("Pop")));
    }

    /**
     * Verifica che la Factory sollevi un'eccezione appropriata (IllegalArgumentException)
     * se viene passato un tipo di generazione sconosciuto o non supportato.
     */
    @Test
    void testFactoryLanciaEccezionePerTipoSconosciuto() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            PlaylistGeneratorFactory.getGenerator("ARTIST", "Queen");
        });
        
        assertTrue(exception.getMessage().contains("Tipo di generazione sconosciuto"));
    }

    /**
     * Verifica che la Factory sollevi un'eccezione se si richiede un filtro per Anno
     * ma viene passata una stringa testuale non convertibile in numero.
     */
    @Test
    void testFactoryLanciaEccezionePerAnnoNonValido() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            PlaylistGeneratorFactory.getGenerator("YEAR", "novecento");
        });
        
        assertTrue(exception.getMessage().contains("L'anno inserito non è valido") || 
                   exception.getMessage().contains("numero"));
    }
    
    /**
     * Verifica il comportamento quando nessun brano corrisponde al criterio.
     * La playlist risultante deve essere valida ma vuota.
     */
    @Test
    void testGenerazionePlaylistVuotaSeCriterioNonMatcha() {
        PlaylistGenerator generator = PlaylistGeneratorFactory.getGenerator("GENRE", "Jazz");
        Playlist playlistJazz = generator.generate("My Jazz", catalogoBrani);
        
        assertNotNull(playlistJazz);
        assertTrue(playlistJazz.getTracks().isEmpty(), "La playlist dovrebbe essere vuota per un genere inesistente nel catalogo");
    }
}