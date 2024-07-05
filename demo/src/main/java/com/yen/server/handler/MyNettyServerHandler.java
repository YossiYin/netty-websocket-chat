package com.yen.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
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
public class MyNettyServerHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * 1.有信息传来时会触发该方法
     * channelRead 中调用了 channelRead0，
     * 其会先做消息类型检查，判断当前message 是否需要传递到下一个handler。
     *
     * @param ctx ctx
     * @param msg 客户端发送的数据
     * @throws Exception 例外
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        log.info("收到来自客户端的信息:{}",byteBuf.toString(CharsetUtil.UTF_8));
        // 获取到信息之后就可以判断消息然后进行各种操作了,例如：
        // 1.如果是刚开始聊天,那么就保存客户端与 Channel 之间的关系(channel集合管理)。后续可以被取出来使用

        // 2.如果传的是业务类型的消息那么就可以进行对应的业务处理。例如：备份聊天信息到缓存/数据库;异步发送消息
        // 可进行异步推送处理长时间的业务

        // 任务队列案例1:用户普通任务(taskQueue队列) (可以执行多次这个方法,添加多个任务,但是会按顺序执行)
        ctx.channel().eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                ctx.writeAndFlush(Unpooled.copiedBuffer("服务端正在异步执行普通任务队列的任务",CharsetUtil.UTF_8));
            }
        });
        // 任务队列案例2:用户自定义定时任务(scheduleTaskQueue队列),以下演示延迟5秒后进行
        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                ctx.writeAndFlush(Unpooled.copiedBuffer("服务端正在异步执行定时任务队列的任务",CharsetUtil.UTF_8));
            }
        }, 5, TimeUnit.SECONDS);
        // 任务队列案例3:异步任务（处理其他线程向本线程调度任务）
        // 首先需要一个集合管理着所有channel，只需要从channel中拿到对应用户标识的channel去执行案例1即可


        // 3.检测来自客户端发送的心跳ping,然后响应pong消息

    }

    /**
     * 消息数据读取完毕
     * 可选方法.因为channelRead0也可以直接拿取通道进行操作
     * 对消息读取完毕后进行的操作(例如存入缓存或者数据库)
     *
     * @param ctx ctx
     * @throws Exception 例外
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("进入channelReadComplete方法");

        ctx.writeAndFlush(Unpooled.copiedBuffer("我是服务端，我成功收到了你的消息",CharsetUtil.UTF_8));
    }

    /**
     * 处理异常,一般是需要关闭通道
     *
     * @param ctx   ctx
     * @param cause 原因
     * @throws Exception 例外
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("聊天系统捕捉到异常,关闭通道...");
        log.error(cause.getMessage(),cause);
        // 关闭通道
        ctx.close();
    }
}
