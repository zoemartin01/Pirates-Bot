package me.zoemartin.piratesBot.modules.logging;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import me.zoemartin.piratesBot.core.exceptions.UnexpectedError;
import me.zoemartin.piratesBot.core.util.Check;
import net.dv8tion.jda.api.audit.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.text.update.GenericTextChannelUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class ChannelListener extends ListenerAdapter {
    private static final String GUILD = "672160078899445761";
    private static final String LOG = "759496568352538654";

    @Override
    public void onGenericTextChannelUpdate(@Nonnull GenericTextChannelUpdateEvent event) {
        if (!event.getGuild().getId().equals(GUILD)) return;

        Guild g = event.getGuild();
        TextChannel c = g.getTextChannelById(LOG);
        if (c == null) return;
        Webhook hook = c.retrieveWebhooks().complete().stream().findAny().orElseThrow();
        WebhookClient client = WebhookClient.withUrl(hook.getUrl());
        WebhookEmbed embed;

        AuditLogEntry e = g.retrieveAuditLogs().type(ActionType.CHANNEL_UPDATE).complete().stream().filter(
            ae -> ae.getTargetId().equals(event.getEntity().getId())
        ).findAny().orElseThrow();

        User actor = e.getUser();

        Check.check(actor != null, UnexpectedError::new);

        if (!e.getTargetType().equals(TargetType.MEMBER)) return;
        Member target = g.getMemberById(e.getTargetId());
        Check.check(target != null, UnexpectedError::new);

        embed = new WebhookEmbedBuilder()
                    .setColor(0xdf136c)
                    .setFooter(new WebhookEmbed.EmbedFooter(String.format("Actor: %s / %s / %s",
                        actor.getName(), actor.getAsTag(), actor.getId()
                    ), e.getUser().getAvatarUrl()))
                    .setTitle(new WebhookEmbed.EmbedTitle("Text Channel Updated", null))
                    .setDescription(String.format("Channel: `%s`\n", event.getEntity().getAsMention()))
                    .setTimestamp(e.getTimeCreated())
                    .build();

        client.send(embed);
    }
}
