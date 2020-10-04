package me.zoemartin.piratesBot.modules.moderation;

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
import org.hibernate.Session;

import javax.persistence.criteria.*;
import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Warn implements GuildCommand {
    @Override
    public Set<Command> subCommands() {
        return Set.of(new list(), new Remove(), new BulkImport(), new BulkImportFile());
    }

    @Override
    public String name() {
        return "warn";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.size() > 1 && Parser.User.isParsable(args.get(0)), CommandArgumentException::new);
        String userId = args.get(0);

        User u = Bot.getJDA().getUserById(Parser.User.parse(userId));
        Check.notNull(u, UserNotFoundException::new);

        String reason = lastArg(1, args, original);

        WarnEntity warnEntity = new WarnEntity(
            original.getGuild().getId(), u.getId(), user.getId(), reason, original.getTimeCreated().toEpochSecond());

        DatabaseUtil.saveObject(warnEntity);
        EmbedBuilder eb = new EmbedBuilder()
                              .setTitle("Warning added")
                              .setAuthor(String.format("%s / %s", u.getAsTag(), u.getId()), null, u.getEffectiveAvatarUrl())
                              .setDescription(String.format("Successfully warned %s for:\n\n%s", u.getAsMention(), warnEntity.getReason()));

        if (!u.isBot())
            u.openPrivateChannel().complete()
                .sendMessageFormat("You have received a warning from a Moderator on `%s`. \n**Reason**:\n\n%s",
                    original.getGuild().getName(), warnEntity.getReason()).queue();

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_MODERATOR;
    }

    @Override
    public String usage() {
        return "warn <user> <reason>";
    }

    @Override
    public String description() {
        return "Warn a user";
    }

    private static class list implements GuildCommand {

        @Override
        public String name() {
            return "list";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            String userId = args.get(0);
            Check.check(args.size() == 1 && Parser.User.isParsable(userId), CommandArgumentException::new);

            User u = Bot.getJDA().getUserById(Parser.User.parse(userId));
            Check.notNull(u, UserNotFoundException::new);

            Session s = DatabaseUtil.getSessionFactory().openSession();
            CriteriaBuilder cb = s.getCriteriaBuilder();

            CriteriaQuery<WarnEntity> q = cb.createQuery(WarnEntity.class);
            Root<WarnEntity> r = q.from(WarnEntity.class);
            List<WarnEntity> warns = s.createQuery(q.select(r).where(
                cb.equal(r.get("guild_id"), original.getGuild().getId()),
                cb.equal(r.get("user_id"), userId))).getResultList();

            List<MessageEmbed> pages = EmbedUtil.pagedFieldEmbed(
                new EmbedBuilder()
                    .setAuthor(String.format("%s / %s", u.getAsTag(), u.getId()),
                        null, u.getEffectiveAvatarUrl())
                    .setTitle("Warnings (" + warns.size() + ")").build(), warns.stream().map(e -> {
                    User moderator = Bot.getJDA().getUserById(e.getModerator_id());
                    return new MessageEmbed.Field("Warn ID: `" + e.getUuid() + "`",
                        String.format("**Responsible Moderator**: %s\n\n" +
                                          "**On**: %s\n\n" +
                                          "**Reason**: %s",
                            moderator != null ? moderator.getAsMention() : e.getModerator_id(),
                            Timestamp.valueOf(Instant.ofEpochSecond(e.getTimestamp())
                                                  .atOffset(ZoneOffset.UTC).toLocalDateTime()),
                            e.getReason()), true);
                }).collect(Collectors.toList())
            );

            PageListener.add(new PagedEmbed(pages, (TextChannel) channel, user));
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_MODERATOR;
        }

        @Override
        public String usage() {
            return "warn list <user>";
        }

        @Override
        public String description() {
            return "Lists a users warns";
        }
    }

    private static class Remove implements GuildCommand {
        @Override
        public String name() {
            return "remove";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.size() == 1, CommandArgumentException::new);

            UUID uuid = UUID.fromString(args.get(0));

            Session s = DatabaseUtil.getSessionFactory().openSession();
            CriteriaBuilder cb = s.getCriteriaBuilder();

            CriteriaQuery<WarnEntity> q = cb.createQuery(WarnEntity.class);
            Root<WarnEntity> r = q.from(WarnEntity.class);
            List<WarnEntity> warns = s.createQuery(q.select(r).where(
                cb.equal(r.get("guild_id"), original.getGuild().getId()),
                cb.equal(r.get("uuid"), uuid))).getResultList();

            WarnEntity warn = warns.isEmpty() ? null : warns.get(0);
            Check.notNull(warn, () -> new ReplyError("No warning with the ID `%s`", uuid));

            User u = Bot.getJDA().getUserById(warn.getUser_id());

            DatabaseUtil.deleteObject(warn);

            EmbedBuilder eb = new EmbedBuilder()
                                  .setTitle("Warning removed")
                                  .setDescription(String.format("Successfully removed warn `%s`", uuid));

            if (u != null)
                eb.setAuthor(String.format("%s / %s", u.getAsTag(), u.getId()), null, u.getEffectiveAvatarUrl());

            channel.sendMessage(eb.build()).queue();
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_MANAGER;
        }

        @Override
        public String usage() {
            return "warn remove <uuid>";
        }

        @Override
        public String description() {
            return "Remove a warning";
        }
    }

    private static class BulkImport implements GuildCommand {

        @Override
        public String name() {
            return "import";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            Check.check(!args.isEmpty(), CommandArgumentException::new);
            String[] split = original.getContentRaw().split("\n");
            List<String> input = List.of(split).subList(1, split.length);

            List<WarnEntity> warns = input.stream().map(s -> {
                List<String> keys = List.of(s.split("\\s+", 3));
                Check.check(keys.size() == 3, CommandArgumentException::new);

                Check.check(Parser.User.isParsable(keys.get(0)), CommandArgumentException::new);
                User u = Bot.getJDA().getUserById(keys.get(0));
                Check.notNull(u, UserNotFoundException::new);

                User mod;
                if (Parser.User.isParsable(keys.get(1))) mod = Bot.getJDA().getUserById(keys.get(1));
                else mod = Bot.getJDA().getUserByTag(keys.get(1));
                Check.notNull(mod, UserNotFoundException::new);

                return new WarnEntity(original.getGuild().getId(), u.getId(), mod.getId(), keys.get(2),
                    original.getTimeCreated().toEpochSecond());
            }).collect(Collectors.toList());

            Message m = channel.sendMessage("Okay... this might take a while").complete();
            warns.forEach(DatabaseUtil::saveObject);
            Set<String> users = warns.stream().map(WarnEntity::getUser_id).collect(Collectors.toCollection(HashSet::new));

            EmbedBuilder eb = new EmbedBuilder().setTitle("Bulk Warn Import");
            eb.setDescription("Imported warns:\n" + String.join("\n", users));
            m.delete().complete();
            channel.sendMessage(eb.build()).queue();
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public String usage() {
            return "warn import \n<user> <moderator> <reason>";
        }

        @Override
        public String description() {
            return "Bulk Import Warns. These warns are added silently. One Line for each Warn.";
        }
    }

    private static class BulkImportFile implements GuildCommand {

        @Override
        public String name() {
            return "importfile";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            Check.check(args.isEmpty(), CommandArgumentException::new);
            Check.check(original.getAttachments().size() == 1, CommandArgumentException::new);
            Message m = channel.sendMessage("Okay... this might take a while").complete();

            InputStreamReader ir;
            BufferedReader br;
            BufferedInputStream bi;
            try {
                ir = new InputStreamReader(original.getAttachments().get(0).retrieveInputStream().get(1, TimeUnit.MINUTES));
                br = new BufferedReader(ir);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new UnexpectedError(e);
            }

            List<String> input = br.lines().collect(Collectors.toList());
            List<WarnEntity> warns = input.stream().map(s -> {
                List<String> keys = List.of(s.split("\\s+", 3));
                Check.check(keys.size() == 3, CommandArgumentException::new);

                Check.check(Parser.User.isParsable(keys.get(0)), CommandArgumentException::new);
                User u = Bot.getJDA().getUserById(keys.get(0));
                Check.notNull(u, UserNotFoundException::new);

                User mod;
                if (Parser.User.isParsable(keys.get(1))) mod = Bot.getJDA().getUserById(keys.get(1));
                else mod = Bot.getJDA().getUserByTag(keys.get(1));
                Check.notNull(mod, UserNotFoundException::new);

                return new WarnEntity(original.getGuild().getId(), u.getId(), mod.getId(), keys.get(2),
                    original.getTimeCreated().toEpochSecond());
            }).collect(Collectors.toList());
            warns.forEach(DatabaseUtil::saveObject);
            Set<String> users = warns.stream().map(WarnEntity::getUser_id).collect(Collectors.toCollection(HashSet::new));

            EmbedBuilder eb = new EmbedBuilder().setTitle("Bulk Warn Import");
            eb.setDescription("Imported warns:\n" + String.join("\n", users));
            m.delete().complete();
            channel.sendMessage(eb.build()).queue();
        }

        @Override
        public CommandPerm commandPerm() {
            return CommandPerm.BOT_ADMIN;
        }

        @Override
        public String usage() {
            return "warn importfile";
        }

        @Override
        public String description() {
            return "Bulk Import Warns. These warns are added silently. Attach a text file with one Line for each warn.";
        }
    }
}
