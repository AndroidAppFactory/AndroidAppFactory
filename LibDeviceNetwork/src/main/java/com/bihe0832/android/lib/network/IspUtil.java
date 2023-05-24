package com.bihe0832.android.lib.network;

public class IspUtil {

    public static final String ISP_TYPE_UNKNOW = "其他";
    public static String getOperatorDesc(String operatorCode) {
        if (operatorCode == null || "".equals(operatorCode)) {
            return ISP_TYPE_UNKNOW;
        }
        switch (operatorCode) {
            case "46000":
            case "46002":
            case "46004":
            case "46007":
            case "46008":
                return "中国移动";
            case "46001":
            case "46006":
            case "46009":
                return "中国联通";
            case "46003":
            case "46005":
            case "46011":
                return "中国电信";
            default:
                return ISP_TYPE_UNKNOW;
        }
    }
}
