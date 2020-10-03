package me.zoemartin.piratesBot.core.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EmbedUtil {
    public static List<MessageEmbed> pagedFieldEmbed(MessageEmbed eb, List<MessageEmbed.Field> fields) {
        LinkedList<List<MessageEmbed.Field>> fList = new LinkedList<>();
        fList.add(new LinkedList<>());

        fields.forEach(field -> {
            int len = fList.getLast().stream()
                          .map(f -> (f.getValue() != null ? f.getValue().length() : 0)
                                        + (f.getName() != null ? f.getName().length() : 0))
                          .reduce(0, Integer::sum);

            int lenW = len + (field.getValue() != null ? field.getValue().length() : 0)
                           + (field.getName() != null ? field.getName().length() : 0);

            if (lenW >= 2500) fList.add(new LinkedList<>());
            fList.getLast().add(field);
        });

        return fList.stream().map(f -> {
            EmbedBuilder b = new EmbedBuilder()
                                 .setTitle(eb.getTitle())
                                 .setColor(eb.getColor());

            b.setFooter(String.format("Page %s out of %s", fList.indexOf(f) + 1, fList.size()));

            f.forEach(b::addField);
            return b.build();
        }).collect(Collectors.toList());
    }

    public static List<MessageEmbed> pagedDescription(MessageEmbed eb, List<String> input) {
        LinkedList<List<String>> fList = new LinkedList<>();
        fList.add(new LinkedList<>());

        input.forEach(s -> {
            int len = fList.getLast().stream()
                          .map(String::length)
                          .reduce(0, Integer::sum) + s.length() + 1;

            if (len >= 1024 || String.join("", fList.getLast()).chars().filter(c -> c == '\n').count() > 25)
                fList.add(new LinkedList<>());
            fList.getLast().add(s);
        });

        return fList.stream().map(f -> {
            EmbedBuilder b = new EmbedBuilder()
                                 .setTitle(eb.getTitle())
                                 .setColor(eb.getColor());

            b.setFooter(String.format("Page %s out of %s", fList.indexOf(f) + 1, fList.size()));
            f.forEach(b::appendDescription);

            return b.build();
        }).collect(Collectors.toList());
    }
}
