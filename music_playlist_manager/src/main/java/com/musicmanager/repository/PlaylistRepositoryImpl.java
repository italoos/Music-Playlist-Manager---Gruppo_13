package com.musicmanager.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.musicmanager.database.DatabaseManager;
import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;

public class PlaylistRepositoryImpl implements PlaylistRepository {

    @Override
    public void save(Playlist playlist) {

        String insertPlaylist =
                "INSERT INTO Playlists (name) VALUES (?);";

        String insertRelation =
                "INSERT INTO Playlist_Tracks (playlist_id, track_id) VALUES (?, ?);";

        Connection conn = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            int playlistId;

            try (PreparedStatement ps =
                         conn.prepareStatement(insertPlaylist, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, playlist.getName());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("ID not generated");
                    playlistId = rs.getInt(1);
                }
            }

            if (playlist.getTracks() != null && !playlist.getTracks().isEmpty()) {

                try (PreparedStatement ps = conn.prepareStatement(insertRelation)) {

                    for (Track t : playlist.getTracks()) {
                        ps.setInt(1, playlistId);
                        ps.setInt(2, t.getId());
                        ps.addBatch();
                    }

                    ps.executeBatch();
                }
            }

            conn.commit();

        } catch (SQLException e) {

            System.err.println("[DB ERROR] save failed: " + e.getMessage());

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("[DB ERROR] rollback failed: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public void update(Playlist playlist) {

        String updatePlaylist =
                "UPDATE Playlists SET name = ? WHERE id = ?;";

        String deleteRelations =
                "DELETE FROM Playlist_Tracks WHERE playlist_id = ?;";

        String insertRelation =
                "INSERT INTO Playlist_Tracks (playlist_id, track_id) VALUES (?, ?);";

        Connection conn = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            int playlistId = playlist.getId();

            try (PreparedStatement ps = conn.prepareStatement(updatePlaylist)) {
                ps.setString(1, playlist.getName());
                ps.setInt(2, playlistId);

                int affected = ps.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(deleteRelations)) {
                ps.setInt(1, playlistId);
                ps.executeUpdate();
            }

            if (playlist.getTracks() != null && !playlist.getTracks().isEmpty()) {

                try (PreparedStatement ps = conn.prepareStatement(insertRelation)) {

                    for (Track t : playlist.getTracks()) {
                        ps.setInt(1, playlistId);
                        ps.setInt(2, t.getId());
                        ps.addBatch();
                    }

                    ps.executeBatch();
                }
            }

            conn.commit();

        } catch (SQLException e) {

            System.err.println("[DB ERROR] update failed: " + e.getMessage());

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("[DB ERROR] rollback failed: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public void delete(int playlistId) {

        String deleteRelations =
                "DELETE FROM Playlist_Tracks WHERE playlist_id = ?;";

        String deletePlaylist =
                "DELETE FROM Playlists WHERE id = ?;";

        Connection conn = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(deleteRelations)) {
                ps.setInt(1, playlistId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(deletePlaylist)) {
                ps.setInt(1, playlistId);
                ps.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {

            System.err.println("[DB ERROR] delete failed: " + e.getMessage());

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("[DB ERROR] rollback failed: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public Playlist findById(int playlistId) {

        String sql =
                "SELECT p.id, p.name, " +
                "t.id AS tid, t.title, t.author, t.length, t.genre, t.\"year\" " +
                "FROM Playlists p " +
                "LEFT JOIN Playlist_Tracks pt ON p.id = pt.playlist_id " +
                "LEFT JOIN Tracks t ON t.id = pt.track_id " +
                "WHERE p.id = ?";

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();

            Playlist playlist = null;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, playlistId);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        if (playlist == null) {
                            playlist = new Playlist(
                                    rs.getInt("id"),
                                    rs.getString("name")
                            );
                        }

                        int trackId = rs.getInt("tid");

                        if (trackId > 0) {
                            playlist.addTrack(new Track(
                                    trackId,
                                    rs.getString("title"),
                                    rs.getString("author"),
                                    rs.getInt("length"),
                                    rs.getString("genre"),
                                    rs.getInt("year")
                            ));
                        }
                    }
                }
            }

            return playlist;

        } catch (SQLException e) {
            System.err.println("[DB ERROR] findById failed: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Playlist> findAll() {

       String sql =
        "SELECT p.id AS pid, p.name, " +
        "t.id AS tid, t.title, t.author, t.length, t.genre, t.\"year\" " +
        "FROM Playlists p " +
        "LEFT JOIN Playlist_Tracks pt ON p.id = pt.playlist_id " +
        "LEFT JOIN Tracks t ON t.id = pt.track_id " +
        "ORDER BY p.id;";

        try {
            Connection conn = DatabaseManager.getInstance().getConnection();

            Map<Integer, Playlist> map = new LinkedHashMap<>();

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    int pid = rs.getInt("pid");

                    Playlist playlist = map.get(pid);

                    if (playlist == null) {
                        playlist = new Playlist(pid, rs.getString("name"));
                        map.put(pid, playlist);
                    }

                    int trackId = rs.getInt("tid");

                    if (trackId > 0) {
                        playlist.addTrack(new Track(
                                trackId,
                                rs.getString("title"),
                                rs.getString("author"),
                                rs.getInt("length"),
                                rs.getString("genre"),
                                rs.getInt("year")
                        ));
                    }
                }
            }

            return new ArrayList<>(map.values());

        } catch (SQLException e) {
            System.err.println("[DB ERROR] findAll failed: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}