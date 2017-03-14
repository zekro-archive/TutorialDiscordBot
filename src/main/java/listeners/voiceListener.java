package listeners;

import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * © zekro 2017
 *
 * @author zekro
 */

public class voiceListener extends ListenerAdapter {

    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        event.getGuild().getTextChannelsByName("voicelog", true).get(0).sendMessage(
                "Member " + event.getVoiceState().getMember().getUser().getName() + " joined voice channel " + event.getChannelJoined().getName() + "."
        ).queue();
    }

}
