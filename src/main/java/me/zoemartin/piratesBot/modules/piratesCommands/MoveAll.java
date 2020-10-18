package me.zoemartin.piratesBot.modules.piratesCommands;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.Parser;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MoveAll implements GuildCommand {
    @Override
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
        Guild g = channel.getGuild();
        Check.check(args.size() >= 2, CommandArgumentException::new);

        VoiceChannel from = Parser.Channel.getVoiceChannel(g, args.get(0));
        String toRef = lastArg(1, args, original);
        VoiceChannel to = Parser.Channel.getVoiceChannel(g, toRef);
        Check.entityReferenceNotNull(from, VoiceChannel.class, args.get(0));
        Check.entityReferenceNotNull(to, VoiceChannel.class, toRef);

        from.getMembers().forEach(member -> g.moveVoiceMember(member, to).queue());
        addCheckmark(original);
    }

    @NotNull
    @Override
    public String name() {
        return "moveall";
    }

    @NotNull
    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_ADMIN;
    }

    @NotNull
    @Override
    public String usage() {
        return "<from> <to>";
    }

    @NotNull
    @Override
    public String description() {
        return "Moves all Members from one VC to another";
    }
}
