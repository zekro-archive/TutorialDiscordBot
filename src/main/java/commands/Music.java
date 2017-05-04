package commands;

import audioCore.AudioInfo;
import audioCore.AudioPlayerSendHandler;
import audioCore.TrackManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zekro on 22.04.2017 / 14:27
 * supremeBot/commands
 * © zekro 2017
 */

public class Music implements Command {

    private static final int PLAYLIST_LIMIT = 1000;

    private static Guild guild;
    private static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private static final Map<String, Map.Entry<AudioPlayer, TrackManager>> players = new HashMap<>();

    public Music() {
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    private boolean hasPlayer(Guild guild) {
        return players.containsKey(guild.getId());
    }

    private AudioPlayer getPlayer(Guild guild) {
        AudioPlayer player;
        if (hasPlayer(guild))
            player = players.get(guild.getId()).getKey();
        else
            player = createPlayer(guild);

        return player;
    }

    private AudioPlayer createPlayer(Guild guild) {
        AudioPlayer nPlayer = playerManager.createPlayer();
        TrackManager manager = new TrackManager(nPlayer);
        nPlayer.addListener(manager);
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(nPlayer));
        players.put(guild.getId(), new AbstractMap.SimpleEntry<>(nPlayer, manager));
        return nPlayer;
    }

    private TrackManager getTrackManager(Guild guild) {
        return players.get(guild.getId()).getValue();
    }

    private void reset(Guild guild) {
        players.remove(guild.getId());
        getPlayer(guild).destroy();
        getTrackManager(guild).purgeQueue();
        guild.getAudioManager().closeAudioConnection();
    }

    public void loadTrack(String identifier, Member author, Message msg) {

        Guild g = author.getGuild();
        getPlayer(guild);

        msg.getTextChannel().sendTyping().queue();
        playerManager.loadItemOrdered(guild, identifier, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                getTrackManager(guild).queue(audioTrack, author);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                if (audioPlaylist.getSelectedTrack() != null)
                    trackLoaded(audioPlaylist.getSelectedTrack());
                else if (audioPlaylist.isSearchResult())
                    trackLoaded(audioPlaylist.getTracks().get(0));
                else {
                    for (int i = 0; Math.min(audioPlaylist.getTracks().size(), PLAYLIST_LIMIT) > i; i++)
                        getTrackManager(guild).queue(audioPlaylist.getTracks().get(i), author);
                }
            }

            @Override
            public void noMatches() {}

            @Override
            public void loadFailed(FriendlyException e) {}
        });

    }

    private boolean isDj(Member member) {
        return member.getRoles().stream().anyMatch(role -> role.getName().equals("DJ"));
    }

    private boolean ifCurrentDj(Member member) {
        return getTrackManager(member.getGuild()).getTrackInfo(getPlayer(member.getGuild()).getPlayingTrack()).getAuthor().equals(member);
    }

    private boolean isIdle(Guild guild, MessageReceivedEvent event) {
        if (!hasPlayer(guild) || getPlayer(guild).getPlayingTrack() == null) {
            event.getTextChannel().sendMessage(":warning: Sorry, but no music is currently playing!").queue();
            return true;
        }
        return false;
    }

    private void skip(Guild guild) {
        getPlayer(guild).stopTrack();
    }

    private String getOrNull(String s) {
        return s.isEmpty() ? "N/A" : s;
    }

    private String getTimestamp(long milis) {
        long seconds = milis / 1000;
        long hours = Math.floorDiv(seconds, 3600);
        seconds = seconds - (hours * 3600);
        long mins = Math.floorDiv(seconds, 60);
        seconds = seconds - (mins * 60);
        return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
    }

    private String buildQueueMessage(AudioInfo info) {
        AudioTrackInfo trackInfo = info.getTrack().getInfo();
        String title = trackInfo.title;
        long length = trackInfo.length;
        return "`  [ " + getTimestamp(length) + " ]  `  " + title + "\n";
    }

    private String gerAudioInfo(AudioTrack track) {
        return
            ":cd:   " + getOrNull(track.getInfo().title) + "\n" +
            ":stopwatch:   " + "`  [ " + getTimestamp(track.getPosition()) + " / " + getTimestamp(track.getInfo().length) + " ]   ` \n" +
            ":microphone:   " + getOrNull(track.getInfo().author) + "\n"
        ;
    }



    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {



    }

    @Override
    public void executed(boolean sucess, MessageReceivedEvent event) {

    }

    @Override
    public String help() {
        return null;
    }
}
