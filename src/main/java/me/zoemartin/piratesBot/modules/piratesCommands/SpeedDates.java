package me.zoemartin.piratesBot.modules.piratesCommands;

import me.zoemartin.rubie.core.CommandPerm;
import me.zoemartin.rubie.core.GuildCommandEvent;
import me.zoemartin.rubie.core.annotations.*;
import me.zoemartin.rubie.core.exceptions.CommandArgumentException;
import me.zoemartin.rubie.core.exceptions.ReplyError;
import me.zoemartin.rubie.core.interfaces.GuildCommand;
import me.zoemartin.rubie.core.util.Check;
import me.zoemartin.rubie.core.util.Parser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.stream.Collectors;

@Command
@CommandOptions(
    name = "speeddating",
    description = "Speeddating",
    usage = "<source channel> <admin role> <category>",
    perm = CommandPerm.BOT_MANAGER,
    botPerms = Permission.VOICE_MOVE_OTHERS

)
public class SpeedDates extends GuildCommand {
    @Override
    public void run(GuildCommandEvent event) {
        var args = event.getArgs();
        Check.check(args.size() >= 3, CommandArgumentException::new);
        var guild = event.getGuild();
        Role admin = Parser.Role.getRole(guild, args.get(1));
        String catRef = lastArg(2, event);
        Category cat = guild.getCategoriesByName(catRef, true).isEmpty() ?
                           null : guild.getCategoriesByName(catRef, true).get(0);

        Check.entityReferenceNotNull(admin, Role.class, args.get(1));
        Check.entityReferenceNotNull(cat, Category.class, catRef);
        VoiceChannel source = Parser.Channel.getVoiceChannel(guild, args.get(0));
        Check.entityReferenceNotNull(source, VoiceChannel.class, args.get(0));

        Map<Member, VoiceChannel> distribution = distribute(source, cat.getVoiceChannels(), admin);

        distribution.forEach((key, value) -> guild.moveVoiceMember(key, value).queue());

        event.reply("Speed Dating",
            "Distributed %s users from the %s channel to these channels:\n • %s",
            distribution.entrySet().size(), source.getName(),
            cat.getVoiceChannels().stream().map(VoiceChannel::getName)
                .collect(Collectors.joining("\n • "))).queue();
    }

    private Map<Member, VoiceChannel> distribute(VoiceChannel from, List<VoiceChannel> to, Role admin) {
        List<Member> membersToDistribute = from.getMembers().stream()
                                               .filter(member -> !member.getUser().isBot())
                                               .filter(member -> !member.getRoles().contains(admin))
                                               .collect(Collectors.toList());

        Collections.shuffle(membersToDistribute);

        Map<Member, VoiceChannel> memberChannels = new HashMap<>();

        for (int i = 0; i < membersToDistribute.size(); i++) {
            Member member = membersToDistribute.get(i);
            VoiceChannel target = to.get(i % to.size());
            memberChannels.put(member, target);
        }

        return memberChannels;
    }

    @SubCommand(SpeedDates.class)
    @CommandOptions(
        name = "setup",
        description = "setup speeddating",
        usage = "<category> <admin role> <channel count>",
        perm = CommandPerm.BOT_ADMIN
    )
    private static class Setup extends GuildCommand {
        @Override
        public void run(GuildCommandEvent event) {
            var args = event.getArgs();
            Check.check(args.size() == 3, CommandArgumentException::new);
            var guild = event.getGuild();

            Role admin = Parser.Role.getRole(guild, args.get(1));
            Check.entityReferenceNotNull(admin, Role.class, args.get(1));
            int count = Parser.Int.parse(args.get(2));
            Check.check(count > 0, () -> new ReplyError("Count must be a valid number and bigger than 0!"));

            Category cat = guild.getCategoriesByName(args.get(0), true).isEmpty() ?
                               null : guild.getCategoriesByName(args.get(0), true).get(0);

            if (cat == null) cat = guild.createCategory(args.get(0)).complete();

            cat.putPermissionOverride(admin).setAllow(Permission.VOICE_CONNECT, Permission.MESSAGE_READ).complete();
            cat.putPermissionOverride(guild.getPublicRole()).setDeny(Permission.MESSAGE_READ).complete();


            cat.getVoiceChannels().forEach(c -> c.delete().queue());
            cat.getTextChannels().forEach(c -> c.delete().queue());

            for (int i = 0; i < count; i++) {
                Role role = guild.createRole().complete();
                role.getManager().setName("sdvc-" + i).complete();
                VoiceChannel voice = cat.createVoiceChannel("sdvc-" + i).complete();
                voice.putPermissionOverride(role).setAllow(Permission.MESSAGE_READ, Permission.VOICE_CONNECT).queue();

                TextChannel text = cat.createTextChannel("sdvc-text-" + i).complete();
                text.putPermissionOverride(role).setAllow(Permission.MESSAGE_READ).queue();

                VoiceRole.addVoiceRoles(guild, voice, Collections.singleton(role));
            }

            event.reply("Speedating Setup",
                "Created SpeedDating Category with %d channels", count);
        }
    }

    @SubCommand(SpeedDates.class)
    @CommandOptions(
        name = "remove",
        description = "remove speeddating",
        usage = "<category>",
        perm = CommandPerm.BOT_ADMIN
    )
    private static class Remove extends GuildCommand {
        @Override
        public void run(GuildCommandEvent event) {
            var args = event.getArgs();
            Check.check(!args.isEmpty(), CommandArgumentException::new);
            var guild = event.getGuild();

            String catRef = lastArg(0, event);
            Category cat = guild.getCategoriesByName(catRef, true).isEmpty() ?
                               null : guild.getCategoriesByName(catRef, true).get(0);
            Check.entityReferenceNotNull(cat, Category.class, catRef);

            for (int i = 0; i < cat.getVoiceChannels().size(); i++) {
                Role r = guild.getRolesByName("sdvc-" + i, true).get(0);
                VoiceChannel voice = Parser.Channel.getVoiceChannel(guild, "sdvc-" + i);
                TextChannel text = Parser.Channel.getTextChannel(guild, "sdvc-text-" + i);

                if (r != null) r.delete().queue();
                if (voice != null) voice.delete().queue();
                if (text != null) text.delete().queue();
            }

            cat.delete().queue();

            event.reply("Speedating Deleted",
                "Removed Speed Dating Category");
        }
    }
}
