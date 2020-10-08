package me.zoemartin.piratesBot.modules.piratesCommands;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.exceptions.ReplyError;
import me.zoemartin.piratesBot.core.interfaces.*;
import me.zoemartin.piratesBot.core.util.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class Assemble implements GuildCommand {
    @Override
    public @NotNull Set<Command> subCommands() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull String name() {
        return "assemble";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
        Guild g = channel.getGuild();

        Check.check(args.size() >= 2, CommandArgumentException::new);

        VoiceChannel vc;
        String vcRef = args.get(0);
        if (vcRef.equalsIgnoreCase("here")) {
            Member m = original.getMember();
            Check.check(m.getVoiceState() != null && m.getVoiceState().inVoiceChannel(),
                () -> new ReplyError("Error, option `here` cannot be invoked without being connected to a voice chat"));
            vc = m.getVoiceState().getChannel();
        } else {
            vc = Parser.Channel.getVoiceChannel(original.getGuild(), vcRef);
        }
        Check.entityReferenceNotNull(vc, VoiceChannel.class, vcRef);

        String roleRef = lastArg(1, args, original);
        Role r = Parser.Role.getRole(original.getGuild(), roleRef);
        Check.entityReferenceNotNull(r, Role.class, roleRef);

        Set<Member> toMove = new HashSet<>();

        g.getVoiceChannels()
            .forEach(voiceChannel -> toMove.addAll(
                voiceChannel.getMembers().stream()
                    .filter(member -> member.getRoles().contains(r))
                    .collect(Collectors.toSet())));

        Assembly a = new Assembly();

        toMove.forEach(member -> new Thread(() -> {
            a.add(member, Objects.requireNonNull(member.getVoiceState()).getChannel());
            g.moveVoiceMember(member, vc).queue();
        }).start());

        embedReply(original, channel, "Assembly",
            "Moved everyone with the role %s to the voice channel `%s`. \nAssembly id: `%s`",
            r.getAsMention(), vc.getName(), Assembly.addAssembly(a)).queue();
    }

    @Override
    public @NotNull CommandPerm commandPerm() {
        return CommandPerm.BOT_MODERATOR;
    }

    @Override
    public @NotNull Collection<Permission> required() {
        return Set.of(Permission.VOICE_MOVE_OTHERS);
    }

    @Override
    public @NotNull String usage() {
        return "<here|#channel> <@role>";
    }

    @Override
    public @NotNull String description() {
        return "Move all Members with a role currently in a voice chat into one voice chat";
    }
}
