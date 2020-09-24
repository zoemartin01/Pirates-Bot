package me.zoemartin.bot.modules.commandProcessing;

import me.zoemartin.bot.base.managers.CommandManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class CommandListener extends ListenerAdapter {
    private static final String PREFIX = ";";

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();
        if (message.startsWith(PREFIX)) CommandManager.process(event, message.substring(PREFIX.length()));
    }
}
