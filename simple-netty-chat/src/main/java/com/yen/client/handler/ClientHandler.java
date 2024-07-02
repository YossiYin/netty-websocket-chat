package com.yen.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端处理器
 * @author Yhx
 * @date 2024/7/1 11:33
 */
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<String>{

    /**
     * 1.客户端成功连接时触发该方法
     *
     * @param ctx ctx
     * @throws Exception 例外
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 客户端和服务端建立连接时调用
        log.info("客户端成功连接");
    }

    /**
     * 客户端收到消息时
     * 当通道有读取事件时触发该方法
     *
     * @param ctx ctx
     * @param msg 消息
     * @throws Exception 例外
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("收到消息:{}", msg);
    }

    /**
     * 特殊:处理异常
     *
     * @param ctx   ctx
     * @param cause 原因
     * @throws Exception 例外
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端捕获到异常:{}",cause.getMessage());
        ctx.close();
    }
}
