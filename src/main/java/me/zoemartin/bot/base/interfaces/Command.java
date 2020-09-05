package me.zoemartin.bot.base.interfaces;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.List;

public interface Command {
    String name();
    String run(User user, MessageChannel channel, List<String> args, Message original);
    Permission required();
    String usage();
}
