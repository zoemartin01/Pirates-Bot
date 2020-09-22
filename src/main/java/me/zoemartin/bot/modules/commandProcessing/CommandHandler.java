package me.zoemartin.bot.modules.commandProcessing;

import me.zoemartin.bot.base.CommandPerm;
import me.zoemartin.bot.base.exceptions.*;
import me.zoemartin.bot.base.interfaces.*;
import me.zoemartin.bot.base.managers.CommandManager;
import me.zoemartin.bot.base.util.Check;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommandHandler implements CommandProcessor {
    private static final Map<String, Map<String, CommandPerm>> userPerms = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, CommandPerm>> rolePerms = new ConcurrentHashMap<>();

    @Override
    public void process(MessageReceivedEvent event, String input) {
        User user = event.getAuthor();
        MessageChannel channel = event.getChannel();

        String[] inputSplit = input.split("\\s+");

        if (inputSplit.length == 0) return;

        String commandString = inputSplit[0].toLowerCase();
        String subCommandString = inputSplit.length > 1 ? inputSplit[1].toLowerCase() : null;

        Command command = CommandManager.getCommands().stream()
                        .filter(c -> commandString.matches(c.regex().toLowerCase()))
                        .findFirst().orElseThrow(() -> new ConsoleError("Command '%s' not found", commandString));

        boolean isSubCommand = false;

        if (!command.subCommands().isEmpty() && subCommandString != null) {
            Command subCommand = command.subCommands().stream()
                                .filter(sc -> subCommandString.matches(sc.regex().toLowerCase()))
                                .findFirst().orElse(null);

            command = subCommand == null ? command : subCommand;
            isSubCommand = subCommand != null;
        }

        final Command cmd = command;

        if (event.isFromGuild()) {
            Guild guild = event.getGuild();
            Member member = guild.getMember(user);
            Check.notNull(member, () -> new ConsoleError("member is null"));
            Check.check(command.required().contains(Permission.UNKNOWN)
                            || member.hasPermission(Permission.ADMINISTRATOR)
                            || command.required().stream().allMatch(member::hasPermission),
                () -> new ConsoleError("Member '%s' doesn't have the required permission for Command '%s'",
                    member.getId(), cmd.name()));

            Check.check(userPerms.get(guild.getId()).get(member.getId()).raw() >= cmd.commandPerm().raw()
                || member.getRoles().stream().filter(role -> rolePerms.containsKey(role.getId()))
                       .anyMatch(role -> rolePerms.get(role.getId()).get(guild.getId()).raw() >= cmd.commandPerm().raw()),
                () -> new ConsoleError("Member '%s' doesn't have the required permission rank for Command '%s'",
                    member.getId(), cmd.name()));
        } else {
            Check.check(!Arrays.asList(command.getClass().getClasses()).contains(GuildCommand.class),
                () -> new ConsoleError("User '%s' attempted to run Command '%s' outside of allowed Scope",
                    user.getId(), cmd.name()));
        }

        List<String> arguments;

        if (isSubCommand) {
            if (inputSplit.length <= 2) arguments = Collections.emptyList();
            else arguments = Arrays.asList(Arrays.copyOfRange(inputSplit, 2, inputSplit.length));
        } else {
            if (inputSplit.length <= 1) arguments = Collections.emptyList();
            else arguments = Arrays.asList(Arrays.copyOfRange(inputSplit, 1, inputSplit.length));
        }

        try {
            cmd.run(user, channel, Collections.unmodifiableList(arguments), event.getMessage(),
                isSubCommand ? subCommandString : commandString);
        } catch (CommandArgumentException e) {
            sendUsage(channel, cmd);
        } catch (ReplyError e) {
            channel.sendMessage(e.getMessage()).queue();
        } catch (ConsoleError e) {
            throw new ConsoleError(String.format("[Command Error] %s: %s", cmd.getClass().getName(), e.getMessage()));
        }

        System.out.printf("[Command used] %s used command %s in %s\n", user.getId(), cmd.getClass().getCanonicalName(),
            event.isFromGuild() ? event.getGuild().getId() : event.getChannel().getId());
    }

    public static void addMemberPerm(String guild, String member, CommandPerm perm) {
        userPerms.computeIfAbsent(guild, k -> new ConcurrentHashMap<>()).put(member, perm);
    }

    public static CommandPerm removeMemberPerm(String guild, String member) {
        return userPerms.getOrDefault(guild, new ConcurrentHashMap<>()).remove(member);
    }

    public static void addRolePerm(String guild, String role, CommandPerm perm) {
        rolePerms.computeIfAbsent(guild, k -> new ConcurrentHashMap<>()).put(role, perm);
    }

    public static CommandPerm removeRolePerm(String guild, String role) {
        return rolePerms.getOrDefault(guild, new ConcurrentHashMap<>()).remove(role);
    }

    private static void sendUsage(MessageChannel channel, Command command) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(command.name() + " usage");
        eb.setDescription(command.usage());
        eb.setColor(0xdf136c);

        channel.sendMessage(eb.build()).queue();
    }
}
