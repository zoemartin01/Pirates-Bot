package me.zoemartin.piratesBot.core.util;

import me.zoemartin.piratesBot.modules.moderation.WarnEntity;
import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import javax.persistence.criteria.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class DatabaseUtil {
    private static final Collection<Class<?>> mapped = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;
    private static Configuration config;

    public static void setConfig(Configuration configuration) {
        config = configuration;
        mapped.forEach(configuration::addAnnotatedClass);
        System.out.println(mapped.toString());
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

    public static <T> List<T> find(Class<T> clazz, Predicate... predicates) {
        Session s = getSessionFactory().openSession();
        CriteriaBuilder cb = s.getCriteriaBuilder();

        CriteriaQuery<T> q = cb.createQuery(clazz);
        Root<T> r = q.from(clazz);
        return s.createQuery(q.select(r).where(predicates)).getResultList();
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    public static void setMapped(Class<?> aClass){
        mapped.add(aClass);
    }
}
