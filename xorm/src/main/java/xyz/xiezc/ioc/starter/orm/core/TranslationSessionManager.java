package xyz.xiezc.ioc.starter.orm.core;

import cn.hutool.log.Log;
import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;


/**
 * @author xiezc
 */
@Getter
public class TranslationSessionManager {

    static Log log = Log.get(TranslationSessionManager.class);

    Session session;
    Transaction transaction;

    public TranslationSessionManager(Session session) {
        this.session = session;
    }


    public Session getSession() {
        if (!session.isOpen()) {
            throw new RuntimeException("session已经关闭，无法使用session");
        }
        if (transaction == null) {
            transaction = session.getTransaction();
            transaction.begin();
        }
        TransactionStatus status = transaction.getStatus();
        switch (status) {
            case NOT_ACTIVE:
                transaction.begin();
                break;
            case ACTIVE:
                break;
            case ROLLED_BACK:
                throw new RuntimeException("事务已经已经回滚，无法再次使用session");
            case COMMITTED:
                throw new RuntimeException("事务已经提交，无法再次使用session");
            case FAILED_COMMIT:
                throw new RuntimeException("事务提交提交失败了，无法再次使用session");
            default:
                throw new RuntimeException("session状态不对，无法使用session");
        }
        return session;
    }


    public void rollback() {
        if (transaction == null) {
            new RuntimeException("事务还未创建，无法回滚");
        }
        TransactionStatus status = transaction.getStatus();
        if (status == TransactionStatus.ACTIVE) {
            transaction.rollback();
            session.close();
        }
        new RuntimeException("事务状态不对，无法提交");
    }

    public void commit() {
        if (transaction == null) {
            new RuntimeException("事务还未创建，无法提交");
        }
        TransactionStatus status = transaction.getStatus();
        if (status == TransactionStatus.ACTIVE) {
            transaction.commit();
            session.close();
        }
        new RuntimeException("事务状态不对，无法提交");
    }
}
