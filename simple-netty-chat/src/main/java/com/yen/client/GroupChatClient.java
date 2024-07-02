package com.yen.client;

import com.yen.client.init.ClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * @author Yhx
 * @date 2024/7/1 11:28
 */
@Slf4j
public class GroupChatClient {
    private final String host;
    private final int port;

    /**
     * 构造器
     */
    public GroupChatClient(String host,int port){
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) {
        new GroupChatClient("127.0.0.1",7000).run();
    }

    public void run(){
        // 1.客户端需要一个事件循环组
        NioEventLoopGroup group = new NioEventLoopGroup();

        // 2.创建客户端的启动对象
        Bootstrap bootstrap = new Bootstrap();

        try {
            // 3.进行相关参数设置
            bootstrap.group(group) // 设置线程组
                    .channel(NioSocketChannel.class) // 设置客户端通道的实现类
                    .handler(new ClientInitializer());
            // 4.启动客户端去连接服务端。
            ChannelFuture channelFuture = bootstrap.connect(host,port).sync();

            // 获取当前channel
            Channel channel = channelFuture.channel();
            // 5.客户端需要输入信息
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()){
                String msg = scanner.nextLine();
                // 使用channel发送消息
                channel.writeAndFlush(msg +"\r\n");
            }
            // 6.监听关闭通道事件
            channel.closeFuture().sync();
        }catch (Exception e){
            log.error("客户端出现异常,关闭中");
        }finally {
            // 7.关闭客户端服务
            group.shutdownGracefully();
        }

    }


}
