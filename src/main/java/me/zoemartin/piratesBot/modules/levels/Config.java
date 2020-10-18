package me.zoemartin.piratesBot.modules.levels;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.*;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.*;
import me.zoemartin.piratesBot.modules.pagedEmbeds.PageListener;
import me.zoemartin.piratesBot.modules.pagedEmbeds.PagedEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Config implements GuildCommand {
    @Override
    public @NotNull Set<Command> subCommands() {
        return Set.of(new Enable(), new RoleRewards(), new Import(), new BlackList(), new Disable(), new Announce(),
            new SetExp(), new Clear());
    }

    @Override
    public @NotNull String name() {
        return "config";
    }

    @Override
    public @NotNull String regex() {
        return "config|conf";
    }

    @Override
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
        help(user, channel, List.of("level", name()), original);
    }

    @Override
    public @NotNull CommandPerm commandPerm() {
        return CommandPerm.BOT_ADMIN;
    }

    @Override
    public @NotNull String description() {
        return "Level Configuration";
    }

    private static class Enable implements GuildCommand {

        @Override
        public @NotNull String name() {
            return "enable";
        }

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            LevelConfig c = Levels.getConfig(original.getGuild());
            c.setEnabled(true);
            DatabaseUtil.updateObject(c);
            addCheckmark(original);
            embedReply(original, channel, "Levels", "Enabled Leveling System").queue();
        }

        @Override
        public @NotNull CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public @NotNull String description() {
            return "Enabled the Leveling System";
        }
    }

    private static class Disable implements GuildCommand {

        @Override
        public @NotNull String name() {
            return "disable";
        }

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            LevelConfig c = Levels.getConfig(original.getGuild());
            c.setEnabled(false);
            DatabaseUtil.updateObject(c);
            addCheckmark(original);
            embedReply(original, channel, "Levels", "Disabled Leveling System").queue();
        }

        @Override
        public @NotNull CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public @NotNull String description() {
            return "Disable the Leveling System";
        }
    }

    private static class Announce implements GuildCommand {

        @Override
        public @NotNull String name() {
            return "announce";
        }

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.size() == 1 && args.get(0).toLowerCase().matches("always|reward|never"),
                CommandArgumentException::new);

            LevelConfig config = Levels.getConfig(original.getGuild());

            switch (args.get(0).toLowerCase()) {
                case "always":
                    config.setAnnouncements(LevelConfig.Announcements.ALL);
                    break;

                case "reward":
                    config.setAnnouncements(LevelConfig.Announcements.REWARDS);
                    break;

                case "never":
                    config.setAnnouncements(LevelConfig.Announcements.NONE);
                    break;
            }

            DatabaseUtil.updateObject(config);
            addCheckmark(original);
            embedReply(original, channel, "Levels", "Level up announcements set to `%s`",
                config.getAnnouncements().toString()).queue();
        }

        @Override
        public @NotNull CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public @NotNull String description() {
            return "Announce level up ALWAYS/REWARD/NEVER";
        }
    }

    private static class SetExp implements GuildCommand {

        @Override
        public @NotNull String name() {
            return "setxp";
        }

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.size() == 2 && Parser.Int.isParsable(args.get(0))
                            && Parser.User.isParsable(args.get(1)), CommandArgumentException::new);

            int xp = Parser.Int.parse(args.get(0));
            User u = CacheUtils.getUserExplicit(args.get(1));

            Check.check(xp >= 0, () -> new ReplyError("Xp must be above 0"));
            UserLevel level = Levels.getUserLevel(original.getGuild(), u);
            level.setExp(xp);
            DatabaseUtil.updateObject(level);
            addCheckmark(original);
            embedReply(original, channel, "Levels", "Set %s's xp to `%s`", u.getAsMention(), xp).queue();
        }

        @Override
        public @NotNull CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public @NotNull String description() {
            return "Set's a users exp";
        }
    }

    /**
     * Input file format: *.json
     * <p>
     * [ { "Userid": id, "Exp": exp }, ... ]
     */
    private static class Import implements GuildCommand {

        @Override
        public @NotNull String name() {
            return "import";
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.isEmpty(), CommandArgumentException::new);
            Check.check(original.getAttachments().size() == 1, CommandArgumentException::new);
            Message m = channel.sendMessage("Okay... this might take a while").complete();
            Instant start = Instant.now();

            String guildId = original.getGuild().getId();
            Type listType = new TypeToken<ArrayList<ImportLevel>>() {
            }.getType();
            Map<String, Integer> levels;
            try (InputStreamReader ir = new InputStreamReader(original.getAttachments()
                                                                  .get(0)
                                                                  .retrieveInputStream()
                                                                  .get(1, TimeUnit.MINUTES))) {
                BufferedReader br = new BufferedReader(ir);
                levels = ((List<ImportLevel>) new Gson().fromJson(br, listType)).stream()
                             .collect(Collectors.toMap(ImportLevel::getUserId, ImportLevel::getExp));
            } catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
                throw new UnexpectedError(e);
            }

            levels.forEach((s, integer) -> Levels.importLevel(new UserLevel(guildId, s, integer)));
            addCheckmark(original);
            m.delete().complete();

            PagedEmbed p = new PagedEmbed(EmbedUtil.pagedDescription(
                new EmbedBuilder().setTitle("Imported Levels: " + levels.size()).build(),
                Stream.concat(
                    Stream.of(String.format("Time taken: %s seconds\n",
                        Duration.between(start, Instant.now()).toSeconds())),
                levels.entrySet().stream()
                    .sorted(Comparator.comparingInt((ToIntFunction<Map.Entry<String, Integer>>) Map.Entry::getValue)
                                .reversed())
                    .map(e -> {
                            User u = Bot.getJDA().getUserById(e.getKey());
                            if (u == null) return String.format("User: `%s` - Level: `%s` - Exp: `%s`\n", e.getKey(),
                                Levels.calcLevel(e.getValue()), e.getValue());
                            return String.format("User: %s - Level: `%s` - Exp: `%s`\n", u.getAsMention(),
                                Levels.calcLevel(e.getValue()), e.getValue());
                        }
                    )).collect(Collectors.toList())),
                channel, user.getUser());

            PageListener.add(p);
        }

        @Override
        public @NotNull CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public @NotNull String description() {
            return "Import Levels";
        }

        private static class ImportLevel implements Serializable {
            @SerializedName(value = "Userid")
            private final String userId;

            @SerializedName(value = "Exp")
            private final int exp;

            public ImportLevel(String userid, int exp) {
                this.userId = userid;
                this.exp = exp;
            }

            public String getUserId() {
                return userId;
            }

            public int getExp() {
                return exp;
            }
        }
    }

    private static class Clear implements GuildCommand {
        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.size() >= 1 && args.get(0).matches("--agree"),
                () -> new ReplyError("This action will clear the entire level database for this server. " +
                                         "To confirm this action rerun this command with the argument `--agree`!"));
            Instant start = Instant.now();

            Message m = channel.sendMessage("Okay... this might take a while").complete();
            Levels.getLevels(original.getGuild()).forEach(DatabaseUtil::deleteObject);
            Levels.clearGuildCache(original.getGuild());
            addCheckmark(original);
            m.delete().complete();
            embedReply(original, channel, "Level Config",
                "Successfully cleared all Levels\nTime taken: %s seconds",
                Duration.between(start, Instant.now()).toSeconds()).queue();
        }

        @NotNull
        @Override
        public String name() {
            return "clear";
        }

        @NotNull
        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @NotNull
        @Override
        public String description() {
            return "WARNING: CLEARS ALL LEVELS FROM THE DATABASE";
        }
    }

    private static class RoleRewards implements GuildCommand {

        @Override
        public @NotNull Set<Command> subCommands() {
            return Set.of(new RoleRewards.Add(), new RoleRewards.Remove(), new RoleRewards.list());
        }

        @Override
        public @NotNull String name() {
            return "rewards";
        }

        @Override
        public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
            help(user, channel, List.of("level", "config", name()), original);
        }

        @Override
        public @NotNull CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public @NotNull String description() {
            return "Manage Role Rewards";
        }

        private static class Add implements GuildCommand {

            @Override
            public @NotNull String name() {
                return "add";
            }

            @Override
            public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
                Check.check(args.size() > 1 && Parser.Int.isParsable(args.get(0)),
                    CommandArgumentException::new);

                String rRef = lastArg(1, args, original);
                Role r = Parser.Role.getRole(original.getGuild(), rRef);
                int level = Parser.Int.parse(args.get(0));

                Check.entityReferenceNotNull(r, Role.class, rRef);
                Check.check(level > 0, () -> new ReplyError("Error, Level must be bigger than 0"));

                LevelConfig config = Levels.getConfig(original.getGuild());
                config.addRewardRole(r.getId(), level);

                DatabaseUtil.updateObject(config);
                addCheckmark(original);
                embedReply(original, channel, "Level Rewards", "Added %s to Level %s",
                    r.getAsMention(), level).queue();
            }

            @Override
            public @NotNull CommandPerm commandPerm() {
                return CommandPerm.BOT_ADMIN;
            }

            @Override
            public @NotNull String usage() {
                return "<level> <role>";
            }

            @Override
            public @NotNull String description() {
                return "Adds Role Rewards";
            }
        }

        private static class Remove implements GuildCommand {

            @Override
            public @NotNull String name() {
                return "remove";
            }

            @Override
            public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
                Check.check(args.size() > 1, CommandArgumentException::new);

                String rRef = lastArg(1, args, original);
                Role r = Parser.Role.getRole(original.getGuild(), rRef);
                int level = Parser.Int.parse(args.get(0));
                Check.entityReferenceNotNull(r, Role.class, rRef);
                Check.check(level > 0, () -> new ReplyError("Error, Level must be bigger than 0"));

                LevelConfig config = Levels.getConfig(original.getGuild());
                if (!config.removeRewardRole(level, r.getId())) return;

                DatabaseUtil.updateObject(config);
                addCheckmark(original);
                embedReply(original, channel, "Level Rewards", "Removed %s from Level %s",
                    r.getAsMention(), level).queue();
            }

            @Override
            public @NotNull String usage() {
                return "<level> <role>";
            }

            @Override
            public @NotNull CommandPerm commandPerm() {
                return CommandPerm.BOT_ADMIN;
            }

            @Override
            public @NotNull String description() {
                return "Removes Role Rewards";
            }
        }

        private static class list implements GuildCommand {

            @Override
            public @NotNull String name() {
                return "list";
            }

            @Override
            public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
                Check.check(args.isEmpty(), CommandArgumentException::new);

                LevelConfig config = Levels.getConfig(original.getGuild());

                PagedEmbed p = new PagedEmbed(EmbedUtil.pagedDescription(
                    new EmbedBuilder().setTitle("Role Rewards").build(),
                    config.getRewardRoles().entrySet().stream()
                        .map(e ->
                                 String.format("Level: `%d` - Role(s): %s\n", e.getKey(),
                                     e.getValue().stream().map(s -> {
                                         Role r = original.getGuild().getRoleById(s);
                                         return r == null ? s : r.getAsMention();
                                     }).collect(Collectors.joining(", "))
                                 )
                        ).collect(Collectors.toList())),
                    channel, user.getUser());

                PageListener.add(p);
                addCheckmark(original);
            }

            @Override
            public @NotNull CommandPerm commandPerm() {
                return CommandPerm.BOT_ADMIN;
            }

            @Override
            public @NotNull String description() {
                return "Lists Role Rewards";
            }
        }
    }
}
