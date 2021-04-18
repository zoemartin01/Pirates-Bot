package me.zoemartin.piratesBot.piratesCommands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Assembly {
    private static final Map<String, Assembly> assemblages = new ConcurrentHashMap<>();

    private final Map<Member, VoiceChannel> assembly;

    public static String addAssembly(Assembly assembly) {
        String id;
        do {
            id = genId();
        } while (assemblages.containsKey(id));

        assemblages.put(id, assembly);
        return id;
    }

    public static Assembly dissolve(String id) {
        return assemblages.remove(id);
    }

    public Assembly() {
        this.assembly = new ConcurrentHashMap<>();
    }

    public void add(Member member, VoiceChannel voiceChannel) {
        assembly.put(member, voiceChannel);
    }

    public Map<Member, VoiceChannel> getAssembly() {
        return Collections.unmodifiableMap(assembly);
    }

    private static String genId() {
        return RandomStringUtils.random(8, "0123456789abcdef");
    }

}
