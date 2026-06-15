package com.musicmanager.repository;

import com.musicmanager.database.DatabaseManager;
import com.musicmanager.model.Track;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Implementazione concreta dell'interfaccia TrackRepository mediante database H2.
 */

public class TrackRepositoryImpl implements TrackRepository {

    /**
     * Recupera tutti i brani musicali presenti nella tabella "Tracks".
     * @return Una lista di oggetti Track rappresentanti tutti i brani musicali presenti nella tabella "Tracks". Se si verifica un errore durante la lettura, viene restituita una lista vuota.
     */

    @Override
    public List<Track> findAll() {

        String sql = "SELECT id, title, author, length, genre, \"year\", playCount FROM Tracks ORDER BY id;";
        List<Track> tracks = new ArrayList<>();

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    tracks.add(new Track(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("length"),
                            rs.getString("genre"),
                            rs.getInt("year"),
                            rs.getInt("playCount")                            
                    ));
                }

                System.out.println("[H2 DATABASE] INFO: Tracks loaded successfully (" + tracks.size() + ").");
            }
        } catch (SQLException e) {
            System.err.println("[H2 DATABASE] ERROR: Tracks loading failed: " + e.getMessage());
        }

        return tracks;

    }

    /**
     * Recupera tutti i brani musicali presenti nella tabella "Tracks" ordinandoli per numero di riproduzione.
     * @return Una lista di oggetti Track rappresentanti tutti i brani musicali presenti nella tabella "Tracks". Se si verifica un errore durante la lettura, viene restituita una lista vuota.
     */
    @Override
    public List<Track> findAllByPlayCount() {

        String sql = "SELECT id, title, author, length, genre, \"year\", playCount FROM Tracks ORDER BY playCount LIMIT 10;";
        List<Track> tracks = new ArrayList<>();

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    tracks.add(new Track(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("length"),
                            rs.getString("genre"),
                            rs.getInt("year"),
                            rs.getInt("playCount")                            
                    ));
                }

                System.out.println("[H2 DATABASE] INFO: Tracks loaded successfully (" + tracks.size() + ").");
            }
        } catch (SQLException e) {
            System.err.println("[H2 DATABASE] ERROR: Tracks loading failed: " + e.getMessage());
        }

        return tracks;
    }

    /**
     * Inserisce un brano musicale nella tabella "Tracks".
     * Supporta sia l'autogenerazione dell'ID sia il reinserimento di un ID specifico,
     * necessario per l'operazione di Undo.
     * @param track Il brano musicale da inserire nella tabella "Tracks". Se l'ID è maggiore di 0, viene utilizzato come ID esplicito; altrimenti, viene autogenerato.
     */

    @Override
    public void save(Track track) {

        String sql;
        boolean useExplicitId = track.getId() > 0;

        if (useExplicitId) {
            sql = "INSERT INTO Tracks (id, title, author, length, genre, \"year\") VALUES (?, ?, ?, ?, ?, ?);";
        } else {
            sql = "INSERT INTO Tracks (title, author, length, genre, \"year\") VALUES (?, ?, ?, ?, ?);";
        }

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            try (PreparedStatement pstmt = useExplicitId ? conn.prepareStatement(sql) : conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                int parameterIndex = 1;

                if (useExplicitId) {
                    pstmt.setInt(parameterIndex++, track.getId());
                }
                pstmt.setString(parameterIndex++, track.getTitle());
                pstmt.setString(parameterIndex++, track.getAuthor());
                pstmt.setInt(parameterIndex++, track.getLength());
                pstmt.setString(parameterIndex++, track.getGenre());
                pstmt.setInt(parameterIndex++, track.getYear());

                pstmt.executeUpdate();

                if (!useExplicitId) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            track.setId(generatedKeys.getInt(1));
                        }
                    }
                }

                System.out.println("[H2 DATABASE] INFO: Track saved successfully (ID: " + track.getId() + ").");

            }
        } catch (SQLException e) {
            System.err.println("[H2 DATABASE] ERROR: Track save failed: " + e.getMessage());
        }

    }

    /**
     * Aggiorna un brano musicale identificato tramite ID.
     * @param track Il brano musicale da aggiornare.
     */

    @Override
    public void update(Track track) {

        String sql = "UPDATE Tracks SET title = ?, author = ?, length = ?, genre = ?, \"year\" = ?, playCount = ? WHERE id = ?;";

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, track.getTitle());
                pstmt.setString(2, track.getAuthor());
                pstmt.setInt(3, track.getLength());
                pstmt.setString(4, track.getGenre());
                pstmt.setInt(5, track.getYear());
                pstmt.setInt(6, track.getPlayCount());
                pstmt.setInt(7, track.getId());
                pstmt.executeUpdate();
                System.out.println("[H2 DATABASE] INFO: Track updated successfully (ID: " + track.getId() + ").");
            }
        } catch (SQLException e) {
            System.err.println("[H2 DATABASE] ERROR: Track update failed (ID: " + track.getId() + "): " + e.getMessage());
        }

    }

    /**
     * Elimina un brano musicale identificato tramite ID.
     * @param id L'ID del brano musicale da eliminare dalla tabella "Tracks".
     */

    @Override
    public void delete(int id) {

        String sql = "DELETE FROM Tracks WHERE id = ?;";

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                System.out.println("[H2 DATABASE] INFO: Track deleted successfully (ID: " + id + ").");
            }
        } catch (SQLException e) {
            System.err.println("[H2 DATABASE] ERROR: Track deletion failed (ID: " + id + "): " + e.getMessage());
        }

    }

}
