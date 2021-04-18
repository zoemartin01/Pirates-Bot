package me.zoemartin.piratesBot.piratesCommands;

import me.zoemartin.rubie.core.CommandPerm;
import me.zoemartin.rubie.core.GuildCommandEvent;
import me.zoemartin.rubie.core.annotations.*;
import me.zoemartin.rubie.core.exceptions.CommandArgumentException;
import me.zoemartin.rubie.core.interfaces.GuildCommand;
import me.zoemartin.rubie.core.util.*;
import me.zoemartin.rubie.modules.pagedEmbeds.PageListener;
import me.zoemartin.rubie.modules.pagedEmbeds.PagedEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildAvailableEvent;
import net.dv8tion.jda.api.events.guild.voice.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Command
@CommandOptions(
    name = "voicerole",
    description = "voiceroles",
    perm = CommandPerm.BOT_ADMIN
)
public class VoiceRole extends GuildCommand {
    private static final Map<String, Collection<VoiceRoleConfig>> voiceRoles = new ConcurrentHashMap<>();

    public static void addVoiceRoles(Guild g, VoiceChannel c, Collection<Role> roles) {
        Collection<VoiceRoleConfig> configs = voiceRoles.computeIfAbsent(g.getId(),
            s -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

        roles.forEach(role -> {
            VoiceRoleConfig conf = new VoiceRoleConfig(g.getId(), role.getId(), c.getId());
            configs.add(conf);
            DatabaseUtil.saveObject(conf);
        });
    }


    public static void removeVoiceRoles(Guild g, VoiceChannel c, Collection<Role> roles) {
        Collection<VoiceRoleConfig> configs = voiceRoles.computeIfAbsent(g.getId(),
            s -> Collections.newSetFromMap(new ConcurrentHashMap<>()));

        Collection<VoiceRoleConfig> remove = new HashSet<>();
        roles.forEach(role -> remove.add(
            configs.stream().filter(config -> config.getChannel_id().equals(c.getId())
                                                  && config.getRole_id().equals(role.getId()))
                .findAny().orElse(null)));

        remove.stream().filter(Objects::nonNull).forEach(config -> {
            DatabaseUtil.deleteObject(config);
            configs.remove(config);
        });
    }

    private static Collection<VoiceRoleConfig> getConfigs(Guild g) {
        return voiceRoles.computeIfAbsent(g.getId(),
            s -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }

    public static void initConfigs() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            List<VoiceRoleConfig> load = session.createQuery("from VoiceRoleConfig", VoiceRoleConfig.class).list();
            Set<String> guilds = load.stream().map(VoiceRoleConfig::getGuild_id).collect(Collectors.toSet());

            guilds.forEach(g -> voiceRoles.computeIfAbsent(g,
                s -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                                    .addAll(load.stream().filter(config -> config.getGuild_id().equals(g))
                                                .collect(Collectors.toSet())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class Listener extends ListenerAdapter {
        @Override
        public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
            Guild g = event.getGuild();
            if (!voiceRoles.containsKey(g.getId())) return;
            if (voiceRoles.get(g.getId()).stream()
                    .noneMatch(config -> config.getChannel_id().equals(event.getChannelJoined().getId())
                                             || config.getChannel_id().equals(event.getChannelLeft().getId()))) return;

            voiceRoles.get(g.getId()).stream()
                .filter(config -> config.getChannel_id().equals(event.getChannelLeft().getId()))
                .forEach(config -> {
                    Role r = g.getRoleById(config.getRole_id());
                    if (r != null) g.removeRoleFromMember(event.getMember(), r).queue();
                });

            voiceRoles.get(g.getId()).stream()
                .filter(config -> config.getChannel_id().equals(event.getChannelJoined().getId()))
                .forEach(config -> {
                    Role r = g.getRoleById(config.getRole_id());
                    if (r != null) g.addRoleToMember(event.getMember(), r).queue();
                });
        }

        @Override
        public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
            Guild g = event.getGuild();
            if (!voiceRoles.containsKey(g.getId())) return;
            if (voiceRoles.get(g.getId()).stream()
                    .noneMatch(config -> config.getChannel_id().equals(event.getChannelJoined().getId()))) return;

            voiceRoles.get(g.getId()).stream()
                .filter(config -> config.getChannel_id().equals(event.getChannelJoined().getId()))
                .forEach(config -> {
                    Role r = g.getRoleById(config.getRole_id());
                    if (r != null) g.addRoleToMember(event.getMember(), r).queue();
                });
        }

        @Override
        public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
            Guild g = event.getGuild();
            if (!voiceRoles.containsKey(g.getId())) return;
            if (voiceRoles.get(g.getId()).stream()
                    .noneMatch(config -> config.getChannel_id().equals(event.getChannelLeft().getId()))) return;

            voiceRoles.get(g.getId()).stream()
                .filter(config -> config.getChannel_id().equals(event.getChannelLeft().getId()))
                .forEach(config -> {
                    Role r = g.getRoleById(config.getRole_id());
                    if (r != null) g.removeRoleFromMember(event.getMember(), r).queue();
                });
        }

        @Override
        public void onGuildAvailable(@NotNull GuildAvailableEvent event) {
            Guild g = event.getGuild();
            if (!voiceRoles.containsKey(g.getId())) return;

            voiceRoles.get(g.getId()).stream().filter(c -> Parser.Channel.getVoiceChannel(g, c.getChannel_id()) == null)
                .forEach(config -> {
                    DatabaseUtil.deleteObject(config);
                    voiceRoles.get(g.getId()).remove(config);
                });
        }
    }

    @Override
    public void run(GuildCommandEvent event) {
        throw new CommandArgumentException();
    }

    @SubCommand(VoiceRole.class)
    @CommandOptions(
        name = "list",
        description = "list voice roles",
        perm = CommandPerm.BOT_MANAGER
    )
    private static class list extends GuildCommand {

        @SuppressWarnings("ConstantConditions")
        @Override
        public void run(GuildCommandEvent event) {
            var g = event.getGuild();
            Collection<VoiceRoleConfig> configs = getConfigs(g);

            PagedEmbed p = new PagedEmbed(EmbedUtil.pagedDescription(
                new EmbedBuilder().setTitle("Voice Roles").build(),
                configs.stream().filter(config -> g.getVoiceChannelById(config.getChannel_id()) != null)
                    .map(config -> String.format("Channel: %s | Role: %s\n",
                        g.getVoiceChannelById(config.getChannel_id()).getName(),
                        g.getRoleById(config.getRole_id()).getAsMention())).collect(Collectors.toList())),
                event);

            PageListener.add(p);
        }
    }

    @SubCommand(VoiceRole.class)
    @CommandOptions(
        name = "add",
        description = "adds a voice role",
        usage = "<channel> <roles...>",
        perm = CommandPerm.BOT_MANAGER
    )
    private static class Add extends GuildCommand {

        @Override
        public void run(GuildCommandEvent event) {
            var args = event.getArgs();
            Check.check(args.size() >= 2, CommandArgumentException::new);
            Guild g = event.getGuild();
            VoiceChannel c = Parser.Channel.getVoiceChannel(g, args.get(0));
            Check.entityReferenceNotNull(c, VoiceChannel.class, args.get(0));

            addVoiceRoles(g, c, args.subList(1, args.size()).stream()
                                    .map(s -> Parser.Role.getRole(g, s))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList()));
            event.addCheckmark();
        }
    }

    @SubCommand(VoiceRole.class)
    @CommandOptions(
        name = "remove",
        description = "removes a voice role",
        usage = "<channel> <roles...>",
        perm = CommandPerm.BOT_MANAGER
    )
    private static class Remove extends GuildCommand {

        @Override
        public void run(GuildCommandEvent event) {
            var args = event.getArgs();
            Check.check(args.size() >= 2, CommandArgumentException::new);
            Guild g = event.getGuild();
            VoiceChannel c = Parser.Channel.getVoiceChannel(g, args.get(0));
            Check.entityReferenceNotNull(c, VoiceChannel.class, args.get(0));

            removeVoiceRoles(g, c, args.subList(1, args.size()).stream()
                                       .map(s -> Parser.Role.getRole(g, s))
                                       .filter(Objects::nonNull).collect(Collectors.toList()));
            event.addCheckmark();
        }
    }

}
