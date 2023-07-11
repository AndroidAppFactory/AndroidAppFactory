package com.bihe0832.android.lib.network;

import com.bihe0832.android.lib.log.ZLog;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import kotlin.text.Charsets;

/**
 * @author hardyshi code@bihe0832.com
 * Created on 2023/7/11.
 * Description: Description
 */
public class ARPUtils {

    public static final int SOCKET_TIMEOUT = 5; // second
    public static final String UDP_DETECT_MSG = "getArp";
    public static final int UDP_DETECT_PORT = 7;

    public static void sendUdpMessage(String ipAddress, int port, String message) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] buffer = message.getBytes(Charsets.UTF_8);
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, inetAddress, port);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendArpReqPacket(String ipStr) {
        sendArpReqPacket(ipStr, 1);
    }

    public static void sendArpReqPacket(String ipStr, int count) {
        try {
            InetAddress inetAddress = IpUtils.getDomainFirstAddr(ipStr);
            if (inetAddress != null) {
                for (int i = 0; i < count; i++) {
                    inetAddress.isReachable(SOCKET_TIMEOUT);
                }
            }
        } catch (Exception e) {
            ZLog.d("sendArpReqPacketerr" + e.toString());
            // ignore
        }
    }


}
