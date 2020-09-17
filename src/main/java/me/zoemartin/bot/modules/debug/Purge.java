package me.zoemartin.bot.modules.debug;

import me.zoemartin.bot.base.exceptions.CommandArgumentException;
import me.zoemartin.bot.base.interfaces.Command;
import me.zoemartin.bot.base.interfaces.GuildCommand;
import me.zoemartin.bot.base.util.Check;
import me.zoemartin.bot.base.util.Parser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Purge implements GuildCommand {
    @Override
    public String name() {
        return "purge";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original) {
        Check.check(args.size() == 1 && Parser.Int.isParsable(args.get(0)), CommandArgumentException::new);
        int amount = Parser.Int.parse(args.get(0));

        Check.check(amount >= 0, CommandArgumentException::new);

        int y = amount % 100 == 0 ? 100 : amount % 100;
        int x = y == 0 ? (amount / 100) - 1 : amount / 100;

        OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);


        Set<List<Message>> msgs = new HashSet<>();
        Message last = original;
        for (int i = 0; i < x; i++) {
            List<Message> m = new ArrayList<>(channel.getHistoryBefore(last.getId(), 100).complete().getRetrievedHistory());
            msgs.add(m);
            last = m.get(m.size() - 1);
        }
        msgs.add(new ArrayList<>(channel.getHistoryBefore(last.getId(), y).complete().getRetrievedHistory()));


        Map<String, Long> count = new ConcurrentHashMap<>();

        msgs.forEach(messages -> {
            messages.removeIf(m -> m.getTimeCreated().isBefore(twoWeeksAgo));

            Map<String, Long> c = messages.stream()
                                      .map(message -> message.getAuthor().getAsTag())
                                      .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            c.forEach((s, aLong) -> count.put(s, count.getOrDefault(s + aLong, aLong)));
        });

        msgs.removeIf(List::isEmpty);

        original.delete().queue();
        Instant start = Instant.now();
        msgs.forEach(messages -> ((TextChannel) channel).deleteMessages(messages).complete());
        Instant end = Instant.now();


        long counter = count.values().stream().mapToLong(Long::longValue).sum();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(String.format("Purged %d messages", counter));
        eb.setColor(0x2F3136);
        StringBuilder sb = new StringBuilder();
        count.forEach((s, aLong) -> sb.append("**").append(s).append("**: ").append(aLong).append(" messages\n"));
        sb.append("\nTime: ").append(Duration.between(start, end).toMillis()).append("ms");
        eb.setDescription(sb.toString());

        channel.sendMessage(eb.build()).complete().delete().queueAfter(5, TimeUnit.SECONDS);
    }

    @Override
    public Permission required() {
        return Permission.ADMINISTRATOR;
    }

    @Override
    public String usage() {
        return "purge <count>";
    }
}
