package com.yen.server.init;


import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import com.yen.model.proto.ChatMessageProto;
import com.yen.server.handler.NettyServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * @author Yhx
 * @date 2024/7/2 16:36
 */
public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger log = LoggerFactory.getLogger(NettyServerInitializer.class);
    private final NettyServerHandler webSocketFrameHandler = new NettyServerHandler();

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
    private long readerIdleTime = 11, writeIdleTime = 11, allIdleTime = 11;

    /**
     * 是否打印debug日志
     */
    private boolean isDebug = false;

    /**
     * Channel初始器配置
     */
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (isDebug) {
            pipeline.addLast(new LoggingHandler());
        }

        // 使用proto协议进行数据传输
        pipeline
                // 配置空闲处理器,触发后会传递给下一个Handler的userEventTrigger方法处理
                .addLast(new IdleStateHandler(readerIdleTime, writeIdleTime, allIdleTime, TimeUnit.SECONDS))
                .addLast(new HttpServerCodec())
                // 支持参数对象解析,比如POST参数,设置聚合内容的最大长度
                .addLast(new HttpObjectAggregator(65536))
                // 支持大数据流写入
                .addLast(new ChunkedWriteHandler())
                // 支持Websocket数据压缩
                .addLast(new WebSocketServerCompressionHandler())
                // Websocket协议配置, 设置访问路径
                .addLast(new WebSocketServerProtocolHandler(webSocketPath,null,true))
                // 解码器: 通过GoogleProtocolBuffers序列化框架动态的切割接收到的ByteBuf
                .addLast(new ProtobufVarint32FrameDecoder())
                // 编码器: GoogleProtocolBuffers长度属性编码器
                .addLast(new ProtobufVarint32LengthFieldPrepender())

                // 协议包解码
                .addLast(new MessageToMessageDecoder<WebSocketFrame>() {
                    @Override
                    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> objs) throws Exception {
                        log.info("收到客户端消息----------------------");
                        if (frame instanceof TextWebSocketFrame) {
                            // 文本消息
                            TextWebSocketFrame textFrame = (TextWebSocketFrame)frame;
                            log.info("消息类型为TextWebSocketFrame");
                        }else if (frame instanceof BinaryWebSocketFrame){
                            // 二进制消息
                            ByteBuf buf = ((BinaryWebSocketFrame) frame).content();
                            objs.add(buf);
                            // 自旋累加
                            buf.retain();
                            log.info("消息类型为BinaryWebSocketFrame");
                        } else if (frame instanceof PongWebSocketFrame) {
                            // PING存活检测消息
                            log.info("消息类型为PongWebSocketFrame");
                        } else if (frame instanceof CloseWebSocketFrame) {
                            // 关闭指令消息
                            log.info("消息类型为CloseWebSocketFrame");
                            ch.close();
                        }
                    }
                })
                // 协议包编码
                .addLast(new MessageToMessageEncoder<MessageLiteOrBuilder>() {
                    @Override
                    protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out) throws Exception {
                        ByteBuf result = null;
                        if (msg instanceof MessageLite) {
                            // 没有build的Protobuf消息
                            result = wrappedBuffer(((MessageLite) msg).toByteArray());
                        }
                        if (msg instanceof MessageLite.Builder) {
                            // 经过build的Protobuf消息
                            result = wrappedBuffer(((MessageLite.Builder) msg).build().toByteArray());
                        }
                        // 将Protbuf消息包装成Binary Frame 消息
                        WebSocketFrame frame = new BinaryWebSocketFrame(result);
                        out.add(frame);
                    }
                })
                // 解码器: 解码自定义的业务消息对象
                .addLast(new ProtobufDecoder(ChatMessageProto.ChatMessage.getDefaultInstance()))
                // 最后增加自定义的处理器,处理业务逻辑
                .addLast(webSocketFrameHandler);
    }
}
