package com.yen.server.handler;

import com.yen.model.proto.ChatMessageProto;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * ws协议文本帧处理器
 *
 * @author Yhx
 * @date 2024/7/3 14:17
 */
@Slf4j
@ChannelHandler.Sharable
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<ChatMessageProto.ChatMessage> {

    /**
     * 服务端收到消息后触发
     *
     * @param ctx ctx
     * @param msg 消息
     * @throws Exception 例外
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatMessageProto.ChatMessage msg) {
        log.info("服务端收到消息[{}]",msg.getContent());
        // 可回复消息
        ChatMessageProto.ChatMessage resMsg = ChatMessageProto.ChatMessage.newBuilder().setContent("服务端已收到").build();
        ctx.channel().writeAndFlush(resMsg);
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
        // ctx.close();
    }
}
