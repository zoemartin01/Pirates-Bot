package me.zoemartin.bot.modules.debug;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.exceptions.ConsoleError;
import me.zoemartin.bot.base.interfaces.Command;
import me.zoemartin.bot.base.util.Check;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

public class Shutdown implements Command {
    @Override
    public Set<Command> subCommands() {
        return Collections.emptySet();
    }

    @Override
    public String name() {
        return "shutdown";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(!user.getId().equals("212591138945630213"),
            () -> new ConsoleError("Non-owner tried a shutdown"));

        Check.check(args.isEmpty() || args.size() == 1 && args.get(0).matches("force|now"),
            CommandArgumentException::new);

        if (args.isEmpty()) {
            channel.sendMessageFormat("Shutting down soon! :)").queue();
            Bot.getJDA().shutdown();
        } else {
            channel.sendMessageFormat("Shutting down now!").complete();
            Bot.getJDA().shutdownNow();
        }
    }

    @Override
    public Permission required() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public String usage() {
        return "`shutdown [force|now]`";
    }

    @Override
    public String description() {
        return "Shuts down the bot";
    }
}
