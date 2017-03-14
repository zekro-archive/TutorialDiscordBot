package commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import core.permsCore;

/**
 * © zekro 2017
 *
 * @author zekro
 */

public class cmdPing implements Command {

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {

        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        if (permsCore.check(event) > 1)
            return;

        event.getTextChannel().sendMessage("Pong!").queue();
    }

    @Override
    public void executed(boolean sucess, MessageReceivedEvent event) {
        System.out.println("[INFO] Command '-ping' wurde ausgeführt!");
    }

    @Override
    public String help() {
        return null;
    }
}
