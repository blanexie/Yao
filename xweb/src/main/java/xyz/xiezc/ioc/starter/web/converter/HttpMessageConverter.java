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

package xyz.xiezc.ioc.starter.web.converter;

import io.netty.handler.codec.http.FullHttpRequest;
import xyz.xiezc.ioc.starter.web.common.ContentType;

import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Strategy interface that specifies a converter that can convert from and to HTTP requests and responses.
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 */
public interface HttpMessageConverter {


    List<ContentType> getSupportContentType();

    /**
     *
     */
    Object[] parseParamaters(FullHttpRequest request, LinkedHashMap<String, Parameter> paramMap);

}
