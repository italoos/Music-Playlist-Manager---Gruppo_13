package com.musicmanager;

import java.util.List;
import com.musicmanager.model.Track;

public class SequentialStrategy implements PlaybackStrategy{

    @Override
    public Track getNext(List<Track> tracks, int currentIndex) {

        if(currentIndex + 1 < tracks.size()) {
            return tracks.get(currentIndex + 1);
        }

        return null;
    }
    
}
