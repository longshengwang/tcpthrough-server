package org.wls.tcpthrough.http.lib;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.http.lib.entity.Router;
import org.wls.tcpthrough.model.GlobalObject;


import java.util.Map;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Created by shirukai on 2018/9/30
 * 基于netty 实现httpSever
 */
public class HttpServer {
    private HttpServerConfig config;
    private int port;
    private Map<String, Router> routers;
    private GlobalObject globalObject;

    NioEventLoopGroup group;

    public HttpServer(GlobalObject globalObject){
        this.globalObject = globalObject;
    }

    public final Logger log = LogManager.getLogger(this.getClass());

    public HttpServerConfig builder() {
        config = new HttpServerConfig(this);
        return config;
    }

    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        group = new NioEventLoopGroup();
        try {
            bootstrap
                    .group(group)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast("decoder", new HttpRequestDecoder())
                                    .addLast("encoder", new HttpResponseEncoder())
                                    .addLast("aggregator", new HttpObjectAggregator(512 * 1024))
                                    .addLast("handler", new HttpServerHandler(routers, globalObject));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
            ChannelFuture future = bootstrap.bind(port);
            future.sync();
            log.info("Start app server at port:{}", port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRouters(Map<String, Router> routers) {
        this.routers = routers;
    }

    public void stopServer(){
        if(group != null){
            group.shutdownGracefully();
        }
    }
}
