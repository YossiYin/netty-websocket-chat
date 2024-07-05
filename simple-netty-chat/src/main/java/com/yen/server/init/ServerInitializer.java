package com.yen.server.init;

import com.yen.server.handler.ServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author Yhx
 * @date 2024/7/2 16:36
 */
public class ServerInitializer extends ChannelInitializer<Channel> {

    private final ServerHandler serverHandler = new ServerHandler();

    /**
     * 多少时间没有读数据，就会触发空闲处理器
     * 参考值：不需要写太短
     */
    private long readerIdleTime = 11;
    /**
     * 多少时间没有写数据，就会触发空闲处理器
     */
    private long writeIdleTime = 11;
    /**
     * 多少时间既没有读也没有写数据，就会触发空闲处理器
     */
    private long allIdleTime = 11;
    @Override
    protected void initChannel(Channel ch) throws Exception {
        // 可以给管道设置处理器、心跳机制、编解码器等
        ch.pipeline()
                // 配置空闲处理器,触发后会传递给下一个Handler的userEventTrigger方法处理。可以只使用一个读空闲.
                .addLast(new IdleStateHandler(readerIdleTime,writeIdleTime,allIdleTime, TimeUnit.SECONDS))
                .addLast("decoder",new StringDecoder()) // 加入解码器
                .addLast("encoder",new StringEncoder()) // 加入编码器
                .addLast(serverHandler); // 最后增加自定义的处理器
    }
}
