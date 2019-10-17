package org.wls.tcpthrough.data;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.wls.tcpthrough.manager.ManagerHandler;
import org.wls.tcpthrough.model.GlobalObject;

/**
 * Created by wls on 2019/10/15.
 */
public class DataTransferServer implements Runnable{

    //    String host;
    private Integer port;
    private GlobalObject globalObject;

    public DataTransferServer(Integer port, GlobalObject globalObject) {
        this.port = port;
        this.globalObject = globalObject;
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new DataTransferHandler(globalObject));
                        }
                    }).childOption(ChannelOption.AUTO_READ, false);

            ChannelFuture cf = bootstrap.bind(port).sync();

            System.out.println("Data Server is Running");
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

//    public DataTransferHandler getDataTransferHandler() {
//        return dataTransferHandler;
//    }
}
