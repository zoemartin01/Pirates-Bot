package me.zoemartin.piratesBot.modules.piratesCommands;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class RandomGroupsCommandTest {
    /**
     * Distribute 50 real users to 10 channels --> 5 users each 
     */
    @Test
    public void testDistributeUsersEqual() {
        List<Member> members = mockMembers(50, false);
        VoiceChannel from = mock(VoiceChannel.class);
        when(from.getMembers()).thenReturn(members);
        VoiceChannel[] to = mockVoiceChannels(10);

        Map<Member, VoiceChannel> distribution = RandomGroupsCommand.distributeUsers(from, to);

        // all 50 users are real and should be distributed
        assertEquals(50, distribution.size());

        for (int i = 0; i < to.length; i++) {
            long amount = countUsersWithChannel(distribution, to[i]);
            // each channel should have 5 users
            assertEquals(5, amount);
        }
    }

    /**
     * Distribute 42 real users to 9 channels --> 4-5 users each
     */
    @Test
    public void testDistributeUsersUnequal() {
        List<Member> members = mockMembers(42, false);
        VoiceChannel from = mock(VoiceChannel.class);
        when(from.getMembers()).thenReturn(members);
        VoiceChannel[] to = mockVoiceChannels(9);

        Map<Member, VoiceChannel> distribution = RandomGroupsCommand.distributeUsers(from, to);

        // all 42 users are real and should be distributed
        assertEquals(42, distribution.size());

        for (int i = 0; i < to.length; i++) {
            long amount = countUsersWithChannel(distribution, to[i]);
            // each channel should have 4-5 users
            assertTrue(4 <= amount && amount <= 5);
        }
    }

    /**
     * Bots should be ignored by user distribution.
     */
    @Test
    public void testDistributeUsersIgnoreBots() {
        // 20 members (12 users, 8 bots)
        List<Member> members = mockMembers(5, true);
        members.addAll(mockMembers(10, false));
        members.addAll(mockMembers(3, true));
        members.addAll(mockMembers(2, false));

        VoiceChannel from = mock(VoiceChannel.class);
        when(from.getMembers()).thenReturn(members);
        VoiceChannel[] to = mockVoiceChannels(6);

        Map<Member, VoiceChannel> distribution = RandomGroupsCommand.distributeUsers(from, to);

        assertEquals(12, distribution.size());

        distribution.keySet().forEach(member -> {
            assertFalse(member.getUser().isBot());
        });

        for (int i = 0; i < to.length; i++) {
            assertEquals(2, countUsersWithChannel(distribution, to[i]));
        }
    }

    private long countUsersWithChannel(Map<Member, VoiceChannel> distribution, VoiceChannel channel) {
        return distribution.entrySet().stream()
            .filter(pair -> pair.getValue() == channel)
            .count();
    }

    private List<Member> mockMembers(int amount, boolean bot) {
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            User user = mock(User.class);
            when(user.isBot()).thenReturn(bot);

            Member member = mock(Member.class);
            when(member.getUser()).thenReturn(user);

            members.add(member);
        }
        return members;
    }

    private VoiceChannel[] mockVoiceChannels(int amount) {
        VoiceChannel[] channels = new VoiceChannel[amount];
        for (int i = 0; i < channels.length; i++) {
            channels[i] = mock(VoiceChannel.class);
        }
        return channels;
    }
}
