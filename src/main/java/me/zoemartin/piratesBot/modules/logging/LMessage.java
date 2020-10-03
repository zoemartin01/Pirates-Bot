package me.zoemartin.piratesBot.modules.logging;

import net.dv8tion.jda.api.entities.Message;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class LMessage {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "guild_id", updatable = false, nullable = false)
    private String guild_id;

    @Column(name = "channel_id", updatable = false, nullable = false)
    private String channel_id;

    @Column(name = "user_id", updatable = false, nullable = false)
    private String user_id;

    @Column(name = "message_id", updatable = false, nullable = false)
    private String message_id;

    @Column(name = "message_content", updatable = false, nullable = false)
    private String message_content;

    @Column(name = "timestamp", updatable = false, nullable = false)
    private long timestamp;

    public LMessage(UUID uuid, String guild_id, String channel_id, String user_id, String message_id,
                    String message_content, long timestamp) {
        this.uuid = uuid;
        this.guild_id = guild_id;
        this.channel_id = channel_id;
        this.user_id = user_id;
        this.message_id = message_id;
        this.message_content = message_content;
        this.timestamp = timestamp;
    }

    public LMessage(String guild_id, String channel_id, String user_id, String message_id,
                    String message_content, long timestamp) {
        this.uuid = UUID.randomUUID();
        this.guild_id = guild_id;
        this.channel_id = channel_id;
        this.user_id = user_id;
        this.message_id = message_id;
        this.message_content = message_content;
        this.timestamp = timestamp;
    }

    public LMessage(Message message) {
        this.uuid = UUID.randomUUID();
        this.guild_id = message.getGuild().getId();
        this.channel_id = message.getChannel().getId();
        this.user_id = message.getAuthor().getId();
        this.message_id = message.getId();
        this.message_content = message.getContentRaw();
        this.timestamp = message.getTimeCreated().toEpochSecond();
    }

    protected LMessage() {
    }

    public String getGuild_id() {
        return guild_id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public String getMessage_content() {
        return message_content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
