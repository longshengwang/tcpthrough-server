package org.wls.tcpthrough.manager;

import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.model.ManagerProtocolBuf;

import java.util.concurrent.Callable;

/**
 * Created by wls on 2019/10/16.
 */
public class ProtocolValidate implements Callable<Object> {
    private static final Logger LOG = LogManager.getLogger(ProtocolValidate.class);
    ManagerHandler managerHandler;
    ChannelHandlerContext ctx;
    public ProtocolValidate(ManagerHandler managerHandler, ChannelHandlerContext ctx){
        this.managerHandler = managerHandler;
        this.ctx = ctx;
    }
    @Override
    public Object call() throws Exception {
        if(!managerHandler.isRegisterOK()){
            LOG.warn("Client " + ctx.channel().remoteAddress() + " cannot transfer a full register protocol to server in the specified time ("+ ManagerHandler.REGISTER_TIMEOUT +" second)! ");
            ctx.channel().close();
        }
        return null;
    }
}
