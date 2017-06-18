package core;

import commands.*;
import listeners.commandListener;
import listeners.readyListener;
import listeners.voiceListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import util.SECRETS;
import util.STATIC;

import java.lang.Exception;

public class Main {

    public static JDABuilder builder;

    public static void main(String[] Args) throws Exception { // Alle Exceptions sind Erweiterungen der nornalen Exception

        builder = new JDABuilder(AccountType.BOT);

        builder.setToken(SECRETS.TOKEN);
        //builder.setAutoReconnect(true); default ist true!

        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);

        builder.setGame(new Game() {
            @Override
            public String getName() {
                return "v." + STATIC.VERSION ;
            }

            @Override
            public String getUrl() {
                return null;
            }

            @Override
            public GameType getType() {
                return GameType.DEFAULT;
            }
        });

        addListeners();
        addCommands();
    }

    public static void addCommands() {

        commandHandler.commands.put("ping", new cmdPing());
        commandHandler.commands.put("say", new say());
        commandHandler.commands.put("test", new Test());
        commandHandler.commands.put("clear", new Clear());
        commandHandler.commands.put("m", new Music());
    }

    public static void addListeners() {

        builder.addListener(new commandListener(), new readyListener(), new voiceListener());
    }

}
