package com.yen.netty;

import com.yen.netty.init.MyNettyServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Yhx
 * @date 2024/7/1 9:56
 */
@Slf4j
public class NettyServer {
    private static int port = 6668;

    public static void main(String[] args) throws InterruptedException {
        // 默认子线程=核数*2
        // 1. 创建BossGroup线程组:处理连接请求,真正的与客户端进行业务处理,然后交给客户端进行处理
        NioEventLoopGroup boss = new NioEventLoopGroup();
        // 2. 创建WorkerGroup。这两个线程组都是无限循环
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            // 3. 创建服务端的启动对象，配置参数
            ServerBootstrap bootstrap = new ServerBootstrap();
            // 4.链式编程进行设置服务器参数
            bootstrap.group(boss, worker) // 设置两个线程组
                    .channel(NioServerSocketChannel.class) // 使用NioSocketChannel 作为服务器的通道实现
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置线程队列得到连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置保持活动连接状态
                    .childHandler(new MyNettyServerInitializer()); // 进行处理器的初始化:给workerGroup 的 EventLoop对应的管道设置处理器。（可以自定义）
            // 5.绑定端口并且设置该操作为同步操作
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            // 为Future对象添加监听器案例(异步)
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        log.info("服务端成功监听端口{}",port);
                    }
                }
            });
            // 6.异步:对关闭通道进行监听,既当有关闭通道事件发生时才会进行处理
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("服务端出现异常{}", e.getMessage());
            // 7.服务器的关闭
            boss.shutdownGracefully().syncUninterruptibly();
            worker.shutdownGracefully().syncUninterruptibly();
        }
    }
}
