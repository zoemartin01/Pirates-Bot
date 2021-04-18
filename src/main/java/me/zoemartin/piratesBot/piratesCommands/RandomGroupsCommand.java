package me.zoemartin.piratesBot.piratesCommands;

import me.zoemartin.rubie.core.CommandPerm;
import me.zoemartin.rubie.core.GuildCommandEvent;
import me.zoemartin.rubie.core.annotations.Command;
import me.zoemartin.rubie.core.annotations.CommandOptions;
import me.zoemartin.rubie.core.exceptions.CommandArgumentException;
import me.zoemartin.rubie.core.exceptions.ReplyError;
import me.zoemartin.rubie.core.interfaces.GuildCommand;
import me.zoemartin.rubie.core.util.Check;
import me.zoemartin.rubie.core.util.Parser;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.stream.Collectors;

@Command
@CommandOptions(
    name = "random-groups",
    alias = "rg",
    description = "Distribute users to random voice channels with an equal group sizes.",
    perm = CommandPerm.BOT_MODERATOR,
    usage = "<from-voice-channel> <group-voice-channels-1> <group-voice-channel-2> [<group-voice-channels...>]",
    botPerms = Permission.VOICE_MOVE_OTHERS

)
public class RandomGroupsCommand extends GuildCommand {
    @Override
    public void run(GuildCommandEvent event) {
        var guild = event.getGuild();
        var args = event.getArgs();

        // there need to be at least 3 voice channels referenced:
        // 1 from-channel + at least 2 group-channels
        Check.check(args.size() >= 2, CommandArgumentException::new);

        VoiceChannel fromChannel = findVoiceChannel(guild, event.getMember(), args.get(0));
        List<VoiceChannel> toChannels = args.subList(1, args.size()).stream()
                                            .map(reference -> findVoiceChannel(guild, event.getMember(), reference))
                                            .distinct()
                                            .collect(Collectors.toList());

        Check.check(toChannels.size() >= 2,
            () -> new ReplyError("Please specify at least two different group voice-channels!"));

        Map<Member, VoiceChannel> distribution = distributeUsers(fromChannel, toChannels);

        distribution.forEach((key, value) -> guild.moveVoiceMember(key, value).queue());

        event.reply("Random Groups",
            "Distributed %s users from the %s channel to these channels:\n • %s",
            distribution.entrySet().size(), fromChannel.getName(),
            toChannels.stream().map(VoiceChannel::getName)
                .collect(Collectors.joining("\n • "))).queue();
    }

    private VoiceChannel findVoiceChannel(Guild guild, Member executor, String reference) {
        if (reference.equalsIgnoreCase("here")) {
            GuildVoiceState voice = executor.getVoiceState();
            Check.check(voice != null && voice.inVoiceChannel(),
                () -> new ReplyError("You can't use `here` if you're not in a voice channel!"));
            return voice.getChannel();
        } else {
            VoiceChannel channel = Parser.Channel.getVoiceChannel(guild, reference);
            Check.entityReferenceNotNull(channel, VoiceChannel.class, reference);
            return channel;
        }
    }

    /**
     * Creates a user-channel map for distributing all users (excluding bots) in one channel to an array of group
     * channels. The resulting distribution aims for similar group sizes (group sizes vary at most by 1).
     *
     * @param from
     *     the channel to distribute users from.
     * @param to
     *     the group channels to distribute users to.
     *
     * @return a map that shows which user needs to be moved in which channel.
     */
    public static Map<Member, VoiceChannel> distributeUsers(VoiceChannel from, List<VoiceChannel> to) {
        List<Member> membersToDistribute = from.getMembers().stream()
                                               .filter(member -> !member.getUser().isBot())
                                               .collect(Collectors.toList());

        Collections.shuffle(membersToDistribute);

        Map<Member, VoiceChannel> memberChannels = new HashMap<>();

        for (int i = 0; i < membersToDistribute.size(); i++) {
            Member member = membersToDistribute.get(i);
            VoiceChannel target = to.get(i % to.size());
            memberChannels.put(member, target);
        }

        return memberChannels;
    }
}
