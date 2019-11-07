package org.wls.tcpthrough;

import org.wls.tcpthrough.model.ManagerProtocolBuf;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.*;

/**
 * Created by wls on 2019/10/16.
 */
public class Tools {
    public static String getMD5(String str){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
//            throw e;
            return null;
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


    public static boolean isProtocolValid(ManagerProtocolBuf.RegisterProtocol registerProtocol){

        if( registerProtocol.getName() != null && !registerProtocol.getName().equals("")
                && registerProtocol.getRemoteHost()!= null && !registerProtocol.getRemoteHost().equals("")
                && registerProtocol.getLocalHost()!= null && !registerProtocol.getLocalHost().equals("")
                && registerProtocol.getRemoteManagerPort()!= -1
                && registerProtocol.getRemoteProxyPort()!= -1
                && registerProtocol.getRemoteDataPort()!= -1
                && registerProtocol.getLocalPort()!= -1){
            return true;
        }
        return false;
    }
//    public static void main(String[] args) {
//        List<String> d = new ArrayList<>(10);
//        List<String> e = new ArrayList<>();


//
//    }


}
