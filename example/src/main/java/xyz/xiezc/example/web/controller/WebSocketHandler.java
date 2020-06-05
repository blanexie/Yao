package xyz.xiezc.example.web.controller;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import xyz.xiezc.ioc.starter.starter.web.annotation.WebSockerController;
import xyz.xiezc.ioc.starter.starter.web.netty.websocket.WebSocketFrameHandler;

@WebSockerController("/websocket")
public class WebSocketHandler implements WebSocketFrameHandler {

    @Override
    public WebSocketFrame handleTextWebSocketFrame(TextWebSocketFrame textWebSocketFrame) {
        String text = textWebSocketFrame.text();
        TextWebSocketFrame textWebSocketFrame1 = new TextWebSocketFrame("resp:" + text);
        return textWebSocketFrame1;
    }

    @Override
    public WebSocketFrame handleBinaryWebSocketFrame(BinaryWebSocketFrame binaryWebSocketFrame) {
        return null;
    }
}
