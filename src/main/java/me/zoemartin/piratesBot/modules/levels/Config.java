package me.zoemartin.piratesBot.modules.levels;

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
import org.json.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class Config implements GuildCommand {
    @Override
    public Set<Command> subCommands() {
        return Set.of(new Enable(), new RoleRewards(), new Import(), new BlackList(), new Disable(), new Announce(),
            new SetExp());
    }

    @Override
    public String name() {
        return "config";
    }

    @Override
    public String regex() {
        return "config|conf";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        help(user, channel, List.of("level", name()), original);
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_ADMIN;
    }

    @Override
    public String description() {
        return "Level Configuration";
    }

    private static class Enable implements GuildCommand {

        @Override
        public String name() {
            return "enable";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            LevelConfig c = Levels.getConfig(original.getGuild());
            c.setEnabled(true);
            DatabaseUtil.updateObject(c);
            addCheckmark(original);
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public String description() {
            return "Enabled the Leveling System";
        }
    }

    private static class Disable implements GuildCommand {

        @Override
        public String name() {
            return "disable";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            LevelConfig c = Levels.getConfig(original.getGuild());
            c.setEnabled(false);
            DatabaseUtil.updateObject(c);
            addCheckmark(original);
            embedReply(original, channel, "Levels", "Disabled Leveling System").queue();
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public String description() {
            return "Disable the Leveling System";
        }
    }

    private static class Announce implements GuildCommand {

        @Override
        public String name() {
            return "announce";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
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
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public String description() {
            return "Announce level up ALWAYS/REWARD/NEVER";
        }
    }

    private static class SetExp implements GuildCommand {

        @Override
        public String name() {
            return "setxp";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
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
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public String description() {
            return "Set's a users exp";
        }
    }

    /**
     * Input file format:
     * *.json
     *
     * {
     *  "levels": [
     *      {
     *          "Userid": id,
     *          "Exp": exp
     *      },
     *      ...
     *  ]
     * }
     */
    private static class Import implements GuildCommand {

        @Override
        public String name() {
            return "import";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.isEmpty(), CommandArgumentException::new);
            Check.check(original.getAttachments().size() == 1, CommandArgumentException::new);
            Message m = channel.sendMessage("Okay... this might take a while").complete();

            InputStreamReader ir;
            BufferedReader br;
            try {
                ir = new InputStreamReader(original.getAttachments().get(0).retrieveInputStream().get(1, TimeUnit.MINUTES));
                br = new BufferedReader(ir);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new UnexpectedError(e);
            }

            Map<String, Integer> levels = new ConcurrentHashMap<>();

            String jsonString = br.lines().collect(Collectors.joining());
            try {
                JSONObject obj = new JSONObject(jsonString);
                JSONArray arr = obj.getJSONArray("levels");
                for (int i = 0; i < arr.length(); i++) {
                    String userId = arr.getJSONObject(i).getString("Userid");
                    int exp = arr.getJSONObject(i).getInt("Exp");
                    levels.put(userId, exp);
                }
            } catch (JSONException e) {
                throw new ReplyError("Import file in wrong format.");
            }

            String guildId = original.getGuild().getId();

            levels.forEach((s, integer) -> Levels.importLevel(new UserLevel(guildId, s, integer)));
            addCheckmark(original);
            m.delete().complete();

            PagedEmbed p = new PagedEmbed(EmbedUtil.pagedDescription(
                new EmbedBuilder().setTitle("Imported Levels: " + levels.size()).build(),
                levels.entrySet().stream()
                    .map(e -> {
                            User u = Bot.getJDA().getUserById(e.getKey());
                            if (u == null) return String.format("User: `%s` - Level: `%s` - Exp: `%s`\n", e.getKey(),
                                Levels.calcLevel(e.getValue()), e.getValue());
                            return String.format("User: %s - Level: `%s` - Exp: `%s`\n", u.getAsMention(),
                                Levels.calcLevel(e.getValue()), e.getValue());
                        }
                    ).collect(Collectors.toList())),
                (TextChannel) channel, user);

            PageListener.add(p);
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public String description() {
            return "Import Levels";
        }
    }

    private static class RoleRewards implements GuildCommand {

        @Override
        public Set<Command> subCommands() {
            return Set.of(new RoleRewards.Add(), new RoleRewards.Remove(), new RoleRewards.list());
        }

        @Override
        public String name() {
            return "rewards";
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
        public String description() {
            return "Manage Role Rewards";
        }

        private static class Add implements GuildCommand {

            @Override
            public String name() {
                return "add";
            }

            @Override
            public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
                Check.check(args.size() > 1 && Parser.Int.isParsable(args.get(0)),
                    CommandArgumentException::new);

                Role r = Parser.Role.getRole(original.getGuild(), lastArg(1, args, original));
                int level = Parser.Int.parse(args.get(0));

                Check.entityNotNull(r, Role.class);
                Check.check(level > 0, () -> new ReplyError("Error, Level must be bigger than 0"));

                LevelConfig config = Levels.getConfig(original.getGuild());
                config.addRewardRole(r.getId(), level);

                DatabaseUtil.updateObject(config);
                addCheckmark(original);
                embedReply(original, channel, "Level Rewards", "Added %s to Level %s",
                    r.getAsMention(), level).queue();
            }

            @Override
            public CommandPerm commandPerm() {
                return CommandPerm.BOT_ADMIN;
            }

            @Override
            public String usage() {
                return "<level> <role>";
            }

            @Override
            public String description() {
                return "Adds Role Rewards";
            }
        }

        private static class Remove implements GuildCommand {

            @Override
            public String name() {
                return "remove";
            }

            @Override
            public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
                Check.check(args.size() > 1, CommandArgumentException::new);

                Role r = Parser.Role.getRole(original.getGuild(), lastArg(1, args, original));
                int level = Parser.Int.parse(args.get(0));
                Check.entityNotNull(r, Role.class);
                Check.check(level > 0, () -> new ReplyError("Error, Level must be bigger than 0"));

                LevelConfig config = Levels.getConfig(original.getGuild());
                if (!config.removeRewardRole(level, r.getId())) return;

                DatabaseUtil.updateObject(config);
                addCheckmark(original);
                embedReply(original, channel, "Level Rewards", "Removed %s from Level %s",
                    r.getAsMention(), level).queue();
            }

            @Override
            public String usage() {
                return "<level> <role>";
            }

            @Override
            public CommandPerm commandPerm() {
                return CommandPerm.BOT_ADMIN;
            }

            @Override
            public String description() {
                return "Removes Role Rewards";
            }
        }

        private static class list implements GuildCommand {

            @Override
            public String name() {
                return "list";
            }

            @Override
            public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
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
                    (TextChannel) channel, user);

                PageListener.add(p);
                addCheckmark(original);
            }

            @Override
            public CommandPerm commandPerm() {
                return CommandPerm.BOT_ADMIN;
            }

            @Override
            public String description() {
                return "Lists Role Rewards";
            }
        }
    }
}
