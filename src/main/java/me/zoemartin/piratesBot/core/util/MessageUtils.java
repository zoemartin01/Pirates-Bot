package me.zoemartin.piratesBot.core.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.time.*;
import java.util.Collection;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    private static final String EVERYONE_REGEX = "@(everyone|here)";
    private static final String ROLE_REGEX = "(<@&(\\d{17,19})>)";
    private static final String BLANK_CHAR = "\u200B";

    public static String cleanMessage(Member member, String message) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) return message;

        EnumSet<Permission> perms = member.getPermissions();
        String output = message;
        Guild guild = member.getGuild();


        if (!perms.contains(Permission.MESSAGE_MENTION_EVERYONE)) {
            output = cleanEveryone(output);

            Pattern pattern = Pattern.compile(ROLE_REGEX);
            Matcher matcher = pattern.matcher(output);

            while(matcher.find()) {
                Role r = guild.getRoleById(matcher.group(2));

                if (r != null && !r.isMentionable()) {
                    output = output.replace(matcher.group(1), String.format("@%s%s", BLANK_CHAR, r.getName()));
                }
            }
        }
        return output;
    }

    private static String cleanEveryone(String msg) {
        return msg.replaceAll(MessageUtils.EVERYONE_REGEX, "@" + BLANK_CHAR + "$1");
    }

    public static String dateAgo(OffsetDateTime start, OffsetDateTime end) {
        Period period = Period.between(start.toLocalDate(), end.toLocalDate());

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();
        int hours = Duration.between(start.toLocalDateTime(), end.toLocalDateTime()).toHoursPart();

        if (years > 0) return String.format("%s years, %s months and %s days", years, months, days);
        if (months > 0) return String.format("%s months, %s days and %s hours", months, days, hours);
        if (days > 0) return String.format("%s days and %s hours", days, hours);
        else return String.format("%s hours", hours);
    }

    public static String getArgsFrom(String original, String substring) {
        return original.substring(original.indexOf(substring));
    }
}
