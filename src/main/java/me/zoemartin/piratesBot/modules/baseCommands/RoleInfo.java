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
        Check.check(args.size() == 1 && Parser.Role.isParsable(args.get(0)),
            CommandArgumentException::new);

        Role role = original.getGuild().getRoleById(Parser.Role.parse(args.get(0)));
        Check.notNull(role, () -> new ReplyError("Error, role not found!"));

        EmbedBuilder eb = new EmbedBuilder();
        eb.addField("Name", role.getName(), true);
        eb.addField("ID", role.getId(), true);
        eb.addField("Color", role.getColor() != null ? "#"+Integer.toHexString(role.getColor().getRGB()).substring(2): "n/a", true);
        eb.addField("Mention", role.getAsMention(), true);
        eb.addField("Member Count", role.getGuild().getMembersWithRoles(role).size() + "", true);
        eb.addField("Position", role.getPositionRaw() + "", true);
        eb.addField("Created ago", MessageUtils.dateAgo(role.getTimeCreated(), OffsetDateTime.now()), true);

        eb.setFooter("ID: " + role.getId());
        eb.setTimestamp(Instant.now());
        eb.setColor(role.getColor());

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
