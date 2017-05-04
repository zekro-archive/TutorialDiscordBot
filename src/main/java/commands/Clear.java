package commands;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by zekro on 03.05.2017 / 18:44
 * supremeBot/commands
 * © zekro 2017
 */

public class Clear implements Command {

    EmbedBuilder error = new EmbedBuilder().setColor(Color.RED);

    private int getInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        int numb = getInt(args[0]);

        if (args.length < 1) {
            event.getTextChannel().sendMessage(
                    error.setDescription("Please enter a number of messages you want to delete!").build()
            ).queue();
        }

        if (numb > 1 && numb <= 100) {

            try {

                MessageHistory history = new MessageHistory(event.getTextChannel());
                List<Message> msgs;

                event.getMessage().delete().queue();

                msgs = history.retrievePast(numb).complete();
                event.getTextChannel().deleteMessages(msgs).queue();

                Message msg = event.getTextChannel().sendMessage(
                        new EmbedBuilder().setColor(Color.GREEN).setDescription("Deleted " + args[0] + " messages!").build()
                ).complete();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        msg.delete().queue();
                    }
                }, 3000);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            event.getTextChannel().sendMessage(
                    error.setDescription("please enter a number of messages between 2 and 100!").build()
            ).queue();
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
