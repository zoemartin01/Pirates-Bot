package me.zoemartin.piratesBot.modules.funCommands;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.LoadModule;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.*;
import me.zoemartin.piratesBot.core.interfaces.Module;
import me.zoemartin.piratesBot.core.managers.CommandManager;
import me.zoemartin.piratesBot.core.util.*;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

@LoadModule
public class Status implements Module, GuildCommand {
    @Override
    public Set<Command> subCommands() {
        return Collections.emptySet();
    }

    @Override
    public String name() {
        return "status";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.size() >= 2, CommandArgumentException::new);

        int id = Parser.Int.parse(args.get(0));
        Check.notNull(id, CommandArgumentException::new);

        Activity.ActivityType type = Activity.ActivityType.fromKey(id);

        Bot.getJDA().getPresence().setActivity(Activity.of(type, lastArg(1, args, original)));
        channel.sendMessageFormat("Set bot status to `%s %s`", Bot.getJDA().getPresence().getActivity().getType(),
            Bot.getJDA().getPresence().getActivity()).queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.OWNER;
    }

    @Override
    public String usage() {
        return "<type id> <status...>";
    }

    @Override
    public String description() {
        return "Sets the bot's status";
    }

    @Override
    public void init() {
        CommandManager.register(new Status());
        CommandManager.register(new Echo());
    }
}
