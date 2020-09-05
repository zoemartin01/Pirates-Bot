package me.zoemartin.bot.modules.baseCommands;

import me.zoemartin.bot.base.interfaces.GuildCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class Help implements GuildCommand {
    @Override
    public String name() {
        return "help";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        channel.sendMessageFormat("I can't really help you with anything at the moment ¯\\_(ツ)_/¯").queue();
    }

    @Override
    public Permission required() {
        return Permission.UNKNOWN;
    }

    @Override
    public String usage() {
        return "null";
    }
}
