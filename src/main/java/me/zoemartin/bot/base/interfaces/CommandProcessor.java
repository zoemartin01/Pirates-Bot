package me.zoemartin.bot.base.interfaces;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface CommandProcessor {
    void process(MessageReceivedEvent event, String input);
}
