package commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import util.STATIC;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zekro on 04.08.2017 / 15:16
 * supremeBot.commands
 * dev.zekro.de - github.zekro.de
 * © zekro 2017
 */


public class Vote implements Command, Serializable {

    private static TextChannel channel;

    private static HashMap<Guild, Poll> polls = new HashMap<>();

    // Liste an emoticons für die nummer der antworten auf den Poll
    private String[] emoti = {":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:", ":nine:", ":keycap_ten:"};

    private class Poll implements Serializable {

        private String creator;
        private String heading;
        private List<String> answers;
        private HashMap<String, Integer> votes;

        private Poll(Member creator, String heading, List<String> answers) {
            this.creator = creator.getUser().getId();
            this.heading = heading;
            this.answers = answers;
            this.votes = new HashMap<>();
        }

        private Member getCreator(Guild guild) {
            return guild.getMember(guild.getJDA().getUserById(creator));
        }

    }


    /**
     * Sendet eine embed message mit default color
     * @param content
     */
    static void message(String content) {
        EmbedBuilder emb = new EmbedBuilder().setDescription(content);
        channel.sendMessage(emb.build()).queue();
    }

    /**
     * Overload: Sendet message mit angegebener color
     * @param content
     * @param color
     */
    static void message(String content, Color color) {
        EmbedBuilder emb = new EmbedBuilder().setDescription(content).setColor(color);
        channel.sendMessage(emb.build()).queue();
    }


    /**
     * Returnt die heading und die antwortem der message aus dem poll
     * @param poll
     * @return
     */
    private EmbedBuilder getParsedPoll(Poll poll, Guild guild) {

        StringBuilder ansSTR = new StringBuilder();
        final AtomicInteger count = new AtomicInteger();

        poll.answers.forEach(s -> {
            int votescount = poll.votes.values().stream().filter(i -> i - 1 == count.get()).findFirst().orElse(0);
            ansSTR.append(emoti[count.get()] + "  -  " + s + "  -  Votes: `" + votescount + "`\n");
            count.addAndGet(1);
        });

        return new EmbedBuilder()
                .setAuthor(poll.getCreator(guild).getEffectiveName() + "'s poll.", null, poll.getCreator(guild).getUser().getAvatarUrl())
                .setDescription(":pencil:   " + poll.heading + "\n\n" + ansSTR.toString())
                .setFooter("Enter '" + STATIC.PREFIX + "vote v <number>' to vote!", null)
                .setColor(Color.cyan);
    }


    /**
     * Erstellt eine Poll instanz und packt sie in die poll HashMap
     * Gibt poll als message wieder
     * @param args
     * @param event
     */
    private void createPoll(String[] args, MessageReceivedEvent event) {

        if (polls.containsKey(event.getGuild())) {
            message("On this guild is currently still a poll running!", Color.red);
            return;
        }

        // new ArrayList<>(), weil Arrays.asList(args).subList(1, args.length) erzeugt "NotSerializable" Exception für "RandomAccessSubList"
        // Quelle: https://stackoverflow.com/questions/6189704/how-to-take-a-valid-sublist-in-java
        String argsSTR = String.join(" ", new ArrayList<>(Arrays.asList(args).subList(1, args.length)));
        List<String> content = Arrays.asList(argsSTR.split("\\|"));
        String heading = content.get(0);
        List<String> answers = new ArrayList<>(content.subList(1, content.size()));

        Poll poll = new Poll(event.getMember(), heading, answers);
        polls.put(event.getGuild(), poll);

        event.getTextChannel().sendMessage(getParsedPoll(poll, event.getGuild()).build()).queue();

    }


    /**
     * Fügt der votes map in der poll instanz den vote des authors hinzu
     * @param args
     * @param event
     */
    private void votePoll(String[] args, MessageReceivedEvent event) {

        if (!polls.containsKey(event.getGuild())) {
            message("There is currently no poll to vote for!", Color.red);
            return;
        }

        Poll poll = polls.get(event.getGuild());

        int vote;
        try {
            vote = Integer.parseInt(args[1]);
            if (vote > poll.answers.size())
                throw new Exception();
        }
        catch (Exception e) {
            message("Please enter a valid number for the vote!", Color.red);
            return;
        }

        if (poll.votes.containsKey(event.getAuthor().getId())) {
            message("Sorry, but you have already voted for this poll!", Color.red);
            return;
        }

        poll.votes.put(event.getAuthor().getId(), vote);
        polls.replace(event.getGuild(), poll);
        event.getMessage().delete().queue();

    }


    /**
     * Sendet eine message mit den momentanen vote stats
     * @param event
     */
    private void voteStats(MessageReceivedEvent event) {

        if (!polls.containsKey(event.getGuild())) {
            message("There is currently no vote running on this guild!", Color.red);
            return;
        }
        event.getTextChannel().sendMessage(getParsedPoll(polls.get(event.getGuild()), event.getGuild()).build()).queue();
    }


    /**
     * Sendet die poll stats in den chat und nimmt den polls aus der polls map
     * @param event
     */
    private void closeVote(MessageReceivedEvent event) {

        if (!polls.containsKey(event.getGuild())) {
            message("There is currently no vote running on this guild!", Color.red);
            return;
        }

        Poll poll = polls.get(event.getGuild());

        if (!event.getMember().equals(poll.getCreator(event.getGuild()))) {
            message("Only the creator of the poll (" + poll.getCreator(event.getGuild()).getAsMention() + ") can close this poll!", Color.red);
            return;
        }

        polls.remove(event.getGuild());
        event.getTextChannel().sendMessage(getParsedPoll(poll, event.getGuild()).build()).queue();
        message("Poll closed by " + event.getAuthor().getAsMention() + ".", new Color(0xFF6D00));

    }


    /**
     * Saved die instanz der klasse "Poll" in einer datei
     * @param guild
     * @throws IOException
     */
    private void savePoll(Guild guild) throws IOException {

        if (!polls.containsKey(guild))
            return;

        String saveFileName = "SERVER_SETTINGS/" + guild.getId() + "/vote";
        Poll currPoll = polls.get(guild);

        FileOutputStream fos = new FileOutputStream(saveFileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(currPoll);
        oos.close();

    }

    /**
     * Liest das objekt aus der klasse und serialized es als "Poll" klassen instanz
     * @param guild
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static Poll getPoll(Guild guild) throws IOException, ClassNotFoundException {

        if (polls.containsKey(guild))
            return null;

        String saveFileName = "SERVER_SETTINGS/" + guild.getId() + "/vote";

        FileInputStream fis = new FileInputStream(saveFileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Poll out = (Poll) ois.readObject();
        ois.close();
        return out;

    }


    public static void loadPolls(JDA jda) {

        jda.getGuilds().forEach(g -> {

            File f = new File("SERVER_SETTINGS/" + g.getId() + "/vote");
            if (f.exists())
                try {
                    polls.put(g, getPoll(g));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

        });

    }



    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        channel = event.getTextChannel();

        if (args.length < 1) {
            message(help(), Color.red);
        }

        switch (args[0]) {

            case "create":
                createPoll(args, event);
                break;

            case "v":
                votePoll(args, event);
                break;

            case "stats":
                voteStats(event);
                break;

            case "close":
                closeVote(event);
                break;
        }

        polls.forEach((guild, poll) -> {

            File path = new File("SERVER_SETTINGS/" + guild.getId() + "/");
            if (!path.exists()) {
                path.mkdirs();
            }

            try {
                savePoll(event.getGuild());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    public void executed(boolean sucess, MessageReceivedEvent event) {

    }

    @Override
    public String help() {
        return null;
    }
}
