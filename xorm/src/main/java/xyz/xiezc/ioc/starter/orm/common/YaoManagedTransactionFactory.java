package xyz.xiezc.ioc.starter.orm.common;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

public class YaoManagedTransactionFactory  extends ManagedTransactionFactory implements TransactionFactory {

}
