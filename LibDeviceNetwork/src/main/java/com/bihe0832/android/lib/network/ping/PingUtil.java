package com.bihe0832.android.lib.network.ping;

import android.content.Context;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.network.IpUtils;
import com.bihe0832.android.lib.network.WifiUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingUtil {

    private static final Pattern TRACE_IP_PATTERN =
            Pattern.compile("(?<=From )(?:[0-9]{1,3}\\.){3}[0-9]{1,3}");

    private static final Pattern DST_HOST_IP_PATTERN =
            Pattern.compile("(?<=from ).*(?=: icmp_seq=1 ttl=)");

    private static final int MAXHOP = 30;

    // 1000：默认返回值
    // -1: 不支持
    // -2: 支持但失败
    // -3: IP地址传入有误
    public static int ping(final String ipStr, final int count) {
        if(ipStr == null || ipStr.length() <= 0 || ipStr.equals(IpUtils.INVALID_IP)) {
            return -3;
        }
        int avgDelay = 1000;

        Process pingProcess = null;
        InputStream inputStream = null;
        BufferedReader bufferReader = null;
        try {
            // 超时时间为1s, 间隔0.2s发一次包？
            pingProcess = Runtime.getRuntime().exec("/system/bin/ping -c " + count + " -W 1 -i 0.2 " + ipStr);
            inputStream = pingProcess.getInputStream();
            bufferReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String pingResult = null;
            String line = null;
            while((line = bufferReader.readLine()) != null) {
                pingResult = line;
            }
            ZLog.d("ping, pingResult:" + pingResult);
            if(pingResult != null) {
                // pingResult sample: rtt min/avg/max/mdev = 1.713/96.363/267.407/109.395 ms, pipe 2
                String pingStats = pingResult.substring(pingResult.indexOf('=') + 1, pingResult.indexOf("ms")).trim();
                int avgStartIndex = pingStats.indexOf('/') + 1;
                int avgEndIndex = pingStats.indexOf('/', avgStartIndex);
                String avgDelayStr = pingStats.substring(avgStartIndex, avgEndIndex).trim();
                double avgDelayD = Double.valueOf(avgDelayStr);
                avgDelay = (int) avgDelayD + 1;
            }

            int exitValue = pingProcess.waitFor();
            if(exitValue == 1) {
                ZLog.d("ping, ping IP failed.");
                return -2;
            } else if(exitValue != 0) {
                ZLog.d("ping, ping IP not support, exitValue:" + exitValue);
                return -1;
            }
        } catch(Exception e) {
            // 如果超时会解析字符串错误，跳到这里，返回-2
            return -2;
        } finally {
            if(pingProcess != null) {
                try {
                    pingProcess.destroy();
                } catch(Exception ignored) {
                }
            }
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch(Exception ignored) {
                }
            }
            if(bufferReader != null) {
                try {
                    bufferReader.close();
                } catch(Exception ignored) {
                }
            }
        }
        return avgDelay;
    }

    private static String getTraceIpWithHop(final String host, int hop) {
        if(host == null || host.length() <= 0 || host.equals(IpUtils.INVALID_IP)) {
            return "";
        }
        StringBuilder hopStr = new StringBuilder();
        String command = "ping -w 1 -c 1 -t " + hop + " " + host;
        Process process = null;
        BufferedReader reader = null;
        try {
            process = Runtime.getRuntime().exec(command);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line;
            while((line = reader.readLine()) != null) {
                hopStr.append(line);
            }
            reader.close();
            int exitValue = process.waitFor();
            // 部分机型会在获取不到相应IP时返回2，即认为找不到文件路径
            if(exitValue != 0 && exitValue != 1 && exitValue != 2) {
                // 不支持ping
                return "";
            }

            // 匹配跳ip
            Matcher m = TRACE_IP_PATTERN.matcher(hopStr.toString());
            if(m.find()) {
                String hopIp = m.group();
                ZLog.d("getTraceIp: " + hopIp + ", with hop:" + hop);
                return hopIp;
            }
        } catch(Throwable ignored) {
        }
        return "";
    }

    private static String getRouterNextHop(final String host) {
        return getTraceIpWithHop(host, 2); // 下两跳
    }

    /*
     * 1000:    ping默认返回值
     * -1:      不支持ping
     * -2:      支持ping但失败
     * -3:      IP地址传入有误
     * -5:      获取下两跳地址失败(会用ping的方式获取，故实际上会包括-1和一部分-2)
     * -6:     下两跳非内网IP
     * -7:     异常
     * -10:    中控没有打开测下一跳的开关
     */
    public static int pingRouterNextHop(String host, int count) {
        try {
            String hopIp = getRouterNextHop(host);
            if(hopIp == null || hopIp.length() <= 0) {
                return -5;
            } else if(!IpUtils.isInnerIP(hopIp)){ // 下一跳为内网ip才进行ping
                return -6;
            } else {
                return ping(hopIp, count);
            }
        } catch(Throwable t) {
            return -7;
        }
    }


    public static String getWlanNextHop(final String host) {
        int curHop = 2;
        try {
            while(curHop <= MAXHOP) {
                String hopIp = getTraceIpWithHop(host, curHop++);
                if(!IpUtils.isInnerIP(hopIp)) {
                    return hopIp;
                }
                // 到达最后一跳则不再循环
                Matcher matcherDstHost = DST_HOST_IP_PATTERN.matcher(hopIp);
                if(matcherDstHost.find()) {
                    break;
                }
            }
        } catch(Exception ignored) {
        }
        return "";
    }

    /*
     * 1000:    ping默认返回值
     * -1:      不支持ping
     * -2:      支持ping但失败
     * -3:      IP地址传入有误
     * -5:      获取外网下一跳地址失败(会用ping的方式获取，故实际上会包括-1和一部分-2)
     * -7:     异常
     * -10:    中控没有打开测外网下一跳的开关
     */
    public static int pingWlanNextHop(String host, int count) {
        try {
            String hopIp = getWlanNextHop(host);
            ZLog.d("pingWlanNextHop: ip:" + hopIp);
            if(hopIp == null || hopIp.length() <= 0) {
                return -5;
            } else {
                return ping(hopIp, count);
            }
        } catch(Throwable t) {
            return -7;
        }
    }

    public static boolean isPingRouter(Context context, String pingIp) {
        return pingIp != null && pingIp.equals(WifiUtil.getGatewayIp(context));
    }

    public interface IPingResultListener {

        void onPingResult(String resultLine);
    }

    public static void ping(String hostname, int count, IPingResultListener pingResultListener) {
        Process pingProcess = null;
        BufferedReader resultReader = null;
        try {
            pingProcess = Runtime.getRuntime().exec("ping -c " + count + " -W 1 -i 0.2 " + hostname);
            resultReader = new BufferedReader(new InputStreamReader(pingProcess.getInputStream(), "GBK"));
            String line;
            while((line = resultReader.readLine()) != null) {
                pingResultListener.onPingResult(line);
            }
            int exitVal = pingProcess.waitFor();
            if(exitVal == 1) {
                ZLog.d("ping gateway failed.");
            } else if(exitVal != 0) {
                ZLog.d("ping gateway not support " + exitVal);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(resultReader != null) {
                try {
                    resultReader.close();
                } catch(Exception ignored) {
                }
            }
            if(pingProcess != null) {
                try {
                    pingProcess.destroy();
                } catch(Exception ignored) {
                }
            }
        }
    }

    // -1: 不支持
    // -2: 支持但失败
    static int getPingTime(String address, int count, float interval) {
        int pingTime = 1000;
        Process mPingProcess = null;
        String result = null;
        InputStream instream = null;
        BufferedReader bufferReader = null;
        try {
            // 1为ping一次操作 1为本次ping操作超时时间为1S
            mPingProcess = Runtime.getRuntime().exec("/system/bin/ping -c " + count +
                    " -W 1 -i " + interval + " " + address);
            instream = mPingProcess.getInputStream();
            bufferReader = new BufferedReader(new InputStreamReader(instream, "GBK"));
            String readline;
            while((readline = bufferReader.readLine()) != null) {
                result = readline;
            }
            if(result != null) {
                String mTimes = result.substring(result.indexOf("=") + 1, result.indexOf("ms")).trim();
                int i = mTimes.indexOf("/") + 1;
                int j = mTimes.indexOf('/', i);
                String mPingTime = mTimes.substring(i, j).trim();
                float ft = Float.valueOf(mPingTime);
                pingTime = (int) ft + 1;
            }

            int mExitValue = mPingProcess.waitFor();
            if(mExitValue == 1) {
                ZLog.d("ping gateway failed.");
                return -2;
            } else if(mExitValue != 0) {
                ZLog.d("ping gateway not support " + mExitValue);
                return -1;
            }
        } catch(Exception e) {
            e.printStackTrace();
            // 如果超时会解析字符串错误，跳到这里，返回-2
            return -2;
        } finally {
            if(mPingProcess != null) {
                try {
                    mPingProcess.destroy();
                } catch(Exception e1) {
                    ZLog.e("ping instream, error:" + e1.getMessage());
                }
            }
        }
        return pingTime;
    }

    // pingResult.meanDelay的值为
    // -1: 不支持
    // -2: 支持但失败
    public static PingResult getPingResult(PingListener pl, final String address, int count,
                                           int interval, boolean isPingRouter, PingThreshold pingThreshold) {
        listener = pl;
        PingResult res = new PingResult(address, -2, 0d, 0d, isPingRouter);
        int i = 0;
        long lastTime = System.currentTimeMillis();
        long curTime = 0;
        int pingTime = -2;

        while(i < count) {
            curTime = System.currentTimeMillis();
            if(curTime - lastTime < interval) {
                continue;
            }
            i++;
            lastTime = curTime;
            if(listener != null) {
                pingTime = getPingTime(address, 1, 0.2f);
                // res.numSent不一定等于pingCount
                res.numSent++;
                if(-1 == pingTime) {
                    ZLog.d("ping gateway not support.");
                    res.avgDelay = -1;
                } else if(-2 == pingTime) {
                    ZLog.d("ping gateway failed.");
                    res.avgDelay = -2;
                } else {
                    res.numReceived++;
                    res.sumDelay += pingTime;
                    if(pingTime >= pingThreshold.getHighDelay()) {
                        res.numHighDelay++;
                    }
                    if(pingTime > res.max) {
                        res.max = pingTime;
                    }
                    if(pingTime < res.min) {
                        res.min = pingTime;
                    }
                }
                if(listener != null) {
                    listener.onPingTime(address, pingTime);
                }
            } else {
                break;
            }
        }
        // 计算均值、高延迟率和丢失率，并生成路由状态描述
        res.freshStat(pingThreshold);
        // 判断网络的优劣
        if(listener != null) {
            listener.onPingResult(res);
        }
        // 如果是中途中断，则仍然从返回值中得到PingResult的信息
        return res;
    }

    static private PingListener listener = null;

    public static void endPing() {
        listener = null;
    }

    public static int compareDouble(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.compareTo(b2);
    }

    // -1: 不支持ping操作
    // -2: ping失败
    public static int pingGateway(Context context, int count) {
        try {
            String gatewayStr = WifiUtil.getGatewayIp(context);
            int avgDelay = PingUtil.ping(gatewayStr, count);
            ZLog.d("pingGateway, ip: " + gatewayStr + ", avgDelay: " + avgDelay);
            return avgDelay;
        } catch(Exception e) {
            ZLog.d("pingGateway, exception:" + e.getMessage());
            return -2;
        }
    }
}
