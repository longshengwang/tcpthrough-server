package org.wls.tcpthrough;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wls.tcpthrough.data.DataTransferServer;
import org.wls.tcpthrough.http.HttpServerManager;
import org.wls.tcpthrough.manager.ManagerHandler;
import org.wls.tcpthrough.manager.ManagerServer;
import org.wls.tcpthrough.model.GlobalObject;

/**
 * Created by wls on 2019/10/15.
 */
public class TcpThroughServer {

    public static final Logger LOG = LogManager.getLogger(TcpThroughServer.class);
    public Integer managerServerPort;
    public Integer dataServerPort;
    public Integer httpPort;
    public ManagerServer managerServer;
    public DataTransferServer dataTransferServer;

    public TcpThroughServer(Integer managerServerPort, Integer dataServerPort, Integer httpPort){
        this.dataServerPort = dataServerPort;
        this.managerServerPort = managerServerPort;
        this.httpPort = httpPort;
    }

    public void run(){
        try{
            GlobalObject globalObject = new GlobalObject();
            ManagerServer managerServer = new ManagerServer(this.managerServerPort, globalObject);
            Thread m_t = new Thread(managerServer);
            m_t.start();

            DataTransferServer dataTransferServer = new DataTransferServer(this.dataServerPort, globalObject);
            Thread d_t = new Thread(dataTransferServer);
            d_t.start();

            HttpServerManager httpServerManager = new HttpServerManager(httpPort, globalObject);
            Thread h_t = new Thread(httpServerManager);
            h_t.start();



            m_t.join();
            d_t.join();
            h_t.join();
        } catch (Exception e){

        } finally {

        }
    }

    public void startDataTransferServer(){

    }

    public void startManagerServer(){

    }


    public static void main(String[] args) {
        LOG.info("Tcp through server is starting");
        TcpThroughServer tcpThroughServer = new TcpThroughServer(9000, 9009, 8080);
        tcpThroughServer.run();
    }
}
