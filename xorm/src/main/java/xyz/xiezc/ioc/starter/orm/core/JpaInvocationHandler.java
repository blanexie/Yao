package xyz.xiezc.ioc.starter.orm.core;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import xyz.xiezc.ioc.starter.common.asm.AsmUtil;
import xyz.xiezc.ioc.starter.config.JpaConfig;
import xyz.xiezc.ioc.starter.orm.annotation.Query;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiezc
 */
public class JpaInvocationHandler implements InvocationHandler {


    private final EntityManager entityManager;

    /**
     * 方法sql
     */
    Map<Method, JpaMethod> methodQueryMap = new HashMap<>();


    public JpaInvocationHandler(Class clazz, EntityManager entityManager) {
        this.entityManager = entityManager;
        Method[] methods = ClassUtil.getDeclaredMethods(BasicRepository.class);
        Method[] declaredMethods = ClassUtil.getDeclaredMethods(clazz);
        for (Method declaredMethod : declaredMethods) {
            Query query = AnnotationUtil.getAnnotation(declaredMethod, Query.class);
            if (query == null) {
                for (Method method : methods) {
                    if (Objects.equals(method.getName(), "save") && Objects.equals(method, declaredMethod)) {
                        JpaMethod jpaMethod = new JpaMethod();
                        jpaMethod.setClazz(clazz);
                        jpaMethod.setMethod(declaredMethod);
                        jpaMethod.isSave = true;
                        methodQueryMap.put(declaredMethod, jpaMethod);
                    }
                }
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
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        JpaMethod jpaMethod = methodQueryMap.get(method);
        if (jpaMethod == null) {
            throw new IllegalArgumentException("method: " + method.getName() + " 没有对应的Hql语句");
        }
        if (jpaMethod.isSave) {
            Object arg = args[0];
            if (ClassUtil.isAssignable(List.class, arg.getClass())) {
                List list = (List) arg;
                for (Object o : list) {
                    entityManager.persist(o);
                }
                return null;
            } else {
                entityManager.persist(arg);
            }
            return null;
        }

        Class<?> returnType = method.getReturnType();
        TypedQuery<?> query = entityManager.createQuery(jpaMethod.getSql(), returnType);
        String[] params = jpaMethod.getParams();
        for (int i = 0; i < params.length; i++) {
            query.setParameter(params[i], args[i]);
        }
        if (ClassUtil.isSimpleValueType(returnType)) {
            //普通类型
            return query.getSingleResult();
        } else if (returnType.isArray()) {
            List<?> resultList = query.getResultList();
            Object[] objects = resultList.toArray();
            return objects;
        } else if (ClassUtil.isAssignable(Collection.class, returnType)) {
            List<?> resultList = query.getResultList();
            Collection<?> collect = resultList.stream().collect(Collectors.toList());
            return collect;
        } else {
            return query.getSingleResult();
        }
    }
}
