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

    private final AudioTrack TRACK;
    private final Member AUTHOR;

    /**
     * Erstellen von Instanzen der Klasse
     * @param track track
     * @param author Einreiher des Tracks
     */
    AudioInfo(AudioTrack track, Member author) {
        this.AUTHOR = author;
        this.TRACK = track;
    }

    public AudioTrack getTrack() {
        return TRACK;
    }

    public Member getAuthor() {
        return AUTHOR;
    }

}
