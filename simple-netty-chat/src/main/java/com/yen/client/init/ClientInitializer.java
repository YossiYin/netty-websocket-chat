package com.yen.client.init;

import com.yen.client.handler.ClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 对客户端管道进行初始化
 * @author Yhx
 * @date 2024/7/1 11:39
 */
public class ClientInitializer extends ChannelInitializer<Channel> {
    private final ClientHandler clientHandler = new ClientHandler();

    @Override
    protected void initChannel(Channel ch) throws Exception {
        // 对管道进行初始化
        ch.pipeline()
                .addLast("decoder",new StringDecoder()) // 加入解码器
                .addLast("encoder",new StringEncoder()) // 假如编码器
                .addLast(clientHandler); // 最后再添加自己的Handler
    }
}
