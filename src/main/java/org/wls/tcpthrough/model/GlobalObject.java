package org.wls.tcpthrough.model;

import io.netty.channel.Channel;
import org.wls.tcpthrough.outer.OuterServer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalObject {

    //the map is used to find ConnectModel for channel id
    private Map<String, ConnectModel> outConnectionMap;

    private Map<Channel, OuterServer> channelMapping;


    public GlobalObject() {
        outConnectionMap = new ConcurrentHashMap<>();
        channelMapping = new ConcurrentHashMap<>();
    }

    public boolean isOuterChannelUUIDExist(String uuid) {
        if (outConnectionMap.containsKey(uuid)) {
            return true;
        } else {
            return false;
        }
    }

    public void putOuterConnection(String uuid, ConnectModel connectModel) {
        outConnectionMap.put(uuid, connectModel);
    }

    public void deleteOuterConnection(String uuid) {
        outConnectionMap.remove(uuid);
    }

    public ConnectModel getOuterConnection(String uuid) {
        return outConnectionMap.get(uuid);
    }


    public void putChannelOuterServer(Channel channel, OuterServer outerServer) {
        channelMapping.put(channel, outerServer);
    }

    public OuterServer getChannelOuterServer(Channel channel) {
        return channelMapping.get(channel);
    }

    public void deleteChannelOuterServer(Channel channel) {
        channelMapping.remove(channel);
    }

}
