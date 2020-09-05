package me.zoemartin.bot.base.interfaces;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface CommandProcessor {
    void process(Collection<Command> commands, MessageReceivedEvent event, String input);
}
