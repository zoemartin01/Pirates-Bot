package me.zoemartin.piratesBot.modules.piratesCommands;

import me.zoemartin.rubie.core.CommandPerm;
import me.zoemartin.rubie.core.GuildCommandEvent;
import me.zoemartin.rubie.core.annotations.Command;
import me.zoemartin.rubie.core.annotations.CommandOptions;
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
    name = "assemble",
    description = "Move all Members with a role currently in a voice chat into one voice chat",
    usage = "<here|#channel> <@role>",
    perm = CommandPerm.BOT_MODERATOR,
    botPerms = Permission.VOICE_MOVE_OTHERS
)
public class Assemble extends GuildCommand {

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run(GuildCommandEvent event) {
        var guild = event.getGuild();
        var args = event.getArgs();
        Check.check(args.size() >= 2, CommandArgumentException::new);

        VoiceChannel vc;
        String vcRef = args.get(0);
        if (vcRef.equalsIgnoreCase("here")) {
            Member m = event.getMember();
            Check.check(m.getVoiceState() != null && m.getVoiceState().inVoiceChannel(),
                () -> new ReplyError("Error, option `here` cannot be invoked without being connected to a voice chat"));
            vc = m.getVoiceState().getChannel();
        } else {
            vc = Parser.Channel.getVoiceChannel(guild, vcRef);
        }
        Check.entityReferenceNotNull(vc, VoiceChannel.class, vcRef);

        String roleRef = lastArg(1, event);
        Role r = Parser.Role.getRole(guild, roleRef);
        Check.entityReferenceNotNull(r, Role.class, roleRef);

        Set<Member> toMove = new HashSet<>();

        guild.getVoiceChannels()
            .forEach(voiceChannel -> toMove.addAll(
                voiceChannel.getMembers().stream()
                    .filter(member -> member.getRoles().contains(r))
                    .collect(Collectors.toSet())));

        Assembly a = new Assembly();

        toMove.forEach(member -> new Thread(() -> {
            a.add(member, Objects.requireNonNull(member.getVoiceState()).getChannel());
            guild.moveVoiceMember(member, vc).queue();
        }).start());

        event.reply( "Assembly",
            "Moved everyone with the role %s to the voice channel `%s`. \nAssembly id: `%s`",
            r.getAsMention(), vc.getName(), Assembly.addAssembly(a)).queue();
    }
}
