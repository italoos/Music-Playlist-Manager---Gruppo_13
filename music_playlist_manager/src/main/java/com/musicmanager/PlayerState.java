package com.musicmanager;

public interface PlayerState {

    void play(PlaybackEngine engine);

    void pause(PlaybackEngine engine);

    void skip(PlaybackEngine engine);
}