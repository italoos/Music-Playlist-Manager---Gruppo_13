package com.musicmanager;

import java.util.List;
import java.util.Random;
import com.musicmanager.model.Track;

public class ShuffleStrategy implements PlaybackStrategy {

    private final Random random = new Random();

    @Override
    public Track getNext(List<Track> tracks, int currentIndex) {

        if(tracks.isEmpty()) {
            return null;
        }

        return tracks.get(
            random.nextInt(tracks.size())
        );
    }
}