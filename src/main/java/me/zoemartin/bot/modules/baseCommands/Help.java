package me.zoemartin.bot.modules.baseCommands;

import me.zoemartin.bot.base.CommandPerm;
import me.zoemartin.bot.base.exceptions.*;
import me.zoemartin.bot.base.interfaces.*;
import me.zoemartin.bot.base.managers.CommandManager;
import me.zoemartin.bot.base.util.Check;
import me.zoemartin.bot.base.util.MessageUtils;
import me.zoemartin.bot.modules.commandProcessing.PermissionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Help implements GuildCommand {
    @Override
    public String name() {
        return "help";
    }

    @Override
    public Set<Command> subCommands() {
        return Set.of(new Cmd());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        StringBuilder sb = new StringBuilder("**Available Commands:**\n\n");

        Guild guild = original.getGuild();
        Member member = original.getMember();

        CommandManager.getCommands().stream()
            .filter(
                command -> PermissionHandler.getMemberPerm(guild.getId(), member.getId()).getPerm().raw()
                               >= command.commandPerm().raw()
                               || member.getRoles().stream().anyMatch(
                    role -> PermissionHandler.getRolePerm(guild.getId(), role.getId()).getPerm().raw()
                                >= command.commandPerm().raw()))

            .sorted(Comparator.comparing(Command::name))
            .forEach(command -> sb.append("`").append(command.name())
                                    .append("` | ").append(command.description()).append("\n\n"));

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Help").setColor(0xdf136c);
        eb.setDescription(sb.toString());
        eb.addField("Additional Help:", "For additional help with a command use: " + usage(), false);

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public String usage() {
        return "help";
    }

    @Override
    public String description() {
        return "Sending help :3";
    }

    private static class Cmd implements GuildCommand {

        @Override
        public String name() {
            return "command";
        }

        @Override
        public String regex() {
            StringBuilder sb = new StringBuilder();
            CommandManager.getCommands().forEach(command -> sb.append(command.regex()).append("|"));
            sb.deleteCharAt(sb.lastIndexOf("|"));
            return sb.toString();
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            AtomicReference<Command> command = new AtomicReference<>(
                CommandManager.getCommands().stream()
                    .filter(c -> invoked.matches(c.regex().toLowerCase()))
                    .findFirst().orElseThrow(() -> new ConsoleError("Command '%s' not found", invoked)));
            Check.notNull(command.get(), () -> new ReplyError("No such command!"));

            List<Command> hierarchy = new LinkedList<>();
            hierarchy.add(command.get());

            args.forEach(s -> {
                Command subCommand = command.get().subCommands().stream()
                                         .filter(sc -> s.matches(sc.regex().toLowerCase()))
                                         .findFirst().orElse(null);

                if (subCommand != null) {
                    command.set(subCommand);
                    hierarchy.add(command.get());
                }
            });


            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("`" + MessageUtils.mergeWhitespace(hierarchy.stream().map(Command::name)
                                                               .collect(Collectors.toList())).toUpperCase() + "`")
                .setColor(0xdf136c);
            eb.addField("Description:", command.get().description(), false);
            eb.addField("Usage: ", "`" + command.get().usage() + "`", false);


            CommandPerm perm = command.get().commandPerm();
            if (perm != CommandPerm.EVERYONE)
                eb.addField("Permission Level:", String.format("`[%d] %s`", perm.raw(), perm.toString()), false);

            StringBuilder aliases = new StringBuilder();
            for (String s : command.get().regex().split("\\|")) {
                if (s.equals(command.get().name())) continue;
                aliases.append(s).append(", ");
            }

            if (aliases.length() > 0) aliases.deleteCharAt(aliases.lastIndexOf(","))
                                          .deleteCharAt(aliases.lastIndexOf(" "));
            eb.addField("Aliases:", String.format("`%s`", aliases.length() > 0 ? aliases : "n/a"), false);

            StringBuilder sub = new StringBuilder();
            Iterator<Command> iterator = command.get().subCommands().iterator();

            while (iterator.hasNext()) {
                Command c = iterator.next();

                if (iterator.hasNext()) sub.append("`├─ ").append(c.name()).append("`\n");
                else sub.append("`└─ ").append(c.name()).append("`");
            }

            if (sub.length() > 0)
                eb.addField("Subcommand(s)", sub.toString(), false);

            channel.sendMessage(eb.build()).queue();
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.EVERYONE;
        }

        @Override
        public String usage() {
            return "help <command>";
        }

        @Override
        public String description() {
            return "Shows a command help page";
        }
    }

    public static void commandHelp(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        new Cmd().run(user, channel, args, original, invoked);
    }
}
