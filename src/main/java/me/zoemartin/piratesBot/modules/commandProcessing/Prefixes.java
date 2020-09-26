package me.zoemartin.piratesBot.modules.commandProcessing;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.util.DatabaseConverter;
import me.zoemartin.piratesBot.core.util.DatabaseUtil;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Table(name = "prefixes")
public class Prefixes {
    private static final Map<String, Prefixes> prefixesMap = new ConcurrentHashMap<>();
    private static String MENTION_PREFIX = null;

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "guild_id", updatable = false, nullable = false)
    private String guild_id;

    @Column(name = "prefixes", nullable = false)
    @Convert(converter = DatabaseConverter.StringListConverter.class)
    private Collection<String> prefixes;

    protected Prefixes() {
    }

    public Prefixes(String guild_id, Collection<String> prefixes) {
        this.uuid = UUID.randomUUID();
        this.guild_id = guild_id;
        this.prefixes = prefixes;
    }

    public void addPrefix(String prefix) {
        prefixes.add(prefix);
    }

    public boolean removePrefix(String prefix) {
        return prefixes.remove(prefix);
    }

    public static void init() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            List<Prefixes> load = session.createQuery("from Prefixes", Prefixes.class).list();
            prefixesMap.putAll(load.stream().collect(Collectors.toMap(p -> p.guild_id, Function.identity())));
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Collection<String> getPrefixes(String guildId) {
        Prefixes p = prefixesMap.getOrDefault(guildId, null);
        Collection<String> prefixes = new HashSet<>();
        if (p != null) prefixes.addAll(p.prefixes);
        if (MENTION_PREFIX == null) MENTION_PREFIX = String.format("<@!%s> ", Bot.getJDA().getSelfUser().getId());
        prefixes.add(MENTION_PREFIX);
        return Collections.unmodifiableCollection(prefixes);
    }

    public static void addPrefix(String guildId, String prefix) {
        Prefixes p;
        if (getPrefixes(guildId).size() == 1) {
            p = new Prefixes(guildId, Collections.newSetFromMap(new ConcurrentHashMap<>()));
            p.addPrefix(prefix);
            DatabaseUtil.saveObject(p);
        } else {
            p = prefixesMap.get(guildId);
            p.addPrefix(prefix);
            DatabaseUtil.updateObject(p);
        }

        prefixesMap.put(guildId, p);
    }

    public static boolean removePrefix(String guildId, String prefix) {
        Prefixes p = prefixesMap.getOrDefault(guildId, null);
        if (p == null || p.prefixes.isEmpty()) return false;
        if (!p.removePrefix(prefix)) return false;

        if (p.prefixes.isEmpty()) {
            DatabaseUtil.deleteObject(p);
            prefixesMap.remove(guildId);
        } else {
            DatabaseUtil.updateObject(p);
            prefixesMap.put(guildId, p);
        }
        return true;
    }
}
