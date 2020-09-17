package me.zoemartin.bot.modules.piratesCommands;

import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.exceptions.ReplyError;
import me.zoemartin.bot.base.interfaces.*;
import me.zoemartin.bot.base.util.Check;
import me.zoemartin.bot.base.util.Parser;
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
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        Check.check(args.size() == 1, CommandArgumentException::new);

        Integer id = Parser.Int.parse(args.get(0));
        Check.notNull(id, CommandArgumentException::new);

        Assembly a = Assembly.dissolve(id);
        Check.notNull(a, () -> new ReplyError("Assembly with id `%s` does not exist", id));

        a.getAssembly().forEach(
            (member, voiceChannel) ->
                new Thread(() -> voiceChannel.getGuild().moveVoiceMember(member, voiceChannel).queue()).start());

        channel.sendMessageFormat("Moved everyone in assembly `%s` back to their original voice channels",
            id).queue();
    }

    @Override
    public Permission required() {
        return Permission.MANAGE_SERVER;
    }

    @Override
    public String usage() {
        return "scatter <id>";
    }
}
