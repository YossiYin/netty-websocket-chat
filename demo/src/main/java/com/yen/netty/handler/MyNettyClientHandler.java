package com.yen.netty.handler;

import com.yen.common.BaseRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端处理器
 * @author Yhx
 * @date 2024/7/1 11:33
 */
@Slf4j
public class MyNettyClientHandler extends SimpleChannelInboundHandler<Object>{

    /**
     * 客户端成功连接时触发该方法
     *
     * @param ctx ctx
     * @throws Exception 例外
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 客户端和服务端建立连接时调用
        log.info("客户端成功连接");
        ctx.writeAndFlush(Unpooled.copiedBuffer("我是客户端，我成功收到了你的消息:",CharsetUtil.UTF_8)).addListeners((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                log.error("IO error,close Channel");
                future.channel().close();
            }
        }) ;
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
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("收到来自服务端的信息");

        ByteBuf byteBuf= (ByteBuf) msg;

        log.info("服务端发来的信息是:{}", byteBuf.toString(CharsetUtil.UTF_8));
        log.info("服务器的地址:{}",ctx.channel().remoteAddress());
    }

    /**
     * 处理异常
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
