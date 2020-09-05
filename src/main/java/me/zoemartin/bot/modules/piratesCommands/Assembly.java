package me.zoemartin.bot.modules.piratesCommands;

import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Assembly {
    private static final List<Assembly> assemblages = new ArrayList<>();

    private final Map<Member, VoiceChannel> assembly;

    public static int addAssembly(Assembly assembly) {
        assemblages.add(assembly);
        return assemblages.size() - 1;
    }

    public static Assembly dissolve(int id) {
        if (0 > id || id >= assemblages.size()) return null;
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


}
