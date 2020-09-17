package me.zoemartin.bot.modules.trigger;

import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.exceptions.ReplyError;
import me.zoemartin.bot.base.interfaces.Command;
import me.zoemartin.bot.base.util.Check;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Trigger implements Command {
    @Override
    public Set<Command> subCommands() {
        return Set.of(new TriggerList(), new TriggerRemove());
    }

    @Override
    public String name() {
        return "trigger";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        Check.check(args.size() > 1, CommandArgumentException::new);

        Triggers t = Triggers.get(original.getGuild());

        String regex = args.get(0);
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException exception) {
            throw new ReplyError("That regex is not valid!");
        }

        String message = original.getContentRaw()
                             .substring(original.getContentRaw().indexOf(regex) + regex.length() + 1);

        t.addTrigger(regex, message);
        channel.sendMessageFormat("Successfully added trigger `%s`", regex).queue();

    }

    @Override
    public Permission required() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public String usage() {
        return "`trigger <regex> <output...>` or \n`trigger remove <regex>` or \n`trigger list`";
    }
}
