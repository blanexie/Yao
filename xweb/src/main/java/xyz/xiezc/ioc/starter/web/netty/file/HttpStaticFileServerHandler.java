/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package xyz.xiezc.ioc.starter.web.netty.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import xyz.xiezc.ioc.starter.web.common.ProgressiveFutureListener;
import xyz.xiezc.ioc.starter.web.common.XWebException;
import xyz.xiezc.ioc.starter.web.common.XWebUtil;
import xyz.xiezc.ioc.starter.web.entity.HttpRequest;

import java.io.*;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * A simple handler that serves incoming HTTP requests to send their respective
 * HTTP responses.  It also implements {@code 'If-Modified-Since'} header to
 * take advantage of browser cache, as described in
 * <a href="http://tools.ietf.org/html/rfc2616#section-14.25">RFC 2616</a>.
 *
 * <h3>How Browser Caching Works</h3>
 * <p>
 * Web browser caching works with HTTP headers as illustrated by the following
 * sample:
 * <ol>
 * <li>Request #1 returns the content of {@code /file1.txt}.</li>
 * <li>Contents of {@code /file1.txt} is cached by the browser.</li>
 * <li>Request #2 for {@code /file1.txt} does not return the contents of the
 *     file again. Rather, a 304 Not Modified is returned. This tells the
 *     browser to use the contents stored in its cache.</li>
 * <li>The server knows the file has not been modified because the
 *     {@code If-Modified-Since} date is the same as the file's last
 *     modified date.</li>
 * </ol>
 *
 * <pre>
 * Request #1 Headers
 * ===================
 * GET /file1.txt HTTP/1.1
 *
 * Response #1 Headers
 * ===================
 * HTTP/1.1 200 OK
 * Date:               Tue, 01 Mar 2011 22:44:26 GMT
 * Last-Modified:      Wed, 30 Jun 2010 21:36:48 GMT
 * Expires:            Tue, 01 Mar 2012 22:44:26 GMT
 * Cache-Control:      private, max-age=31536000
 *
 * Request #2 Headers
 * ===================
 * GET /file1.txt HTTP/1.1
 * If-Modified-Since:  Wed, 30 Jun 2010 21:36:48 GMT
 *
 * Response #2 Headers
 * ===================
 * HTTP/1.1 304 Not Modified
 * Date:               Tue, 01 Mar 2011 22:44:28 GMT
 *
 * </pre>
 */
@ChannelHandler.Sharable
public class HttpStaticFileServerHandler extends SimpleChannelInboundHandler<HttpRequest> {

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(HTTP_DATE_FORMAT, Locale.US);
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;

    public String staticPath;

    public HttpStaticFileServerHandler(String staticPath) {
        String s = ClassUtil.getClassPath() + File.separatorChar + staticPath;
        File file = new File(s);
        FileUtil.mkdir(file);
        this.staticPath = s;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        try {
            this.handle(ctx, request);
        } catch (Exception e) {
            FullHttpResponse errorResponse = XWebUtil.getErrorResponse(new XWebException(e));
            this.sendAndCleanupConnection(ctx, errorResponse, request.isKeepAlive());
        }
    }


