package me.zoemartin.bot;

import me.zoemartin.bot.base.managers.CommandManager;
import me.zoemartin.bot.base.managers.ModuleManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Bot {
    private static JDABuilder builder;
    private static JDA jda;

    public static void main(String[] args) throws LoginException {
        builder = JDABuilder.createDefault(args[0]);

        builder.enableIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setCompression(Compression.NONE);
        builder.setActivity(Activity.watching("y'all"));

        ModuleManager.init();

        jda = builder.build();
    }

    public static void addListener(Object... listeners) {
        builder.addEventListeners(listeners);
    }

    public static JDA getJDA() {
        return jda;
    }
}
