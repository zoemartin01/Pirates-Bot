package me.zoemartin.bot.modules.trigger;

import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.exceptions.ReplyError;
import me.zoemartin.bot.base.interfaces.Command;
import me.zoemartin.bot.base.util.Check;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Trigger implements Command {
    @Override
    public String name() {
        return "trigger";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        Check.check(args.size() > 1, CommandArgumentException::new);

        Triggers t = Triggers.get(original.getGuild());

        if (args.get(0).toLowerCase().matches("list")) {
            Check.check(args.size() == 1, CommandArgumentException::new);

            StringBuilder sb = new StringBuilder("All available triggers: \n`");

            // TODO: symbol count check
            t.getTriggers().forEach((s, s2) -> sb.append(s).append(" - ").append(s2).append("\n"));

            sb.deleteCharAt(sb.lastIndexOf("\n"));
            sb.append("`");

            channel.sendMessageFormat(sb.toString()).queue();

        } else if (args.get(0).toLowerCase().matches("remove|delete|del")) {
            Check.check(args.size() == 2, CommandArgumentException::new);
            Check.check(t.isTrigger(args.get(1)), () -> new ReplyError("That trigger does not exist!"));

            channel.sendMessageFormat("Removed the trigger `%s`", t.removeTrigger(args.get(1))).queue();
        } else {
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
    }

    @Override
    public Permission required() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public String usage() {
        return "Usage: `trigger <regex> <output...>` \n or `trigger remove <regex>` \n or `trigger list`";
    }
}
