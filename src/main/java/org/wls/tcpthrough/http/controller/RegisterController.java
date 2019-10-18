package org.wls.tcpthrough.http.controller;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.wls.tcpthrough.http.lib.annotation.RouterMapping;
import org.wls.tcpthrough.model.ConnectModel;
import org.wls.tcpthrough.model.GlobalObject;
import org.wls.tcpthrough.model.ManagerProtocolBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wls on 2019/10/18.
 */
public class RegisterController extends BaseController {
    public RegisterController(GlobalObject globalObject, ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        super(globalObject, channelHandlerContext, request);
    }

    @RouterMapping(api = "/register/list", method = "GET")
    public List<Map<String, String>> statisticList() {
//        List<Map<String, String>> res = new ArrayList<>();
//        List<ConnectModel> list = globalObject.getConnectList();
//        list.forEach(connectModel -> {
//            Map<String ,String> map = new HashMap<>();
//            connectModel.getChannelId();
//            ManagerProtocolBuf.RegisterProtocol registerProtocol = connectModel.getOuterHandler().getRegisterProtocol();
//            map.put("name", registerProtocol.getName());
//            map.put("channel_id", connectModel.getChannelId());
//            map.put("out_address", connectModel.getOutChannel().remoteAddress().toString());
//            res.add(map);
//        });
//        return res;
        return null;
    }


}
