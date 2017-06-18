package commands;

import AudioCore.AudioInfo;
import AudioCore.PlayerSendHandler;
import AudioCore.TrackManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zekro on 18.06.2017 / 01:56
 * supremeBot.commands
 * dev.zekro.de - github.zekro.de
 * © zekro 2017
 */

public class Music implements Command {

    private static final int PLAYLIST_LIMIT = 1000;
    private static Guild guild;
    private static final AudioPlayerManager MANAGER = new DefaultAudioPlayerManager();
    private static final Map<Guild, Map.Entry<AudioPlayer, TrackManager>> PLAYERS = new HashMap<>();


    /**
     * Registrieren des Players als AudioSource für den AudioStream
     */
    public Music() {
        AudioSourceManagers.registerRemoteSources(MANAGER);
    }

    /**
     * Erstelle AudioPlayer für Guild
     * @param g Guild
     * @return AudioPlayer
     */
    private AudioPlayer createPlayer(Guild g) {
        AudioPlayer p = MANAGER.createPlayer();
        TrackManager m = new TrackManager(p);
        p.addListener(m);
        guild.getAudioManager().setSendingHandler(new PlayerSendHandler(p));

        PLAYERS.put(g, new AbstractMap.SimpleEntry<>(p, m));

        return p;
    }


    /**
     * Returnt ob Guild einen existierenden Player besitzt
     * @param g Guild
     * @return Guild hat player
     */
    private boolean hasPlayer(Guild g) {
        return PLAYERS.containsKey(g);
    }


    /**
     * Return den momentanen Player der Guild oder erstellt einen Player
     * @param g Guild
     * @return AudioPlayer
     */
    private AudioPlayer getPlayer(Guild g) {
        if (hasPlayer(g))
            return PLAYERS.get(g).getKey();
        else
            return createPlayer(g);
    }


    /**
     * Returnt den momentanen Track manager der Guild
     * @param g Guild
     * @return TrackManager
     */
    private TrackManager getTrackManager(Guild g) {
        return PLAYERS.get(g).getValue();
    }


    /**
     * Returnt ob der momentane player der Guild einen track zum spielen hat
     * @param g Guild
     * @return player ist idle
     */
    private boolean isIdle(Guild g) {
        return !hasPlayer(g) || getPlayer(g).getPlayingTrack() == null;
    }

    /**
     * Get track from argument
     * @param identifier Indetifier als URL oder als search result
     * @param author Author des Commands
     * @param msg Message des Commands
     */
    private void loadTrack(String identifier, Member author, Message msg) {

        Guild guild = author.getGuild();
        getPlayer(guild);

        MANAGER.setFrameBufferDuration(1000);
        MANAGER.loadItemOrdered(guild, identifier, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                System.out.println("TEST");
                getTrackManager(guild).queue(track, author);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (int i = 0; i < (playlist.getTracks().size() > PLAYLIST_LIMIT ? 1000 : playlist.getTracks().size()); i++) {
                    getTrackManager(guild).queue(playlist.getTracks().get(i), author);
                }
            }

            @Override
            public void noMatches() {
                System.out.println("NO MATCHES");
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exception.printStackTrace();
            }
        });

    }

    /**
     * Skipt den momentanen track durch stoppen des momentanen tracks
     * @param g Guild
     */
    private void skip(Guild g) {
        getPlayer(g).stopTrack();
    }


    /**
     * Returnt Timestamp in Zeitformat
     * @param milis Timestamp
     * @return Zeitformat
     */
    private String getTimestamp(long milis) {
        long seconds = milis / 1000;
        long hours = Math.floorDiv(seconds, 3600);
        seconds = seconds - (hours * 3600);
        long mins = Math.floorDiv(seconds, 60);
        seconds = seconds - (mins * 60);
        return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
    }


    /**
     * Erstellt track info für "queue" command
     * @param info TrackInfo
     * @return String
     */
    private String buildQueueMessage(AudioInfo info) {
        AudioTrackInfo trackInfo = info.getTrack().getInfo();
        String title = trackInfo.title;
        long length = trackInfo.length;
        return "`[ " + getTimestamp(length) + " ]` " + title + "\n";
    }


    private void sendErrorMsg(MessageReceivedEvent event, String content) {
        event.getTextChannel().sendMessage(new EmbedBuilder().setColor(Color.red).setDescription(content).build()).queue();
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        guild = event.getGuild();

        if (args.length < 1) {
            sendErrorMsg(event, help());
            return;
        }

        switch (args[0].toLowerCase()) {

            case "p":
            case "play":

                if (args.length < 2) {
                    sendErrorMsg(event, "Pelase include a valid source!");
                    return;
                }
                String input = Arrays.stream(args).skip(1).map(s -> " " + s).collect(Collectors.joining()).substring(1);

                if (!(input.startsWith("http://") || input.startsWith("https://"))) {
                    input = "ytsearch: " + input;
                }

                loadTrack(input, event.getMember(), event.getMessage());

                break;


            case "s":
            case "skip":

                if (isIdle(guild)) return;
                for (int i = (args.length > 1 ? Integer.parseInt(args[1]) : 1); i == 1; i--) {
                    skip(guild);
                }

                break;


            case "stop":

                getTrackManager(guild).purgeQueue();
                skip(guild);
                guild.getAudioManager().closeAudioConnection();

                break;


            case "shuffle":

                if (isIdle(guild)) return;
                getTrackManager(guild).shuffleQueue();

                break;


            case "now":
            case "playing":
            case "np":
            case "info":

                if (isIdle(guild)) return;

                AudioTrack track = getPlayer(guild).getPlayingTrack();
                AudioTrackInfo info = track.getInfo();

                event.getTextChannel().sendMessage(
                        new EmbedBuilder()
                            .setDescription("**CURRENT TRACK INFO**\n")
                            .addField("Title", info.title, false)
                            .addField("Duration", "`[ " + getTimestamp(track.getPosition()) + " / " + getTimestamp(track.getDuration()) + "]`", false)
                            .addField("Author", info.author, false)
                            .build()
                ).queue();

                break;


            case "queue":

                if (isIdle(guild)) return;

                int sideNumb = args.length > 1 ? Integer.parseInt(args[1]) : 1;

                List<String> tracks = new ArrayList<>();
                List<String> tracksSubList;
                getTrackManager(guild).getQueue().forEach(audioInfo -> tracks.add(buildQueueMessage(audioInfo)));

                if (tracks.size() > 20)
                    tracksSubList = tracks.subList((sideNumb-1)*20, (sideNumb-1)*20+20);
                else
                    tracksSubList = tracks;

                String out = tracksSubList.stream().collect(Collectors.joining("\n"));
                int sideNumbAll = tracks.size() >= 20 ? tracks.size() / 20 : 1;

                event.getTextChannel().sendMessage(
                        new EmbedBuilder()
                                .setDescription(
                                        "**QUEUE**\n\n" +
                                        "*[" + getTrackManager(guild).getQueue().size() + " Tracks | Side " + sideNumb + "/" + sideNumbAll + "]*\n\n" +
                                        out
                                )
                                .build()
                ).queue();

                break;
        }

    }

    @Override
    public void executed(boolean sucess, MessageReceivedEvent event) {

    }

    @Override
    public String help() {
        return null;
    }
}
