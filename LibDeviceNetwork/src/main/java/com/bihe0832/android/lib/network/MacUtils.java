package com.bihe0832.android.lib.network;

import android.os.Build;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.network.wifi.WifiUtil;
import com.bihe0832.android.lib.utils.os.BuildUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/3/31.
 * Description: Description
 */
public class MacUtils {

    private static final String INVALID_MAC = "00:00:00:00:00:00";
    private static final String MAC_RE = "^%s\\s+0x1\\s+0x[2|6]\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$";
    private final static int BUF = 8 * 1024;

    public static final int IO_BUFFER_SIZE = 8 * 1024;

    public static String getLanMacAddr(String wifiGateIp) {
        String ipMac = "";
        BufferedReader bufferedReader = null;
        try {
            if (wifiGateIp != null && wifiGateIp.length() != 0) {
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), IO_BUFFER_SIZE);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(INVALID_MAC)) {
                        continue;
                    }
                    if (!line.contains(wifiGateIp)) {
                        continue;
                    }
                    String linePattern = String.format(MAC_RE, wifiGateIp.replace(".", "\\."));
                    Pattern pattern = Pattern.compile(linePattern);
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        String macAddr = matcher.group(1);
                        if (!macAddr.equals(INVALID_MAC)) {
                            ipMac = macAddr;
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return ipMac;
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
                                //ipMacMap.put(ip, macAddr);
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
                                //ipMacMap.put(ip, macAddr);
                            }

                        }
                    }


                }
            }
        } catch (Exception e) {
            // ignore
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return ipMacMap;
    }

    public static HashMap<String, String> getHardwareAddress(Vector<String> ipList) {
        String hw = INVALID_MAC;
        HashMap<String, String> mapList = new HashMap<String, String>();
        BufferedReader bufferedReader = null;
        try {
            if (ipList != null && ipList.size() != 0) {
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), BUF);
                String line;
                Matcher matcher;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(INVALID_MAC)) {
                        continue;
                    }
                    boolean flag = false;
                    for (String ip : ipList) {
                        if (!line.contains(ip)) {
                            continue;
                        }
                        String ptrn = String.format(MAC_RE, ip.replace(".", "\\."));
                        Pattern pattern = Pattern.compile(ptrn);
                        matcher = pattern.matcher(line);
                        if (matcher.matches()) {
                            hw = matcher.group(1);
                            if (!hw.equals(INVALID_MAC)) {
                                // ZLog.debug("neighborPhones ip:" + ip + ",mac:"
                                // + hw);
                                mapList.put(ip, hw);
                                flag = true;
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return mapList;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    ZLog.e("getHardwareAddress, error:" + e.getMessage());
                }
            }
        }
        return mapList;
    }
}
