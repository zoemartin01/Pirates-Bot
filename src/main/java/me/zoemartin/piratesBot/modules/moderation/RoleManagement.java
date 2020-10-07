package me.zoemartin.piratesBot.modules.moderation;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.exceptions.ReplyError;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class RoleManagement implements GuildCommand {
    @Override
    public Set<Command> subCommands() {
        return Set.of(new Add(), new Remove());
    }

    @Override
    public String name() {
        return "role";
    }

    @Override
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
        help(user, channel, Collections.singletonList(name()), original);
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_MANAGER;
    }

    @Override
    public Collection<Permission> required() {
        return Set.of(Permission.MANAGE_ROLES);
    }

    @Override
    public String description() {
        return "Role Management";
    }

    private static class Add implements GuildCommand {

        @Override
        public String name() {
            return "add";
        }

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.size() >= 2, CommandArgumentException::new);

            Guild g = original.getGuild();
            Member m = CacheUtils.getMember(g, args.get(0));
            Role r = Parser.Role.getRole(g, lastArg(1, args, original));

            Check.entityNotNull(m, Member.class);
            Check.entityNotNull(r, Role.class);
            g.addRoleToMember(m, r).queue();

            addCheckmark(original);
            embedReply(original, channel, "Role Management", "Added %s to %s", m.getAsMention(),
                r.getAsMention()).queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_MANAGER;
        }

        @Override
        public Collection<Permission> required() {
            return Set.of(Permission.MANAGE_ROLES);
        }

        @Override
        public String usage() {
            return "<@user> <role>";
        }

        @Override
        public String description() {
            return "Adds a role to a user";
        }
    }

    private static class Remove implements GuildCommand {

        @Override
        public String name() {
            return "remove";
        }

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.size() >= 2, CommandArgumentException::new);

            Guild g = original.getGuild();
            Member m = CacheUtils.getMember(g, args.get(0));
            Role r = Parser.Role.getRole(g, lastArg(1, args, original));

            Check.entityNotNull(m, Member.class);
            Check.entityNotNull(r, Role.class);
            Check.check(m.getRoles().contains(r), () -> new ReplyError("Member does not have that role"));
            g.removeRoleFromMember(m, r).queue();

            addCheckmark(original);
            embedReply(original, channel, "Role Management", "Removed %s from %s", m.getAsMention(),
                r.getAsMention()).queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_MANAGER;
        }

        @Override
        public Collection<Permission> required() {
            return Set.of(Permission.MANAGE_ROLES);
        }

        @Override
        public String description() {
            return "Removes a role from a user";
        }
    }
}
