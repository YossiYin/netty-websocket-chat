package com.yen.server;

import com.yen.server.init.MyNettyClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Yhx
 * @date 2024/7/1 11:28
 */
@Slf4j
public class NettyClient {

    public static void main(String[] args) throws InterruptedException {
        // 1.客户端需要一个事件循环组
        NioEventLoopGroup group = new NioEventLoopGroup();

        // 2.创建客户端的启动对象
        Bootstrap bootstrap = new Bootstrap();

        try {
            // 3.进行相关参数设置
            bootstrap.group(group) // 设置线程组
                    .channel(NioSocketChannel.class) // 设置客户端通道的实现类
                    .handler(new MyNettyClientInitializer());
            // 4.启动客户端去连接服务端
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6668).sync();
            // 5.监听关闭通道事件
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            // 关闭客户端服务
            log.error("客户端出现异常,关闭中");
            group.shutdownGracefully();
        }

    }
}
