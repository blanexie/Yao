package xyz.xiezc.ioc.orm.common.transaction;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @Description YaoTransactionFactory
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/7/2 3:21 下午
 **/
public class YaoTransactionFactory implements TransactionFactory {

    @Override
    public Transaction newTransaction(Connection conn) {
        return new YaoTransaction(conn);
    }

    @Override
    public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
        return new YaoTransaction(ds, level, autoCommit);
    }
}