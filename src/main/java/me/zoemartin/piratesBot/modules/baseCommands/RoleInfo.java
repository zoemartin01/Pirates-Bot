package me.zoemartin.piratesBot.modules.baseCommands;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.exceptions.ReplyError;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

public class RoleInfo implements GuildCommand {
    @Override
    public String name() {
        return "roleinfo";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(!args.isEmpty(), CommandArgumentException::new);

        Role role = null;
        String orig = original.getContentRaw();

        if (Parser.Role.isParsable(args.get(0))) role = original.getGuild().getRoleById(Parser.Role.parse(args.get(0)));
        else {
            List<Role> roles = original.getGuild()
                                   .getRolesByName(orig.substring(orig.indexOf(args.get(0))), true);

            if (!roles.isEmpty()) role = roles.get(0);
        }
        Check.entityNotNull(role, Role.class);

        EmbedBuilder eb =
            new EmbedBuilder()
                .addField("Name", role.getName(), true)
                .addField("ID", role.getId(), true)
                .addField("Color", role.getColor() != null ? "#" + Integer.toHexString(
                    role.getColor().getRGB()).substring(2) : "n/a", true)
                .addField("Mention", role.getAsMention(), true)
                .addField("Member Count", String.valueOf(role.getGuild().getMembersWithRoles(role).size()), true)
                .addField("Position", String.valueOf(role.getPositionRaw()), true)
                .addField("Hoisted", String.valueOf(role.isHoisted()), true)
                .addField("Mentionable", String.valueOf(role.isMentionable()), true)
                .addField("Created ago", MessageUtils.dateAgo(role.getTimeCreated(), OffsetDateTime.now()), false)

                .setFooter("ID: " + role.getId())
                .setTimestamp(Instant.now())
                .setColor(role.getColor());

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.BOT_MODERATOR;
    }

    @Override
    public String usage() {
        return "roleinfo <@role>";
    }

    @Override
    public String description() {
        return "Gives information about a role";
    }
}
