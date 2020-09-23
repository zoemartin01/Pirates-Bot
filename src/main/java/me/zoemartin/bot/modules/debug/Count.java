package me.zoemartin.bot.modules.debug;

import me.zoemartin.bot.base.CommandPerm;
import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.interfaces.Command;
import me.zoemartin.bot.base.util.Check;
import me.zoemartin.bot.base.util.Parser;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

public class Count implements Command {
    @Override
    public Set<Command> subCommands() {
        return Collections.emptySet();
    }

    @Override
    public String name() {
        return "count";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.size() == 1 && Parser.Int.isParsable(args.get(0)), CommandArgumentException::new);
        int amount = Parser.Int.parse(args.get(0));

        for (int i = 0; i < amount; i++) {
            channel.sendMessageFormat("%d", i).queue();
        }
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_ADMIN;
    }

    @Override
    public String usage() {
        return "count <to>";
    }

    @Override
    public String description() {
        return "Counts from 0 to a n-1";
    }
}
