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
package xyz.xiezc.ioc.starter.web;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import xyz.xiezc.ioc.starter.web.entity.FileItem;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;

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

public class MergeRequestHandler extends SimpleChannelInboundHandler<HttpObject> {

    Log log = LogFactory.get(MergeRequestHandler.class);

    private HttpRequest httpRequest;

    private static final HttpDataFactory HTTP_DATA_FACTORY =
            new DefaultHttpDataFactory(true); // Disk if size exceed

    private static final ByteBuf EMPTY_BUF = Unpooled.copiedBuffer("", CharsetUtil.UTF_8);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof io.netty.handler.codec.http.HttpRequest) {
            httpRequest = new HttpRequest();
            httpRequest.setNettyHttpRequest((io.netty.handler.codec.http.HttpRequest) msg);
            return;
        }
        if (null != httpRequest && msg instanceof HttpContent) {
            httpRequest.appendContent((HttpContent) msg);
        }
        if (msg instanceof LastHttpContent) {
            if (null != httpRequest) {
                parseHttpRequest();
                ctx.fireChannelRead(httpRequest);
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }

    private void parseHttpRequest() {
        try {
            io.netty.handler.codec.http.HttpRequest nettyRequest = httpRequest.getNettyHttpRequest();
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, nettyRequest);
            boolean isMultipart = decoder.isMultipart();

            List<ByteBuf> byteBuffs = new ArrayList<>(httpRequest.getContents().size());

            for (HttpContent content : httpRequest.getContents()) {
                if (!isMultipart) {
                    byteBuffs.add(content.content().copy());
                }

                decoder.offer(content);
                this.readHttpDataChunkByChunk(decoder);
                content.release();
            }
            if (!byteBuffs.isEmpty()) {
                httpRequest.setBody(Unpooled.copiedBuffer(byteBuffs.toArray(new ByteBuf[0])));
            }
            parseCookies();
            httpRequest.setMergeSuccess(true);
        } catch (Exception e) {
            throw new RuntimeException("build decoder fail", e);
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!isResetByPeer(cause)) {
            log.error(cause.getMessage(), cause);
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
    private void readHttpDataChunkByChunk(HttpPostRequestDecoder decoder) {
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
                        writeHttpData(data);
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

    private void writeHttpData(InterfaceHttpData data) {
        try {
            InterfaceHttpData.HttpDataType dataType = data.getHttpDataType();
            if (dataType == InterfaceHttpData.HttpDataType.Attribute) {
                parseAttribute((Attribute) data);
            } else if (dataType == InterfaceHttpData.HttpDataType.FileUpload) {
                parseFileUpload((FileUpload) data);
            }
        } catch (IOException e) {
            log.error("Parse request parameter error", e);
        }
    }

    private void parseAttribute(Attribute attribute) throws IOException {
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
    private void parseFileUpload(FileUpload fileUpload) throws IOException {
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
                Paths.get(fileUpload.getFile().getParent()), "blade_", "_upload");

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


    private void parseCookies() {
        io.netty.handler.codec.http.HttpRequest nettyHttpRequest = httpRequest.getNettyHttpRequest();
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