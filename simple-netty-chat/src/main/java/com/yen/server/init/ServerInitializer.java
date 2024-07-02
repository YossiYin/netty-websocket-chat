package com.yen.server.init;

import com.yen.server.handler.ServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author Yhx
 * @date 2024/7/2 16:36
 */
public class ServerInitializer extends ChannelInitializer<Channel> {

    private final ServerHandler serverHandler = new ServerHandler();
    @Override
    protected void initChannel(Channel ch) throws Exception {
        // 可以给管道设置处理器、心跳机制、编解码器等
        ch.pipeline()
                .addLast("decoder",new StringDecoder()) // 加入解码器
                .addLast("encoder",new StringEncoder()) // 假如编码器
                .addLast(serverHandler); // 最后增加自定义的处理器
    }
}
