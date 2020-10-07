package me.zoemartin.piratesBot.core.interfaces;

import me.zoemartin.piratesBot.core.CommandPerm;
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

    CommandPerm commandPerm();

    default Collection<Permission> required() {
        return Collections.singleton(Permission.UNKNOWN);
    }

    /**
     * The command's parameters. If this command does not take any parameters do not overwrite this.
     * @return the command's parameters
     */
    default String usage() {
        return name();
    }

    String description();
}
