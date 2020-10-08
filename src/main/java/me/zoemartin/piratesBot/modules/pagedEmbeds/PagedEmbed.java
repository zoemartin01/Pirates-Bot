package me.zoemartin.piratesBot.modules.pagedEmbeds;

import net.dv8tion.jda.api.entities.*;

import java.util.List;

public class PagedEmbed {
    static final String BACK = "U+2B05";
    static final String FORWARD = "U+27A1";
    static final String STOP = "U+23F9";

    private final List<MessageEmbed> pages;
    private final String guildId;
    private final String userId;
    private final Message message;
    private int current;

    public PagedEmbed(List<MessageEmbed> pages, TextChannel channel, User user) {
        this.pages = pages;
        this.guildId = channel.getGuild().getId();
        this.userId = user.getId();
        this.message = channel.sendMessage(this.pages.get(current = 0)).complete();
        if (pages.size() > 1) {
            message.addReaction(BACK).queue();
            message.addReaction(FORWARD).queue();
            message.addReaction(STOP).queue();
        }
    }

    public PagedEmbed(List<MessageEmbed> pages, TextChannel channel, User user, int start) {
        this.pages = pages;
        this.guildId = channel.getGuild().getId();
        this.userId = user.getId();
        this.current = start > pages.size() || start < 1 ? 0 : start - 1;
        this.message = channel.sendMessage(this.pages.get(this.current)).complete();
        if (pages.size() > 1) {
            message.addReaction(BACK).queue();
            message.addReaction(FORWARD).queue();
            message.addReaction(STOP).queue();
        }
    }

    public String getGuildId() {
        return guildId;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessageId() {
        return message.getId();
    }

    public void next() {
        if (current == pages.size() - 1) return;
        message.editMessage(pages.get(++current)).queue();
    }

    public void last() {
        if (current == 0) return;
        message.editMessage(pages.get(--current)).queue();
    }

    public void stop() {
        message.clearReactions().queue();
    }

    public int getPages() {
        return pages.size();
    }
}
