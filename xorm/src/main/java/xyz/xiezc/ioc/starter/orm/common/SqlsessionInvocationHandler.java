package xyz.xiezc.ioc.starter.orm.common;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class SqlsessionInvocationHandler implements InvocationHandler {

    SqlSessionFactory sqlSessionFactory;

    private static ThreadLocal<SqlSession> sqlSessionThreadLocal = new ThreadLocal<>();


    public SqlsessionInvocationHandler(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        SqlSession sqlSession = sqlSessionThreadLocal.get();

        if (sqlSession == null) {
            sqlSession = sqlSessionFactory.openSession(true);
            sqlSessionThreadLocal.set(sqlSession);
        }
        return method.invoke(sqlSession, args);
    }
}
