package me.zoemartin.bot.modules.baseCommands;

import me.zoemartin.bot.base.CommandPerm;
import me.zoemartin.bot.base.exceptions.*;
import me.zoemartin.bot.base.interfaces.*;
import me.zoemartin.bot.base.managers.CommandManager;
import me.zoemartin.bot.base.util.Check;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

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
                command -> CommandManager.getMemberPerm(guild.getId(), member.getId()).raw()
                               >= command.commandPerm().raw()
                               || member.getRoles().stream().anyMatch(
                    role -> CommandManager.getRolePerm(guild.getId(), role.getId()).raw()
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
            Command command = CommandManager.getCommands().stream()
                                  .filter(c -> invoked.matches(c.regex().toLowerCase()))
                                  .findFirst().orElseThrow(() -> new ConsoleError("Command '%s' not found", invoked));
            Check.notNull(command, () -> new ReplyError("No such command!"));

            Command sc;
            if (args.size() < 1) sc = null;
            else sc = command.subCommands().stream()
                          .filter(subCommand -> args.get(0).matches(subCommand.regex().toLowerCase()))
                          .findFirst().orElse(null);

            boolean isSubcommand = sc != null;

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("`" + (isSubcommand ? command.name() + " " + sc.name() : command.name()).toUpperCase() + "`").setColor(0xdf136c);
            eb.addField("Description:", isSubcommand ? sc.description() : command.description(), false);
            eb.addField("Usage: ", "`" + (isSubcommand ? sc.usage() : command.usage()) + "`", false);


            CommandPerm perm = isSubcommand ? sc.commandPerm() : command.commandPerm();
            if (perm != CommandPerm.EVERYONE)
                eb.addField("Permission Level:", String.format("`[%d] %s`", perm.raw(), perm.toString()), false);

            StringBuilder aliases = new StringBuilder();
            for (String s : command.regex().split("\\|")) {
                if (s.equals(command.name())) continue;
                aliases.append(s).append(", ");
            }

            if (aliases.length() > 0) aliases.deleteCharAt(aliases.lastIndexOf(","))
                                          .deleteCharAt(aliases.lastIndexOf(" "));
            eb.addField("Aliases:", String.format("`%s`", aliases.length() > 0 ? aliases : "n/a"), false);

            StringBuilder sub = new StringBuilder();
            Iterator<Command> iterator = command.subCommands().iterator();

            while (iterator.hasNext()) {
                Command c = iterator.next();

                if (iterator.hasNext()) sub.append("`├─ ").append(c.name()).append("`\n");
                else sub.append("`└─ ").append(c.name()).append("`");
            }

            if (sub.length() > 0 && !isSubcommand) eb.addField("Subcommand(s)", sub.toString(), false);

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
}
