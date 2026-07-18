package br.flmane.servidor.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.hibernate.Session;

public class HibernateUtil {

    private static EntityManagerFactory factory;

    public static Session getSession() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory("flmane-jpa");
        }
        EntityManager entityManager = factory.createEntityManager();
        return entityManager.unwrap(org.hibernate.Session.class);
    }

}