package audioCore;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;

/**
 * Created by zekro on 18.06.2017 / 11:26
 * supremeBot.audioCore
 * dev.zekro.de - github.zekro.de
 * © zekro 2017
 */

public class AudioInfo {

    private final AudioTrack TRACK;
    private final Member AUTHOR;

    /**
     * Erstellt eine Instanz der Klasse AudioInfo.
     * @param track AudioTrack
     * @param author Member, der den Track eingereiht hat
     */
    public AudioInfo(AudioTrack track, Member author) {
        this.TRACK = track;
        this.AUTHOR = author;
    }

    public AudioTrack getTrack() {
        return TRACK;
    }

    public Member getAuthor() {
        return AUTHOR;
    }

}
