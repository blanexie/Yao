package xyz.xiezc.ioc.starter.orm.core;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;


public class HibernateUtil {
    public static SessionFactory sessionFactory;
    static Log log = Log.get(HibernateUtil.class);
    // ThreadLocal可以隔离多个线程的数据共享，因此不再需要对线程同步
    private static final ThreadLocal<TranslationSessionManager> sessionThreadLocal = ThreadUtil.createThreadLocal(true);

    public static void init(Properties properties,Class<?>... clazzes) {
        try {
            // 采用默认的hibernate.cfg.xml来启动一个Configuration的实例
            Configuration cfg = new Configuration()
                    .configure();
            // 以Configuration实例来创建SessionFactory实例
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(properties).build();
            for (Class<?> clazz : clazzes) {
                cfg.addAnnotatedClass(clazz);
            }
            sessionFactory = cfg.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            log.error(ex);
            throw new ExceptionInInitializerError(ex);
        }
    }


    public  static TranslationSessionManager currentSession()
            throws HibernateException {
        //通过线程对象.get()方法安全创建Session
        TranslationSessionManager translationSessionManager = sessionThreadLocal.get();
        // 如果该线程还没有Session,则创建一个新的Session
        if (translationSessionManager == null) {
            translationSessionManager = new TranslationSessionManager(sessionFactory.openSession());
            // 将获得的Session变量存储在ThreadLocal变量session里
            sessionThreadLocal.set(translationSessionManager);
        }
        return translationSessionManager;
    }

}
