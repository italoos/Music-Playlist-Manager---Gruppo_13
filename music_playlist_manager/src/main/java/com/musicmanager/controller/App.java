package com.musicmanager.controller;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    /**
     * Avvia l'applicazione JavaFX.
     * @param stage Lo stage principale dell'applicazione.
     * @throws IOException Se si verifica un errore durante il caricamento del file FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 900, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Imposta la radice della scena con il file FXML specificato.
     * @param fxml Il nome del file FXML da caricare (senza estensione).
     * @throws IOException Se si verifica un errore durante il caricamento del file FXML.
     */
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Carica il file FXML specificato e restituisce il nodo radice.
     * @param fxml Il nome del file FXML da caricare (senza estensione).
     * @return Il nodo radice del file FXML caricato.
     * @throws IOException Se si verifica un errore durante il caricamento del file FXML.
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/musicmanager/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /**
     * Il punto di ingresso dell'applicazione.
     * @param args Gli argomenti della riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
