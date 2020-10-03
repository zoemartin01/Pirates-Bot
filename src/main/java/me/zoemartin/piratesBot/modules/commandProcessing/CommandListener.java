package me.zoemartin.piratesBot.modules.commandProcessing;

import me.zoemartin.piratesBot.core.exceptions.UnexpectedError;
import me.zoemartin.piratesBot.core.interfaces.CommandLogger;
import me.zoemartin.piratesBot.core.managers.CommandManager;
import me.zoemartin.piratesBot.core.util.DatabaseUtil;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.time.Instant;
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
        CommandLogger cl = CommandManager.getCommandLogger();

        if (cl != null && !event.isWebhookMessage()) cl.log(event.getMessage());


        if (event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }

        String message = event.getMessage().getContentRaw();
        Collection<String> prefixes = Prefixes.getPrefixes(event.getGuild().getId());

        prefixes.forEach(s -> {
            if (message.startsWith(s)) {
                CommandManager.process(event, message.substring(s.length()));
            }
        });
    }
}
