package me.zoemartin.bot.modules.trigger;

import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.interfaces.GuildCommand;
import me.zoemartin.bot.base.util.Check;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class TriggerList implements GuildCommand {
    @Override
    public String name() {
        return "list";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        Check.check(args.size() == 1, CommandArgumentException::new);

        Triggers t = Triggers.get(original.getGuild());
        StringBuilder sb = new StringBuilder("All available triggers: \n`");

        // TODO: symbol count check
        t.getTriggers().forEach((s, s2) -> sb.append(s).append(" - ").append(s2).append("\n"));

        sb.deleteCharAt(sb.lastIndexOf("\n"));
        sb.append("`");

        channel.sendMessageFormat(sb.toString()).queue();
    }

    @Override
    public Permission required() {
        return Permission.MANAGE_SERVER;
    }

    @Override
    public String usage() {
        return "trigger list";
    }
}
