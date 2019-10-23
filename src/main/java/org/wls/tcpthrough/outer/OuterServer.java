package org.wls.tcpthrough.outer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.model.GlobalObject;
import org.wls.tcpthrough.model.ManagerProtocolBuf;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;
import org.wls.tcpthrough.model.ResponseType;
import org.wls.tcpthrough.model.TrustIpsModel;

/**
 * Created by wls on 2019/10/15.
 */
public class OuterServer implements Runnable{

    private static final Logger LOG = LogManager.getLogger(OuterServer.class);

    private RegisterProtocol registerProtocol;
    public ChannelFuture channelFuture;
    private Channel managerChannel;
    private GlobalObject globalObject;
    public GlobalTrafficShapingHandler gtsh;

    // The read limit of traffic shape does not work well when the auto read is false。
    // So we need dataGtsh for data channel
    // the read limit = gtsh's write limit, the write limit = gtsh's read limit
    public GlobalTrafficShapingHandler dataGtsh;
    private TrustIpsModel trustIpModel;


    public OuterServer(RegisterProtocol registerProtocol, Channel managerChannel, GlobalObject globalObject) {
        this.registerProtocol = registerProtocol;
        this.globalObject = globalObject;
        this.managerChannel = managerChannel;
        this.trustIpModel = new TrustIpsModel();

    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            // read limit not work well, so we should add the gtsh to the data transfer。
            gtsh = new GlobalTrafficShapingHandler(workGroup, 0, 0, 1000);
            dataGtsh = new GlobalTrafficShapingHandler(workGroup, 0, 0, 1000);

            // Read/write limit example
            /*
            gtsh = new GlobalTrafficShapingHandler(workGroup, 1024*1024, 2*1024*1024, 1000);
            dataGtsh = new GlobalTrafficShapingHandler(workGroup, 2*1024*1024, 1024*1024, 1000);
            */

            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("gtsh", gtsh);
//                            socketChannel.pipeline().addLast(new ChannelTrafficShapingHandler(1024 * 1024,1024 * 1024));

                            socketChannel.pipeline().addLast( new OuterHandler(managerChannel, globalObject, registerProtocol, trustIpModel));

                        }
                    }).childOption(ChannelOption.AUTO_READ, false);

            channelFuture = bootstrap.bind(registerProtocol.getRemoteProxyPort()).sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        LOG.info("Proxy Server run successfully. Proxy Port:" + registerProtocol.getRemoteProxyPort());
                    } else {
                        LOG.error("Proxy Server run failed. Proxy Port:" + registerProtocol.getRemoteProxyPort());
                        ManagerProtocolBuf.ManagerResponse response = ManagerProtocolBuf.ManagerResponse
                                .newBuilder()
                                .setType(ResponseType.REGISTER_FAIL.get())
                                .setValue("PORT_IS_ALREADY_USED:" + registerProtocol.getRemoteProxyPort())
                                .build();
                        managerChannel.writeAndFlush(response);
                    }
                }
            });


            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            LOG.error("", e);
            LOG.error("Proxy Server run failed. Proxy Port:" + registerProtocol.getRemoteProxyPort());
            ManagerProtocolBuf.ManagerResponse response = ManagerProtocolBuf.ManagerResponse
                    .newBuilder()
                    .setType(ResponseType.REGISTER_FAIL.get())
                    .setValue("PORT_IS_ALREADY_USED:" + registerProtocol.getRemoteProxyPort())
                    .build();
            managerChannel.writeAndFlush(response);
        } finally {
            globalObject.deleteChannelSingleOuterServer(managerChannel, this);
            /*
            one name has multiple out server.
            So only remove the channel when the manage channel is inactive
            */
            // globalObject.removeName(registerProtocol.getName());

            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            LOG.info("The proxy server is closed. Proxy port: " + registerProtocol.getRemoteProxyPort());
        }
    }

    public RegisterProtocol getRegisterProtocol() {
        return registerProtocol;
    }

    public String getDescription(){
        StringBuilder builder = new StringBuilder();
        builder.append("\n[OuterServer]\n")
                .append("    remote proxy port : "  + registerProtocol.getRemoteProxyPort() + "\n")
                .append("    client name : "  + registerProtocol.getName() + "\n")
                .append("    client host : " + registerProtocol.getLocalHost() + "\n")
                .append("    client port : " + registerProtocol.getLocalPort() + "\n")
                .append("    server manage : " + registerProtocol.getIsRemoteManage() + "\n")
                .append("    is auth : " + registerProtocol.getIsAuth())
                .append("\n");
        return builder.toString();
    }

    public Channel getManagerChannel() {
        return managerChannel;
    }

    public TrustIpsModel getTrustIpModel() {
        return trustIpModel;
    }
}
