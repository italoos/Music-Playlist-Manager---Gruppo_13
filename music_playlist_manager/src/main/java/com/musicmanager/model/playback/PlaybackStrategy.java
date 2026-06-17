package com.musicmanager.model.playback;

import java.util.List;
import com.musicmanager.model.Track;

public interface PlaybackStrategy {

    default Track getFirst(List<Track> tracks, Track preferredTrack) {
        if (tracks.isEmpty()) {
            return null;
        }

        if (preferredTrack != null && tracks.contains(preferredTrack)) {
            return preferredTrack;
        }

        return tracks.get(0);
    }

    Track getNext(List<Track> tracks, int currentIndex);

}
