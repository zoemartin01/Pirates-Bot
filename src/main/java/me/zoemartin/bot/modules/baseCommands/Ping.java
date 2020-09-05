package me.zoemartin.bot.modules.baseCommands;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.interfaces.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class Ping implements Command {
    @Override
    public String name() {
        return "ping";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        long time = System.currentTimeMillis();
        Message m = channel.sendMessage("Pong!").complete();

        m.editMessage("Ping: " + (m.getTimeCreated().toInstant().toEpochMilli() - time)
                          + "ms | Websocket: " + Bot.getJDA().getGatewayPing() + "ms").queue();
    }

    @Override
    public Permission required() {
        return Permission.UNKNOWN;
    }

    @Override
    public String usage() {
        return "Nothing you dingus";
    }
}
