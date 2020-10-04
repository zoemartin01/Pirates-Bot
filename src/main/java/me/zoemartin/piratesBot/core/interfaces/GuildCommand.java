package me.zoemartin.piratesBot.core.interfaces;

import me.zoemartin.piratesBot.core.util.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface GuildCommand extends Command {
    default MessageAction embedReply(@NotNull Message original, @NotNull MessageChannel channel,
                                     @Nullable String title, @NotNull String replyFormat, @Nullable Object... args) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(original.getGuild().getSelfMember().getColor());
        if (title != null) eb.setTitle(title);
        eb.setDescription(String.format(replyFormat, args));
        return channel.sendMessage(eb.build());
    }

    default String lastArg(int expectedIndex, List<String> args, Message original) {
        return args.size() == expectedIndex + 1
                   ? args.get(expectedIndex) : MessageUtils.getArgsFrom(
            original.getContentRaw(), args.get(expectedIndex));
    }
}
