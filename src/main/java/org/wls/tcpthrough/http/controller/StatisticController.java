package org.wls.tcpthrough.http.controller;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.traffic.TrafficCounter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.Tools;
import org.wls.tcpthrough.http.lib.annotation.JsonParam;
import org.wls.tcpthrough.http.lib.annotation.RouterMapping;
import org.wls.tcpthrough.model.ConnectModel;
import org.wls.tcpthrough.model.GlobalObject;
import org.wls.tcpthrough.model.ManagerProtocolBuf;
import org.wls.tcpthrough.model.ResponseType;
import org.wls.tcpthrough.outer.OuterServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class StatisticController extends BaseController {
    public static final Logger LOG = LogManager.getLogger(StatisticController.class);

    public StatisticController(GlobalObject globalObject, ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        super(globalObject, channelHandlerContext, request);
    }

    /**
     * 测试GET请求
     *
     * @return list
     */
    @RouterMapping(api = "/tcpth/list", method = "GET")
    public List<Map<String, String>> statisticList() {
        List<Map<String, String>> res = new ArrayList<>();
        List<ConnectModel> list = globalObject.getConnectList();

        for (List<OuterServer> outerServers : globalObject.getAllOuterServers()) {
            for (OuterServer outerServer : outerServers) {
                TrafficCounter trafficCounter = outerServer.gtsh.trafficCounter();
                Map<String, String> map = new HashMap<>();
                ManagerProtocolBuf.RegisterProtocol registerProtocol = outerServer.getRegisterProtocol();
                map.put("name", registerProtocol.getName());
                map.put("proxy_port", registerProtocol.getRemoteProxyPort() + "");
                map.put("local", registerProtocol.getLocalHost() + ":" + registerProtocol.getLocalPort());
                map.put("is_remote_manage", registerProtocol.getIsRemoteManage() + "");
                map.put("read_speed", (trafficCounter.lastReadThroughput() >> 10) + "KB/s");
                map.put("write_speed", (trafficCounter.lastWriteThroughput() >> 10) + "KB/s");
                map.put("read_bytes", (trafficCounter.cumulativeReadBytes() >> 10) + "KB");
                map.put("write_bytes", (trafficCounter.cumulativeWrittenBytes() >> 10) + "KB");

                AtomicInteger connection_count = new AtomicInteger();
                list.forEach(connectModel -> {
                    if (connectModel.getOuterHandler().getRegisterProtocol().getName().equals(registerProtocol.getName())
                            && connectModel.getOuterHandler().getRegisterProtocol().getRemoteProxyPort() == registerProtocol.getRemoteProxyPort()) {
                        connection_count.addAndGet(1);
                    }
                });
                map.put("out_connection_count", connection_count.get() + "");
                res.add(map);
            }
        }
        return res;
    }


    /**
     * 测试POST请求
     * {
     * name: xxxx,
     * proxy_port: 080,
     * local_host: localhost,
     * local_port: 22
     * }
     *
     * @param json json
     * @return boolean
     */
    @RouterMapping(api = "/tcpth/register/add", method = "POST")
    public boolean addRegister(@JsonParam("json") JSONObject json) {
        LOG.info("[ HTTP Server Request] Add register. \n" + json.toJSONString());
        String name = json.getString("name");
        String proxy_port = json.getString("proxy_port");
        String local_host = json.getString("local_host");
        String local_port = json.getString("local_port");
        if (proxy_port == null || local_host == null || local_port == null || name == null) {
            LOG.warn("Some data is empty");
            return false;
        }
        try {
            Integer.parseInt(proxy_port);
            Integer.parseInt(local_port);
        } catch (Exception e) {
            LOG.warn("Data Format is not correct");
            return false;
        }


        List<Integer> openedPorts = new ArrayList<>();
        for (List<OuterServer> outerServers : globalObject.getAllOuterServers()) {
            outerServers.forEach(outerServer -> openedPorts.add(outerServer.getRegisterProtocol().getRemoteProxyPort()));
        }
        if (openedPorts.indexOf(Integer.parseInt(proxy_port)) != -1) {
            LOG.error("The proxy port( " + proxy_port + " ) has been used");
            return false;
        }

        String newRegisterStr = proxy_port + "," + local_host + "," + local_port;

        ManagerProtocolBuf.ManagerResponse response = ManagerProtocolBuf.ManagerResponse.newBuilder()
                .setType(ResponseType.NEW_CONF_RESPONSE.get())
                .setValue(newRegisterStr)
                .setValueMd5(Tools.getMD5(newRegisterStr))
                .build();

        boolean hasSend = false;
        if (globalObject.getManageChannelByName(name) != null) {
            globalObject.getManageChannelByName(name).writeAndFlush(response);
            hasSend = true;
        } else {
            LOG.warn("The channel of the name( " + name + " ) cannot be found!");
        }
        return hasSend;
    }

    @RouterMapping(api = "/tcpth/register/delete", method = "POST")
    public boolean deleteRegister(@JsonParam("json") JSONObject json) {
        LOG.info("[ HTTP Server Request] Remove register. \n" + json.toJSONString());
        String name = (String) json.get("name");
        String proxy_port = (String) json.get("proxy_port");
        if (proxy_port == null || name == null) {
            LOG.warn("Some data is empty");
            return false;
        }
        try {
            Integer.parseInt(proxy_port);
        } catch (Exception e) {
            LOG.warn("Data Format is not correct");
            return false;
        }
        String newRegisterStr = proxy_port;
        ManagerProtocolBuf.ManagerResponse response = ManagerProtocolBuf.ManagerResponse.newBuilder()
                .setType(ResponseType.DELETE_CONF_RESPONSE.get())
                .setValue(newRegisterStr)
                .setValueMd5(Tools.getMD5(newRegisterStr))
                .build();

        try {
            if (globalObject.getManageChannelByName(name) != null) {
                List<OuterServer> outerServers = globalObject.getChannelOuterServer(globalObject.getManageChannelByName(name));
                OuterServer outerServer = outerServers.stream()
                        .filter(o -> o.getRegisterProtocol().getRemoteProxyPort() == Integer.parseInt(proxy_port))
                        .findFirst()
                        .orElse(null);
                if(outerServer != null){
                    if(!outerServer.getRegisterProtocol().getIsRemoteManage()){
                        LOG.warn("The client (name : " + name +") is not allow to remote manage!");
                        return false;
                    } else {
                        globalObject.getManageChannelByName(name).writeAndFlush(response);
                        outerServer.channelFuture.channel().close();
                    }
                } else {
                    LOG.warn("The outserver is not exist!");
                }
            } else {
                LOG.warn("The client name(" + name+") is not exist!");
            }

        } catch (Exception e) {
            LOG.error("", e);
            return false;
        }
        return true;
    }
}
