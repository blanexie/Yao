package xyz.xiezc.ioc.starter.orm.core;

import cn.hutool.core.thread.ThreadUtil;
import lombok.Data;
import lombok.Synchronized;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import static xyz.xiezc.ioc.starter.orm.core.TranslationStatus.commit;
import static xyz.xiezc.ioc.starter.orm.core.TranslationStatus.rollback;


@Data
public class TranslationEntityManager {

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
        if(translationStatus == commit){
            throw new RuntimeException("已经提交, 无法再次提交");
        }
        if(translationStatus == rollback){
            throw new RuntimeException("已经回滚了, 无法提交");
        }
        transaction.commit();
        entityManager.close();
        entityManager=null;
        translationStatus = commit;

    }

    @Synchronized
    public void rollback() {
        if(translationStatus == commit){
             throw new RuntimeException("已经提交, 无法回滚");
        }
        transaction.rollback();
        entityManager.close();
        entityManager=null;
        translationStatus = TranslationStatus.rollback;
    }

}
