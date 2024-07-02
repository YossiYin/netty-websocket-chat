package com.yen.netty.init;

import com.yen.netty.handler.MyNettyServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;


public class MyNettyServerInitializer extends ChannelInitializer<Channel> {

    private final MyNettyServerHandler myNettyServerHandler = new MyNettyServerHandler();

    @Override
    protected void initChannel(Channel ch) throws Exception {
        // 可以给管道设置处理器、心跳机制、编解码器等
        ch.pipeline()
                .addLast(myNettyServerHandler); // addLast:给管道的最后增加自定义的处理器
    }
}
