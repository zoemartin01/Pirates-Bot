package me.zoemartin.piratesBot.core.interfaces;

import me.zoemartin.piratesBot.Bot;
import me.zoemartin.piratesBot.core.util.Help;
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

    void run(Member user, TextChannel channel, List<String> args, Message original, String invoked);

    default void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        run(original.getMember(), original.getTextChannel(), args, original, invoked);
    }

    @SuppressWarnings("ConstantConditions")
    default void addCheckmark(Message message) {
        message.addReaction(Bot.getJDA().getEmoteById("762424762412040192")).queue();
    }

    default void help(Member user, MessageChannel channel, List<String> args, Message original) {
        if (Help.getHelper() != null) Help.getHelper().send(user.getUser(), channel, args, original, args.get(0));
    }
}
