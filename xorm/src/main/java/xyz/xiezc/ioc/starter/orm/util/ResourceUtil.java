package xyz.xiezc.ioc.starter.orm.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.EnumerationIter;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.resource.FileResource;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.util.*;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author xiezc
 * @Version 1.0
 * @Date 2020/6/16 11:36 上午
 **/
public class ResourceUtil {

    static Log log = LogFactory.get(ResourceUtil.class);

    private List<Resource> findResources(Class clazz, String prefixStr, String suffixStr) throws IOException {
        //获取传入的class类的文件路径地址

        String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        String prefixPackagePath = prefixStr;
        List<Resource> classFiles = new ArrayList<>();
        //根据这个地址的后缀来判断 是否是jar包， 如果jar包就使用JarFile类来读取文件
        if (path.endsWith(".jar")) {
            try (JarFile jarFile = new JarFile(path)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String name = jarEntry.getName();
                    if (name.endsWith(suffixStr) && name.startsWith(prefixPackagePath)) {
                        Resource resourceObj = cn.hutool.core.io.resource.ResourceUtil.getResourceObj(name);
                        classFiles.add(resourceObj);
                    }
                }
            }
        } else {
            Resource resourceObj = cn.hutool.core.io.resource
                    .ResourceUtil
                    .getResourceObj(URLUtil.CLASSPATH_URL_PREFIX + prefixPackagePath);
            List<File> files = FileUtil.loopFiles(resourceObj.getUrl().getFile(), file -> file.getName().endsWith(suffixStr));
            List<FileResource> collect = files.stream().map(FileResource::new).collect(Collectors.toList());
            classFiles.addAll(collect);
        }
        return classFiles;
    }
}