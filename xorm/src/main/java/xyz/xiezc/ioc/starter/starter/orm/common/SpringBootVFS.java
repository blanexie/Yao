/**
 * Copyright 2015-2016 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.xiezc.ioc.starter.starter.orm.common;

import cn.hutool.core.util.ClassUtil;
import org.apache.ibatis.io.VFS;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Hans Westerbeek
 * @author Eddú Meléndez
 */
public class SpringBootVFS extends VFS {

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    protected List<String> list(URL url, String path) throws IOException {
        String s = path.replaceAll("/", ".");
        Set<Class<?>> classes = ClassUtil.scanPackage(s);
        List<String> resourcePaths = new ArrayList<String>();
        classes.forEach(cl -> {
            String name = cl.getName();
            String s1 = name.replaceFirst(s, "");
            String s2 = s1.replaceAll("\\.", "/") + ".class";
            resourcePaths.add(s2);
        });
        return resourcePaths;
    }

    private static String preserveSubpackageName(final URI uri, final String rootPath) {
        final String uriStr = uri.toString();
        final int start = uriStr.indexOf(rootPath);
        return uriStr.substring(start, uriStr.length());
    }

}
