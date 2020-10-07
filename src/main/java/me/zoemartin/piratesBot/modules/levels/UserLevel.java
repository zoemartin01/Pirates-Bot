package me.zoemartin.piratesBot.modules.levels;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "levels")
public class UserLevel {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID uuid;

    @Column(updatable = false, nullable = false)
    private String guild_id;

    @Column(updatable = false, nullable = false)
    private String user_id;

    @Column(nullable = false)
    private int exp;

    public UserLevel(String guild_id, String user_id, int exp) {
        this.uuid = UUID.randomUUID();
        this.guild_id = guild_id;
        this.user_id = user_id;
        this.exp = exp;
    }

    public UserLevel(String guild_id, String user_id) {
        this.uuid = UUID.randomUUID();
        this.guild_id = guild_id;
        this.user_id = user_id;
        this.exp = 0;
    }

    protected UserLevel() {
    }

    public String getGuild_id() {
        return guild_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void addExp(int exp) {
        this.exp += exp;
    }
}
