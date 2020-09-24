package me.zoemartin.bot.base.util;

import me.zoemartin.bot.modules.commandProcessing.MemberPermission;
import me.zoemartin.bot.modules.commandProcessing.RolePermission;
import me.zoemartin.bot.modules.trigger.Trigger;
import org.hibernate.*;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class DatabaseUtil {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;
    private static Configuration config;

    public static void setConfig(Configuration configuration) {
        config = configuration;

        configuration.addAnnotatedClass(MemberPermission.class);
        configuration.addAnnotatedClass(RolePermission.class);
        configuration.addAnnotatedClass(Trigger.class);
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                registry = new StandardServiceRegistryBuilder().applySettings(config.getProperties()).build();
                sessionFactory = config.buildSessionFactory(registry);
            } catch (Exception e) {
                e.printStackTrace();
                if (registry != null) {
                    StandardServiceRegistryBuilder.destroy(registry);
                }
            }
        }
        return sessionFactory;
    }

    public static void saveObject(Object... objects) {
        Transaction transaction = null;
        try (Session session = getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (Object object : objects) {
                session.save(object);
            }
            transaction.commit();
            session.close();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public static void deleteObject(Object... objects) {
        Transaction transaction = null;
        try (Session session = getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (Object object : objects) {
                session.delete(object);
            }
            transaction.commit();
            session.close();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public static void updateObject(Object... objects) {
        Transaction transaction = null;
        try (Session session = getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            for (Object object : objects) {
                session.update(object);
            }
            transaction.commit();
            session.close();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
