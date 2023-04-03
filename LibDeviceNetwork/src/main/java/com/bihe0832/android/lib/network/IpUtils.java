package com.bihe0832.android.lib.network;


import com.bihe0832.android.lib.log.ZLog;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class IpUtils {

    public static final String INVALID_IP = "0.0.0.0";
    public static final String BROADCAST_IP = "255.255.255.255";

    private static final Pattern IPV4_PATTERN =
            Pattern.compile(
                    "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private static final Pattern IPV6_STD_PATTERN =
            Pattern.compile(
                    "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN =
            Pattern.compile(
                    "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

    public static boolean isValidIpv4Address(final String input) {
        if(input == null || input.length() <= 0) {
            return false;
        }
        if(INVALID_IP.equals(input) || BROADCAST_IP.equals(input)) {
            return false;
        }
        return IPV4_PATTERN.matcher(input).matches();
    }

    public static boolean isIpv4Address(final String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    public static boolean isIpv6Address(final String input) {
        return isIpv6StdAddress(input) || isIpv6HexCompressedAddress(input);
    }

    public static boolean isIpAddress(final String input) {
        return isIpv4Address(input) || isIpv6Address(input);
    }

    private static boolean isIpv6StdAddress(final String input) {
        return IPV6_STD_PATTERN.matcher(input).matches();
    }

    private static boolean isIpv6HexCompressedAddress(final String input) {
        return IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
    }

    public static List<String> getDomainIpList(final String domain) {
        ArrayList<String> ret = new ArrayList<String>();
        InetAddress[] addrs = getDomainAddrList(domain);
        if(addrs != null && addrs.length > 0) {
            for(InetAddress addr : addrs) {
                ret.add(addr.getHostAddress());
            }
        }
        return ret;
    }

    public static List<String> getDomainIpListOnlyIPv4(final String domain) {
        ArrayList<String> ret = new ArrayList<String>();
        InetAddress[] addrs = getDomainAddrList(domain);
        if (addrs != null && addrs.length > 0) {
            for (InetAddress addr : addrs) {
                if (addr instanceof Inet4Address) {
                    ret.add(addr.getHostAddress());
                }
            }
        }
        return ret;
    }

    public static String getDomainFirstIp(final String domain) {
        InetAddress firstAddr = getDomainFirstAddr(domain);
        if(firstAddr != null) {
            return firstAddr.getHostAddress();
        }
        return "";
    }

    // 注意可能会返回null
    public static InetAddress[] getDomainAddrList(final String domain) {
        InetAddress[] ips = null;
        int retry = 2;

        if(isIpAddress(domain)) {
            // IP地址就直接使用原生方法返回，不再另开线程
            try {
                return InetAddress.getAllByName(domain);
            } catch(Exception e) {
                // ignore
            }
        }

        while(retry-- > 0) {
            try {
                DnsLookupThread dnsQuery = new DnsLookupThread(domain);
                dnsQuery.start();
                dnsQuery.join(3000); // 当前线程等待超时时间
                ips = dnsQuery.getAddrs();
                if(ips == null || ips.length <= 0) {
                    continue;
                }

                return ips;
            } catch(Exception e) {
                // ignore
            }
        }
        ZLog.w("dns解析addrs失败:" + domain);
        return null;
    }

    // 注意可能会返回null
    public static InetAddress getDomainFirstAddr(final String domain) {
        InetAddress[] addrs = getDomainAddrList(domain);
        if(addrs != null && addrs.length > 0) {
            return addrs[0];
        }
        return null;
    }

    public static String getNetDomainFirstIp(final String domain, final int netid) {
        InetAddress firstAddr = getNetDomainFirstAddr(domain, netid);
        if(firstAddr != null) {
            return firstAddr.getHostAddress();
        }
        return "";
    }

    public static InetAddress getNetDomainFirstAddr(final String domain, final int netid) {
        InetAddress[] addrs = dnsOnNet(domain, netid);
        if(addrs != null && addrs.length > 0) {
            return addrs[0];
        }
        return null;
    }

    // 注意可能会返回null
    public static InetAddress[] dnsOnNet(final String domain, final int netid) {
        InetAddress[] ips = null;
        int retry = 2;
        while(retry-- > 0) {
            try {
                DnsLookupThread dnsQuery = new DnsLookupThread(domain, netid);
                dnsQuery.start();
                dnsQuery.join(3000); // 当前线程等待超时时间
                ips = dnsQuery.getAddrs();
                if(ips == null || ips.length <= 0) {
                    continue;
                }

                return ips;
            } catch(Exception e) {
                // ignore
            }
        }
        ZLog.w("dnsOnNet解析addrs失败:" + domain + ", netid:" + netid);
        return null;
    }

    // 网络序直接转成ipstr
    public static String ipn2s(int ipNetSeq) {
        return (ipNetSeq & 0xFF) + "." + (0xFF & ipNetSeq >> 8) + "." + (0xFF & ipNetSeq >> 16) + "."
                + (0xFF & ipNetSeq >> 24);
    }

    // 主机序直接转成ipstr
    public static String iph2s(int ipHostSeq) {
        return ((ipHostSeq >> 24) & 0xFF) + "." + ((ipHostSeq >> 16) & 0xFF) + "." + ((ipHostSeq >> 8) & 0xFF) + "."
                + (ipHostSeq & 0xFF);
    }

    //ipstr转为ipint
    public static int ips2h(String ipStr) {
        int ipHostSeq = 0;
        if(ipStr == null) {
            return ipHostSeq;
        }
        String[] ipAddrArr = ipStr.split("\\.");
        if(ipAddrArr.length != 4) {
            return ipHostSeq;
        }
        try {
            for(int i = 3; i >= 0; i--) {
                int ip = Integer.parseInt(ipAddrArr[3 - i]);
                // left shifting 24,16,8,0 and bitwise OR
                ipHostSeq |= ip << (i * 8);
            }
        } catch(Exception e) {
            // ignore
        }
        return ipHostSeq;
    }

    public static int ipn2h(int ipNetSeq) {
        String ipStr = ipn2s(ipNetSeq);
        return ips2h(ipStr);
    }

    public static boolean isInnerIP(String ip) {
        long ipNum = getIpNum(ip);
        /*
         私有IP：A类  10.0.0.0-10.255.255.255
         B类  172.16.0.0-172.31.255.255
         C类  192.168.0.0-192.168.255.255
         还有127这个网段是环回地址
         */
        // 注意，0x***后面要加L，否则java会认为是int类型，部分十六进制数会溢出导致判断出错
        return (ipNum >= 0x0A000000L && ipNum <= 0x0AFFFFFFL) ||
                (ipNum >= 0xAC100000L && ipNum <= 0xAC1FFFFFL) ||
                (ipNum >= 0xC0A80000L && ipNum <= 0xC0A8FFFFL)
                || ip.equals("127.0.0.1");
    }

    private static long getIpNum(String ipAddress) {
        String[] ip = ipAddress.split("\\.");
        long a = Integer.parseInt(ip[0]);
        long b = Integer.parseInt(ip[1]);
        long c = Integer.parseInt(ip[2]);
        long d = Integer.parseInt(ip[3]);

        return a * 256 * 256 * 256 + b * 256 * 256 + c * 256 + d;
    }

    private static class DnsLookupThread extends Thread {
        private InetAddress[] mAddrs = null;
        private String mHostname = null;
        private int mNetid = -1;

        private DnsLookupThread(String hostname) {
            this(hostname, -1);
        }

        private DnsLookupThread(String hostname, int netid) {
            this.mHostname = hostname;
            this.mNetid = netid;
        }

        @Override
        public void run() {
            try {
                InetAddress[] localAddrs = null;
                if(mNetid > 0) {
                    // 绑定netId的DNS请求
                    Method dnsMethod = InetAddress.class.getMethod("getAllByNameOnNet", String.class, int.class);
                    localAddrs = (InetAddress[]) dnsMethod.invoke(null, mHostname, mNetid);
                } else {
                    // 一般DNS请求
                    localAddrs = InetAddress.getAllByName(mHostname);
                }
                setAddrs(localAddrs);
            } catch(Throwable t) {
                ZLog.d("dns exception:" + t.getMessage());
            }
        }

        private synchronized void setAddrs(InetAddress[] localAddrs) {
            mAddrs = localAddrs;
        }

        private synchronized InetAddress[] getAddrs() {
            return mAddrs;
        }
    }

    /*检验IP段和prefixLength_是否对应，防止addRoute报异常*/
    public static boolean verifyIpSegmentValidity(String ipSegment, String prefixLength_) {
        try {
            if (!isValidIpv4Address(ipSegment)) {
                return false;
            }
            int prefixLength = Integer.parseInt(prefixLength_);

            if (prefixLength < 0 || prefixLength > 32) {
                return false;
            }

            InetAddress[] addrs = InetAddress.getAllByName(ipSegment);
            InetAddress inetAddress = null;
            if (addrs != null && addrs.length > 0) {
                inetAddress = addrs[0];
            }

            if (inetAddress == null) {
                return false;
            }

            int offset = prefixLength / 8;
            byte[] bytes = inetAddress.getAddress();
            if (offset < bytes.length) {
                for (bytes[offset] <<= prefixLength % 8; offset < bytes.length; ++offset) {
                    if (bytes[offset] != 0) {
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            ZLog.e("verifyIpSegmentValidity failed:" + e.getMessage());
        }
        return false;
    }


}
