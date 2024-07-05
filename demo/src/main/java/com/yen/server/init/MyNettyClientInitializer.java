package com.yen.server.init;

import com.yen.server.handler.MyNettyClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

/**
 * 对客户端管道进行初始化
 * @author Yhx
 * @date 2024/7/1 11:39
 */
public class MyNettyClientInitializer extends ChannelInitializer<Channel> {
    private final MyNettyClientHandler myNettyClientHandler = new MyNettyClientHandler();

    @Override
    protected void initChannel(Channel ch) throws Exception {
        // 对管道进行初始化
        ch.pipeline().addLast(myNettyClientHandler);
    }
}
