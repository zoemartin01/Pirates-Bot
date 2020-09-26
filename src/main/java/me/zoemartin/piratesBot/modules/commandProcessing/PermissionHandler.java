package me.zoemartin.piratesBot.modules.commandProcessing;

import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.util.DatabaseUtil;
import org.hibernate.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionHandler {
    private static final Map<String, Set<MemberPermission>> memberPerms = new ConcurrentHashMap<>();
    private static final Map<String, Set<RolePermission>> rolePerms = new ConcurrentHashMap<>();

    public static void initPerms() {
        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            List<MemberPermission> load = session.createQuery("from MemberPermission", MemberPermission.class).list();
            load.forEach(m -> memberPerms.computeIfAbsent(m.getGuild_id(),
                s -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(m));
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Session session = DatabaseUtil.getSessionFactory().openSession()) {
            List<RolePermission> load = session.createQuery("from RolePermission", RolePermission.class).list();
            load.forEach(r -> rolePerms.computeIfAbsent(r.getGuild_id(),
                s -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(r));
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MemberPermission getMemberPerm(String guildId, String memberId) {
        return memberPerms.getOrDefault(guildId, Collections.emptySet())
                   .stream().filter(memberPermission -> memberPermission.getMember_id().equals(memberId))
                   .findAny().orElse(new MemberPermission(guildId, memberId, CommandPerm.EVERYONE));
    }

    public static RolePermission getRolePerm(String guildId, String roleId) {
        return rolePerms.getOrDefault(guildId, Collections.emptySet())
                   .stream().filter(memberPermission -> memberPermission.getRole_id().equals(roleId))
                   .findAny().orElse(new RolePermission(guildId, roleId, CommandPerm.EVERYONE));
    }

    public static void addRolePerm(String guildId, String roleId, CommandPerm perm) {
        RolePermission rp;
        if (!getMemberPerm(guildId, roleId).getPerm().equals(CommandPerm.EVERYONE)) {
            rp = getRolePerm(guildId, roleId);
            rp.setPerm(perm);
            DatabaseUtil.updateObject(rp);
        } else {
            rp = new RolePermission(guildId, roleId, perm);
            DatabaseUtil.saveObject(rp);
        }
        rolePerms.computeIfAbsent(guildId, s -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
            .add(rp);
    }

    public static void addMemberPerm(String guildId, String memberId, CommandPerm perm) {
        MemberPermission mp;
        if (!getMemberPerm(guildId, memberId).getPerm().equals(CommandPerm.EVERYONE)) {
            mp = getMemberPerm(guildId, memberId);
            mp.setPerm(perm);
            DatabaseUtil.updateObject(mp);
        } else {
            mp = new MemberPermission(guildId, memberId, perm);
            DatabaseUtil.saveObject(mp);
        }
        memberPerms.computeIfAbsent(guildId, s -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
            .add(mp);
    }

    public static boolean removeMemberPerm(String guildId, String memberId) {
        MemberPermission mp = memberPerms.getOrDefault(guildId,
            Collections.emptySet()).stream().filter(memberPermission -> memberPermission.getMember_id().equals(memberId))
                                  .findFirst().orElse(null);

        if (mp == null) return false;

        DatabaseUtil.deleteObject(mp);
        return memberPerms.get(guildId).remove(mp);
    }

    public static boolean removeRolePerm(String guildId, String roleId) {
        RolePermission rp = rolePerms.getOrDefault(guildId,
            Collections.emptySet()).stream().filter(memberPermission -> memberPermission.getRole_id().equals(roleId))
                                .findFirst().orElse(null);

        if (rp == null) return false;

        DatabaseUtil.deleteObject(rp);
        return rolePerms.get(guildId).remove(rp);
    }

    public static Collection<MemberPermission> getMemberPerms(String guildId) {
        return memberPerms.getOrDefault(guildId, Collections.emptySet());
    }

    public static Collection<RolePermission> getRolePerms(String guildId) {
        return rolePerms.getOrDefault(guildId, Collections.emptySet());
    }
}
