package me.zoemartin.piratesBot.modules.debug;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.*;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.Parser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Purge implements GuildCommand {
    @Override
    public Set<Command> subCommands() {
        return Collections.emptySet();
    }

    @Override
    public String name() {
        return "purge";
    }

    @Override
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
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
        msgs.forEach(messages -> channel.deleteMessages(messages).complete());
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
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_MANAGER;
    }

    @Override
    public String usage() {
        return "<count>";
    }

    @Override
    public String description() {
        return "Purges the last sent messages in this channel";
    }
}
