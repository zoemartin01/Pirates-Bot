package me.zoemartin.bot.modules.commandProcessing;

import me.zoemartin.bot.base.CommandPerm;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "role_permission")
public class RolePermission {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "guild_id", updatable = false, nullable = false)
    private String guild_id;

    @Column(name = "role_id", updatable = false, nullable = false)
    private String role_id;

    @Column(name = "command_perm", nullable = false)
    @Convert(converter = CommandPerm.Converter.class)
    private CommandPerm perm;

    public RolePermission(UUID uuid, String guild_id, String role_id, CommandPerm perm) {
        this.uuid = uuid;
        this.guild_id = guild_id;
        this.role_id = role_id;
        this.perm = perm;
    }

    public RolePermission(String guild_id, String role_id, CommandPerm perm) {
        this.uuid = UUID.randomUUID();
        this.guild_id = guild_id;
        this.role_id = role_id;
        this.perm = perm;
    }

    protected RolePermission() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getGuild_id() {
        return guild_id;
    }

    public String getRole_id() {
        return role_id;
    }

    public CommandPerm getPerm() {
        return perm;
    }

    public void setPerm(CommandPerm perm) {
        this.perm = perm;
    }
}
