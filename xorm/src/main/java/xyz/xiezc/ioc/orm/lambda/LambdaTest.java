package xyz.xiezc.ioc.orm.lambda;

import cn.hutool.json.JSONUtil;
import lombok.Data;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

@Data
public class LambdaTest {

    private String fieldA;

    public static void main(String[] args) throws Exception {
        SerializedLambda serializedLambda = doSFunction(LambdaTest::getFieldA);
        System.out.println("方法名：" + serializedLambda.getImplMethodName());
        System.out.println("类名：" + serializedLambda.getImplClass());
        System.out.println("serializedLambda：" + JSONUtil.toJsonStr(serializedLambda));
    }

    private static <T, R> SerializedLambda doSFunction(SFunction<T, R> func) throws Exception {
        // 直接调用writeReplace
        Method writeReplace = func.getClass().getDeclaredMethod("writeReplace");
        writeReplace.setAccessible(true);
        Object sl = writeReplace.invoke(func);
        java.lang.invoke.SerializedLambda serializedLambda = (java.lang.invoke.SerializedLambda) sl;
        return serializedLambda;
    }
}