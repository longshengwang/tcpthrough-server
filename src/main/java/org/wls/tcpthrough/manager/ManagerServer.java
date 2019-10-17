package org.wls.tcpthrough.manager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.model.GlobalObject;
import org.wls.tcpthrough.model.ManagerProtocolBuf;

import java.util.concurrent.TimeUnit;

/**
 * Created by wls on 2019/10/15.
 */
public class ManagerServer implements Runnable{
    private static final Logger LOGGER = LogManager.getLogger(ManagerServer.class);
    private Integer port;
    private GlobalObject globalObject;

    public ManagerServer(Integer port, GlobalObject globalObject){
        this.port = port;
        this.globalObject = globalObject;
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(10);
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {

                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //解码器，通过Google Protocol Buffers序列化框架动态的切割接收到的ByteBuf
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            //服务器端接收的是客户端RequestUser对象，所以这边将接收对象进行解码生产实列
                            pipeline.addLast(new ProtobufDecoder(ManagerProtocolBuf.RegisterProtocol.getDefaultInstance()));
                            //Google Protocol Buffers编码器
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            //Google Protocol Buffers编码器
                            pipeline.addLast(new ProtobufEncoder());

                            pipeline.addLast(new ManagerHandler(globalObject));

                        }
                    });

            ChannelFuture cf = bootstrap.bind(port).sync();

            System.out.println("Re server run ssss\n");
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

//    public static void main(String[] args) throws InterruptedException {
//        ManagerServer server = new ManagerServer(1099);
//        new Thread(server).start();
//        TimeUnit.SECONDS.sleep(100000);
//    }
}
