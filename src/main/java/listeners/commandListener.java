package listeners;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import util.STATIC;
import core.*;

/**
 * © zekro 2017
 *
 * @author zekro
 */
public class commandListener extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent event) {

        if(event.getMessage().getContent().startsWith(STATIC.PREFIX) && !event.getMessage().getAuthor().equals(event.getJDA().getSelfUser()) {
            commandHandler.handleCommand(commandHandler.parse.parser(event.getMessage().getContent(), event));
        }

    }

}
