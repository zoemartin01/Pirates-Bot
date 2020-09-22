package me.zoemartin.bot.modules.baseCommands;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.CommandPerm;
import me.zoemartin.bot.base.interfaces.Command;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

public class Ping implements Command {
    @Override
    public String name() {
        return "ping";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        long time = System.currentTimeMillis();
        Message m = channel.sendMessage("Pong!").complete();

        m.editMessage("Ping: " + (m.getTimeCreated().toInstant().toEpochMilli() - time)
                          + "ms | Websocket: " + Bot.getJDA().getGatewayPing() + "ms").queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public String usage() {
        return "Nothing you dingus";
    }

    @Override
    public String description() {
        return "table tennis?";
    }
}
