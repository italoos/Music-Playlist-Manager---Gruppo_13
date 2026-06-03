package com.musicmanager;

public interface PlaybackObserver{


    void update(Track CurrentTrack, Playlist CurrentPlaylist, int currentTime, boolean isPlaying);


}