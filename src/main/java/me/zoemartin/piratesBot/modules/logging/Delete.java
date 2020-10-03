package me.zoemartin.piratesBot.modules.logging;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.*;
import me.zoemartin.piratesBot.core.exceptions.UnexpectedError;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.DatabaseUtil;
import net.dv8tion.jda.api.audit.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.hibernate.Session;

import javax.annotation.Nonnull;
import javax.persistence.criteria.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Delete extends ListenerAdapter {
    private static final String GUILD = "672160078899445761";
    private static final String LOG = "759496568352538654";
    private static final String NAME = "BobbieLogs";


    @Override
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        if (!event.getGuild().getId().equals(GUILD)) return;

        Guild g = event.getGuild();
        List<AuditLogEntry> logs = g.retrieveAuditLogs().type(ActionType.MESSAGE_DELETE).complete();

        TextChannel c = g.getTextChannelById(LOG);
        if (c == null) return;

        Webhook hook = c.retrieveWebhooks().complete().stream()
                           .findAny().orElseThrow();

        AuditLogEntry e = logs.stream().filter(
            ae -> ae.getTimeCreated().isAfter(OffsetDateTime.now().minus(1, ChronoUnit.SECONDS))
        ).findAny().orElse(null);

        Session s = DatabaseUtil.getSessionFactory().openSession();

        CriteriaBuilder cb = s.getCriteriaBuilder();

        CriteriaQuery<LMessage> q = cb.createQuery(LMessage.class);
        Root<LMessage> r = q.from(LMessage.class);
        List<LMessage> lMessageList = s.createQuery(q.select(r).where(cb.equal(r.get("message_id"), event.getMessageId()))).getResultList();
        LMessage lm;
        if (lMessageList.isEmpty()) lm = null;
        else lm = lMessageList.get(0);


        WebhookClient client = WebhookClient.withUrl(hook.getUrl());
        WebhookEmbed embed;

        if (lm == null) {
            if (e == null) {
                embed = new WebhookEmbedBuilder()
                            .setColor(0xdf136c)
                            .setTitle(new WebhookEmbed.EmbedTitle("Message Deleted", null))
                            .setDescription(String.format("Message ID: `%s`", event.getMessageId()))
                            .setTimestamp(Instant.now())
                            .build();
            } else {
                User actor = e.getUser();

                Check.check(actor != null, UnexpectedError::new);

                if (!e.getTargetType().equals(TargetType.MEMBER)) return;
                Member target = g.getMemberById(e.getTargetId());
                Check.check(target != null, UnexpectedError::new);

                embed = new WebhookEmbedBuilder()
                            .setColor(0xdf136c)
                            .setAuthor(new WebhookEmbed.EmbedAuthor(String.format("Target: %s / %s / %s",
                                target.getEffectiveName(), target.getUser().getAsTag(), target.getId()),
                                target.getUser().getAvatarUrl(), null))
                            .setFooter(new WebhookEmbed.EmbedFooter(String.format("Actor: %s / %s / %s",
                                actor.getName(), actor.getAsTag(), actor.getId()
                            ), e.getUser().getAvatarUrl()))
                            .setTitle(new WebhookEmbed.EmbedTitle("Message Deleted", null))
                            .setDescription(String.format("Message ID: `%s`", event.getMessageId()))
                            .setTimestamp(e.getTimeCreated())
                            .build();
            }
        } else {
            if (e == null) {
                Member target = event.getGuild().getMemberById(lm.getUser_id());

                embed = new WebhookEmbedBuilder()
                            .setColor(0xdf136c)
                            .setAuthor(new WebhookEmbed.EmbedAuthor(String.format("Target: %s / %s / %s",
                                target.getEffectiveName(), target.getUser().getAsTag(), target.getId()),
                                target.getUser().getAvatarUrl(), null))
                            .setTitle(new WebhookEmbed.EmbedTitle("Message Deleted", null))
                            .setFooter(new WebhookEmbed.EmbedFooter(String.format("Actor: %s / %s / %s",
                                target.getEffectiveName(), target.getUser().getAsTag(), target.getId()
                            ), target.getUser().getAvatarUrl()))
                            .setDescription(String.format("%s", lm.getMessage_content()))
                            .addField(new WebhookEmbed.EmbedField(true, "Channel",
                                event.getGuild().getTextChannelById(lm.getChannel_id()).getAsMention()))
                            .setTimestamp(Instant.ofEpochSecond(lm.getTimestamp()))
                            .build();
            } else {
                User actor = e.getUser();

                Check.check(actor != null, UnexpectedError::new);

                if (!e.getTargetType().equals(TargetType.MEMBER)) return;
                Member target = g.getMemberById(e.getTargetId());
                Check.check(target != null, UnexpectedError::new);

                embed = new WebhookEmbedBuilder()
                            .setColor(0xdf136c)
                            .setAuthor(new WebhookEmbed.EmbedAuthor(String.format("Target: %s / %s / %s",
                                target.getEffectiveName(), target.getUser().getAsTag(), target.getId()),
                                target.getUser().getAvatarUrl(), null))
                            .setFooter(new WebhookEmbed.EmbedFooter(String.format("Actor: %s / %s / %s",
                                actor.getName(), actor.getAsTag(), actor.getId()
                            ), e.getUser().getAvatarUrl()))
                            .setTitle(new WebhookEmbed.EmbedTitle("Message Deleted", null))
                            .addField(new WebhookEmbed.EmbedField(true, "Channel",
                                event.getGuild().getTextChannelById(lm.getChannel_id()).getAsMention()))
                            .setDescription(String.format("%s", lm.getMessage_content()))
                            .setTimestamp(Instant.ofEpochSecond(lm.getTimestamp()))
                            .build();
            }

            DatabaseUtil.deleteObject(lm);
        }

        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername("bobbie:log"); // use this username
        //builder.setAvatarUrl(avatarUrl); // use this avatar
        builder.addEmbeds(embed);
        client.send(builder.build());

        super.onGuildMessageDelete(event);
    }
}
