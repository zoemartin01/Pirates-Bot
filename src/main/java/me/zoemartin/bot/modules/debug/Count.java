package me.zoemartin.bot.modules.debug;

import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.interfaces.Command;
import me.zoemartin.bot.base.util.Check;
import me.zoemartin.bot.base.util.Parser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class Count implements Command {
    @Override
    public String name() {
        return "count";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        Check.check(args.size() == 1 && Parser.Int.isParsable(args.get(0)), CommandArgumentException::new);
        int amount = Parser.Int.parse(args.get(0));

        for (int i = 0; i < amount; i++) {
            channel.sendMessageFormat("%d", i).queue();
        }
    }

    @Override
    public Permission required() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public String usage() {
        return "count <to>";
    }
}
