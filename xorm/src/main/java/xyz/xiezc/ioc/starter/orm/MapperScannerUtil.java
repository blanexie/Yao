package xyz.xiezc.ioc.starter.orm;

import cn.hutool.core.io.FileUtil;
import lombok.Data;
import org.apache.ibatis.io.ResolverUtil;
import xyz.xiezc.ioc.starter.orm.common.BaseMapper;
import xyz.xiezc.ioc.starter.orm.xml.DocumentMapperDefine;
import xyz.xiezc.ioc.starter.orm.xml.MapperDefine;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class MapperScannerUtil {


    List<DocumentMapperDefine> documentMapperDefines;

    /**
     * 处理所有的mapper接口对应的xml文件
     *
     * @param mapperPaths
     */
    public void dealMapperXml(List<String> mapperPaths, List<String> mapperLocations) {
        //获取所有的mapper接口。
        Set<Class<?>> baseMapperClazz = mapperPaths.stream()
                .map(mapperPath -> getBaseMapperClazz(mapperPath))
                .flatMap(Collection::stream).collect(Collectors.toSet());
        //获取所有的xml文件
        Set<File> collect = mapperLocations.stream()
                .map(mapperLocation -> {
                    String s = mapperLocation.replaceAll("\\.", "/");
                    List<File> files = FileUtil.loopFiles(s, file -> file.getName().endsWith(".xml"));
                    return files;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());


        List<DocumentMapperDefine> documentPars = collect.stream()
                .map(path -> {
                    try {
                        return new DocumentMapperDefine(path);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(documentMapperDefine -> documentMapperDefine != null)
                .collect(Collectors.toList());

        List<MapperDefine> mapperDefines = baseMapperClazz.stream()
                .map(mapperClazz -> new MapperDefine(mapperClazz))
                .collect(Collectors.toList());

        //找到mapper接口对应的xml文档
        for (MapperDefine mapperDefine : mapperDefines) {
            boolean findDoc = false;
            for (DocumentMapperDefine documentMapperDefine : documentPars) {
                String nameSpace = documentMapperDefine.getNameSpace();
                String name = mapperDefine.getMapperInterface().getName();
                if (Objects.equals(name, nameSpace)) {
                    documentMapperDefine.setMapperDefine(mapperDefine);
                    findDoc = true;
                }
            }
            if (!findDoc) {
                documentPars.add(new DocumentMapperDefine(mapperDefine));
            }
        }

        for (DocumentMapperDefine documentMapperDefine : documentPars) {
            documentMapperDefine.checkDoc();
        }
        documentMapperDefines = documentPars;
    }


    /**
     * 获取实现了BaseMapper接口的接口
     *
     * @param mapperPath
     * @return
     */
    private Set<Class<?>> getBaseMapperClazz(String mapperPath) {
        ResolverUtil resolverUtil = new ResolverUtil();
        ResolverUtil implementations = resolverUtil.findImplementations(BaseMapper.class, mapperPath);
        Set<Class<?>> classes = implementations.getClasses();
        return classes;
    }


}
