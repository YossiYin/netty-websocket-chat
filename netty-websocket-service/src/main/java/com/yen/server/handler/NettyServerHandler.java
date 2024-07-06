package com.yen.server.handler;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.yen.model.proto.ChatMessageProto.ChatMessage;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.Descriptors.Descriptor;
import java.util.Map;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Parser;

import com.yen.model.proto.ChatMessageProto;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * ws协议文本帧处理器
 *
 * @author Yhx
 * @date 2024/7/3 14:17
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<ChatMessageProto.ChatMessage> {

    /**
     * 负责客户端Channel管理(线程安全)
     */
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 接收处理客户端发送数据
     *
     * @param ctx ctx
     * @param msg 消息
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatMessageProto.ChatMessage msg) {
     long fromUserId = msg.getFromUserId();
     long toUserId = msg.getToUserId();
     String content = msg.getContent();
     String sendTime = msg.getSendTime();
     int type = msg.getType();
     int status = msg.getStatus();
     long id = msg.getId();
        log.info("服务端收到消息: [\nfromUserId={},\n toUserId={},\n content='{}',\n sendTime={},\n type={},\n status={},\n id={}\n]",
                fromUserId, toUserId, content, sendTime, type, status, id);


        // 异步线程处理业务逻辑

    }

    /**
     * 客户端连接后触发
     *
     * @param ctx ctx
     * @throws Exception 例外
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 长ID才是channel唯一值
        String channelId = ctx.channel().id().asLongText();
        log.info("{}客户端已连接",channelId);
    }

    /**
     * 客户端断开后触发
     *
     * @param ctx ctx
     * @throws Exception 例外
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        log.info("{}客户端已断开",channelId);
    }

    /**
     * channel捕获到异常
     *
     * @param ctx   ctx
     * @param cause 原因
     * @throws Exception 例外
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("{}通道发送异常,正在关闭...",ctx.channel().id().asLongText());
        // 发送异常关闭连接
        ctx.close();
    }
}
