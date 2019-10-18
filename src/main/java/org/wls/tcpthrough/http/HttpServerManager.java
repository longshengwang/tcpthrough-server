package org.wls.tcpthrough.http;


import org.wls.tcpthrough.http.controller.AuthController;
import org.wls.tcpthrough.http.controller.StatisticController;
import org.wls.tcpthrough.http.lib.*;
import org.wls.tcpthrough.model.GlobalObject;

/**
 * Created by wls on 2019/8/8.
 */
public class HttpServerManager implements Runnable {
    Integer port;
    HttpServer server;
    GlobalObject globalObject;
    public HttpServerManager(Integer port, GlobalObject p){
        this.port = port;
        this.globalObject = p;
    }

    @Override
    public void run() {
        server = new HttpServer(this.globalObject);
        server.builder()
                .setPort(port)
                .setControllers(StatisticController.class, AuthController.class)
                .create().start();
    }

    public void close(){
        this.server.stopServer();
    }


}
