package com.yen.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 自定义Netty服务器处理器
 * 真正的处理通道里的信息的方法
 * 选择SimpleChannelInboundHandler为处理器是因为
 * 1.支持泛型，消息不需要进行类型强制转化(需要配置编解码器)。此处可以升级为使用Protobuf高效编码解码
 * 2.使用了ReferenceCountUtil.release(msg)，即自动释放消息
 * 一个需要注意的点，即如果使用这个类，并且后续的处理器中仍需要读取消息，则必须手工调用
 * ReferenceCountUtil.retain(msg)，也就是让消息的引用计数加1，
 * 否则框架对引用计数为0的消息会执行释放和回收。
 *
 * @author Yhx
 * @date 2024/7/1 10:29
 */
@Slf4j
// 标记了Sharable才能共享该实例
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    /**
     * 所有客户端共享的线程组,管理所以的channel
     * 多个线程共享
     * GlobalEventExecutor.INSTANCE是一个全局事件执行器
     * 特点:
     * 1.会自动清除断开连接的channel
     *
     * 其他人的案例是使用自己定义一个集合列表，自己存入进去，但可能会有线程安全问题
     */
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 1.此方法是第一个被调用的方法,表示连接建立
     * 连接建立后立刻执行
     *
     * @param ctx ctx
     * @throws Exception 例外
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("有用户已加入连接handlerAdded:[{}]",ctx.channel().remoteAddress());
        // 将当前channel加入到集合管理
        Channel channel = ctx.channel();
        // [1.上线提醒功能]:将该用户加入聊天的信息推送给其他在线的用户
        // 使用自带的方法实现全部推送,原理是循环所有他管理的通道
        channelGroup.writeAndFlush("[客户端]" + channel.remoteAddress() + "加入聊天");
        channelGroup.add(channel);
    }

    /**
     * 2.该方法表示Channel处于活动状态时触发
     *
     * @param ctx ctx
     * @throws Exception 例外
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // [2.上线提醒功能]
        log.info("有用户已上线检测到channelActive()方法执行:[{}]",ctx.channel().remoteAddress());
    }

    /**
     * 3.channel处于非活动状态时会触发该方法
     *
     * @param ctx ctx
     * @throws Exception 例外
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // [3.离线提醒功能]
        log.info("有用户处于不活跃状态channelInactive:[{}]",ctx.channel().remoteAddress());
    }

    /**
     * 4.断开连接时触发该方法
     *
     * @param ctx ctx
     * @throws Exception 例外
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("有用户已断开handlerRemoved:[{}]",ctx.channel().remoteAddress());
        Channel channel = ctx.channel();
        // [4.全服提醒用户下线功能]
        channelGroup.writeAndFlush("【全体通知】有用户断线："+channel.remoteAddress());
    }


    /**
     * 特殊:有信息传来时才会触发该方法
     *
     * @param ctx ctx
     * @param msg 客户端发送的数据
     * @throws Exception 例外
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("收到来自客户端的信息:{}",msg);
        // 获取到信息之后就可以判断消息然后进行各种操作了,例如：
        // 1.如果是刚开始聊天,那么就保存客户端与 Channel 之间的关系(channel集合管理)。后续可以被取出来使用
        // 2.如果传的是业务类型的消息那么就可以进行对应的业务处理。例如：备份聊天信息到缓存/数据库;异步发送消息
        // 可进行异步推送处理长时间的业务

        // [5.群发消息功能]:注意点是需要排除自己
        Channel channel = ctx.channel();
        channelGroup.forEach(ch -> {
            if (channel != ch){
                // 只要不是自己就发
                ch.writeAndFlush("[用户]" + channel.remoteAddress() + "群发了消息:" + msg + "\n");
            }
        });

    }

    /**
     * 特殊：消息数据读取完毕时触发该方法
     * 也就是在channelRead0()之后触发
     * 可选方法.因为channelRead0也可以直接拿取通道进行操作
     * 对消息读取完毕后进行的操作(例如存入缓存或者数据库)
     *
     * @param ctx ctx
     * @throws Exception 例外
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("我是服务端，我成功收到了你的消息",CharsetUtil.UTF_8));
    }



    /**
     * 特殊:处理channel中发生的异常
     * 一般是需要关闭通道
     *
     * @param ctx   ctx
     * @param cause 原因
     * @throws Exception 例外
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("该通道异常,关闭通道...");
        log.debug(cause.getMessage(),cause);
        // 关闭通道
        ctx.close();
    }
}
