package me.zoemartin.piratesBot.modules.levels;

import me.zoemartin.piratesBot.core.util.DatabaseConverter;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Table(name = "level_config")
public class LevelConfig {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID uuid;

    @Column(updatable = false, nullable = false)
    private String guild_id;

    @Column
    private boolean enabled;

    @Column
    @Convert(converter = DatabaseConverter.StringListConverter.class)
    private Collection<String> blacklisted_channels;

    @Column
    @Convert(converter = DatabaseConverter.StringListConverter.class)
    private Collection<String> blacklisted_roles;

    @Column
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "role_reward")
    @Convert(converter = DatabaseConverter.StringListConverter.class, attributeName = "value")
    private Map<Integer, Collection<String>> rewardRoles;

    @Column(nullable = false)
    @Convert(converter = Announcements.Converter.class)
    private Announcements announcements;

    public LevelConfig(UUID uuid, String guild_id, boolean enabled, Collection<String> blacklisted_channels,
                       Map<Integer, Collection<String>> rewardRoles) {
        this.uuid = uuid;
        this.guild_id = guild_id;
        this.enabled = enabled;
        this.blacklisted_channels = blacklisted_channels;
        this.rewardRoles = rewardRoles;
    }

    public LevelConfig(String guild_id, boolean enabled) {
        this.uuid = UUID.randomUUID();
        this.guild_id = guild_id;
        this.enabled = enabled;
        this.announcements = Announcements.NONE;
        this.blacklisted_channels = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.blacklisted_roles = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.rewardRoles = new ConcurrentHashMap<>();
    }

    protected LevelConfig() {
    }

    public String getGuild_id() {
        return guild_id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Collection<String> getBlacklistedChannels() {
        return Collections.unmodifiableCollection(blacklisted_channels);
    }

    public boolean addBlacklistedChannel(String channelId) {
        return blacklisted_channels.add(channelId);
    }

    public boolean removeBlacklistedChannel(String channelId) {
        return blacklisted_channels.remove(channelId);
    }

    public Collection<String> getBlacklistedRoles() {
        return Collections.unmodifiableCollection(blacklisted_roles);
    }

    public boolean addBlacklistedRole(String roleId) {
        return blacklisted_roles.add(roleId);
    }

    public boolean removeBlacklistedRole(String roleId) {
        return blacklisted_roles.remove(roleId);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    public Map<Integer, Collection<String>> getRewardRoles() {
        return rewardRoles;
    }

    public Collection<String> getRewardRoles(int lvl) {
        return rewardRoles.getOrDefault(lvl, Collections.emptySet());
    }

    public void addRewardRole(String roleId, int level) {
        rewardRoles.computeIfAbsent(level, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(roleId);
    }

    public boolean removeRewardRole(int level, String roleId) {
        return rewardRoles.getOrDefault(level, new HashSet<>()).remove(roleId);
    }

    public Announcements getAnnouncements() {
        return announcements;
    }

    public void setAnnouncements(Announcements announcements) {
        this.announcements = announcements;
    }

    enum Announcements {
        NONE(0),
        REWARDS(1),
        ALL(2);

        final int raw;

        Announcements(int raw) {
            this.raw = raw;
        }

        public int raw() {
            return raw;
        }

        public static Announcements fromNum(Integer num) {
            if (num == null) return null;

            return Set.of(Announcements.values()).stream().filter(a -> num.equals(a.raw())).findAny()
                       .orElse(null);
        }

        private static class Converter implements AttributeConverter<Announcements, Integer> {
            @Override
            public Integer convertToDatabaseColumn(Announcements attribute) {
                return attribute.raw();
            }

            @Override
            public Announcements convertToEntityAttribute(Integer dbData) {
                return Announcements.fromNum(dbData);
            }
        }
    }
}
