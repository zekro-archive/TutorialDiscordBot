package commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import util.STATIC;

import java.awt.Color;
import java.io.*;
import java.util.*;

/**
 * Created by zekro on 04.08.2017 / 15:16
 * supremeBot.commands
 * dev.zekro.de - github.zekro.de
 * © zekro 2017
 */


public class Vote implements Command, Serializable {

    private static TextChannel channel;

    private static HashMap<Guild, Poll> voteHash = new HashMap<>();

    private String[] emoti = {":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:", ":nine:", ":keycap_ten:"};


    static void message(String content) {
        EmbedBuilder emb = new EmbedBuilder().setDescription(content);
        channel.sendMessage(emb.build()).queue();
    }

    static void message(String content, Color color) {
        EmbedBuilder emb = new EmbedBuilder().setDescription(content).setColor(color);
        channel.sendMessage(emb.build()).queue();
    }


    private class Poll implements Serializable {

        private String guild;
        private String message;
        private String creator;
        private List<String> content;
        private HashMap<String, Integer> votes;

        private Poll(Guild guild, Member creator, Message message, List<String> content) {
            this.message = message.getId();
            this.guild = guild.getId();
            this.creator = creator.getUser().getId();
            this.content = content;
            this.votes = new HashMap<>();
        }

        private Member getCreator(Guild guild) {
            return guild.getMember(guild.getJDA().getUserById(creator));
        }

        private Guild getGuild(JDA jda) {
            return jda.getGuildById(guild);
        }

        private Message getMessage(Guild guild) {
            ArrayList<Message> msgs = new ArrayList<>();
            guild.getTextChannels().forEach(c -> msgs.add(c.getMessageById(message).complete()));
            return msgs.get(0);
        }

        private HashMap<Member, Integer> getVotes(Guild guild) {
            HashMap<Member, Integer> out = new HashMap<>();
            votes.forEach((s, integer) -> out.put(guild.getMember(guild.getJDA().getUserById(s)), integer));
            return out;
        }

    }


    private void createPoll(String[] args, MessageReceivedEvent event) {

        if (voteHash.containsKey(event.getGuild())) {
            message("A poll is currently running! Please close that poll before opening another one!", Color.red);
            return;
        }

        StringBuilder argsAS = new StringBuilder();
        Arrays.stream(args).skip(1).forEach(s -> argsAS.append(s + " "));

        List<String> thiscontent = Arrays.asList(argsAS.toString().split("\\|"));

        StringBuilder sb = new StringBuilder();
        int count = 0;
        ArrayList<String> thisconv = new ArrayList<>();
        thiscontent.stream().skip(1).forEach(thisconv::add);
        for ( String s : thisconv ) {
            sb.append("" + emoti[count] + "  -  " + s + "\n");
            count++;
        }

        Message msg =   event.getTextChannel().sendMessage(new EmbedBuilder().setColor(new Color(0x007FFF))
                .setTitle(":pencil:   " + event.getMember().getEffectiveName() + " created new vote.", null)
                .setDescription("\n" + thiscontent.get(0) + "\n\n" + sb.toString() + "\n\n*Use `" + STATIC.PREFIX + "vote v <number>` to vote.*")
                .build()).complete();

        voteHash.put(event.getGuild(), new Poll(event.getGuild(), event.getMember(), msg, thiscontent));

    }

    private void votePoll(String[] args, MessageReceivedEvent event) {

        if (args.length < 2) {
            message(help(), Color.red);
            return;
        } else if (!voteHash.containsKey(event.getGuild())) {
            message("There is currently no poll running to vote for!", Color.red);
            return;
        } else if (voteHash.get(event.getGuild()).getVotes(event.getGuild()).containsKey(event.getMember())) {
            message("Sorry, " + event.getAuthor().getAsMention() + ", you can only vote **once** for a poll!", Color.red);
            return;
        }

        int votenumb;
        try {
            votenumb = Integer.parseInt(args[1]);
        } catch (Exception e) {
            message("Please enter a valid number as vote.", Color.red);
            return;
        }

        if (votenumb > voteHash.get(event.getGuild()).content.stream().skip(1).count()) {
            message("Please enter a valid number as vote.", Color.red);
            return;
        }

        voteHash.get(event.getGuild()).votes.put(event.getMember().getUser().getId(), votenumb);

        event.getMessage().delete().queue();

    }

