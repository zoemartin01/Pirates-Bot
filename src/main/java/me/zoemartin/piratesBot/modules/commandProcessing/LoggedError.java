package me.zoemartin.piratesBot.modules.commandProcessing;

import net.dv8tion.jda.api.entities.Message;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Arrays;
import java.util.UUID;

@Entity
@Table(name = "errors")
public class LoggedError {
    @Id
    @Column
    @GeneratedValue
    private UUID uuid;

    @Column
    private String guild_id;

    @Column
    private String channel_id;

    @Column
    private String user_id;

    @Column
    private String message_id;

    @Column
    private String invoked_message;

    @Column
    private String error_message;

    @Column(columnDefinition="TEXT")
    private String error_stacktrace;

    @Column
    private long timestamp;

    public LoggedError(UUID uuid, String guild_id, String channel_id, String user_id, String message_id, String invoked_message, String error_message, String error_stacktrace, long timestamp) {
        this.uuid = uuid;
        this.guild_id = guild_id;
        this.channel_id = channel_id;
        this.user_id = user_id;
        this.message_id = message_id;
        this.invoked_message = invoked_message;
        this.error_message = error_message;
        this.error_stacktrace = error_stacktrace;
        this.timestamp = timestamp;
    }

    public LoggedError(String guild_id, String channel_id, String user_id, String message_id, String invoked_message, String error_message, String error_stacktrace, long timestamp) {
        this.uuid = UUID.randomUUID();
        this.guild_id = guild_id;
        this.channel_id = channel_id;
        this.user_id = user_id;
        this.message_id = message_id;
        this.invoked_message = invoked_message;
        this.error_message = error_message;
        this.error_stacktrace = error_stacktrace;
        this.timestamp = timestamp;
    }

    public LoggedError(String guild_id, String channel_id, String user_id, String message_id, String invoked_message, String error_message, StackTraceElement[] error_stacktrace, long timestamp) {
        this.uuid = UUID.randomUUID();
        this.guild_id = guild_id;
        this.channel_id = channel_id;
        this.user_id = user_id;
        this.message_id = message_id;
        this.invoked_message = invoked_message;
        this.error_message = error_message;
        this.error_stacktrace = Arrays.toString(error_stacktrace);
        this.timestamp = timestamp;
    }

    protected LoggedError() {
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

    public String getInvoked_message() {
        return invoked_message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getError_stacktrace() {
        return error_stacktrace;
    }

    public String getError_message() {
        return error_message;
    }
}
