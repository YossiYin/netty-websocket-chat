package com.yen.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author Yhx
 * @date 2024/6/30 10:49
 */
public class NIOClient {
    /**
     * 1. 获得通道
     * 2. 构建连接
     * 3. 包装数据
     * 4. 发送数据
     *
     * @param args args
     */
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();

        socketChannel.configureBlocking(false);

        // 连接服务端端口
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);

        if (!socketChannel.connect(inetSocketAddress)){
            while (!socketChannel.finishConnect()){
                System.out.println("未连接上，但并不会阻塞，可以进行其他业务");
            }
        }

        // 连接成功
        // 发送数据到服务端
        String str = "Hello,客户端发送消息";
        // 使用api快速构造buffer
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());

        // 发送数据，把buffer数据写入到channel中
        socketChannel.write(buffer);

        // 停止查看效果
        System.in.read();
    }
}
