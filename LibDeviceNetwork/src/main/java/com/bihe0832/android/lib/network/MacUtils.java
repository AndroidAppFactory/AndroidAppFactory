package com.bihe0832.android.lib.network;

import android.os.Build;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.os.BuildUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * @author zixie code@bihe0832.com
 * Created on 2023/3/31.
 * Description: Description
 */
public class MacUtils {

    private static final String INVALID_MAC = "00:00:00:00:00:00";
    private static final String MAC_RE = "^%s\\s+0x1\\s+0x[2|6]\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$";
    private final static int BUF = 8 * 1024;

    public static final int IO_BUFFER_SIZE = 8 * 1024;

    public static String getLanMacAddr(String wifiGateIp) {
        Set<String> set = new HashSet<>();
        set.add(wifiGateIp);
        Map<String, String> data = getLanMacAddr(set);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getValue().contains(wifiGateIp)) {
                return entry.getKey();
            }
        }
        return INVALID_MAC;
    }

    public static Map<String, String> getLanMacAddr(Set<String> ipList) {
        Map<String, String> ipMacMap = new HashMap<>();
        BufferedReader bufferedReader = null;
        String line;
        try {
            if (null != ipList && ipList.size() != 0) {
                if (BuildUtils.INSTANCE.getSDK_INT() > Build.VERSION_CODES.P) {
                    Process proc = Runtime.getRuntime().exec("ip neigh show");
                    proc.waitFor();
                    bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                } else {
                    bufferedReader = new BufferedReader(
                            new InputStreamReader(new FileInputStream("/proc/net/arp"), Charset.forName("UTF-8")),
                            IO_BUFFER_SIZE);
                }

                while ((line = bufferedReader.readLine()) != null) {
                    ZLog.d("getLanMacAddr1 lien:" + line);
                    String[] lineSegments = line.split(" +");
                    if (BuildUtils.INSTANCE.getSDK_INT() > Build.VERSION_CODES.P) {
                        if (lineSegments.length > 4) {
                            String ip = lineSegments[0];
                            String macAddr = lineSegments[4];
                            /*
                             * 有些时候同一网段下多个ip可能拥有相同的mac地址，
                             * 因此我们反向维护这个映射，去除mac相同的ip，同时
                             * 我们在ip最后加上" 重复次数"
                             */
                            if (!macAddr.equals(INVALID_MAC)) {
                                ZLog.d("putLanMacAddr:" + ip + " " + macAddr);
                                String macIP = ipMacMap.get(macAddr);
                                if (macIP == null) {
                                    ipMacMap.put(macAddr, ip + " 1");
                                } else {
                                    int dup = Integer.parseInt(macIP.split(" ")[1]);
                                    ipMacMap.put(macAddr, macIP.split(" ")[0] + " " + String.valueOf(dup + 1));
                                }
                            }

                        }
                    } else {
                        if (lineSegments.length > 4) {
                            String ip = lineSegments[0];
                            String macAddr = lineSegments[3];
                            /*
                             * 有些时候同一网段下多个ip可能拥有相同的mac地址，
                             * 因此我们反向维护这个映射，去除mac相同的ip，同时
                             * 我们在ip最后加上" 重复次数"
                             */
                            if (!macAddr.equals(INVALID_MAC)) {
                                ZLog.d("putLanMacAddr:" + ip + " " + macAddr);
                                String macIP = ipMacMap.get(macAddr);
                                if (macIP == null) {
                                    ipMacMap.put(macAddr, ip + " 1");
                                } else {
                                    int dup = Integer.parseInt(macIP.split(" ")[1]);
                                    ipMacMap.put(macAddr, macIP.split(" ")[0] + " " + String.valueOf(dup + 1));
                                }
                            }

                        }
                    }


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return ipMacMap;
    }

    public static Map<String, String> getHardwareAddress(Vector<String> ipList) {
        Set<String> set = new HashSet<>();
        set.addAll(ipList);
        return getLanMacAddr(set);
    }
}
