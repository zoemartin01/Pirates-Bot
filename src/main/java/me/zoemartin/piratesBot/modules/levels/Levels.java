package me.zoemartin.piratesBot.modules.levels;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.LoadModule;
import me.zoemartin.piratesBot.core.interfaces.Module;
import me.zoemartin.piratesBot.core.managers.CommandManager;
import me.zoemartin.piratesBot.core.util.DatabaseUtil;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jodah.expiringmap.ExpiringMap;
import org.hibernate.Session;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@LoadModule
public class Levels extends ListenerAdapter implements Module {
    private static final Map<String, Map<String, UserLevel>> levels = new ConcurrentHashMap<>();
    private static final Map<String, LevelConfig> configs = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> timeout = new ConcurrentHashMap<>();

    @Override
    public void init() {
        Bot.addListener(this);
        DatabaseUtil.setMapped(UserLevel.class);
        DatabaseUtil.setMapped(LevelConfig.class);
        CommandManager.register(new Level());
    }

    @Override
    public void initLate() {
        initLevels();
        initConfigs();
    }

    private void initLevels() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            List<UserLevel> load = session.createQuery("from UserLevel", UserLevel.class).list();
            load.forEach(l -> levels.computeIfAbsent(l.getGuild_id(),
                s -> new ConcurrentHashMap<>()).put(l.getUser_id(), l));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initConfigs() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            List<LevelConfig> load = session.createQuery("from LevelConfig", LevelConfig.class).list();
            load.forEach(c -> configs.put(c.getGuild_id(), c));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        new Thread(() -> process(event)).start();
    }

    @SuppressWarnings("ConstantConditions")
    private void process(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        Guild g = event.getGuild();
        User u = event.getAuthor();
        LevelConfig config = getConfig(g);
        if (!config.isEnabled()) return;
        if (timeout.getOrDefault(g.getId(), Collections.emptySet()).contains(u.getId())) return;
        if (config.getBlacklistedChannels().contains(event.getChannel().getId())) return;
        if (event.getMember().getRoles().stream().anyMatch(role -> config.getBlacklistedRoles().contains(role.getId())))
            return;


        UserLevel level;

        boolean exists = levels.getOrDefault(g.getId(), Collections.emptyMap()).containsKey(u.getId());

        if (exists) level = getUserLevel(g, u);
        else level = new UserLevel(g.getId(), u.getId());

        int before = calcLevel(level.getExp());
        level.addExp(ThreadLocalRandom.current().nextInt(15, 26));
        int after = calcLevel(level.getExp());

        timeout.computeIfAbsent(g.getId(),
            k -> Collections.newSetFromMap(ExpiringMap.builder().expiration(1, TimeUnit.MINUTES).build()))
            .add(u.getId());

        if (after > before) {
            levelUp(event, after);
        }

        if (exists)
            DatabaseUtil.updateObject(level);
        else {
            DatabaseUtil.saveObject(level);
            levels.computeIfAbsent(g.getId(), k -> new ConcurrentHashMap<>()).put(level.getUser_id(), level);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void levelUp(GuildMessageReceivedEvent event, int level) {
        Guild g = event.getGuild();
        Collection<String> roles = getConfig(g).getRewardRoles(level);

        roles.forEach(s -> {
            Role r = g.getRoleById(s);
            if (r != null) g.addRoleToMember(event.getMember(), r).queue();
        });

        LevelConfig config = getConfig(g);

        switch (config.getAnnouncements()) {
            case ALL:
                if (roles.isEmpty())
                    event.getAuthor().openPrivateChannel().complete().sendMessageFormat(
                        "Hey, %s! Congratulations on hitting level %s in %s! " +
                            "Thanks for being here! " +
                            "\uD83D\uDC9A",
                        event.getAuthor().getName(), level, g.getName()
                    ).queue();
                else
                    event.getAuthor().openPrivateChannel().complete().sendMessageFormat(
                        "Hey, %s! Congratulations on hitting level %s in %s! " +
                            "Hope you enjoy your new server privileges, and hey, thanks for being here " +
                            "\uD83D\uDC9A",
                        event.getAuthor().getName(), level, g.getName()
                    ).queue();
                break;

            case REWARDS:
                if (!roles.isEmpty())
                    event.getAuthor().openPrivateChannel().complete().sendMessageFormat(
                        "Hey, %s! Congratulations on hitting level %s in %s! " +
                            "Hope you enjoy your new server privileges, and hey, thanks for being here " +
                            "\uD83D\uDC9A",
                        event.getAuthor().getName(), level, g.getName()
                    ).queue();
                break;
        }
    }

    @Nonnull
    public static UserLevel getUserLevel(Guild g, User user) {
        UserLevel l =  levels.computeIfAbsent(g.getId(), k -> new ConcurrentHashMap<>()).getOrDefault(user.getId(),
            null);
        if (l == null) {
            l = new UserLevel(g.getId(), user.getId());
            levels.get(g.getId()).put(l.getUser_id(), l);
            DatabaseUtil.saveObject(l);
        }
        return l;
    }

    public static Collection<UserLevel> getLevels(Guild g) {
        return Collections.unmodifiableCollection(levels.getOrDefault(g.getId(), Collections.emptyMap()).values());
    }

    public static void importLevel(UserLevel userLevel) {
        Map<String, UserLevel> lvls = levels.computeIfAbsent(userLevel.getGuild_id(),
            k -> new ConcurrentHashMap<>());
        if (lvls.containsKey(userLevel.getUser_id())) DatabaseUtil.deleteObject(lvls.get(userLevel.getUser_id()));
        lvls.put(userLevel.getUser_id(), userLevel);
        DatabaseUtil.saveObject(userLevel);
    }

    public static LevelConfig getConfig(Guild g) {
        LevelConfig config = configs.getOrDefault(g.getId(), null);
        if (config == null) {
            config = new LevelConfig(g.getId(), false);
            configs.put(g.getId(), config);
            DatabaseUtil.saveObject(config);
        }
        return config;
    }

    public static int calcLevel(int exp) {
        double x = exp + 1;
        double pow = Math.cbrt(
            Math.sqrt(3) * Math.sqrt(3888.0 * Math.pow(x, 2) + (291600.0 * x) - 207025.0) - 108.0 * x - 4050.0);
        return (int) (-pow / (2.0 * Math.pow(3.0, 2.0 / 3.0) * Math.pow(5.0, 1.0 / 3.0)) -
                          (61.0 * Math.cbrt(5.0 / 3.0)) / (
                              2.0 * pow) - (9.0 / 2.0));
    }

    public static int calcExp(int lvl) {
        return (int) (5.0/6.0 * lvl * (2 * Math.pow(lvl, 2) + 27 * lvl + 91));
    }

}
