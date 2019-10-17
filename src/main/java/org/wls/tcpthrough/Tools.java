package org.wls.tcpthrough;

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

    public static void main(String[] args) {
        System.out.println(Tools.uuid());
    }

}
