/**
 * Copyright (c) 2018, biezhi 王爵 nice (biezhi.me@gmail.com)
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
package xyz.xiezc.ioc.starter.starter.web.netty;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import xyz.xiezc.ioc.starter.starter.web.entity.FileItem;
import xyz.xiezc.ioc.starter.starter.web.entity.HttpRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Merge Netty HttpObject as {@link HttpRequest}
 *
 * @author biezhi
 * 2018/10/15
 */
@ChannelHandler.Sharable
public class ParseRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    Log log = LogFactory.get(ParseRequestHandler.class);

    private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(true); // Disk if size exceed

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        log.info("进入ParseRequestHandler：{}",msg.uri());
        HttpRequest httpRequest = parseHttpRequest(ctx, msg);
        ctx.fireChannelRead(httpRequest);
    }

    private HttpRequest parseHttpRequest(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
        try {
            //构建我们自己的httpRequest
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.setNettyHttpRequest(fullHttpRequest);
            //解析cookie
            parseCookies(httpRequest);
            //解析公共的部分
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(fullHttpRequest.uri());
            httpRequest.setQueryParamMap(queryStringDecoder.parameters());
            httpRequest.setPath(queryStringDecoder.path());
            httpRequest.setMethod(fullHttpRequest.method().name());
            httpRequest.setHttpVersion(fullHttpRequest.protocolVersion());
            httpRequest.setKeepAlive(HttpUtil.isKeepAlive(fullHttpRequest));
            //判断请求是get还是post
            if (StrUtil.equalsIgnoreCase("GET", httpRequest.getMethod())) {
                return httpRequest;
            }
            //post 请求
            if (StrUtil.equalsIgnoreCase("POST", httpRequest.getMethod())) {
                httpRequest.setContentType(HttpUtil.getMimeType(fullHttpRequest).toString());
                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, fullHttpRequest);
                //非multipart的类型的post请求，请求中只会有一个HttpContent. 所以
                if (!decoder.isMultipart()) {
                    ByteBuf content = fullHttpRequest.content();
                    ByteBuf byteBuf = ByteBufUtil.readBytes(ctx.alloc(), content, content.readableBytes());
                    httpRequest.setBody(byteBuf);
                    return httpRequest;
                }
                //multipart类型的应用
                this.readHttpDataChunkByChunk(decoder, httpRequest);

                return httpRequest;
            }
            return httpRequest;
        } catch (Exception e) {
            throw new RuntimeException("build decoder fail", e);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        if (!isResetByPeer(cause)) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(500));
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    boolean isResetByPeer(Throwable e) {
        if (null != e.getMessage() &&
                e.getMessage().contains("Connection reset by peer")) {
            return true;
        }
        return false;
    }

    /**
     * Example of reading request by chunk and getting values from chunk to chunk
     */
    private void readHttpDataChunkByChunk(HttpPostRequestDecoder decoder, HttpRequest httpRequest) {
        try {
            HttpData partialContent = null;
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    // check if current HttpData is a FileUpload and previously set as partial
                    if (partialContent == data) {
                        partialContent = null;
                    }
                    try {
                        // new value
                        writeHttpData(data, httpRequest);
                    } finally {
                        data.release();
                    }
                }
            }
            // Check partial decoding for a FileUpload
            InterfaceHttpData data = decoder.currentPartialHttpData();
            if (data != null) {
                if (partialContent == null) {
                    partialContent = (HttpData) data;
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            // end
        }
    }

    private void writeHttpData(InterfaceHttpData data, HttpRequest httpRequest) {
        try {
            InterfaceHttpData.HttpDataType dataType = data.getHttpDataType();
            if (dataType == InterfaceHttpData.HttpDataType.Attribute) {
                parseAttribute((Attribute) data, httpRequest);
            } else if (dataType == InterfaceHttpData.HttpDataType.FileUpload) {
                parseFileUpload((FileUpload) data, httpRequest);
            }
        } catch (IOException e) {
            log.error("Parse request parameter error", e);
        }
    }

    private void parseAttribute(Attribute attribute, HttpRequest httpRequest) throws IOException {
        var name = attribute.getName();
        var value = attribute.getValue();
        Map<String, List<String>> parameters = httpRequest.getBodyParamMap();
        List<String> values;
        if (parameters.containsKey(name)) {
            values = parameters.get(name);
            values.add(value);
        } else {
            values = new ArrayList<>();
            values.add(value);
            parameters.put(name, values);
        }
    }

    /**
     * Parse FileUpload to {@link FileItem}.
     *
     * @param fileUpload netty http file upload
     */
    private void parseFileUpload(FileUpload fileUpload, HttpRequest httpRequest) throws IOException {
        if (!fileUpload.isCompleted()) {
            return;
        }
        FileItem fileItem = new FileItem();
        fileItem.setName(fileUpload.getName());
        fileItem.setFileName(fileUpload.getFilename());

        // Upload the file is moved to the specified temporary file,
        // because FileUpload will be release after completion of the analysis.
        // tmpFile will be deleted automatically if they are used.
        Path tmpFile = Files.createTempFile(
                Paths.get(fileUpload.getFile().getParent()), "xweb_", "_upload");

        Path fileUploadPath = Paths.get(fileUpload.getFile().getPath());
        Files.move(fileUploadPath, tmpFile, StandardCopyOption.REPLACE_EXISTING);

        fileItem.setFile(tmpFile.toFile());
        fileItem.setPath(tmpFile.toFile().getPath());
        fileItem.setContentType(fileUpload.getContentType());
        fileItem.setLength(fileUpload.length());

        Map<String, FileItem> fileItems = httpRequest.getFileItems();
        if (fileItems == null) {
            fileItems = new HashMap<>();
            httpRequest.setFileItems(fileItems);
        }
        fileItems.put(fileItem.getName(), fileItem);
    }


    private void parseCookies(HttpRequest httpRequest) {
        FullHttpRequest nettyHttpRequest = httpRequest.getNettyHttpRequest();
        HttpHeaders headers = nettyHttpRequest.headers();
        String headerName = "Cookie";
        String cookieStr = "";

        if (headers.contains(headerName)) {
            cookieStr = headers.get(headerName);
        } else {
            cookieStr = headers.get(headerName.toLowerCase());
        }
        if (StrUtil.isNotEmpty(cookieStr)) {
            Set<Cookie> decode = ServerCookieDecoder.LAX.decode(cookieStr);
            httpRequest.setCookies(decode);
        }
    }

}