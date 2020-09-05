package me.zoemartin.bot.modules.commandProcessing;

import me.zoemartin.bot.base.exceptions.*;
import me.zoemartin.bot.base.interfaces.*;
import me.zoemartin.bot.base.util.Check;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.*;

public class CommandHandler implements CommandProcessor {
    @Override
    public void process(Collection<Command> commands, MessageReceivedEvent event, String input) {
        User user = event.getAuthor();
        MessageChannel channel = event.getChannel();

        String[] inputSplit = input.split("\\s+");

        if (inputSplit.length == 0) return;

        Command c = commands.stream()
                        .filter(command -> command.name().equalsIgnoreCase(inputSplit[0]))
                        .findFirst().orElseThrow(() -> new ConsoleError("Command '%s' not found", inputSplit[0]));

        if (event.isFromGuild()) {
            Guild guild = event.getGuild();
            Member member = guild.getMember(user);
            Check.notNull(member, () -> new ConsoleError("member is null"));
            Check.check(c.required() == Permission.UNKNOWN
                            || member.hasPermission(c.required()) || member.hasPermission(Permission.ADMINISTRATOR),
                () -> new ConsoleError("Member '%s' doesn't have the required permission for Command '%s'",
                    member.getId(), c.name()));
        } else {
            Check.check(!Arrays.asList(c.getClass().getClasses()).contains(GuildCommand.class),
                () -> new ConsoleError("User '%s' attempted to run Command '%s' outside of allowed Scope",
                    user.getId(), c.name()));
        }

        List<String> arguments;

        if (inputSplit.length == 1) arguments = Collections.emptyList();
        else arguments = Arrays.asList(Arrays.copyOfRange(inputSplit, 1, inputSplit.length));

        try {
            c.run(user, channel, Collections.unmodifiableList(arguments), event.getMessage());
        } catch (CommandArgumentException e) {
            channel.sendMessage(c.usage()).queue();
        } catch (ReplyError e) {
            channel.sendMessage(e.getMessage()).queue();
        }
    }
}
