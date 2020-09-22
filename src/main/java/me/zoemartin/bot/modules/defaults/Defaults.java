package me.zoemartin.bot.modules.defaults;

import me.zoemartin.bot.Bot;
import me.zoemartin.bot.base.CommandPerm;
import me.zoemartin.bot.base.LoadModule;
import me.zoemartin.bot.base.interfaces.Module;
import me.zoemartin.bot.modules.commandProcessing.CommandHandler;
import me.zoemartin.bot.modules.commandProcessing.CommandListener;
import me.zoemartin.bot.modules.trigger.Triggers;

import java.util.concurrent.*;

@LoadModule(loadAfter = {CommandListener.class, Triggers.class})
public class Defaults implements Module {

    @Override
    public void init() {
        /*
         * Pirates Server Defaults
         */
        // zowee#0001
        CommandHandler.addMemberPerm("747773810517868555", "212591138945630213", CommandPerm.OWNER);
        // admin
        CommandHandler.addRolePerm("747773810517868555", "747858818205221054", CommandPerm.BOT_MANAGER);
        // tutor
        CommandHandler.addRolePerm("747773810517868555", "747774531992813579", CommandPerm.BOT_MODERATOR);

        /*
         * Test Server Defaults
         */
        // zowee#0001
        CommandHandler.addMemberPerm("672160078899445761", "212591138945630213", CommandPerm.OWNER);


        // Add triggers after JDA is built
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (Bot.getJDA() != null) {
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
