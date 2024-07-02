package com.yen.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Yhx
 * @date 2024/6/30 10:30
 */
public class NIOServer {

    /**
     * 1. 客户端连接时通过ServerScoketChannel获取ScoketChannel
     * 2. 将ScoketChannel注册到SelectionKey里（register），注册后将返回一个SelectionKey（与Selctor关联）
     * 3. Selector进行select()方法进行监听，若有事件发生则得到对应的SelectionKey
     * 4. 然后通过SelectionKey获取到注册的Channel
     * 5. 对获得到的Channel完成对应的业务
     *
     * @param args args
     * @throws IOException IOException
     */
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        Selector selector = Selector.open();

        serverSocketChannel.socket().bind(new InetSocketAddress(6666));
        serverSocketChannel.configureBlocking(false);

        // 将channel注册到selector，绑定事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 等待客户端连接
        while (true){
            if (selector.select(1000) == 0){
                System.out.println("等待1秒，无连接");
                continue;
            }

            // >0，则拿到相关事件了
            // 拿到有事件发生的key
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // 获取key值
                // 给客户端生成Channel
                if (key.isAcceptable()){
                    System.out.println("客户端连接成功，生成socketChannel");
                    // 对应了OP_ACCEPT事件
                    SocketChannel socketChannel= serverSocketChannel.accept();
                    socketChannel.configureBlocking(false); // 设置为非阻塞
                    // 将Channel注册到selector，并关注事件，并提供buffer
                    socketChannel.register(selector,SelectionKey.OP_READ,ByteBuffer.allocate(1024));
                }
                // 读取客户端数据
                if (key.isReadable()){
                    // 获取对应channel
                    SocketChannel channel = (SocketChannel)key.channel();
                    // 获取到关联的buffer
                    ByteBuffer buffer = (ByteBuffer)key.attachment();
                    // 读取数据
                    System.out.println("from 客户端：" + new String(buffer.array()));
                }
                // 手动删除key，防止重复操作
                iterator.remove();
            }

        }


    }
}
