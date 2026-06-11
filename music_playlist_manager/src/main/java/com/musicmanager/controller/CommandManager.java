package com.musicmanager.controller;

import java.util.Stack;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;

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

    /** canUndo indica se è possibile eseguire un'operazione di Undo. */

    private final BooleanProperty canUndo = new SimpleBooleanProperty(false);

    /** Restituisce la proprietà di sola lettura che indica se l'operazione di Undo è disponibile. */

    public ReadOnlyBooleanProperty canUndoProperty() {
        return canUndo;
    }

    /** Esegue un comando e lo registra nella cronologia. */

    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
        canUndo.set(true);
    }

    /** Annulla l'ultimo comando registrato nella cronologia. */

    public void undoLastCommand() {
        if (!history.isEmpty()) {
            Command lastCommand = history.pop();
            lastCommand.undo();
        } else {
            System.out.println("[COMMAND MANAGER] INFO: No commands to undo.");
        }
        canUndo.set(!history.isEmpty());
    }

}