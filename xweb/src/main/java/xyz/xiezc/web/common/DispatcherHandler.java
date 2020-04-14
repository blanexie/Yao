package xyz.xiezc.web.common;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONUtil;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.MultiMap;
import xyz.xiezc.ioc.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.annotation.Inject;
import xyz.xiezc.web.annotation.GetMapping;
import xyz.xiezc.web.annotation.PostMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DispatcherHandler extends AbstractHandler {

    /**
     * 路径处理器
     */
    public static final Map<String, MappingHandler> mappingHandlerMap = new HashMap<>();


    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response
    ) throws IOException, ServletException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);


        String pathInfo = baseRequest.getPathInfo();
        MappingHandler mappingHandler = mappingHandlerMap.get(pathInfo);
        if (mappingHandler == null) {
            baseRequest.setHandled(true);
            return;
        }

        Method method = mappingHandler.getMethod();
        //校验请求的psot 还是 get
        String method1 = baseRequest.getMethod();
        if (StrUtil.equalsIgnoreCase(method1, "get")) {
            GetMapping annotation = AnnotationUtil.getAnnotation(method, GetMapping.class);
            if (annotation == null) {
                baseRequest.setHandled(true);
                return;
            }
        } else if (StrUtil.equalsIgnoreCase(method1, "post")) {
            PostMapping annotation = AnnotationUtil.getAnnotation(method, PostMapping.class);
            if (annotation == null) {
                baseRequest.setHandled(true);
                return;
            }
        } else {
            baseRequest.setHandled(true);
            return;
        }

        Map<String, String> params = ServletUtil.getParamMap(baseRequest);
        Parameter[] parameters = method.getParameters();
        Object[] paramObjs = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            parameter.getParameterizedType();
            String name = parameter.getName();
            String param = params.get(name);
            Class<?> type = parameter.getType();

            if (ClassUtil.isAssignable(type, Set.class) || ClassUtil.isAssignable(type, List.class)) {
                String[] split = param.split(",");
                Collection<String> strings = CollUtil.toCollection(CollUtil.newArrayList(split));
                paramObjs[i] = strings;
            }
            if (type.isArray()) {
                String[] split = param.split(",");
                paramObjs[i] = split;
            }

            if (ClassUtil.isAssignable(type, Integer.class)) {
                Integer integer = Convert.toInt(param);
                paramObjs[i] = integer;
            }
            if (ClassUtil.isAssignable(type, String.class)) {
                paramObjs[i] = param;
            }
            if (ClassUtil.isAssignable(type, Boolean.class)) {
                paramObjs[i] = Convert.toBool(param);
            }
            if (ClassUtil.isAssignable(type, Character.class)) {
                paramObjs[i] = Convert.toChar(param);
            }
            if (ClassUtil.isAssignable(type, Number.class)) {
                paramObjs[i] = Convert.toNumber(param);
            }
        }

        Object bean = mappingHandler.getBeanDefinition().getBean();
        Object invoke = ReflectUtil.invoke(bean, method, paramObjs);
        PrintWriter out = response.getWriter();
        out.println(JSONUtil.toJsonStr(invoke));
        baseRequest.setHandled(true);

    }
}
