package xyz.xiezc.ioc.starter.orm.core;

import cn.hutool.log.Log;
import lombok.Data;
import lombok.Synchronized;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import static xyz.xiezc.ioc.starter.orm.core.TranslationStatus.commit;
import static xyz.xiezc.ioc.starter.orm.core.TranslationStatus.rollback;


@Data
public class TranslationEntityManager {

   static Log log=Log.get(TranslationEntityManager.class);
    private final EntityManagerFactory entityManagerFactory;

    EntityManager entityManager;
    EntityTransaction transaction;
    TranslationStatus translationStatus;

    public TranslationEntityManager(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Synchronized
    public EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = entityManagerFactory.createEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();
            translationStatus = TranslationStatus.begin;
        } else {
            if (translationStatus == null) {
                transaction = entityManager.getTransaction();
                transaction.begin();
                translationStatus = TranslationStatus.begin;
                return entityManager;
            }
            if (translationStatus == TranslationStatus.begin) {
                return entityManager;
            }
            if (translationStatus == commit) {
                transaction = entityManager.getTransaction();
                transaction.begin();
                translationStatus = TranslationStatus.begin;
                return entityManager;
            }
            if (translationStatus == TranslationStatus.rollback) {
                transaction = entityManager.getTransaction();
                transaction.begin();
                translationStatus = TranslationStatus.begin;
                return entityManager;
            }
        }
        return entityManager;
    }

    @Synchronized
    public void commit() {
        if (translationStatus == commit) {
            throw new RuntimeException("已经提交, 无法再次提交");
        }
        if (translationStatus == rollback) {
            throw new RuntimeException("已经回滚了, 无法提交");
        }
        transaction.commit();
        translationStatus = commit;
        this.closeEntityManager();
    }

    @Synchronized
    public void rollback() {
        if (translationStatus == commit) {
            throw new RuntimeException("已经提交, 无法回滚");
        }
        transaction.rollback();
        translationStatus = TranslationStatus.rollback;
        this.closeEntityManager();
    }

    /**
     * Close the given JPA EntityManager,
     * catching and logging any cleanup exceptions thrown.
     * @see javax.persistence.EntityManager#close()
     */
    private   void closeEntityManager() {
        if (entityManager != null) {
            log.debug("Closing JPA EntityManager");
            try {
                if (entityManager.isOpen()) {
                    entityManager.close();
                    entityManager = null;
                }
            }
            catch (PersistenceException ex) {
                log.debug("Could not close JPA EntityManager", ex);
            }
            catch (Throwable ex) {
                log.debug("Unexpected exception on closing JPA EntityManager", ex);
            }
        }
    }
}
