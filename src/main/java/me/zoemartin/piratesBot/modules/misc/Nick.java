package me.zoemartin.piratesBot.modules.misc;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.GuildCommand;
import me.zoemartin.piratesBot.core.util.Check;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Nick implements GuildCommand {
    @Override
    public void run(Member user, TextChannel channel, List<String> args, Message original, String invoked) {
        Check.check(!args.isEmpty(), CommandArgumentException::new);
        user.modifyNickname(lastArg(0, args, original)).queue();
        addCheckmark(original);
    }

    @NotNull
    @Override
    public String name() {
        return "nick";
    }

    @NotNull
    @Override
    public CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @NotNull
    @Override
    public String usage() {
        return "<new nickname>";
    }

    @NotNull
    @Override
    public String description() {
        return "Set your own nickname";
    }
}
