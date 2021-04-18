package me.zoemartin.piratesBot.piratesCommands;

import me.zoemartin.rubie.core.CommandPerm;
import me.zoemartin.rubie.core.GuildCommandEvent;
import me.zoemartin.rubie.core.annotations.Command;
import me.zoemartin.rubie.core.annotations.CommandOptions;
import me.zoemartin.rubie.core.exceptions.CommandArgumentException;
import me.zoemartin.rubie.core.exceptions.ReplyError;
import me.zoemartin.rubie.core.interfaces.GuildCommand;
import me.zoemartin.rubie.core.util.Check;
import net.dv8tion.jda.api.Permission;

@Command
@CommandOptions(
    name = "scatter",
    description = "Move members moved with `assemble` back",
    usage = "<id>",
    perm = CommandPerm.BOT_MODERATOR,
    botPerms = Permission.VOICE_MOVE_OTHERS

)
public class Scatter extends GuildCommand {
    @Override
    public void run(GuildCommandEvent event) {
        var args = event.getArgs();
        Check.check(args.size() == 1, CommandArgumentException::new);

        String id = args.get(0);
        Assembly a = Assembly.dissolve(id);
        Check.notNull(a, () -> new ReplyError("Assembly with id `%s` does not exist", id));

        a.getAssembly().forEach(
            (member, voiceChannel) ->
                new Thread(() -> voiceChannel.getGuild().moveVoiceMember(member, voiceChannel).queue()).start());

        event.reply("Scatter",
            "Moved everyone in assembly `%s` back to their original voice channels", id).queue();
    }
}
