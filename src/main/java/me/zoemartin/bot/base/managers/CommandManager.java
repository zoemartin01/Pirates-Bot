package me.zoemartin.bot.base.managers;

import me.zoemartin.bot.base.CommandPerm;
import me.zoemartin.bot.base.exceptions.*;
import me.zoemartin.bot.base.interfaces.*;
import me.zoemartin.bot.base.util.Check;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager {
    private static final Map<String, Map<String, CommandPerm>> userPerms = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, CommandPerm>> rolePerms = new ConcurrentHashMap<>();

    private CommandManager() {
        throw new IllegalAccessError();
    }

    private static final Collection<Command> registered = new HashSet<>();

    private static CommandProcessor processor;

    public static void register(Command c) {
        registered.add(c);
    }

    public static void setCommandProcessor(CommandProcessor cp) {
        processor = cp;
    }

    public static void process(MessageReceivedEvent event, String input) {
        new Thread(() -> {
            try {
                processor.process(event, input);
            } catch (ConsoleError e) {
                if (event.getAuthor().getId().equals("212591138945630213"))
                    event.getChannel().sendMessageFormat("Error: `%s`", e.getMessage()).queue();
                else System.err.println(e.getMessage());
            }
        }).start();
    }

    public static Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(registered);
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

    public static CommandPerm getMemberPerm(String guild, String member) {
        return userPerms.getOrDefault(guild, Collections.emptyMap()).getOrDefault(member, CommandPerm.EVERYONE);
    }

    public static CommandPerm getRolePerm(String guild, String role) {
        return rolePerms.getOrDefault(guild, Collections.emptyMap()).getOrDefault(role, CommandPerm.EVERYONE);
    }
}
