package org.wls.tcpthrough.model;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by wls on 2019/10/23.
 */
public class TrustIpsModel {
    private List<String> ips = null;
    private Lock lock;

    public TrustIpsModel() {
        ips = new ArrayList<>();
        lock = new ReentrantLock();
    }

    public void addIp(String ip) {
        lock.lock();
        try {
            if(!ips.contains(ip)){
                ips.add(ip);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean deleteIps(String ip) {
        boolean tryLock = lock.tryLock();
        if (tryLock) {
            try {
                ips.removeIf(_ip -> _ip.equals(ip));
                return true;
            } finally {
                lock.unlock();
            }
        } else {
            return false;
        }
    }

    public boolean contains(String ip){
        lock.lock();
        try {
            return ips.contains(ip);
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(InetSocketAddress inetAddress){
        lock.lock();
        try{
            if(inetAddress.getAddress().isLoopbackAddress()){
                return ips.contains("127.0.0.1") || ips.contains("localhost");
            } else {
                if(inetAddress.getAddress() instanceof Inet4Address){
                    Inet4Address inet4 = (Inet4Address) inetAddress.getAddress();
                    return ips.contains(inet4.getHostAddress());
                } else {
                    return false;
                }
            }
        } finally {
            lock.unlock();
        }

    }

    public List<String>  getIps(){
        List<String> res = new ArrayList<>();
        ips.forEach(ip-> res.add(ip));
        return res;
    }

    public void  copyIps(List<String> copyIps){
        ips.forEach(ip-> copyIps.add(ip));
    }
}

