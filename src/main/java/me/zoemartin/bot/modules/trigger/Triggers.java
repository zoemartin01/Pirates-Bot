package me.zoemartin.bot.modules.trigger;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.interfaces.Module;
import me.zoemartin.bot.base.managers.CommandManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@LoadModule
public class Triggers extends ListenerAdapter implements Module {
    private static final Map<Guild, Triggers> triggers = new ConcurrentHashMap<>();
    private final Map<String, String> guildTriggers;

    @Override
    public void init() {
        CommandManager.register(new Trigger());
        Bot.addListener(new Triggers());
    }

    public Triggers() {
        this.guildTriggers = new ConcurrentHashMap<>();
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (!hasTriggers(event.getGuild())) return;

        new Thread(() -> get(event.getGuild()).process(event.getMessage().getContentRaw(), event.getChannel())).start();
    }

    private void process(String input, TextChannel channel) {
        guildTriggers.forEach((s, s2) -> {
            if (input.matches(s)) {
                channel.sendMessageFormat(s2).queue();
            }
        });
    }

    public void addTrigger(String regex, String output) {
        guildTriggers.put(regex, output);
    }

    public boolean isTrigger(String regex) {
        return guildTriggers.containsKey(regex);
    }

    public String removeTrigger(String regex) {
        return guildTriggers.remove(regex);
    }

    public Map<String, String> getTriggers() {
        return Collections.unmodifiableMap(guildTriggers);
    }

    public static Triggers get(Guild g) {
        return triggers.computeIfAbsent(g, k -> new Triggers());
    }

    public static boolean hasTriggers(Guild g) {
        return triggers.containsKey(g);
    }
}
