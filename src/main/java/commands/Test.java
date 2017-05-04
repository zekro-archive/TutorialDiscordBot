package commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * Created by zekro on 02.05.2017 / 10:51
 * supremeBot/commands
 * © zekro 2017
 */
public class Test implements Command {
    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {

        List<Role> roleList = event.getMessage().getMentionedRoles();
        Guild guild = event.getGuild();

        event.getMessage().getMentionedUsers().stream()
                .filter(user -> guild.getMember(user).getRoles().stream().anyMatch(role -> roleList.contains(role)))
                .forEach(user -> guild.getController().removeRolesFromMember(guild.getMember(user), roleList).queue());
    }

    @Override
    public void executed(boolean sucess, MessageReceivedEvent event) {
    }

    @Override
    public String help() {
        return null;
    }
}
