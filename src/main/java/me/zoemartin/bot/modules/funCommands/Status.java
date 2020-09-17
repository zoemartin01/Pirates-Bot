package me.zoemartin.bot.modules.funCommands;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.interfaces.*;
import me.zoemartin.bot.base.interfaces.Module;
import me.zoemartin.bot.base.managers.CommandManager;
import me.zoemartin.bot.base.util.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

@LoadModule
public class Status implements Module, Command {
    @Override
    public Set<Command> subCommands() {
        return Collections.emptySet();
    }

    @Override
    public String name() {
        return "status";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        Check.check(args.size() >= 2, CommandArgumentException::new);

        int id = Parser.Int.parse(args.get(0));
        Check.notNull(id, CommandArgumentException::new);

        Activity.ActivityType type = Activity.ActivityType.fromKey(id);

        StringBuilder sb = new StringBuilder();

        args.subList(1, args.size()).forEach(s -> sb.append(s).append(" "));
        sb.deleteCharAt(sb.lastIndexOf(" "));

        Bot.getJDA().getPresence().setActivity(Activity.of(type, sb.toString()));
        channel.sendMessageFormat("Set bot status to `%s %s`", Bot.getJDA().getPresence().getActivity().getType(),
            Bot.getJDA().getPresence().getActivity()).queue();
    }

    @Override
    public Permission required() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public String usage() {
        return "status <type id> <status...>";
    }

    @Override
    public void init() {
        CommandManager.register(new Status());
        CommandManager.register(new Echo());
    }
}
