package xyz.xiezc.ioc.starter.orm.annotation.handler;

import xyz.xiezc.ioc.ApplicationContextUtil;
import xyz.xiezc.ioc.annotation.AnnotationHandler;
import xyz.xiezc.ioc.annotation.Component;
import xyz.xiezc.ioc.definition.BeanDefinition;
import xyz.xiezc.ioc.definition.FieldDefinition;
import xyz.xiezc.ioc.definition.MethodDefinition;
import xyz.xiezc.ioc.starter.orm.annotation.MapperScan;

@Component
public class MapperScanAnnotationHandler extends AnnotationHandler<MapperScan> {
    @Override
    public Class<MapperScan> getAnnotationType() {
        return MapperScan.class;
    }

    @Override
    public void processClass(MapperScan annotation, Class clazz, ApplicationContextUtil contextUtil) {
        String[] value = annotation.value();
        for (String s : value) {
            XormConfiguration.mapperScan.add(s);
        }
    }

    @Override
    public void processMethod(MethodDefinition methodDefinition, MapperScan annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }

    @Override
    public void processField(FieldDefinition fieldDefinition, MapperScan annotation, BeanDefinition beanDefinition, ApplicationContextUtil contextUtil) {

    }
}
