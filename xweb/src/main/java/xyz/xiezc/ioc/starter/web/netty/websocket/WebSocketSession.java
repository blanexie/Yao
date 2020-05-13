package xyz.xiezc.ioc.starter.web.netty.websocket;

import cn.hutool.core.util.IdUtil;
import io.netty.channel.Channel;
import lombok.Data;

/**
 * @author biezhi
 * @date 2017/10/30
 */
@Data
public class WebSocketSession {

    private Channel channel;
    private String uuid;

    public WebSocketSession(Channel channel) {
        this.channel = channel;
        this.uuid = IdUtil.fastSimpleUUID();
    }
}
