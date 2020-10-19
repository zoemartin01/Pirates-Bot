package me.zoemartin.piratesBot.modules.piratesCommands;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.exceptions.ReplyError;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.Parser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class SpeedDates implements GuildCommand {
    @Override
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.size() == 2, CommandArgumentException::new);
        Guild g = original.getGuild();
        String catRef = lastArg(1, args, original);
        Category cat = g.getCategoriesByName(catRef, true).isEmpty() ?
                           null : g.getCategoriesByName(catRef, true).get(0);

        Check.entityReferenceNotNull(cat, Category.class, catRef);
        VoiceChannel source = Parser.Channel.getVoiceChannel(g, args.get(0));
        Check.entityReferenceNotNull(source, VoiceChannel.class, args.get(0));

        Map<Member, VoiceChannel> distribution =
            RandomGroupsCommand.distributeUsers(source, cat.getVoiceChannels());

        distribution.forEach((key, value) -> g.moveVoiceMember(key, value).queue());

        embedReply(original, channel, "Speed Dating",
            "Distributed %s users from the %s channel to these channels:\n • %s",
            distribution.entrySet().size(), source.getName(),
            cat.getVoiceChannels().stream().map(VoiceChannel::getName)
                .collect(Collectors.joining("\n • "))).queue();
    }

    @NotNull
    @Override
    public Set<Command> subCommands() {
        return Set.of(new Setup(), new Remove());
    }

    @NotNull
    @Override
    public String name() {
        return "speeddating";
    }

    @NotNull
    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_MANAGER;
    }

    @NotNull
    @Override
    public String usage() {
        return "<source channel> <category>";
    }

    @NotNull
    @Override
    public String description() {
        return "Speeddating";
    }

    private static class Setup implements GuildCommand {

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.size() == 3, CommandArgumentException::new);
            Guild g = original.getGuild();

            Role admin = Parser.Role.getRole(g, args.get(1));
            Check.entityReferenceNotNull(admin, Role.class, args.get(1));
            int count = Parser.Int.parse(args.get(2));
            Check.check(count > 0, () -> new ReplyError("Count must be a valid number and bigger than 0!"));

            Category cat = g.getCategoriesByName(args.get(0), true).isEmpty() ?
                               null : g.getCategoriesByName(args.get(0), true).get(0);

            if (cat == null) cat = g.createCategory(args.get(0)).complete();

            cat.putPermissionOverride(admin).setAllow(Permission.VOICE_CONNECT, Permission.MESSAGE_READ).complete();
            cat.putPermissionOverride(g.getPublicRole()).setDeny(Permission.MESSAGE_READ).complete();


            cat.getVoiceChannels().forEach(c -> c.delete().queue());
            cat.getTextChannels().forEach(c -> c.delete().queue());

            for (int i = 0; i < count; i++) {
                Role role = g.createRole().complete();
                role.getManager().setName("sdvc-" + i).complete();
                VoiceChannel voice = cat.createVoiceChannel("sdvc-" + i).complete();
                voice.putPermissionOverride(role).setAllow(Permission.MESSAGE_READ, Permission.VOICE_CONNECT).queue();

                TextChannel text = cat.createTextChannel("sdvc-text-" + i).complete();
                text.putPermissionOverride(role).setAllow(Permission.MESSAGE_READ).queue();

                VoiceRole.addVoiceRoles(g, voice, Collections.singleton(role));
            }

            embedReply(original, channel, "Speedating Setup",
                "Created SpeedDating Category with %d channels", count);
        }

        @NotNull
        @Override
        public String name() {
            return "setup";
        }

        @NotNull
        @Override
        public String usage() {
            return "<category> <admin role> <channel count>";
        }

        @NotNull
        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @NotNull
        @Override
        public String description() {
            return "setup speeddating";
        }
    }

    private static class Remove implements GuildCommand {

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(!args.isEmpty(), CommandArgumentException::new);
            Guild g = original.getGuild();

            String catRef = lastArg(0, args, original);
            Category cat = g.getCategoriesByName(catRef, true).isEmpty() ?
                               null : g.getCategoriesByName(catRef, true).get(0);
            Check.entityReferenceNotNull(cat, Category.class, catRef);

            for (int i = 0; i < cat.getVoiceChannels().size(); i++) {
                Role r = g.getRolesByName("sdvc-" + i, true).get(0);
                VoiceChannel voice = Parser.Channel.getVoiceChannel(g, "sdvc-" + i);
                TextChannel text = Parser.Channel.getTextChannel(g, "sdvc-text-" + i);

                if (r != null) r.delete().queue();
                if (voice != null) voice.delete().queue();
                if (text != null) text.delete().queue();
            }

            cat.delete().queue();

            embedReply(original, channel, "Speedating Deleted",
                "Removed Speed Dating Category");
        }

        @NotNull
        @Override
        public String name() {
            return "remove";
        }

        @NotNull
        @Override
        public String usage() {
            return "<category>";
        }

        @NotNull
        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @NotNull
        @Override
        public String description() {
            return "remove speeddating";
        }
    }
}
