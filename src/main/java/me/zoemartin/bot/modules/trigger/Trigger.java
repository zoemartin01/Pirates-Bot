package me.zoemartin.bot.modules.trigger;

import me.zoemartin.bot.base.CommandPerm;
import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.exceptions.ReplyError;
import me.zoemartin.bot.base.interfaces.Command;
import me.zoemartin.bot.base.interfaces.GuildCommand;
import me.zoemartin.bot.base.util.Check;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Trigger implements Command {
    @Override
    public Set<Command> subCommands() {
        return Set.of(new TList(), new Remove());
    }

    @Override
    public String name() {
        return "trigger";
    }

    @Override
    public String regex() {
        return "trigger|autoresponse|ar";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
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
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_MANAGER;
    }

    @Override
    public String usage() {
        return "`trigger <regex> <output...>` or \n`trigger remove <regex>` or \n`trigger list`";
    }

    @Override
    public String description() {
        return "Create/List/Remove a regex message trigger";
    }

    private static class TList implements GuildCommand {
        @Override
        public String name() {
            return "list";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            Triggers t = Triggers.get(original.getGuild());

            if (t.getTriggers().isEmpty()) {
                channel.sendMessageFormat("No triggers available!").queue();
                return;
            }

            StringBuilder sb = new StringBuilder("All available triggers: \n`");

            // TODO: symbol count check
            t.getTriggers().forEach((s, s2) -> sb.append(s).append(" - ").append(s2).append("\n"));

            sb.deleteCharAt(sb.lastIndexOf("\n"));
            sb.append("`");

            channel.sendMessageFormat(sb.toString()).queue();
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_MODERATOR;
        }

        @Override
        public String usage() {
            return "`trigger list`";
        }

        @Override
        public String description() {
            return "Lists all triggers";
        }
    }

    private static class Remove implements GuildCommand {
        @Override
        public String name() {
            return "remove";
        }

        @Override
        public String regex() {
            return "remove|delete|del|rem|rm";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            Triggers t = Triggers.get(original.getGuild());

            Check.check(args.size() == 1, CommandArgumentException::new);
            Check.check(t.isTrigger(args.get(0)), () -> new ReplyError("That trigger does not exist!"));

            channel.sendMessageFormat("Removed the trigger `%s`", t.removeTrigger(args.get(0))).queue();
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_MANAGER;
        }

        @Override
        public String usage() {
            return "`trigger remove <regex>`";
        }

        @Override
        public String description() {
            return "Deletes a trigger";
        }
    }
}