    @SneakyThrows
    public void handle(ChannelHandlerContext ctx, HttpRequest httpRequest) {
        FullHttpRequest request = httpRequest.getNettyHttpRequest();
        boolean keepAlive = httpRequest.isKeepAlive();
        //获取静态文件
        File file = getStaticFile(ctx, request, keepAlive);
        if (file == null) {
            sendError(ctx, NOT_FOUND, keepAlive);
            return;
        }
        // 检查文件是否有修改，如果没有，直接返回前端
        if (checkCacheValidation(ctx, request, keepAlive, file)) return;

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            //生成响应信息，并设置基本属性
            HttpResponse response = getHttpResponse(request, file);
            // Write the initial line and the header.
            ctx.write(response);
            // Write the content.
            writeFile(ctx, keepAlive, raf);
        } catch (FileNotFoundException ignore) {
            sendError(ctx, NOT_FOUND, keepAlive);
            return;
        }
    }


    private void writeFile(ChannelHandlerContext ctx, boolean keepAlive, RandomAccessFile raf) throws IOException {
        long fileLength = raf.length();
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        if (ctx.pipeline().get(SslHandler.class) == null) {
            sendFileFuture = ctx.write(
                    new DefaultFileRegion(raf.getChannel(), 0, fileLength),
                    ctx.newProgressivePromise()
            );
            // Write the end marker.
            lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

        } else {
            sendFileFuture = ctx.writeAndFlush(
                    new HttpChunkedInput(
                            new ChunkedFile(raf, 0, fileLength, 8192)
                    ),
                    ctx.newProgressivePromise());
            // HttpChunkedInput will write the end marker (LastHttpContent) for us.
            lastContentFuture = sendFileFuture;
        }
        sendFileFuture.addListener(new ProgressiveFutureListener());

        // Decide whether to close the connection or not.
        if (!keepAlive) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private HttpResponse getHttpResponse(FullHttpRequest request, File file) {

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        HttpUtil.setContentLength(response, file.length());
        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file);

        if (!HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        return response;
    }

    private boolean checkCacheValidation(ChannelHandlerContext ctx, FullHttpRequest request, boolean keepAlive, File file) {
        String ifModifiedSince = request.headers().get(HttpHeaderNames.IF_MODIFIED_SINCE);
        if (StrUtil.isNotBlank(ifModifiedSince)) {
            LocalDateTime ifModifiedSinceDate = LocalDateTime.parse(ifModifiedSince, dateTimeFormatter);
            LocalDateTime fileLastModifiedDate = LocalDateTime.ofEpochSecond(file.lastModified() / 1000, 0, ZoneOffset.ofHours(8));
            Duration between = Duration.between(ifModifiedSinceDate, fileLastModifiedDate);
            long seconds = between.getSeconds();
            if (seconds == 0) {
                this.sendNotModified(ctx, keepAlive);
                return true;
            }
        }
        return false;
    }

    private File getStaticFile(ChannelHandlerContext ctx, FullHttpRequest request, boolean keepAlive) {
        final String uri = request.uri();
        final String path = sanitizeUri(uri);
        File file = new File(path);
        if (file.isHidden() || !file.exists()) {
            this.sendError(ctx, NOT_FOUND, keepAlive);
            return null;
        }

        if (!file.isFile()) {
            sendError(ctx, FORBIDDEN, keepAlive);
            return null;
        }
        return file;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (ctx.channel().isActive()) {
            sendError(ctx, INTERNAL_SERVER_ERROR, false);
        }
    }

    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    private String sanitizeUri(String uri) {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + '.') ||
                uri.contains('.' + File.separator) ||
                uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' ||
                INSECURE_URI.matcher(uri).matches()) {
            return null;
        }
        // Convert to absolute path.
        return this.staticPath + File.separator + uri;
    }


    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, boolean keepAlive) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        this.sendAndCleanupConnection(ctx, response, keepAlive);
    }

    /**
     * When file timestamp is the same as what the browser is sending up, send a "304 Not Modified"
     *
     * @param ctx Context
     */
    private void sendNotModified(ChannelHandlerContext ctx, boolean keepAlive) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED, Unpooled.EMPTY_BUFFER);
        setDateHeader(response);
        this.sendAndCleanupConnection(ctx, response, keepAlive);
    }

    /**
     * If Keep-Alive is disabled, attaches "Connection: close" header to the response
     * and closes the connection after the response being sent.
     */
    private void sendAndCleanupConnection(ChannelHandlerContext ctx, FullHttpResponse response, boolean keepAlive) {
        HttpUtil.setContentLength(response, response.content().readableBytes());
        if (!keepAlive) {
            // We're going to close the connection as soon as the response is sent,
            // so we should also make it clear for the client.
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ChannelFuture flushPromise = ctx.writeAndFlush(response);

        if (!keepAlive) {
            // Close the connection as soon as the response is sent.
            flushPromise.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Sets the Date header for the HTTP response
     *
     * @param response HTTP response
     */
    private static void setDateHeader(FullHttpResponse response) {
        String format = LocalDateTime.now().format(dateTimeFormatter);
        response.headers().set(HttpHeaderNames.DATE, format);
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response    HTTP response
     * @param fileToCache file to extract content type
     */
    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        // Date header
        ZoneId of = ZoneId.of(HTTP_DATE_GMT_TIMEZONE);
        ZonedDateTime zonedDateTime = LocalDateTime.now().atZone(of);
        response.headers().set(HttpHeaderNames.DATE, dateTimeFormatter.format(zonedDateTime));
        // Add cache headers
        response.headers().set(HttpHeaderNames.EXPIRES, zonedDateTime.plusSeconds(HTTP_CACHE_SECONDS).format(dateTimeFormatter));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        // 将时间戳转为当前时间
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(fileToCache.lastModified(), 0, ZoneOffset.ofHours(8));
        ZonedDateTime zonedDateTime1 = localDateTime.atZone(of);
        response.headers().set(HttpHeaderNames.LAST_MODIFIED, zonedDateTime1.format(dateTimeFormatter));
    }


    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response HTTP response
     * @param file     file to extract content type
     */
    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response HTTP response
     * @param file     file to extract content type
     */
    private static void setContentTypeHeader(HttpResponse response, File file) {
        String contentType = XWebUtil.mimeType(file.getName());
        if (null == contentType) {
            contentType = URLConnection.guessContentTypeFromName(file.getName());
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    }

}
