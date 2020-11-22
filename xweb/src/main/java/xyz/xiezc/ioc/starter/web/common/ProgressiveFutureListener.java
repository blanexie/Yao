package xyz.xiezc.ioc.starter.web.common;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;

public class ProgressiveFutureListener implements ChannelProgressiveFutureListener {

    Log log= LogFactory.get(ProgressiveFutureListener.class);

    @Override
    public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
        if (total < 0) { // total unknown
            log.info(future.channel() + " Transfer progress: " + progress);
        } else {
            log.info(future.channel() + " Transfer progress: " + progress + " / " + total);
        }
    }

    @Override
    public void operationComplete(ChannelProgressiveFuture future) {
        log.info(future.channel() + " Transfer complete.");
    }
}

