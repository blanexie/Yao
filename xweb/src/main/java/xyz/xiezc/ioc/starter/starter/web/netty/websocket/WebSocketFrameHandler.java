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
package xyz.xiezc.ioc.starter.starter.web.netty.websocket;

import io.netty.handler.codec.http.websocketx.*;

/**
 * Echoes uppercase content of text frames.
 */
public interface WebSocketFrameHandler {

    default WebSocketFrame handle(WebSocketFrame webSocketFrame) {
        if (webSocketFrame instanceof PingWebSocketFrame) {
            return this.handlePingWebSocketFrame((PingWebSocketFrame) webSocketFrame);
        }
        if (webSocketFrame instanceof TextWebSocketFrame) {
            return this.handleTextWebSocketFrame((TextWebSocketFrame) webSocketFrame);
        }
        if (webSocketFrame instanceof BinaryWebSocketFrame) {
            return this.handleBinaryWebSocketFrame((BinaryWebSocketFrame) webSocketFrame);
        }
        if (webSocketFrame instanceof CloseWebSocketFrame) {
            return this.handleCloseWebSocketFrame((CloseWebSocketFrame) webSocketFrame);
        }
        return null;
    }

    /**
     * @param closeWebSocketFrame
     * @return
     */
    default WebSocketFrame handleCloseWebSocketFrame(CloseWebSocketFrame closeWebSocketFrame) {
        return closeWebSocketFrame.retain();
    }


    /**
     * @param pingWebSocketFrame
     * @return
     */
    default WebSocketFrame handlePingWebSocketFrame(PingWebSocketFrame pingWebSocketFrame) {
        return new PongWebSocketFrame(pingWebSocketFrame.content().retain());
    }


    /**
     * @param textWebSocketFrame
     * @return
     */
    WebSocketFrame handleTextWebSocketFrame(TextWebSocketFrame textWebSocketFrame);

    /**
     * @param binaryWebSocketFrame
     * @return
     */
    WebSocketFrame handleBinaryWebSocketFrame(BinaryWebSocketFrame binaryWebSocketFrame);

}
