package org.wls.tcpthrough.http.controller;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.wls.tcpthrough.model.GlobalObject;

public class BaseController {
    protected GlobalObject globalObject;
    protected ChannelHandlerContext channelHandlerContext;
    protected FullHttpRequest request;

    public BaseController(GlobalObject globalObject, ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        this.globalObject = globalObject;
        this.channelHandlerContext = channelHandlerContext;
        this.request = request;
    }

}
