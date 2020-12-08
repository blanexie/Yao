package xyz.xiezc.ioc.starter.web.process;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.io.file.PathUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import xyz.xiezc.ioc.starter.annotation.core.Autowire;
import xyz.xiezc.ioc.starter.annotation.core.Configuration;
import xyz.xiezc.ioc.starter.common.asm.AsmUtil;
import xyz.xiezc.ioc.starter.core.context.ApplicationContext;
import xyz.xiezc.ioc.starter.core.definition.BeanDefinition;
import xyz.xiezc.ioc.starter.core.process.BeanFactoryPostProcess;
import xyz.xiezc.ioc.starter.web.DispatcherHandler;
import xyz.xiezc.ioc.starter.web.annotation.Controller;
import xyz.xiezc.ioc.starter.web.annotation.GetMapping;
import xyz.xiezc.ioc.starter.web.annotation.PostMapping;
import xyz.xiezc.ioc.starter.web.common.Helper;
import xyz.xiezc.ioc.starter.web.entity.RequestDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author xiezc
 */
@Configuration
public class WebBeanFactoryPostProcess implements BeanFactoryPostProcess {

    Log log = LogFactory.get(WebBeanFactoryPostProcess.class);

    @Override
    public void process(ApplicationContext applicationContext) {
        Collection<BeanDefinition> values = applicationContext.getSingletonBeanDefinitionMap().values();
        for (BeanDefinition beanDefinition : values) {
            Class<?> beanClass = beanDefinition.getBeanClass();
            Controller controller = AnnotationUtil.getAnnotation(beanClass, Controller.class);
            if (controller == null) {
                continue;
            }
            String controllerPath = controller.value();
            Method[] publicMethods = ClassUtil.getPublicMethods(beanClass);
            for (Method publicMethod : publicMethods) {
                GetMapping getMapping = publicMethod.getAnnotation(GetMapping.class);
                if (getMapping != null) {
                    RequestDefinition requestDefinition = new RequestDefinition();
                    requestDefinition.setAnnotation(getMapping);
                    requestDefinition.setBeanDefinition(beanDefinition);
                    requestDefinition.setInvokeMethod(publicMethod);
                    requestDefinition.setHttpMethod(HttpMethod.GET);
                    String path = "/" + controllerPath + "/" + getMapping.value();
                    requestDefinition.setPath(Helper.normalizeUrl(path));
                    //获取参数的名称
                    String[] paramNames = AsmUtil.getMethodParamsAndAnnotaton(publicMethod);
                    Parameter[] parameters = publicMethod.getParameters();
                    for (int i = 0; i < parameters.length; i++) {
                        requestDefinition.getParameterMap().put(paramNames[i], parameters[i]);
                    }
                    log.info("scan GET request path:{}",requestDefinition.getPath());
                    DispatcherHandler.requestDefinitionMap.put(requestDefinition.getPath(), requestDefinition);
                }
                PostMapping postMapping = publicMethod.getAnnotation(PostMapping.class);
                if (postMapping != null) {
                    RequestDefinition requestDefinition = new RequestDefinition();
                    requestDefinition.setAnnotation(postMapping);
                    requestDefinition.setBeanDefinition(beanDefinition);
                    requestDefinition.setInvokeMethod(publicMethod);
                    requestDefinition.setHttpMethod(HttpMethod.POST);
                    String path = "/" + controllerPath + "/" + postMapping.value();
                    requestDefinition.setPath(Helper.normalizeUrl(path));
                    //获取参数的名称
                    String[] paramNames = AsmUtil.getMethodParamsAndAnnotaton(publicMethod);
                    Parameter[] parameters = publicMethod.getParameters();
                    for (int i = 0; i < parameters.length; i++) {
                        requestDefinition.getParameterMap().put(paramNames[i], parameters[i]);
                    }
                    log.info("scan POST request path:{}",requestDefinition.getPath());
                    DispatcherHandler.requestDefinitionMap.put(requestDefinition.getPath(), requestDefinition);
                }
            }
        }
    }


}

