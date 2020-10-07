package me.zoemartin.piratesBot.modules.levels;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.*;
import me.zoemartin.piratesBot.modules.pagedEmbeds.PageListener;
import me.zoemartin.piratesBot.modules.pagedEmbeds.PagedEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class BlackList implements GuildCommand {

    @Override
    public String name() {
        return "blacklist";
    }

    @Override
    public Set<Command> subCommands() {
        return Set.of(new list(), new Channel(), new role());
    }

    @Override
    public String regex() {
        return "bl|blacklist";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        help(user, channel, List.of("level", "config", name()), original);
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_ADMIN;
    }

    @Override
    public String usage() {
        return "help";
    }

    @Override
    public String description() {
        return "Blacklist config";
    }

    private static class list implements GuildCommand {
        @Override
        public String name() {
            return "list";
        }

        @Override
        public String regex() {
            return "l|list";
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            Guild g = original.getGuild();
            LevelConfig config = Levels.getConfig(g);

            Collection<String> roles = config.getBlacklistedRoles();
            Collection<String> channels = config.getBlacklistedChannels();

            if (roles.isEmpty() && channels.isEmpty()) return;

            PagedEmbed p = new PagedEmbed(EmbedUtil.pagedDescription(
                new EmbedBuilder().setTitle("Blacklistings").build(),
                Stream.concat(roles.stream().filter(s -> !s.isEmpty()).map(s -> g.getRoleById(s) == null ? s :
                                                                                    g.getRoleById(s).getAsMention()),
                    channels.stream().filter(s -> !s.isEmpty()).map(s -> g.getTextChannelById(s) == null ? s :
                                                                             g.getTextChannelById(s).getAsMention()))
                    .map(s -> String.format("%s\n",s)).collect(Collectors.toList())),
                (TextChannel) channel, user
            );

            PageListener.add(p);
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public String description() {
            return "Lists all blacklistings";
        }
    }

    private static class Channel implements GuildCommand {
        @Override
        public Set<Command> subCommands() {
            return Set.of(new Channel.Remove());
        }

        @Override
        public String name() {
            return "channel";
        }

        @Override
        public String regex() {
            return "c|channel";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            Check.check(!args.isEmpty(), CommandArgumentException::new);
            TextChannel c = Parser.Channel.getTextChannel(original.getGuild(),
                lastArg(0, args, original));

            Check.entityNotNull(c, TextChannel.class);
            LevelConfig config = Levels.getConfig(original.getGuild());
            config.addBlacklistedChannel(c.getId());
            DatabaseUtil.updateObject(config);
            addCheckmark(original);
            embedReply(original, channel, "Level Blacklist", "Blacklisted %s",
                c.getAsMention()).queue();
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public String usage() {
            return "<channel>";
        }

        @Override
        public String description() {
            return "Blacklist a channel";
        }

        private static class Remove implements GuildCommand {

            @Override
            public String name() {
                return "remove";
            }

            @Override
            public String regex() {
                return "rm|remove";
            }

            @Override
            public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
                Check.check(!args.isEmpty(), CommandArgumentException::new);
                TextChannel c = Parser.Channel.getTextChannel(original.getGuild(),
                    lastArg(0, args, original));

                Check.entityNotNull(c, TextChannel.class);
                LevelConfig config = Levels.getConfig(original.getGuild());
                if (!config.removeBlacklistedChannel(c.getId())) return;
                DatabaseUtil.updateObject(config);
                addCheckmark(original);
                embedReply(original, channel, "Level Blacklist", "Unblacklisted %s",
                    c.getAsMention()).queue();
            }

            @Override
            public CommandPerm commandPerm() {
                return CommandPerm.BOT_ADMIN;
            }

            @Override
            public String usage() {
                return "<channel>";
            }

            @Override
            public String description() {
                return "Removes a blacklisted channel";
            }
        }
    }

    private static class role implements GuildCommand {
        @Override
        public Set<Command> subCommands() {
            return Set.of(new role.Remove());
        }

        @Override
        public String name() {
            return "role";
        }

        @Override
        public String regex() {
            return "r|role";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            Check.check(!args.isEmpty(), CommandArgumentException::new);
            Role r = Parser.Role.getRole(original.getGuild(),
                lastArg(0, args, original));

            Check.entityNotNull(r, Role.class);
            LevelConfig config = Levels.getConfig(original.getGuild());
            config.addBlacklistedRole(r.getId());
            DatabaseUtil.updateObject(config);
            addCheckmark(original);
            embedReply(original, channel, "Level Blacklist", "Blacklisted %s",
                r.getAsMention()).queue();
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public String usage() {
            return "<role>";
        }

        @Override
        public String description() {
            return "Blacklist a channel";
        }

        private static class Remove implements GuildCommand {

            @Override
            public String name() {
                return "remove";
            }

            @Override
            public String regex() {
                return "rm|remove";
            }

            @Override
            public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
                Check.check(!args.isEmpty(), CommandArgumentException::new);
                Role r = Parser.Role.getRole(original.getGuild(),
                    lastArg(0, args, original));

                Check.entityNotNull(r, Role.class);
                LevelConfig config = Levels.getConfig(original.getGuild());
                if (!config.removeBlacklistedRole(r.getId())) return;
                DatabaseUtil.updateObject(config);
                addCheckmark(original);
                embedReply(original, channel, "Level Blacklist", "Unblacklisted %s",
                    r.getAsMention()).queue();
            }

            @Override
            public CommandPerm commandPerm() {
                return CommandPerm.BOT_ADMIN;
            }

            @Override
            public String usage() {
                return "<role>";
            }

            @Override
            public String description() {
                return "Removes a blacklisted role";
            }
        }
    }
}
