package AudioCore;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;

/**
 * Created by zekro on 18.06.2017 / 01:26
 * supremeBot.AudioCore
 * dev.zekro.de - github.zekro.de
 * © zekro 2017
 */

public class AudioInfo {

    private final AudioTrack track;
    private final Member author;

    /**
     * Erstellen von Instanzen der Klasse
     * @param track track
     * @param author Einreiher des Tracks
     */
    AudioInfo(AudioTrack track, Member author) {
        this.author = author;
        this.track = track;
    }

    public AudioTrack getTrack() {
        return track;
    }

    public Member getAuthor() {
        return author;
    }

}
