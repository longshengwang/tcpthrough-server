package org.wls.tcpthrough.manager;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.Tools;
import org.wls.tcpthrough.model.GlobalObject;
import org.wls.tcpthrough.model.ManagerProtocolBuf.ManagerResponse;
import org.wls.tcpthrough.model.ManagerProtocolBuf.RegisterProtocol;
import org.wls.tcpthrough.model.ResponseType;
import org.wls.tcpthrough.outer.OuterServer;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ManagerHandler extends SimpleChannelInboundHandler<RegisterProtocol> {
    private static final Logger LOG = LogManager.getLogger(ManagerHandler.class);

    public static final int REGISTER_TIMEOUT = 1;


    public ManagerHandler(GlobalObject globalObject) {
        this.globalObject = globalObject;
    }

    private GlobalObject globalObject;

    // use to save register info
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
        LOG.info("Manage Client send register protocol to server." + Tools.protocolToString(msg));
        OuterServer outerServer;
        // if is null, mean the first register
        if (registerProtocol == null) {
            registerProtocol = RegisterProtocol.newBuilder(msg).build();

            //valid the protocol
            if (!Tools.isProtocolValid(registerProtocol)) {
                LOG.warn("Register protocol is not valid!");
                ManagerResponse response = ManagerResponse
                        .newBuilder()
                        .setType(ResponseType.REGISTER_FAIL.get())
                        .setValue("REGISTER_PROTOCOL_NOT_VALID")
                        .build();
                ctx.channel().writeAndFlush(response);
                registerProtocol = null;
                return;
            }

            //if server has security key, check the client password is correct or not.
            if(globalObject.getSecurityKey() != null && !globalObject.getSecurityKey().equals(registerProtocol.getPassword())){
                LOG.warn("Register protocol password is not correct!");
                ManagerResponse response = ManagerResponse
                        .newBuilder()
                        .setType(ResponseType.REGISTER_FAIL.get())
                        .setValue("REGISTER_PROTOCOL_PASSWORD_NOT_CORRECT")
                        .build();
                ctx.channel().writeAndFlush(response);
                registerProtocol = null;
                return;
            }

            //check if the name is registered
            if (globalObject.registerName(registerProtocol.getName(), ctx.channel())) {
                outerServer = new OuterServer(registerProtocol, ctx.channel(), globalObject);
            } else {
                LOG.warn("Name : " + registerProtocol.getName() + " has been used by other");
                ManagerResponse response = ManagerResponse
                        .newBuilder()
                        .setType(ResponseType.REGISTER_FAIL.get())
                        .setValue("NAME_HAS_BEEN_USED:" + registerProtocol.getName())
                        .build();
                ctx.channel().writeAndFlush(response);
                registerProtocol = null;
                return;
            }
        } else {
            // not the first register
            RegisterProtocol newestRp = RegisterProtocol.newBuilder(msg).build();
            outerServer = new OuterServer(newestRp, ctx.channel(), globalObject);
        }
        new Thread(outerServer).start();
        globalObject.putChannelOuterServer(ctx.channel(), outerServer);

        ManagerResponse response = ManagerResponse
                .newBuilder()
                .setType(ResponseType.REGISTER_RESPONSE.get())
                .build();

        ctx.channel().writeAndFlush(response).addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                LOG.debug("Reply register success");
            } else {
                LOG.error("Reply register fail");
            }
        });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("One Channel is try to connect manager server");
        ctx.executor().schedule(new ProtocolValidate(this, ctx), REGISTER_TIMEOUT, TimeUnit.SECONDS);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("ManagerHander exceptionCaught:", cause);

        List<OuterServer> outerServerList = globalObject.getChannelOuterServer(ctx.channel());
        if (outerServerList != null) {
            outerServerList.forEach(outerServer -> outerServer.channelFuture.channel().close());
            globalObject.deleteChannelOuterServerList(ctx.channel());
        }
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("One manage channel is Inactive. Remote channel is " + ctx.channel().remoteAddress());
        List<OuterServer> outerServerList = globalObject.getChannelOuterServer(ctx.channel());
        if (outerServerList != null) {
            outerServerList.forEach(outerServer -> {
                LOG.info("Outer Server is closing : " + outerServer.getDescription());
                outerServer.channelFuture.channel().close();
            });
            globalObject.deleteChannelOuterServerList(ctx.channel());
        }
        ctx.channel().close();

        if (registerProtocol != null) {
            globalObject.removeName(registerProtocol.getName());
        }
    }

    public boolean isRegisterOK() {
        return registerProtocol != null;
    }
}

