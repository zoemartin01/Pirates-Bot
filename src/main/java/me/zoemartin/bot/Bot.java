package me.zoemartin.bot;

import me.zoemartin.bot.base.managers.ModuleManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;

public class Bot {
    private static JDABuilder builder;
    private static JDA jda = null;

    public static final String VERSION = "0.0.6";
    public static final String JDA_VERSION = "4.2.0_203";

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
