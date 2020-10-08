package me.zoemartin.piratesBot.modules.debug;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.util.Check;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Shutdown implements Command {
    private static final int EXIT_CODE_PROPER_SHUTDOWN = 0;
    private static final int EXIT_CODE_RESTART = 10;
    private static final int EXIT_CODE_UPGRADE = 20;

    @Override
    public @NotNull Set<Command> subCommands() {
        return Set.of(new Force(), new Upgrade(), new Restart());
    }

    @Override
    public @NotNull String name() {
        return "shutdown";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        channel.sendMessageFormat("Shutting down soon! :)").complete();
        Bot.shutdownWithCode(EXIT_CODE_PROPER_SHUTDOWN, false);
    }

    @Override
    public @NotNull CommandPerm commandPerm() {
        return CommandPerm.OWNER;
    }

    @Override
    public @NotNull String description() {
        return "Shuts down the bot after all RestActions have finished";
    }

    private static class Force implements Command {

        @Override
        public @NotNull String name() {
            return "force";
        }

        @Override
        public @NotNull String regex() {
            return "--force|-f|now";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            channel.sendMessageFormat("Shutting down now!").complete();
            Bot.shutdownWithCode(EXIT_CODE_PROPER_SHUTDOWN, true);
        }

        @Override
        public @NotNull CommandPerm commandPerm() {
            return CommandPerm.OWNER;
        }

        @Override
        public @NotNull String description() {
            return "Forces the bot to shut down and cancels RestActions";
        }
    }

    private static class Upgrade implements Command {

        @Override
        public @NotNull String name() {
            return "upgrade";
        }

        @Override
        public @NotNull String regex() {
            return "--upgrade|-u";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            channel.sendMessageFormat("Upgrading the bot and rebooting!").complete();
            Bot.shutdownWithCode(EXIT_CODE_UPGRADE, true);
        }

        @Override
        public @NotNull CommandPerm commandPerm() {
            return CommandPerm.OWNER;
        }

        @Override
        public @NotNull String description() {
            return "Upgrades the bot to the current version and restarts";
        }
    }

    private static class Restart implements Command {

        @Override
        public @NotNull String name() {
            return "restart";
        }

        @Override
        public @NotNull String regex() {
            return "--restart|-r|--reboot";
        }

        @Override
        public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
            channel.sendMessageFormat("Restarting the bot!").complete();
            Bot.shutdownWithCode(EXIT_CODE_RESTART, true);
        }

        @Override
        public @NotNull CommandPerm commandPerm() {
            return CommandPerm.OWNER;
        }

        @Override
        public @NotNull String description() {
            return "Upgrades the bot to the current version and restarts";
        }
    }
}
