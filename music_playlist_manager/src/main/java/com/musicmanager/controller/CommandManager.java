package com.musicmanager.controller;

import java.util.Stack;

/**
 * Questa classe gestisce l'esecuzione, l'annullamento e la cronologia dei comandi.
 *
 * Scelta architetturale - Struttura dati LIFO (Stack):
 * Memorizza in ordine cronologico i comandi di inserimento e rimozione.
 * Questa struttura garantisce che l'operazione di Undo annulli l'ultima azione
 * eseguita dall'utente, mantenendo coerente la cronologia delle operazioni.
 */

public class CommandManager {

    /** Stack che memorizza la cronologia dei comandi. */

    private final Stack<Command> history = new Stack<>();

    /** Esegue un comando e lo registra nella cronologia. */

    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
    }

    /** Annulla l'ultimo comando registrato nella cronologia. */

    public void undoLastCommand() {
        if (!history.isEmpty()) {
            Command lastCommand = history.pop();
            lastCommand.undo();
        } else {
            System.out.println("[COMMAND MANAGER] INFO: No commands to undo.");
        }
    }

}