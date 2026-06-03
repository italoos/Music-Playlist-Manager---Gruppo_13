package com.musicmanager.controller;

/**
 * Interfaccia Command.
 * Definisce i metodi fondamentali per l'incapsulamento delle operazioni di inserimento e rimozione,
 * e per la gestione dell'annullamento.
 *
 * Scelta architetturale - Command Pattern:
 * Trasforma una richiesta in un oggetto.
 * Questo disaccoppia il componente che invoca l'azione (Controller) da quello che la esegue,
 * permettendo la gestione delle operazioni e della cronologia di annullamento (Undo).
 */

public interface Command {

    /**
     * Esegue l'operazione associata al comando.
     */

    void execute();

    /**
     * Annulla l'ultima operazione, ripristinando lo stato precedente.
     */

    void undo();

}