package me.zoemartin.piratesBot;

import me.zoemartin.piratesBot.core.managers.ModuleManager;
import me.zoemartin.piratesBot.core.util.DatabaseUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import javax.security.auth.login.LoginException;
import java.util.Properties;

public class Bot {
    private static JDABuilder builder;
    private static JDA jda = null;
    private static final String OWNER = "212591138945630213";

    public static final String VERSION = "0.2.1";
    public static final String JDA_VERSION = "4.2.0_203";

    public static void main(String[] args) throws LoginException {
        builder = JDABuilder.createDefault(args[0]);

        ModuleManager.init();

        Configuration config = new Configuration();
        Properties settings = new Properties();

        settings.put(Environment.DRIVER, "org.postgresql.Driver");
        settings.put(Environment.URL, args[1]);
        settings.put(Environment.USER, args[2]);
        settings.put(Environment.PASS, args[3]);
        settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQL82Dialect");
        settings.put(Environment.POOL_SIZE, 1);
        settings.put(Environment.SHOW_SQL, true);
        settings.put(Environment.HBM2DDL_AUTO, "update");
        config.setProperties(settings);

        DatabaseUtil.setConfig(config);

        ModuleManager.initLate();

        builder.enableIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setCompression(Compression.NONE);
        builder.setActivity(Activity.watching("y'all"));

        jda = builder.build();
    }

    public static void addListener(Object... listeners) {
        builder.addEventListeners(listeners);
    }

    public static JDA getJDA() {
        return jda;
    }

    public static String getOWNER() {
        return OWNER;
    }
}
