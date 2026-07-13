package com.booknest.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAConfig {
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("BookNestPU");
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}