package com.musicmanager;

import java.util.List;
import com.musicmanager.model.Track;

public interface PlaybackStrategy {

    Track getNext(List<Track> tracks, int currentIndex);

}