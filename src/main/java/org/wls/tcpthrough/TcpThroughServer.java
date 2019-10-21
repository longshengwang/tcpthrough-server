package org.wls.tcpthrough;

import org.apache.commons.cli.*;
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
    public static final int HTTP_DEFAULT_PORT = 8080;
    public static final int DATA_SERVER_DEFAULT_PORT = 9009;
    public static final int MANAGE_DEFAULT_PORT = 9000;
    public Integer managerServerPort;
    public Integer dataServerPort;
    public Integer httpPort;
    public String  securityKey;
    public ManagerServer managerServer;
    public DataTransferServer dataTransferServer;

    public TcpThroughServer(Integer managerServerPort, Integer dataServerPort, Integer httpPort, String securityKey){
        this.dataServerPort = dataServerPort;
        this.managerServerPort = managerServerPort;
        this.httpPort = httpPort;
        this.securityKey = securityKey;
    }

    public void run(){
        try{
            GlobalObject globalObject = new GlobalObject();
            globalObject.setSecurityKey(this.securityKey);

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

    public static void main(String[] args) {
        LOG.info("Tcp through server is starting");
        if (args.length != 0) {
            Options options = new Options();
            options.addRequiredOption("m", "manage-port", true, "manage server port");
            options.addRequiredOption("d", "data-port", true, "data server port");
            options.addRequiredOption("n", "http-port", true, "http server port");
            options.addOption("s", "security-key", true, "server security key (For client password)");
            options.addOption(Option.builder("h")
                    .longOpt("help")
                    .desc("show this help message and exit program")
                    .build());

            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();

            CommandLine cmd = null;
            try {
                cmd = parser.parse(options, args);
            } catch (ParseException e) {
                return;
            }
            if (cmd.hasOption('h') || cmd.hasOption("--help")) {
                formatter.printHelp("tt", options, false);
                return;
            }

            String managePort = cmd.getOptionValue("m");
            String dataPort = cmd.getOptionValue("d");
            String httpPort = cmd.getOptionValue("n");
            String security = cmd.getOptionValue("s");
            System.out.println("security:" + security);
            try{
                int mgmtPortNum = Integer.parseInt(managePort);
                int dataPortNum = Integer.parseInt(dataPort);
                int httpPortNum = Integer.parseInt(httpPort);
                TcpThroughServer tcpThroughServer = new TcpThroughServer(mgmtPortNum, dataPortNum, httpPortNum, security);
                tcpThroughServer.run();
            }catch (Exception e){
                System.out.println("The input is not valid");
            }



        } else {
            TcpThroughServer tcpThroughServer = new TcpThroughServer(MANAGE_DEFAULT_PORT, DATA_SERVER_DEFAULT_PORT, HTTP_DEFAULT_PORT, null);
            tcpThroughServer.run();
        }




    }
}
