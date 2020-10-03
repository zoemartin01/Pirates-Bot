package me.zoemartin.piratesBot.modules.baseCommands;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.*;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.managers.CommandManager;
import me.zoemartin.piratesBot.core.util.Check;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Usage implements GuildCommand {
    @Override
    public String name() {
        return "usage";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(!args.isEmpty(), CommandArgumentException::new);

        LinkedList<Command> commands = new LinkedList<>();
        args.forEach(s -> {
            if (commands.isEmpty()) commands.add(CommandManager.getCommands().stream()
                                                       .filter(c -> s.matches(c.regex().toLowerCase()))
                                                       .findFirst().orElseThrow(
                    () -> new ConsoleError("Command '%s' not found", invoked)));
            else commands.getLast().subCommands().stream()
                     .filter(sc -> s.matches(sc.regex().toLowerCase()))
                     .findFirst().ifPresent(commands::add);

        });
        EmbedBuilder eb = new EmbedBuilder()
                              .setTitle("`" + commands.stream().map(Command::name)
                                                  .collect(Collectors.joining(" ")).toUpperCase() + "` usage")
                              .setDescription(Stream.concat(
                                  Stream.of(commands.getLast()), commands.getLast().subCommands().stream())
                                                  .map(c -> String.format("`%s`", c.usage()))
                                                  .collect(Collectors.joining(" or\n")))
                              .setColor(0xdf136c);

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public String usage() {
        return "usage <command>";
    }

    @Override
    public String description() {
        return "Shows a commands usage page";
    }
}
