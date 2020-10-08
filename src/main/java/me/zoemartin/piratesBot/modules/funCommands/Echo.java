package me.zoemartin.piratesBot.modules.funCommands;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.*;
import me.zoemartin.piratesBot.core.interfaces.*;
import me.zoemartin.piratesBot.core.util.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Echo implements GuildCommand {
    @Override
    public @NotNull Set<Command> subCommands() {
        return Set.of(new To(), new Edit());
    }

    @Override
    public @NotNull String name() {
        return "echo";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
        Check.check(!args.isEmpty(), CommandArgumentException::new);

        String echo = lastArg(0, args, original);

        channel.sendMessageFormat(MessageUtils.cleanMessage(original.getMember(), echo)).queue();
        original.addReaction("U+2705").queue();
    }

    @Override
    public @NotNull CommandPerm commandPerm() {
        return CommandPerm.BOT_MANAGER;
    }

    @Override
    public @NotNull String usage() {
        return "<message...>";
    }

    @Override
    public @NotNull String description() {
        return "Makes the bot say stuff";
    }

    @SuppressWarnings("ConstantConditions")
    private static class To implements GuildCommand {
        @Override
        public @NotNull String name() {
            return "to";
        }

        @Override
        public @NotNull String regex() {
            return ">>|to";
        }

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.size() > 1 && Parser.Channel.isParsable(args.get(0)),
                CommandArgumentException::new);

            TextChannel c = original.getGuild().getTextChannelById(Parser.Channel.parse(args.get(0)));
            Check.entityNotNull(c, TextChannel.class);
            Check.check(original.getMember().hasPermission(c, Permission.MESSAGE_WRITE),
                () -> new ConsoleError("Member '%s' doesn't have write permissions in channel '%s'",
                    original.getMember().getId(), c.getId()));

            StringBuilder sb = new StringBuilder();
            Check.notNull(c, () -> new ReplyError("Channel '%s' does not exist", args.get(1)));

            args.subList(1, args.size()).forEach(s -> sb.append(s).append(" "));

            c.sendMessageFormat(MessageUtils.cleanMessage(original.getMember(), sb.toString())).queue();
            original.addReaction("U+2705").queue();
        }

        @Override
        public @NotNull CommandPerm commandPerm() {
            return CommandPerm.BOT_MANAGER;
        }

        @Override
        public @NotNull Collection<Permission> required() {
            return Set.of(Permission.MESSAGE_MANAGE);
        }


        @Override
        public @NotNull String usage() {
            return "<#channel> <message...>";
        }

        @Override
        public @NotNull String description() {
            return "Makes the bot say stuff in a different channel";
        }
    }

    private static class Edit implements GuildCommand {
        @Override
        public @NotNull String name() {
            return "--edit";
        }

        @Override
        public @NotNull String regex() {
            return "-e|--edit";
        }

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.size() > 1 && Parser.Message.isParsable(args.get(0)), CommandArgumentException::new);

            String message, channelId;
            channelId = Parser.Message.parse(args.get(0)).getLeft();
            message = Parser.Message.parse(args.get(0)).getRight();

            TextChannel c = original.getGuild().getTextChannelById(channelId);
            Check.entityNotNull(c, TextChannel.class);
            Message m = c.retrieveMessageById(message).complete();
            Check.entityNotNull(m, Message.class);

            Check.check(m.getAuthor().getId().equals(Bot.getJDA().getSelfUser().getId()), UnexpectedError::new);

            StringBuilder sb = new StringBuilder();
            args.subList(1, args.size()).forEach(s -> sb.append(s).append(" "));

            m.editMessage(sb.toString()).queue();
            original.addReaction("U+2705").queue();
        }

        @Override
        public @NotNull CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public @NotNull String usage() {
            return "<message_link> <message...>";
        }

        @Override
        public @NotNull String description() {
            return "Edits an echoed message";
        }
    }
}
