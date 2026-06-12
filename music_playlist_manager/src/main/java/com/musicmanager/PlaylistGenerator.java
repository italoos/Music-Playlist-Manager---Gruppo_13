package com.musicmanager;

import java.util.List;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;

public interface PlaylistGenerator {

    Playlist generate(String playlistName,List<Track> tracks);
}