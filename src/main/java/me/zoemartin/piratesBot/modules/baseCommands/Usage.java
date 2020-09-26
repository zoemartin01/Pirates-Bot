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
import java.util.stream.Collectors;

public class Usage implements GuildCommand {
    @Override
    public String name() {
        return "usage";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(!args.isEmpty(), CommandArgumentException::new);

        Command command = CommandManager.getCommands().stream()
                              .filter(c -> c.name().equalsIgnoreCase(args.get(0)))
                              .findFirst().orElseThrow(() -> new ConsoleError("Command '%s' not found", args.get(0)));
        Check.notNull(command, () -> new ReplyError("No such command!"));

        Command sc;
        if (args.size() == 1) sc = null;
        else sc = command.subCommands().stream()
                      .filter(subCommand -> subCommand.name().equalsIgnoreCase(args.get(1)))
                      .findFirst().orElse(null);

        sendUsage(channel, sc == null ? command : sc);
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

    private static void sendUsage(MessageChannel channel, Command command) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("`" + command.name().toUpperCase() + "` usage");

        List<String> usage = command.subCommands().stream().map(Command::usage).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("`").append(command.usage()).append("` or\n");
        usage.forEach(s -> sb.append("`").append(s).append("` or\n"));
        sb.delete(sb.length() - 3, sb.length());

        eb.setDescription(sb.toString());
        eb.setColor(0xdf136c);

        channel.sendMessage(eb.build()).queue();
    }
}
