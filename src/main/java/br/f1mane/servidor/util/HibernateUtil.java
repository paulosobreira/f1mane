package br.f1mane.servidor.util;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {

    private static EntityManagerFactory factory;

    public static Session getSession() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory("f1mane-jpa");
        }
        EntityManager entityManager = factory.createEntityManager();
        System.out.println(factory.getProperties());
        return entityManager.unwrap(org.hibernate.Session.class);
    }

}