package org.wls.tcpthrough.manager;

import io.netty.channel.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.model.GlobalObject;
import org.wls.tcpthrough.model.ManagerProtocolBuf.ManagerResponse;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;
import org.wls.tcpthrough.model.ResponseType;
import org.wls.tcpthrough.outer.OuterServer;

import java.util.concurrent.TimeUnit;

public class ManagerHandler extends SimpleChannelInboundHandler<RegisterProtocol> {
    private static final Logger LOGGER = LogManager.getLogger(ManagerHandler.class);


    public ManagerHandler(GlobalObject globalObject){
        this.globalObject = globalObject;
    }

    private GlobalObject globalObject;
    private RegisterProtocol registerProtocol = null;


//    /*
//        [读]
//        注册协议   protocol code  1个字
//                  length         4个字节
//                  内容            由length决定
//
//                  protocol—code
//        登陆      1   + length
//        ------------------------------------------------------
//
//        [写]
//        回复协议   protocol code  1个字节
//                  channel id     16个字节
//
//                  protocol code
//        注册返回   1
//        有访问     2  +  channel id
//     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RegisterProtocol msg) throws Exception {
        LOGGER.info("Server channelRead0");
        registerProtocol = RegisterProtocol.newBuilder(msg).build();

        OuterServer outerServer = new OuterServer(registerProtocol, ctx.channel(), globalObject);
        new Thread(outerServer).start();
        globalObject.putChannelOuterServer(ctx.channel(), outerServer);

        ManagerResponse response = ManagerResponse
                .newBuilder()
                .setType(ResponseType.REGISTER_RESPONSE.get())
//                .setChannel()
                .build();

        ctx.channel().writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()){
                    System.out.println("===》Server回复注册成功！");
                } else {
                    System.out.println("===> server回复注册失败");
                }
            }
        });
        LOGGER.info("Server channelRead0 finish");

//        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Server channel active");
//        super.channelActive(ctx);
//        ManagerProtocolBuf.ManagerResponse response = ManagerProtocolBuf.ManagerResponse.newBuilder()
//                .setUserName("zhihao.miao").setAge(27).setPassword("123456").build();
//        ctx.channel().writeAndFlush(user);

        ctx.executor().schedule(new ProtocolValidate(this, ctx), 1, TimeUnit.SECONDS);
//        ctx.channel().read()
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("Server exceptionCaught");
        cause.printStackTrace();
        OuterServer outerServer = globalObject.getChannelOuterServer(ctx.channel());
        if (outerServer != null) {
            outerServer.channelFuture.channel().close();
            globalObject.deleteChannelOuterServer(ctx.channel());
        }
        ctx.channel().close();
    }


    public boolean isRegisterOK() {
        return registerProtocol != null;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Server channelInactive");
        OuterServer outerServer = globalObject.getChannelOuterServer(ctx.channel());
        if (outerServer != null) {
            outerServer.channelFuture.channel().close();
            globalObject.deleteChannelOuterServer(ctx.channel());
        }
        ctx.channel().close();
    }
}

