package me.zoemartin.piratesBot.modules.trigger;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.*;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.EmbedUtil;
import me.zoemartin.piratesBot.modules.pagedEmbeds.PageListener;
import me.zoemartin.piratesBot.modules.pagedEmbeds.PagedEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class TriggerCommand implements GuildCommand {
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
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.size() > 1, CommandArgumentException::new);

        String regex = args.get(0);
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException exception) {
            throw new ReplyError("That regex is not valid!");
        }

        String message = lastArg(1, args, original);

        Triggers.addTrigger(original.getGuild(), regex, message);
        channel.sendMessageFormat("Successfully added trigger `%s`", regex).queue();

    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_MANAGER;
    }

    @Override
    public String usage() {
        return "<regex> <output...>";
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
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.isEmpty(), CommandArgumentException::new);
            Check.check(Triggers.hasTriggers(original.getGuild()),
                () -> new EntityNotFoundException("No triggers found!"));

            PagedEmbed p = new PagedEmbed(EmbedUtil.pagedDescription(
                new EmbedBuilder().setTitle("Available Triggers").build(), Triggers.getTriggers(
                    original.getGuild()).stream().map(t -> String.format("`%s` - `%s`\n",
                    t.getRegex(), t.getOutput())).collect(Collectors.toList())),
                channel, user.getUser());

            PageListener.add(p);
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_MODERATOR;
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
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.size() == 1, CommandArgumentException::new);
            Check.check(Triggers.isTrigger(original.getGuild(), args.get(0)),
                () -> new ReplyError("That trigger does not exist!"));

            channel.sendMessageFormat("Removed the trigger `%s`", Triggers.removeTrigger(original.getGuild(),
                args.get(0))).queue();
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_MANAGER;
        }

        @Override
        public String usage() {
            return "<regex>";
        }

        @Override
        public String description() {
            return "Deletes a trigger";
        }
    }
}
