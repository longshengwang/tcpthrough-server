package org.wls.tcpthrough.model;

import io.netty.channel.Channel;
import org.wls.tcpthrough.outer.OuterHandler;

/**
 * Created by wls on 2019/10/17.
 */
public class ConnectModel {
    private String channelId;
    private OuterHandler outerHandler;
    private Channel outChannel;

    public ConnectModel(String channelId, OuterHandler outerHandler, Channel outChannel){
        this.outerHandler = outerHandler;
        this.channelId = channelId;
        this.outChannel = outChannel;
    }
//    ManagerProtocolBuf.RegisterProtocol registerProtocol;


    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public OuterHandler getOuterHandler() {
        return outerHandler;
    }

    public void setOuterHandler(OuterHandler outerHandler) {
        this.outerHandler = outerHandler;
    }

    public Channel getOutChannel() {
        return outChannel;
    }

    public void setOutChannel(Channel outChannel) {
        this.outChannel = outChannel;
    }
}
