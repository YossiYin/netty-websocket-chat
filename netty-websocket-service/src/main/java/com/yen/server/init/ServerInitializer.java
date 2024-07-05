package com.yen.server.init;


import com.yen.model.proto.ChatMessageProto;
import com.yen.server.handler.WebSocketFrameHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author Yhx
 * @date 2024/7/2 16:36
 */
public class ServerInitializer extends ChannelInitializer<Channel> {
    private final WebSocketFrameHandler webSocketFrameHandler = new WebSocketFrameHandler();

    /**
     * 浏览器请求websocket接口时 ws://ip:host//api/chat    对应路径
     */
    private final String webSocketPath = "/api/chat";
    /**
     * 可以用于限制请求体或响应体的最大大小
     * 限制的是每次http请求的内容大小
     */
    private final int maxContentLength = 8192;
    /**
     * readerIdleTime:多少时间没有读数据，就会触发空闲处理器
     * writeIdleTime:多少时间没有写数据，就会触发空闲处理器
     * allIdleTime:多少时间既没有读也没有写数据，就会触发空闲处理器
     * 参考值：不需要写太短
     */
    private long readerIdleTime = 11 ,writeIdleTime = 11 ,allIdleTime = 11;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        // 可以给管道设置处理器、心跳机制、编解码器等

        // 使用proto协议进行数据传输
        ch.pipeline()
                .addLast(new IdleStateHandler(readerIdleTime,writeIdleTime,allIdleTime, TimeUnit.SECONDS)) // 可配置空闲处理器,触发后会传递给下一个Handler的userEventTrigger方法处理。可以只使用一个读空闲.
                .addLast("decoder",new ProtobufDecoder(ChatMessageProto.ChatMessage.getDefaultInstance())) // 指定对哪种对象进行解码
                .addLast("encoder",new ProtobufEncoder()) // 加入protobuf编码器
                .addLast(webSocketFrameHandler); // 最后增加自定义的处理器,处理业务逻辑
    }
}
