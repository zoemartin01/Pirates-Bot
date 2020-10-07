package me.zoemartin.piratesBot.modules.commandProcessing;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.*;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.interfaces.CommandProcessor;
import me.zoemartin.piratesBot.core.managers.CommandManager;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.DatabaseUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler implements CommandProcessor {
    @Override
    public void process(GuildMessageReceivedEvent event, String input) {
        User user = event.getAuthor();
        MessageChannel channel = event.getChannel();

        List<String> inputs = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
        while (m.find())
            inputs.add(m.group(1).replace("\"", ""));

        LinkedList<Command> commands = new LinkedList<>();
        inputs.forEach(s -> {
            if (commands.isEmpty()) commands.add(CommandManager.getCommands().stream()
                                                     .filter(c -> s.matches(c.regex().toLowerCase()))
                                                     .findFirst().orElse(null));
            else if (commands.getLast() != null) commands.getLast().subCommands().stream()
                     .filter(sc -> s.matches(sc.regex().toLowerCase()))
                     .findFirst().ifPresent(commands::add);

        });

        if (commands.isEmpty() || commands.getLast() == null) return;

        int commandLevel = commands.size();
        Command command = commands.getLast();

        Guild guild = event.getGuild();
        Member member = guild.getMember(user);
        Check.notNull(member, () -> new ConsoleError("member is null"));
        Check.check(command.required().contains(Permission.UNKNOWN)
                        || member.hasPermission(Permission.ADMINISTRATOR)
                        || command.required().stream().allMatch(member::hasPermission),
            () -> new ConsoleError("Member '%s' doesn't have the required permission for Command '%s'",
                member.getId(), command.name()));

        if (command.commandPerm().equals(CommandPerm.OWNER) || !member.hasPermission(Permission.ADMINISTRATOR))
            Check.check(PermissionHandler.getMemberPerm(guild.getId(),
                member.getId()).getPerm().raw() >= command.commandPerm().raw()
                            || member.getRoles().stream().anyMatch(
                role -> PermissionHandler.getRolePerm(guild.getId(), role.getId()).getPerm().raw() >= command.commandPerm().raw()),
                () -> new ConsoleError("Member '%s' doesn't have the required permission rank for Command '%s'",
                    member.getId(), command.name()));

        List<String> arguments;

        arguments = inputs.subList(commandLevel, inputs.size());


        try {
            command.run(user, channel, Collections.unmodifiableList(arguments), event.getMessage(), inputs.get(commands.size() - 1));
        } catch (CommandArgumentException e) {
            sendUsage(channel, commands);
        } catch (ReplyError e) {
            channel.sendMessage(e.getMessage()).queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
        } catch (ConsoleError e) {
            throw new ConsoleError(String.format("[Command Error] %s: %s", command.getClass().getName(), e.getMessage()));
        } catch (Exception e) {
            LoggedError error = new LoggedError(event.getGuild().getId(), event.getChannel().getId(), event.getAuthor().getId(),
                event.getMessageId(), event.getMessage().getContentRaw(), e.getMessage(), e.getStackTrace(), System.currentTimeMillis());

            DatabaseUtil.saveObject(error);

            channel.sendMessageFormat(
                "> :warning: Ooops an unexpected error has occurred. If this happens again send this to the Developer\n> `%s`", error.getUuid()).queue();

            throw e;
        }

        System.out.printf("[Command used] %s used command %s in %s\n", user.getId(), command.getClass().getCanonicalName(),
            event.getGuild().getId());
    }

    private static void sendUsage(MessageChannel channel, LinkedList<Command> commands) {
        EmbedBuilder eb = new EmbedBuilder()
                              .setTitle("`" + commands.stream().map(Command::name)
                                                  .collect(Collectors.joining(" ")).toUpperCase() + "` usage")
                              .setDescription(Stream.concat(
                                  Stream.of(commands.getLast()), commands.getLast().subCommands().stream())
                                                  .map(c -> String.format("`%s`", c.usage()))
                                                  .collect(Collectors.joining(" or\n")))
                              .setColor(0xdf136c);

        channel.sendMessage(eb.build()).queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
    }
}
