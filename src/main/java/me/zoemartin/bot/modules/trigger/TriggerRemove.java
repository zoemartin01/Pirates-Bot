package me.zoemartin.bot.modules.trigger;

import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.exceptions.ReplyError;
import me.zoemartin.bot.base.interfaces.GuildCommand;
import me.zoemartin.bot.base.util.Check;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class TriggerRemove implements GuildCommand {
    @Override
    public String name() {
        return "remove";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        Triggers t = Triggers.get(original.getGuild());

        Check.check(args.size() == 1, CommandArgumentException::new);
        Check.check(t.isTrigger(args.get(0)), () -> new ReplyError("That trigger does not exist!"));

        channel.sendMessageFormat("Removed the trigger `%s`", t.removeTrigger(args.get(0))).queue();
    }

    @Override
    public Permission required() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public String usage() {
        return "trigger remove <regex>";
    }
}
