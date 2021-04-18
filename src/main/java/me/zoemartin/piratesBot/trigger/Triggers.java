package me.zoemartin.piratesBot.trigger;

import me.zoemartin.rubie.Bot;
import me.zoemartin.rubie.core.annotations.Module;
import me.zoemartin.rubie.core.interfaces.ModuleInterface;
import me.zoemartin.rubie.core.util.CollectorsUtil;
import me.zoemartin.rubie.core.util.DatabaseUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Module
public class Triggers extends ListenerAdapter implements ModuleInterface {
    private static final Map<String, Collection<Trigger>> triggers = new ConcurrentHashMap<>();

    @Override
    public void init() {
        Bot.addListener(new Triggers());
    }

    @Override
    public void initLate() {
        initTriggers();
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (!hasTriggers(event.getGuild())) return;

        new Thread(() -> getTriggers(event.getGuild()).forEach(s -> {
            if (event.getMessage().getContentRaw().matches(s.getRegex())) {
                event.getChannel().sendMessageFormat(s.getOutput()).queue();
            }
        })).start();
    }

    private static void initTriggers() {
        triggers.putAll(DatabaseUtil.loadGroupedCollection("from Trigger", Trigger.class,
            Trigger::getGuild_id,
            Function.identity(),
            CollectorsUtil.toConcurrentSet()));
    }

    public static void addTrigger(Guild guild, String regex, String output) {
        Trigger trigger = new Trigger(guild.getId(), regex, output);

        triggers.computeIfAbsent(guild.getId(),
            s -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(trigger);

        DatabaseUtil.saveObject(trigger);
    }

    public static boolean isTrigger(Guild guild, String regex) {
        return getTriggers(guild).stream().anyMatch(trigger -> trigger.getRegex().equals(regex));
    }

    public static boolean removeTrigger(Guild guild, String regex) {
        Trigger trigger = triggers.get(guild.getId()).stream().filter(t -> t.getRegex().equals(regex))
                              .findAny().orElseThrow(NoSuchElementException::new);

        DatabaseUtil.deleteObject(trigger);
        return triggers.get(guild.getId()).remove(trigger);
    }

    public static Collection<Trigger> getTriggers(Guild guild) {
        return Collections.unmodifiableCollection(triggers.getOrDefault(guild.getId(), Collections.emptySet()));
    }

    public static boolean hasTriggers(Guild guild) {
        return triggers.containsKey(guild.getId());
    }
}
