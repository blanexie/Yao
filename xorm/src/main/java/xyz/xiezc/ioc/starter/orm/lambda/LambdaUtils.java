/*
 * Copyright (c) 2011-2020, baomidou (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package xyz.xiezc.ioc.starter.orm.lambda;

import xyz.xiezc.ioc.starter.orm.util.ExceptionUtils;
import xyz.xiezc.ioc.starter.orm.util.StringUtil;
import xyz.xiezc.ioc.starter.orm.xml.EntityTableDefine;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Locale.ENGLISH;

/**
 * Lambda 解析工具类
 *
 * @author HCL, MieMie
 * @since 2018-05-10
 */
public final class LambdaUtils {

    /**
     * 字段映射
     */
    private static final Map<Class<?>, EntityTableDefine> LAMBDA_MAP = new ConcurrentHashMap<>();

    /**
     * SerializedLambda 反序列化缓存
     */
    private static final Map<Class<?>, WeakReference<SerializedLambda>> FUNC_CACHE = new ConcurrentHashMap<>();

    /**
     * 解析 lambda 表达式, 该方法只是调用了 {@link SerializedLambda#resolve(SFunction)} 中的方法，在此基础上加了缓存。
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @param <T>  类型，被调用的 Function 对象的目标类型
     * @return 返回解析后的结果
     * @see SerializedLambda#resolve(SFunction)
     */
    public static <T> SerializedLambda resolve(SFunction<T, ?> func) {
        Class<?> clazz = func.getClass();
        return Optional.ofNullable(FUNC_CACHE.get(clazz))
                .map(WeakReference::get)
                .orElseGet(() -> {
                    SerializedLambda lambda = SerializedLambda.resolve(func);
                    FUNC_CACHE.put(clazz, new WeakReference<>(lambda));
                    return lambda;
                });
    }

    /**
     * 格式化 key 将传入的 key 变更为大写格式
     *
     * <pre>
     *     Assert.assertEquals("USERID", formatKey("userId"))
     * </pre>
     *
     * @param key key
     * @return 大写的 key
     */
    public static String formatKey(String key) {
        return key.toUpperCase(ENGLISH);
    }

    /**
     * 将传入的表信息加入缓存
     *
     * @param tableInfo 表信息
     */
    public static void installCache(EntityTableDefine tableInfo) {
        LAMBDA_MAP.put(tableInfo.getTable().getClazz(), tableInfo);
    }

    /**
     * 获取对应的表字段与对象的属性关系对象
     *
     * @param func
     * @param <T>
     * @return
     */
    public static <T> EntityTableDefine.ColumnProp getColumnProp(SFunction<T, ?> func) {
        SerializedLambda resolve = LambdaUtils.resolve(func);
      return getColumnProp(resolve);
    }

    /**
     * 获取对应的表字段与对象的属性关系对象
     *
     * @param resolve
     * @return
     */
    public static  EntityTableDefine.ColumnProp getColumnProp(SerializedLambda resolve) {
        String implMethodName = resolve.getImplMethodName();
        Class<?> implClass = resolve.getImplClass();
        if (implMethodName.startsWith("get")) {
            return getColumnProp(implClass, implMethodName, "get");
        }

        if (implMethodName.startsWith("is")) {
            return getColumnProp(implClass, implMethodName, "is");
        }

        throw ExceptionUtils.mpe("{}, 未找到对应的字段", implMethodName);
    }


    private static EntityTableDefine.ColumnProp getColumnProp(Class<?> clazz, String implMethodName, String prefixStr) {
        implMethodName = implMethodName.replaceFirst(prefixStr, "");
        String propertyName = StringUtil.lownCaseFirstChar(implMethodName);

        Optional<EntityTableDefine.ColumnProp> optional = Optional.ofNullable(LAMBDA_MAP.get(clazz)).map(entityTableDefine -> {
            EntityTableDefine.ColumnProp id = entityTableDefine.getId();
            if (Objects.equals(id.getProperty(), propertyName)) {
                return id;
            }
            Optional<EntityTableDefine.ColumnProp> any = entityTableDefine.getColumns().stream().filter(columnProp -> Objects.equals(columnProp.getProperty(), propertyName)).findAny();
            return any.get();
        });
        return optional.get();
    }


}
