package me.zoemartin.piratesBot.modules.baseCommands;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.*;
import me.zoemartin.piratesBot.core.interfaces.*;
import me.zoemartin.piratesBot.core.managers.CommandManager;
import me.zoemartin.piratesBot.core.util.*;
import me.zoemartin.piratesBot.modules.commandProcessing.PermissionHandler;
import me.zoemartin.piratesBot.modules.pagedEmbeds.PageListener;
import me.zoemartin.piratesBot.modules.pagedEmbeds.PagedEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
        Guild guild = original.getGuild();
        Member member = original.getMember();

        PagedEmbed p = new PagedEmbed(EmbedUtil.pagedDescription(new EmbedBuilder()
                                                                     .setTitle("Help").setColor(0xdf136c).build(),
            CommandManager.getCommands().stream()
                .filter(
                    command -> PermissionHandler.getMemberPerm(guild.getId(), member.getId()).getPerm().raw()
                                   >= command.commandPerm().raw()
                                   || member.getRoles().stream().anyMatch(
                        role -> PermissionHandler.getRolePerm(guild.getId(), role.getId()).getPerm().raw()
                                    >= command.commandPerm().raw()))

                .sorted(Comparator.comparing(Command::name))
                .map(command -> String.format("`%s` | %s\n\n", command.name(), command.description()))
                .collect(Collectors.toList())),
            channel, user.getUser());

        PageListener.add(p);
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public String description() {
        return "Sending help :3";
    }

    private static class Cmd implements GuildCommand {

        @Override
        public String name() {
            return "<command>";
        }

        @Override
        public String regex() {
            StringBuilder sb = new StringBuilder();
            CommandManager.getCommands().forEach(command -> sb.append(command.regex()).append("|"));
            sb.deleteCharAt(sb.lastIndexOf("|"));
            return sb.toString();
        }

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
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

            String name = hierarchy.stream().map(Command::name)
                              .collect(Collectors.joining(" "));
            Command cmd = command.get();
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("`" + name.toUpperCase() + "`")
                .setColor(0xdf136c);
            eb.addField("Description:", cmd.description(), false);
            eb.addField("Usage: ", cmd.name().equals(cmd.usage()) ?
                                       String.format("`%s`", name) : String.format("`%s %s`", name, cmd.usage()),
                false);


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
        public String description() {
            return "Shows a command help page";
        }
    }

    public static void helper() {
        me.zoemartin.piratesBot.core.util.Help.setHelper(
            (user, channel, args, original, invoked) -> new Cmd().run(user, channel, args, original, invoked));
    }

    public static void commandHelp(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        new Cmd().run(user, channel, args, original, invoked);
    }
}
