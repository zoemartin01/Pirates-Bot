package me.zoemartin.bot.modules.funCommands;

import me.zoemartin.bot.base.CommandPerm;
import me.zoemartin.bot.base.exceptions.*;
import me.zoemartin.bot.base.interfaces.*;
import me.zoemartin.bot.base.util.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

public class Echo implements GuildCommand {
    @Override
    public Set<Command> subCommands() {
        return Set.of(new To());
    }

    @Override
    public String name() {
        return "echo";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(!args.isEmpty(), CommandArgumentException::new);

        StringBuilder sb = new StringBuilder();
        args.forEach(s -> sb.append(s).append(" "));

        channel.sendMessageFormat(MessageUtils.cleanMessage(original.getMember(), sb.toString())).queue();
        original.addReaction("U+2705").queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_MANAGER;
    }

    @Override
    public String usage() {
        return "echo <message...>";
    }

    @Override
    public String description() {
        return "Makes the bot say stuff";
    }

    @SuppressWarnings("ConstantConditions")
    private static class To implements GuildCommand {
        @Override
        public String name() {
            return "to";
        }

        @Override
        public String regex() {
            return ">>|to";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.size() > 1 && Parser.Channel.isParsable(args.get(0)),
                CommandArgumentException::new);

            TextChannel c = original.getGuild().getTextChannelById(Parser.Channel.parse(args.get(0)));
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
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_MANAGER;
        }

        @Override
        public Collection<Permission> required() {
            return Set.of(Permission.MESSAGE_MANAGE);
        }


        @Override
        public String usage() {
            return "echo >> #channel <message...>";
        }

        @Override
        public String description() {
            return "Makes the bot say stuff in a different channel";
        }
    }
}
