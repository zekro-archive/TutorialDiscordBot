package AudioCore;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by zekro on 18.06.2017 / 01:30
 * supremeBot.PACKAGE_NAME
 * dev.zekro.de - github.zekro.de
 * © zekro 2017
 */

public class TrackManager extends AudioEventAdapter {

    private final AudioPlayer player;
    private final Queue<AudioInfo> queue;


    /**
     * Instanzieren der Audioplayer für verschiedene Guilds
     * @param player
     */
    public TrackManager(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }


    /**
     * Einreihen eines Tracks in die Queue
     * @param track zu spielender Track
     * @param author Einreiher des Tracks
     */
    public void queue(AudioTrack track, Member author) {
        AudioInfo info = new AudioInfo(track, author);
        queue.add(info);

        // Wenn kein Track gespielt wird, wird mit diesem Track
        // der Player gestartet.
        if (player.getPlayingTrack() == null) {
            player.playTrack(track);
        }
    }


    /**
     * Returnt alle tracks in der Queue als Set<AudioInfo>
     * @return queue as Set<AudioInfo>
     */
    public Set<AudioInfo> getQueue() {
        return new LinkedHashSet<>(queue);
    }


    /**
     * Returnt AudioInfo des momentan gespielten Tracks in der Queue
     * @param track current Track
     * @return track as AudioInfo
     */
    public AudioInfo getInfo(AudioTrack track) {
        return queue.stream()
                .filter(info -> info.getTrack().equals(track))
                .findFirst().orElse(null);
    }


    /**
     * Purges queue.
     */
    public void purgeQueue() {
        queue.clear();
    }


    /**
     * Queue shufflen
     */
    public void shuffleQueue() {

        // Momentane Queue abspeichern
        List<AudioInfo> cQueue = new ArrayList<>(getQueue());

        // Momentanen Track abspeichern
        AudioInfo current = cQueue.get(0);

        // Momentanen Track aus savelist entfernen
        cQueue.remove(0);

        // Savelist shufflen
        Collections.shuffle(cQueue);

        // Momentanen track wieder anfügen an OBERSTER STELLE
        cQueue.add(0, current);

        // Queue clearen
        purgeQueue();

        // Savelist queuen
        queue.addAll(cQueue);
    }


    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {

        AudioInfo info = queue.element();
        VoiceChannel vChan = info.getAuthor().getVoiceState().getChannel();

        // Wenn Author nicht in voicechannel ist, stoppe player
        // SONST audioverbindung öffnen
        if (vChan == null) {
            player.stopTrack();
        } else {
            info.getAuthor().getGuild().getAudioManager().openAudioConnection(vChan);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        Guild g = queue.poll().getAuthor().getGuild();

        // Wenn queue leer ist, audioconnection beenden
        // SONST nächsten track in queue spielen
        if (queue.isEmpty()) {
            g.getAudioManager().closeAudioConnection();
        } else {
            player.playTrack(queue.element().getTrack());
        }
    }

}
