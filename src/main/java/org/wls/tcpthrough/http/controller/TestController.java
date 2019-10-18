package org.wls.tcpthrough.http.controller;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.wls.tcpthrough.http.lib.annotation.JsonParam;
import org.wls.tcpthrough.http.lib.annotation.PathParam;
import org.wls.tcpthrough.http.lib.annotation.RequestParam;
import org.wls.tcpthrough.http.lib.annotation.RouterMapping;
import org.wls.tcpthrough.model.GlobalObject;

import java.util.List;
import java.util.Map;


public class TestController extends BaseController {
    GlobalObject proxyConfig;

    public TestController(GlobalObject proxyConfig, ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        super(proxyConfig, channelHandlerContext, request);
    }

    /**
     * 测试GET请求
     *
     * @param name name
     * @param id   id
     * @return map
     */
    @RouterMapping(api = "/api/v1/test/get/{id}", method = "GET")
    public List<Map<String, String>> testGet(
            @RequestParam("name") String name,
            @PathParam("id") String id
    ) {
//        System.out.println(this.proxyConfig.id);
//        this.proxyConfig.id = "19";
//        Map<String, Object> map = new HashMap<>(16);
//        map.put("name", name);
//        map.put("id", id);
//        return map;
//        List<Map<String, String>> list = new ArrayList<>();
//        list.add(new ConnModel().toMap());
//        list.add(new ConnModel().toMap());
//        list.add(new ConnModel().toMap());
//        list.add(new ConnModel().toMap());
//        return list;
        return null;
    }

    /**
     * 测试POST请求
     *
     * @param json json
     * @return json
     */
    @RouterMapping(api = "/api/v1/test/post", method = "POST")
    public JSONObject testPost(
            @JsonParam("json") JSONObject json
    ) {
        return json;
    }
}
