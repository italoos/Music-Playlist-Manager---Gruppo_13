package com.musicmanager.model.playback;

import com.musicmanager.model.Playlist;
import com.musicmanager.model.Track;

public interface PlaybackObserver{


    void update(Track CurrentTrack, Playlist CurrentPlaylist, int currentTime, boolean isPlaying);


}
