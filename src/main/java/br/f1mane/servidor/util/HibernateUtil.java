package br.f1mane.servidor.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;

public class HibernateUtil {

    private static EntityManagerFactory factory;

    public static Session getSession() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory("flmane-jpa");
        }
        EntityManager entityManager = factory.createEntityManager();
        System.out.println(factory.getProperties());
        return entityManager.unwrap(org.hibernate.Session.class);
    }

}