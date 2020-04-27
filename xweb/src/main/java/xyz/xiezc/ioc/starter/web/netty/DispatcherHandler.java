//package xyz.xiezc.ioc.starter.web.netty;
//
//import cn.hutool.core.annotation.AnnotationUtil;
//import cn.hutool.core.exceptions.ExceptionUtil;
//import cn.hutool.core.util.CharsetUtil;
//import cn.hutool.core.util.ReflectUtil;
//import cn.hutool.core.util.StrUtil;
//import cn.hutool.extra.servlet.ServletUtil;
//import org.eclipse.jetty.server.Request;
//import org.eclipse.jetty.server.handler.AbstractHandler;
//import xyz.xiezc.ioc.annotation.Component;
//import xyz.xiezc.ioc.definition.ParamDefinition;
//import xyz.xiezc.ioc.starter.web.annotation.GetMapping;
//import xyz.xiezc.ioc.starter.web.annotation.PostMapping;
//import xyz.xiezc.ioc.starter.web.common.ContentType;
//import xyz.xiezc.ioc.starter.web.common.MappingHandler;
//import xyz.xiezc.ioc.starter.web.common.WebContext;
//import xyz.xiezc.ioc.starter.web.handler.HttpMessageConverter;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.lang.reflect.Method;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class DispatcherHandler   {
//
//    /**
//     * 路径处理器
//     */
//    public static final Map<String, MappingHandler> mappingHandlerMap = new HashMap<>();
//
//    /**
//     * 路径处理器
//     */
//    public Map<ContentType, HttpMessageConverter> httpMessageConverterMap = new HashMap<>();
//
//
//    @Override
//    public void handle(String target,
//                       Request baseRequest,
//                       HttpServletRequest request,
//                       HttpServletResponse response
//    ) throws IOException {
//
//        String pathInfo = baseRequest.getPathInfo();
//        MappingHandler mappingHandler = mappingHandlerMap.get(pathInfo);
//        if (mappingHandler == null) {
//            baseRequest.setHandled(true);
//            return;
//        }
//        //校验请求的psot 还是 get
//        Method method = mappingHandler.getMethod();
//        if (StrUtil.equalsIgnoreCase(baseRequest.getMethod(), "get")) {
//            GetMapping annotation = AnnotationUtil.getAnnotation(method, GetMapping.class);
//            if (annotation == null) {
//                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                baseRequest.setHandled(true);
//                return;
//            }
//        } else if (StrUtil.equalsIgnoreCase(baseRequest.getMethod(), "post")) {
//            PostMapping annotation = AnnotationUtil.getAnnotation(method, PostMapping.class);
//            if (annotation == null) {
//                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                baseRequest.setHandled(true);
//                return;
//            }
//        } else {
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            baseRequest.setHandled(true);
//            return;
//        }
//
//        //获取调用方法的参数
//        String header = ServletUtil.getHeader(baseRequest, "Content-Type", CharsetUtil.CHARSET_UTF_8);
//        ContentType contentType;
//        if (header == null && StrUtil.equalsIgnoreCase(baseRequest.getMethod(), "get")) {
//            contentType = ContentType.FORM_URLENCODED;
//        } else {
//            if (header.contains(";")) {
//                List<String> split = StrUtil.split(header, ';');
//                header = split.get(0);
//            }
//            contentType = ContentType.getByValue(header);
//        }
//        HttpMessageConverter httpMessageConverter = httpMessageConverterMap.get(contentType);
//        if (httpMessageConverter == null) {
//            ExceptionUtil.wrapAndThrow(new RuntimeException("contentType:" + header + "; do not find HttpMessageConverter"));
//        }
//        WebContext.get().setRequest(request);
//        WebContext.get().setResponse(response);
//        httpMessageConverter.read(mappingHandler, contentType, baseRequest);
//
//        //反射调用方法
//        Object bean = mappingHandler.getBeanDefinition().getBean();
//        ParamDefinition[] paramDefinitions = mappingHandler.getParamDefinitions();
//        Object[] params = new Object[paramDefinitions.length];
//        for (int i = 0; i < paramDefinitions.length; i++) {
//            params[i] = paramDefinitions[i].getParam();
//        }
//        Object invoke = ReflectUtil.invoke(bean, method, params);
//        //回写数据
//        httpMessageConverter.write(invoke, ContentType.JSON, response);
//        WebContext.remove();
//        baseRequest.setHandled(true);
//    }
//}
