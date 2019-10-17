package org.wls.tcpthrough.manager;

import io.netty.channel.ChannelHandlerContext;
import org.wls.tcpthrough.model.ManagerProtocolBuf;

import java.util.concurrent.Callable;

/**
 * Created by wls on 2019/10/16.
 */
public class ProtocolValidate implements Callable<Object> {

    ManagerHandler managerHandler;
    ChannelHandlerContext ctx;
    public ProtocolValidate(ManagerHandler managerHandler, ChannelHandlerContext ctx){
        this.managerHandler = managerHandler;
        this.ctx = ctx;
    }
    @Override
    public Object call() throws Exception {
        if(!managerHandler.isRegisterOK()){
            ctx.channel().close();
        }
        return null;
    }
}
