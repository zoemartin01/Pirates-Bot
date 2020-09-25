package me.zoemartin.bot.base.interfaces;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public interface CommandProcessor {
    void process(GuildMessageReceivedEvent event, String input);
}
