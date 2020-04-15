/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.xiezc.web.handler;

import xyz.xiezc.web.common.ContentType;
import xyz.xiezc.web.common.MappingHandler;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Strategy interface that specifies a converter that can convert from and to HTTP requests and responses.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface HttpMessageConverter<T> {


    List<ContentType> getSupportContentType();

    /**
     *
     */
    void read(MappingHandler mappingHandler, HttpServletRequest inputStream)
            throws IOException;

    /**
     *
     */
    void write(T t, ContentType contentType, HttpServletResponse outputMessage)
            throws IOException;

}
