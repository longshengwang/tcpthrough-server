package org.wls.tcpthrough.http.controller;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.http.lib.annotation.PathParam;
import org.wls.tcpthrough.http.lib.annotation.RouterMapping;
import org.wls.tcpthrough.model.GlobalObject;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthController extends BaseController{
    private static final Logger LOG = LogManager.getLogger(AuthController.class);

    private static Map<String, String> authKeyMap = new ConcurrentHashMap<>();

    public AuthController(GlobalObject globalObject, ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        super(globalObject, channelHandlerContext, request);
    }


//    @RouterMapping(api = "/tcpth/auth/{name}", method = "GET")
//    public String genNameAuthKeys(@PathParam("name") String name)
//    {
////        InetSocketAddress insocket = (InetSocketAddress) channelHandlerContext.channel()
////                .remoteAddress();
//
//        UUID uuid = UUID.randomUUID();
//        authKeyMap.put(uuid.toString(), name);
//        return "<ip:port>:" + "/auth/validate/" + uuid.toString();
//    }

    @RouterMapping(api = "/tcpth/auth/addtrustip/{clientName}/{ip}", method = "GET")
    public boolean addTrustIp(@PathParam("ip") String ip, @PathParam("clientName") String clientName)
    {
        Channel mgmtChannel = globalObject.getManageChannelByName(clientName);
        if(mgmtChannel == null){
            return false;
        } else {
            globalObject.getChannelOuterServer(mgmtChannel).forEach(outerServer -> outerServer.getTrustIpModel().addIp(ip));
            return true;
        }
    }

    @RouterMapping(api = "/tcpth/auth/rmtrustip/{clientName}/{ip}", method = "GET")
    public boolean rmTrustIp(@PathParam("ip") String ip, @PathParam("clientName") String clientName)
    {
        Channel mgmtChannel = globalObject.getManageChannelByName(clientName);
        if(mgmtChannel == null){
            return false;
        } else {
            globalObject.getChannelOuterServer(mgmtChannel).forEach(outerServer -> outerServer.getTrustIpModel().deleteIps(ip));
            return false;
        }
    }

//    /**
//     * If use nginx to proxy the validate api. 'X-Real-IP' is used to save the real remote ip in the headers
//     *
//     */
//    @RouterMapping(api = "/auth/validate/{uuid}", method = "GET")
//    public String validate(@PathParam("uuid") String uuid) throws Exception {
//        if(authKeyMap.get(uuid) != null){
//            String name = authKeyMap.get(uuid);
//            authKeyMap.remove(uuid);
//
//            if(request.headers().get("X-Real-IP") != null ){
////                proxyConfig.addTrustIps(name, request.headers().get("X-Real-IP"));
//            } else {
//                InetSocketAddress insocket = (InetSocketAddress) channelHandlerContext.channel()
//                        .remoteAddress();
//                LOG.error(insocket.getAddress());
////                proxyConfig.addTrustIps(name, insocket.getAddress().toString().substring(1));
//            }
//
//            return "OK";
//        } else {
//            throw new Exception("KEY_NOT_VALID");
//        }
//
//    }
}
