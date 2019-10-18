package org.wls.tcpthrough;

import org.wls.tcpthrough.model.ManagerProtocolBuf;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * Created by wls on 2019/10/16.
 */
public class Tools {
    public static String getMD5(String str) throws Exception{
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            throw e;
        }
    }


    public static String uuid(){
        return String.valueOf(UUID.randomUUID());
    }


    public static String protocolToString(ManagerProtocolBuf.RegisterProtocol registerProtocol){
        StringBuilder builder = new StringBuilder();
        builder.append("\nRegisterProtocol:\n")
                .append("    RemoteProxyPort : "  + registerProtocol.getRemoteProxyPort() + "\n")
                .append("    Name : "  + registerProtocol.getName() + "\n")
                .append("    LocalHost : " + registerProtocol.getLocalHost() + "\n")
                .append("    LocalPort : " + registerProtocol.getLocalPort() + "\n")
                .append("    IsRemoteManage : " + registerProtocol.getIsRemoteManage() + "\n")
                .append("    IsAuth : " + registerProtocol.getIsAuth() + "\n")
                .append("    IsEncrypt : " + registerProtocol.getIsEncrypt());
        return builder.toString();

    }

//    public static void main(String[] args) {
//    }

}
