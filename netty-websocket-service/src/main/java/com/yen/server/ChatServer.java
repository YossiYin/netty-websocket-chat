package com.yen.server;


import com.yen.server.init.NettyServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Yhx
 * @date 2024/7/2 16:31
 */
@Slf4j
public class ChatServer {

    /**
     * 监听端口
     */
    private int nettyPort;

    /**
     * 构造器
     *
     * @param nettyPort 端口
     */
    public ChatServer(int nettyPort) {
        this.nettyPort = nettyPort;
    }

    /**
     * 启动
     *
     * @param args args
     */
    public static void main(String[] args) {
        new ChatServer(7000).run();
    }

    /**
     * 启动方法，处理客户端的请求
     */
    public void run() {
        // 1. 创建网络服务器(创建BossGroup线程组:处理连接请求)
        NioEventLoopGroup boss = new NioEventLoopGroup();
        // 2. 创建Worker线程
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            // 3. 创建Netty服务端启动类
            ServerBootstrap bootstrap = new ServerBootstrap();
            // 4.链式编程进行配置服务器参数
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class) // 使用NIO通道
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // 设置保持活动连接状态
                    .childHandler(new NettyServerInitializer()); // 进行自定义初始化
            // 5.绑定端口并且设置该操作为同步操作
            ChannelFuture channelFuture = bootstrap.bind(nettyPort).sync();
            // 6.监听是否启动成功
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        log.info("Netty聊天服务端启动成功! 监听端口:{}", nettyPort);
                    }
                }
            });
            // 6.异步对关闭通道进行监听,既当有关闭通道事件发生时才会进行处理
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            log.error("Netty服务端出现异常{}", e.getMessage());

        } finally {
            // 7.最终都需要服务器的关闭
            boss.shutdownGracefully().syncUninterruptibly();
            worker.shutdownGracefully().syncUninterruptibly();
        }

    }

}
