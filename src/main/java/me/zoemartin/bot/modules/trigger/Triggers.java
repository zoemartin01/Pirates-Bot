package me.zoemartin.bot.modules.trigger;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.interfaces.Module;
import me.zoemartin.bot.base.managers.CommandManager;
import me.zoemartin.bot.base.util.DatabaseUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.hibernate.Session;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@LoadModule
public class Triggers extends ListenerAdapter implements Module {
    private static final Map<String, Set<Trigger>> triggers = new ConcurrentHashMap<>();

    @Override
    public void init() {
        CommandManager.register(new TriggerCommand());
        Bot.addListener(new Triggers());
        DatabaseUtil.setMapped(Trigger.class);
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
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            List<Trigger> load = session.createQuery("from Trigger", Trigger.class).list();
            load.forEach(t -> triggers.computeIfAbsent(t.getGuild_id(),
                s -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(t));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static Set<Trigger> getTriggers(Guild guild) {
        return Collections.unmodifiableSet(triggers.getOrDefault(guild.getId(), Collections.emptySet()));
    }

    public static boolean hasTriggers(Guild guild) {
        return triggers.containsKey(guild.getId());
    }
}
