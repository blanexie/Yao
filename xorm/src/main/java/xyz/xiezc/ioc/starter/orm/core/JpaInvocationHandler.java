package xyz.xiezc.ioc.starter.orm.core;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.starter.common.asm.AsmUtil;
import xyz.xiezc.ioc.starter.config.JpaConfig;
import xyz.xiezc.ioc.starter.orm.annotation.Query;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiezc
 */
public class JpaInvocationHandler implements InvocationHandler {

    ThreadLocal<TranslationEntityManager> threadLocal = ThreadUtil.createThreadLocal(false);
    /**
     * 方法sql
     */
    Map<Method, JpaMethod> methodQueryMap = new HashMap<>();


    public JpaInvocationHandler(Class clazz, EntityManagerFactory entityManagerFactory) {
        threadLocal.set(new TranslationEntityManager(entityManagerFactory));

        Method[] declaredMethods = ClassUtil.getDeclaredMethods(clazz);
        for (Method declaredMethod : declaredMethods) {
            Query query = AnnotationUtil.getAnnotation(declaredMethod, Query.class);
            if (query == null) {

                continue;
            }
            JpaMethod jpaMethod = new JpaMethod();
            jpaMethod.setClazz(clazz);
            jpaMethod.setMethod(declaredMethod);
            String[] params = AsmUtil.getMethodParamsAndAnnotaton(declaredMethod);
            jpaMethod.setParams(params);
            jpaMethod.setSql(query.value());
            methodQueryMap.put(declaredMethod, jpaMethod);
        }
        Method[] methods = ClassUtil.getDeclaredMethods(BasicRepository.class);
        for (Method method : methods) {
            if (Objects.equals(method.getName(), "save")) {
                JpaMethod jpaMethod = new JpaMethod();
                jpaMethod.setClazz(clazz);
                jpaMethod.setMethod(method);
                jpaMethod.isSave = true;
                methodQueryMap.put(method, jpaMethod);
            }
        }
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        JpaMethod jpaMethod = methodQueryMap.get(method);
        if (jpaMethod == null) {
            throw new IllegalArgumentException("method: " + method.getName() + " 没有对应的Hql语句");
        }

        TranslationEntityManager translationEntityManager = threadLocal.get();
        EntityManager entityManager = translationEntityManager.getEntityManager();
        try {
            if (jpaMethod.isSave) {
                Object arg = args[0];
                if (ClassUtil.isAssignable(List.class, arg.getClass())) {
                    List list = (List) arg;
                    for (Object o : list) {
                        entityManager.persist(o);
                    }
                } else {
                    entityManager.persist(arg);
                }
                translationEntityManager.commit();
                return null;
            }

            Class<?> returnType = method.getReturnType();
            if (ClassUtil.isSimpleValueType(returnType)) {
                TypedQuery<?> query = entityManager.createQuery(jpaMethod.getSql(), returnType);
                String[] params = jpaMethod.getParams();
                for (int i = 0; i < params.length; i++) {
                    query.setParameter(params[i], args[i]);
                }
                translationEntityManager.commit();
                //普通类型
                return query.getSingleResult();
            }
            if (returnType.isArray()) {
                Class<?> componentType = returnType.getComponentType();
                TypedQuery<?> query = entityManager.createQuery(jpaMethod.getSql(), componentType);
                String[] params = jpaMethod.getParams();
                for (int i = 0; i < params.length; i++) {
                    query.setParameter(params[i], args[i]);
                }
                List<?> resultList = query.getResultList();
                Object[] objects = resultList.toArray();
                translationEntityManager.commit();
                return objects;
            }
            if (ClassUtil.isAssignable(Collection.class, returnType)) {
                Type genericReturnType = method.getGenericReturnType();
                ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
                Class<?> actualTypeArgument = (Class<?>) parameterizedType.getActualTypeArguments()[0];

                TypedQuery<?> query = entityManager.createQuery(jpaMethod.getSql(), actualTypeArgument);
                String[] params = jpaMethod.getParams();
                for (int i = 0; i < params.length; i++) {
                    query.setParameter(params[i], args[i]);
                }

                List<?> resultList = query.getResultList();
                Collection<?> collect = resultList.stream().collect(Collectors.toList());
                translationEntityManager.commit();
                return collect;
            }

            TypedQuery<?> query = entityManager.createQuery(jpaMethod.getSql(), returnType);
            String[] params = jpaMethod.getParams();
            for (int i = 0; i < params.length; i++) {
                query.setParameter(params[i], args[i]);
            }
            translationEntityManager.commit();
            return query.getSingleResult();
        } catch (Exception e) {
            translationEntityManager.rollback();
            throw e;
        }

    }
}
