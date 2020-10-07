package me.zoemartin.piratesBot.modules.piratesCommands;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.exceptions.ReplyError;
import me.zoemartin.piratesBot.core.interfaces.*;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.Parser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

public class Scatter implements GuildCommand {
    @Override
    public Set<Command> subCommands() {
        return Collections.emptySet();
    }

    @Override
    public String name() {
        return "scatter";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.size() == 1, CommandArgumentException::new);

        Integer id = Parser.Int.parse(args.get(0));
        Check.notNull(id, CommandArgumentException::new);

        Assembly a = Assembly.dissolve(id);
        Check.notNull(a, () -> new ReplyError("Assembly with id `%s` does not exist", id));

        a.getAssembly().forEach(
            (member, voiceChannel) ->
                new Thread(() -> voiceChannel.getGuild().moveVoiceMember(member, voiceChannel).queue()).start());

        embedReply(original, channel, "Scatter",
            "Moved everyone in assembly `%s` back to their original voice channels", id).queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_MODERATOR;
    }

    @Override
    public Collection<Permission> required() {
        return Set.of(Permission.VOICE_MOVE_OTHERS);
    }

    @Override
    public String usage() {
        return "<id>";
    }

    @Override
    public String description() {
        return "Move members moved with `assemble` back";
    }
}
