package me.zoemartin.bot.modules.commandProcessing;

import me.zoemartin.bot.base.managers.CommandManager;
import me.zoemartin.bot.modules.baseCommands.Prefix;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Collection;

public class CommandListener extends ListenerAdapter {
    private static final String PREFIX = ";";

    /*@Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();
        if (message.startsWith(PREFIX)) CommandManager.process(event, message.substring(PREFIX.length()));
    }*/

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();
        Collection<String> prefixes = Prefixes.getPrefixes(event.getGuild().getId());

        prefixes.forEach(s -> {
            if (message.startsWith(s)) CommandManager.process(event, message.substring(s.length()));
        });
    }
}
