package me.zoemartin.piratesBot.core.managers;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.exceptions.ConsoleError;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.interfaces.CommandProcessor;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;

public class CommandManager {
    private CommandManager() {
        throw new IllegalAccessError();
    }

    private static final Collection<Command> registered = new HashSet<>();

    private static CommandProcessor processor;

    public static void register(Command c) {
        registered.add(c);
    }

    public static void setCommandProcessor(CommandProcessor cp) {
        processor = cp;
    }

    public static void process(GuildMessageReceivedEvent event, String input) {
        new Thread(() -> {
            try {
                processor.process(event, input);
            } catch (ConsoleError e) {
                if (event.getAuthor().getId().equals(Bot.getOWNER()))
                    event.getChannel().sendMessageFormat("Error: `%s`", e.getMessage()).queue();
                else System.err.println(e.getMessage());
            }
        }).start();
    }

    public static Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(registered);
    }
}