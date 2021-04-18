package me.zoemartin.piratesBot.piratesCommands;

import me.zoemartin.rubie.core.annotations.DatabaseEntity;
import me.zoemartin.rubie.core.interfaces.DatabaseEntry;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@DatabaseEntity
@Entity
@Table(name = "voiceroles")
public class VoiceRoleConfig implements DatabaseEntry {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID uuid;

    @Column
    private String guild_id;

    @Column
    private String role_id;

    @Column
    private String channel_id;

    public VoiceRoleConfig(String guild_id, String role_id, String channel_id) {
        this.guild_id = guild_id;
        this.role_id = role_id;
        this.channel_id = channel_id;
    }

    public VoiceRoleConfig() {
    }

    public String getGuild_id() {
        return guild_id;
    }

    public String getRole_id() {
        return role_id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        VoiceRoleConfig config = (VoiceRoleConfig) o;

        return new EqualsBuilder()
                   .append(guild_id, config.guild_id)
                   .append(role_id, config.role_id)
                   .append(channel_id, config.channel_id)
                   .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                   .append(guild_id)
                   .append(role_id)
                   .append(channel_id)
                   .toHashCode();
    }
}
