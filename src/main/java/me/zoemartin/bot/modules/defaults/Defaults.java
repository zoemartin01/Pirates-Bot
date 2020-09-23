package me.zoemartin.bot.modules.defaults;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.CommandPerm;
import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.interfaces.Module;
import me.zoemartin.bot.base.managers.CommandManager;
import me.zoemartin.bot.modules.commandProcessing.CommandListener;
import me.zoemartin.bot.modules.trigger.Triggers;

import java.util.concurrent.*;

@LoadModule(loadAfter = {CommandListener.class})
public class Defaults implements Module {

    @Override
    public void init() {
        /*
         * Pirates Server Defaults
         */
        // admin
        CommandManager.addRolePerm("747773810517868555", "747858818205221054", CommandPerm.BOT_MANAGER);
        // tutor
        CommandManager.addRolePerm("747773810517868555", "747774531992813579", CommandPerm.BOT_MODERATOR);

        /*
         * Test Server Defaults
         */
        // Bot Roles
        CommandManager.addRolePerm("672160078899445761", "758058237122707548", CommandPerm.BOT_MODERATOR);
        CommandManager.addRolePerm("672160078899445761", "758058198405087232", CommandPerm.BOT_ADMIN);
        CommandManager.addRolePerm("672160078899445761", "758058159222030406", CommandPerm.BOT_MANAGER);



        // Add stuff after JDA is built
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            // zowee#0001 bot owner on ever avaliable guild
            if (Bot.getJDA() != null) {
                Bot.getJDA().getGuilds().forEach(
                    guild ->
                        CommandManager.addMemberPerm(guild.getId(), "212591138945630213", CommandPerm.OWNER));
                addTriggers();
                executor.shutdown();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public static void addTriggers() {
        Triggers t = Triggers.get(Bot.getJDA().getGuildById("747773810517868555"));

        t.addTrigger("[Yy]ohoho!?", "und ne Buddel voll Rum!");
    }
}
