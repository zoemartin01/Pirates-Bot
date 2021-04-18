package me.zoemartin.piratesBot.piratesCommands;

import me.zoemartin.rubie.core.CommandPerm;
import me.zoemartin.rubie.core.GuildCommandEvent;
import me.zoemartin.rubie.core.annotations.Command;
import me.zoemartin.rubie.core.annotations.CommandOptions;
import me.zoemartin.rubie.core.exceptions.CommandArgumentException;
import me.zoemartin.rubie.core.interfaces.GuildCommand;
import me.zoemartin.rubie.core.util.Check;
import me.zoemartin.rubie.core.util.Parser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.VoiceChannel;

@Command
@CommandOptions(
    name = "moveall",
    usage = "<from> <to>",
    description = "Moves all Members from one VC to another",
    perm = CommandPerm.BOT_ADMIN,
    botPerms = Permission.VOICE_MOVE_OTHERS
)
public class MoveAll extends GuildCommand {
    @Override
    public void run(GuildCommandEvent event) {
        var guild = event.getGuild();
        var args = event.getArgs();
        Check.check(args.size() >= 2, CommandArgumentException::new);

        VoiceChannel from = Parser.Channel.getVoiceChannel(guild, args.get(0));
        String toRef = lastArg(1, event);
        VoiceChannel to = Parser.Channel.getVoiceChannel(guild, toRef);
        Check.entityReferenceNotNull(from, VoiceChannel.class, args.get(0));
        Check.entityReferenceNotNull(to, VoiceChannel.class, toRef);

        from.getMembers().forEach(member -> guild.moveVoiceMember(member, to).queue());
        event.addCheckmark();
    }
}
