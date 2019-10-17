package org.wls.tcpthrough.model;

/**
 * Created by wls on 2019/10/16.
 */
public class ManagerProtocolValidate {
    public static boolean execute(ManagerProtocolBuf.RegisterProtocol registerProtocol){
        if(registerProtocol.getName() == null || registerProtocol.getName().equals("")){
            return false;
        }
//        if(registerProtocol.)

        return false;
    }
}
