package listeners;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * © zekro 2017
 *
 * @author zekro
 */

public class readyListener extends ListenerAdapter {

    public void onReady(ReadyEvent event) {

        String out = "\nThis bot is running on following servers: \n";

        System.out.println(out + event.getJDA().getGuilds().stteam().map(guild->guild.getName()+" ("+guild.getIdLong()+")").collect(Collectors.joining("\n")));

        //for (Guild g : event.getJDA().getGuilds() ) {
        //    g.getTextChannels().get(0).sendMessage(
        //            "Hey guys! Im back again!"
        //    ).queue();
        //}

    }

}