    private void statsPoll(MessageReceivedEvent event) {

        if (!voteHash.containsKey(event.getGuild())) {
            message("There is currently no poll running!", Color.red);
            return;
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        ArrayList<String> thisconv = new ArrayList<>();
        voteHash.get(event.getGuild()).content.stream().skip(1).forEach(thisconv::add);
        for ( String s : thisconv ) {
            int thiscount = count;
            sb.append("" + emoti[count] + "  -  " + s + "  -  Votes: `" + voteHash.get(event.getGuild()).getVotes(event.getGuild()).entrySet().stream().filter(memberIntegerEntry -> memberIntegerEntry.getValue().equals(thiscount + 1)).count() + "` " + "\n");
            count++;
        }

        Message msg =   event.getTextChannel().sendMessage(new EmbedBuilder().setColor(new Color(0x007FFF))
                .setTitle(":pencil:   Current vote from " + event.getMember().getEffectiveName() + ".", null)
                .setDescription("\n" + voteHash.get(event.getGuild()).content.get(0) + "\n\n" + sb.toString())
                .build()).complete();
    }

    private void closePoll(MessageReceivedEvent event) {

        if (!voteHash.containsKey(event.getGuild())) {
            message("There is currently no poll running to vote for!", Color.red);
            return;
        }

        if (event.getMember().equals(voteHash.get(event.getGuild()).getCreator(event.getGuild()))) {
            statsPoll(event);
            voteHash.remove(event.getGuild());
            new File("SERVER_SETTINGS/" + event.getGuild().getId() + "/vote").delete();
            event.getTextChannel().sendMessage(new EmbedBuilder().setColor(new Color(0xFF5600)).setDescription("Vote closed by " + event.getMember().getAsMention() + ".").build()).queue();
        } else {
            message("Sorry, " + event.getMember().getAsMention() + ", only the creator of the poll or a member with at least permission level 1 can close a running poll.", Color.red);
        }

    }

    private void savePoll(Guild guild) throws IOException {

        if (!voteHash.containsKey(guild))
            return;

        String saveFileName = "SERVER_SETTINGS/" + guild.getId() + "/vote";

        File f = new File("SERVER_SETTINGS/" + guild.getId() + "/");
        if (!new File(saveFileName).exists())
            f.mkdirs();

        Poll currPoll = voteHash.get(guild);

        FileOutputStream fos = new FileOutputStream(saveFileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(currPoll);
        oos.close();

    }

    public static void loadAllPolls(ReadyEvent event) {

        event.getJDA().getGuilds().forEach(g -> {

            File voteSave = new File("SERVER_SETTINGS/" + g.getId() + "/vote");
            if (voteSave.exists()) {
                try {
                    voteHash.put(g, getPoll(g));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        });

    }

    private static Poll getPoll(Guild guild) throws IOException, ClassNotFoundException {

        if (voteHash.containsKey(guild))
            return null;

        String saveFileName = "SERVER_SETTINGS/" + guild.getId() + "/vote";

        FileInputStream fis = new FileInputStream(saveFileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Poll out = (Poll) ois.readObject();
        ois.close();
        return out;

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
            case "start":
                createPoll(args, event);
                break;

            case "v":
            case "vote":
                votePoll(args, event);
                break;

            case "votes":
            case "stats":
                statsPoll(event);
                break;

            case "stop":
            case "end":
            case "close":
                closePoll(event);
                break;

            default:
                message(help(), Color.red);
        }

        voteHash.forEach((guild, poll) -> {
            try {
                savePoll(guild);
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
