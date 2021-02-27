package com.bihe0832.android.lib.network;

import java.util.HashMap;


public class IspUtil {
    public static final String CMCC = "中国移动";
    public static final String CUCC = "中国联通";
    public static final String CTCC = "中国电信";
    public static final String CTT = "中国铁通";
    public static final String GWBN = "长城宽带";
    public static final String HTBN = "互通宽带";
    public static final String EDU = "教育网";
    public static final String GR = "广东广电";
    public static final String GY = "广东盈通";
    public static final String STVC = "天威视讯";
    public static final String DEFAULT_ISP = "其他";

    private static final HashMap<Integer, String> ISP_MAP = new HashMap<Integer, String>() {{
        put(159, CTT);
        put(159918, HTBN);
        put(169070, EDU);
        put(181, CUCC);
        put(200928, GR);
        put(258, GWBN);
        put(502, GY);
        put(564, CMCC);
        put(567, CTCC);
        put(645, STVC);
    }};

    public static String getIspName(int ispCode) {
        String ispName = ISP_MAP.get(ispCode);
        return ispName != null ? ispName : DEFAULT_ISP;
    }

}
