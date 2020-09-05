package me.zoemartin.bot.modules.debug;

import me.zoemartin.bot.base.exceptions.ConsoleError;
import me.zoemartin.bot.base.interfaces.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class Sleep implements Command {
    @Override
    public String name() {
        return "sleep";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        channel.sendMessageFormat("Started sleeping").queue();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new ConsoleError(e.getMessage());
        }
        channel.sendMessageFormat("Ended sleeping").queue();
    }

    @Override
    public Permission required() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public String usage() {
        return "Usage: sleep";
    }
}
