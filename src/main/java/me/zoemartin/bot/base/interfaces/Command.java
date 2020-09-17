package me.zoemartin.bot.base.interfaces;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

public interface Command {
    default Set<Command> subCommands() {
        return Collections.emptySet();
    }

    String name();

    default String regex() {
        return name();
    }

    void run(User user, MessageChannel channel, List<String> args, Message original, String invoked);

    Permission required();

    String usage();

    String description();
}
