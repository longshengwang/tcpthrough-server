package org.wls.tcpthrough.outer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.wls.tcpthrough.model.GlobalObject;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;

/**
 * Created by wls on 2019/10/15.
 */
public class OuterServer implements Runnable{

    RegisterProtocol registerProtocol;
    public ChannelFuture channelFuture;
    private Channel managerChannel;
    private GlobalObject globalObject;

    public OuterServer(RegisterProtocol registerProtocol, Channel managerChannel, GlobalObject globalObject) {
        this.registerProtocol = registerProtocol;
        this.globalObject = globalObject;
        this.managerChannel = managerChannel;
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
                            socketChannel.pipeline().addLast( new OuterHandler(managerChannel, globalObject));
                        }
                    }).childOption(ChannelOption.AUTO_READ, false);

            channelFuture = bootstrap.bind(registerProtocol.getRemoteProxyPort()).sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        System.out.println("代理端口future is success");
                    } else {
                        System.out.println("代理端口future is not success");
                    }
                }
            });

            System.out.println("代理端口成功启动。端口：" + registerProtocol.getRemoteProxyPort());
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("代理端口已经关闭" + registerProtocol.getRemoteProxyPort());
        }
    }

}
