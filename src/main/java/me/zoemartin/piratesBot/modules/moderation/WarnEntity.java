package me.zoemartin.piratesBot.modules.moderation;

import javax.persistence.*;
import java.util.UUID;

@Entity(name = "warns")
public class WarnEntity {

    @Id
    @GeneratedValue
    @Column
    private UUID uuid;

    @Column
    private String guild_id;

    @Column
    private String user_id;

    @Column
    private String moderator_id;

    @Column
    private String reason;

    @Column
    private long timestamp;

    protected WarnEntity() {
    }

    public WarnEntity(String guild_id, String user_id, String moderator_id, String reason, long timestamp) {
        this.uuid = UUID.randomUUID();
        this.guild_id = guild_id;
        this.user_id = user_id;
        this.moderator_id = moderator_id;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public WarnEntity(UUID uuid, String guild_id, String user_id, String moderator_id, String reason, long timestamp) {
        this.uuid = uuid;
        this.guild_id = guild_id;
        this.user_id = user_id;
        this.moderator_id = moderator_id;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getGuild_id() {
        return guild_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getModerator_id() {
        return moderator_id;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
