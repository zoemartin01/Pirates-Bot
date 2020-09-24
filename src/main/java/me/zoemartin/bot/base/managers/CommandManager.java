package me.zoemartin.bot.base.managers;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.exceptions.ConsoleError;
import me.zoemartin.bot.base.interfaces.Command;
import me.zoemartin.bot.base.interfaces.CommandProcessor;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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

    public static void process(MessageReceivedEvent event, String input) {
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
