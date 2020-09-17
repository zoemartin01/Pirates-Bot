package me.zoemartin.bot.modules.piratesCommands;

import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.exceptions.ReplyError;
import me.zoemartin.bot.base.interfaces.*;
import me.zoemartin.bot.base.util.Check;
import me.zoemartin.bot.base.util.Parser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.stream.Collectors;

public class Assemble implements GuildCommand {
    @Override
    public Set<Command> subCommands() {
        return Collections.emptySet();
    }

    @Override
    public String name() {
        return "assemble";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Guild g = ((GuildChannel) channel).getGuild();

        Check.check(args.size() == 2, CommandArgumentException::new);
        Check.check(Parser.Channel.isParsable(args.get(0)), CommandArgumentException::new);
        Check.check(Parser.Role.isParsable(args.get(1)), CommandArgumentException::new);

        VoiceChannel vc = g.getVoiceChannelById(Parser.Channel.parse(args.get(0)));

        Check.check(vc != null, () -> new ReplyError("Channel '%s' does not exist", args.get(0)));

        Role r = g.getRoleById(Parser.Role.parse(args.get(1)));
        Check.check(r != null, () -> new ReplyError("Role '%s' does not exist", args.get(1)));

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

        channel.sendMessageFormat("Moved everyone with the role %s to the voice channel `%s`. Assembly id: `%s`",
            r.getAsMention(), vc.getName(), Assembly.addAssembly(a))
            .queue();
    }

    @Override
    public Permission required() {
        return Permission.MANAGE_SERVER;
    }

    @Override
    public String usage() {
        return "`assemble <here|#channel> <@role>`";
    }

    @Override
    public String description() {
        return "Move all Members with a role currently in a voice chat into one voice chat";
    }
}
