package com.bihe0832.android.lib.device.cpu;

import android.os.Build.VERSION_CODES;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.os.BuildUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2024/3/5.
 *         Description:
 */
public class CPUHelper {

    private static final int CORE_NUM_MAX = 256;

    /**
     * 获取系统CPU核心数
     *
     * @return the number of cores
     */
    public static int getNumberOfCores() {
        try {
            ZLog.d("getNumberOfCores call");
            int num = -1;
            if (BuildUtils.INSTANCE.getSDK_INT() >= VERSION_CODES.JELLY_BEAN_MR1) {
                num = Runtime.getRuntime().availableProcessors();
            } else {
                num = getNumCoresOldPhones();
            }
            // 最大核数
            if (num > CORE_NUM_MAX) {
                ZLog.w("getNumberOfCores error for num:" + num);
                num = -1;
            }
            ZLog.d("getNumberOfCores num:" + num);
            return num;
        } catch (Exception e) {
            ZLog.w("getNumberOfCores error:" + e.getMessage());
        }
        return -1;
    }

    private static int getNumCoresOldPhones() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            if (null == files) {
                return 1;
            }
            return files.length;
        } catch (Exception e) {
            ZLog.w("getNumCoresOldPhones error:" + e.getMessage());
            return 1;
        }
    }

    /**
     * 获取CPU最大频率
     *
     * @return the max cpu freq
     */
    public static String getMaxCpuFreq() {
        ZLog.d("getMaxCpuFreq call");
        String result = "-1";
        try {
            int cores = getNumberOfCores();
            List<Double> cpuList = getCPUFreq(cores);
            double max = 0.0;
            if (cpuList != null && cpuList.size() != 0) {
                max = cpuList.get(0);
                for (double i : cpuList) {
                    if (i > max) {
                        max = i;
                    }
                }
            }
            result = String.valueOf(max);
            ZLog.d("getMaxCpuFreq result:" + result);
        } catch (Exception e) {
            ZLog.w("getNumCoresOldPhones error:" + e.getMessage());
        }
        return result;
    }

    // 遍历CPU主频值
    private static List<Double> getCPUFreq(int cores) {
        ZLog.d("getCPUFreq call for cores:" + cores);
        List<Double> cpuList = new ArrayList<Double>();
        for (int i = 0; i < cores; i++) {
            BufferedReader reader = null;
            try {
                String path = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq";
                reader =
                        new BufferedReader(
                                new InputStreamReader(new FileInputStream(path), "UTF-8"), 512);
                String result = reader.readLine();
                double freq = -1.0;
                if (result.length() > 0) {
                    freq = Double.parseDouble(result.trim());
                    cpuList.add(freq);
                }
                reader.close();
                reader = null;
                ZLog.d("getCPUFreq num:" + i + ", freq:" + freq);
            } catch (Exception e) {
                ZLog.w("getCPUFreq error:" + e.getMessage());
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        ZLog.w("getCPUFreq close input stream error:" + e.getMessage());
                    }
                }
            }
        }
        return cpuList;
    }

    /**
     * 获取CPU信息
     *
     * @return the cpu info
     */
    @SuppressWarnings("resource")
    public static Map<String, String> getCpuInfo() {
        Map<String, String> map = new HashMap<String, String>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("/proc/cpuinfo"), "UTF-8");
            while (scanner.hasNextLine()) {
                String[] vals = scanner.nextLine().split(": ");
                if (vals.length > 1) {
                    map.put(vals[0].trim(), vals[1].trim());
                }
            }
            scanner.close();
            scanner = null;
        } catch (Exception e) {
            try {
                if (null != scanner) {
                    scanner.close();
                }
            } catch (Exception ex) {
                ZLog.w("getCpuInfo close scanner error:" + e.getMessage());
            }
            ZLog.w("getCpuInfo error:" + e.getMessage());
        }
        return map;
    }

    /**
     * 获取当前系统CPU使用率 0.072164945
     *
     * @return the total cpu rate
     */
    public static float getTotalCPURate() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            String[] toks = load.split(" ");
            String toks2 = toks[2];
            String toks3 = toks[3];
            String toks4 = toks[4];
            String toks5 = toks[5]; // idle
            String toks6 = toks[6];
            String toks7 = toks[7];
            String toks8 = toks[8];
            if (toks2 == null || toks2.equals("")) {
                toks2 = "0";
            } else if (toks3 == null || toks3.equals("")) {
                toks3 = "0";
            } else if (toks4 == null || toks4.equals("")) {
                toks4 = "0";
            } else if (toks5 == null || toks5.equals("")) {
                toks5 = "0";
            } else if (toks6 == null || toks6.equals("")) {
                toks6 = "0";
            } else if (toks7 == null || toks7.equals("")) {
                toks7 = "0";
            } else if (toks8 == null || toks8.equals("")) {
                toks8 = "0";
            }
            final long idle1 = Long.parseLong(toks5);
            final long cpu1 =
                    Long.parseLong(toks2)
                            + Long.parseLong(toks3)
                            + Long.parseLong(toks4)
                            + Long.parseLong(toks6)
                            + Long.parseLong(toks7)
                            + Long.parseLong(toks8);
            try {
                Thread.sleep(300);
            } catch (Exception e) {
                // ignore
            }
            reader.seek(0);
            load = reader.readLine();
            reader.close();
            toks = load.split(" ");
            toks2 = toks[2];
            toks3 = toks[3];
            toks4 = toks[4];
            toks5 = toks[5]; // idle
            toks6 = toks[6];
            toks7 = toks[7];
            toks8 = toks[8];
            if (toks2 == null || toks2.equals("")) {
                toks2 = "0";
            } else if (toks3 == null || toks3.equals("")) {
                toks3 = "0";
            } else if (toks4 == null || toks4.equals("")) {
                toks4 = "0";
            } else if (toks5 == null || toks5.equals("")) {
                toks5 = "0";
            } else if (toks6 == null || toks6.equals("")) {
                toks6 = "0";
            } else if (toks7 == null || toks7.equals("")) {
                toks7 = "0";
            } else if (toks8 == null || toks8.equals("")) {
                toks8 = "0";
            }
            long idle2 = Long.parseLong(toks5);
            long cpu2 =
                    Long.parseLong(toks2)
                            + Long.parseLong(toks3)
                            + Long.parseLong(toks4)
                            + Long.parseLong(toks6)
                            + Long.parseLong(toks7)
                            + Long.parseLong(toks8);
            if (0 == (cpu2 + idle2) - (cpu1 + idle1)) {
                return 0;
            }
            return (float) 100 * (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
        } catch (Exception e) {
            ZLog.w("getTotalCPURate error:" + e.getMessage());
        }
        return 0;
    }

    /**
     * 获取当前进程CPU使用率
     *
     * @return the process cpu rate
     */
    public static float getProcessCPURate() {
        try {
            float totalCpuTime1 = getTotalCpuTime();
            float processCpuTime1 = getProcessCpuTime();

            try {
                Thread.sleep(300);
            } catch (Exception e) {
                // ignore
            }

            float totalCpuTime2 = getTotalCpuTime();
            float processCpuTime2 = getProcessCpuTime();

            if (0 == totalCpuTime2 - totalCpuTime1) {
                return 0.0f;
            }

            float cpuRate =
                    100 * (processCpuTime2 - processCpuTime1) / (totalCpuTime2 - totalCpuTime1);

            return cpuRate;
        } catch (Exception e) {
            return 0.0f;
        }
    }

    /**
     * 获取系统总CPU使用时间
     *
     * @return the total cpu time
     */
    public static long getTotalCpuTime() {
        String[] cpuInfos = null;
        long totalCpu;
        try {
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(new FileInputStream("/proc/stat"), "UTF-8"),
                            1000);
            // cpu 11030645 1330783 15707102 38353237 68615 1635648 1036809 0 0
            // 0
            String load = reader.readLine();
            reader.close();
            if (null == load) {
                return 0;
            }
            cpuInfos = load.split(" ");
            if (cpuInfos == null) {
                return 0;
            }
            // 所有时间均为系统启动累计到当前的时间，单位均为：jiffies 1jiffies=0.01s 从2开始计算是因为cpu之后有两个空格
            String ci2 = cpuInfos[2]; // user (11030645) 用户态的CPU时间 ，不包含nice值
            String ci3 = cpuInfos[3]; // nice (1330783) 负进程所占用的CPU时间
            String ci4 = cpuInfos[4]; // system (15707102) 核心时间
            String ci5 = cpuInfos[5]; // idle (38353237) 除硬盘IO等待时间以外其它等待时间
            String ci6 = cpuInfos[6]; // iowait (68615) 硬盘IO等待时间
            String ci7 = cpuInfos[7]; // irq (1635648) 硬中断时间
            String ci8 = cpuInfos[8]; // softirq (1036809) 软中断时间

            if (ci2 == null || ci2.equals("")) {
                ci2 = "0";
            } else if (ci3 == null || ci3.equals("")) {
                ci3 = "0";
            } else if (ci4 == null || ci4.equals("")) {
                ci4 = "0";
            } else if (ci5 == null || ci5.equals("")) {
                ci5 = "0";
            } else if (ci6 == null || ci6.equals("")) {
                ci6 = "0";
            } else if (ci7 == null || ci7.equals("")) {
                ci7 = "0";
            } else if (ci8 == null || ci8.equals("")) {
                ci8 = "0";
            }
            // 系统CPU总时间 = user + nice + system + idle + iowait + irq + softirq
            totalCpu =
                    Long.parseLong(ci2)
                            + Long.parseLong(ci3)
                            + Long.parseLong(ci4)
                            + Long.parseLong(ci5)
                            + Long.parseLong(ci6)
                            + Long.parseLong(ci7)
                            + Long.parseLong(ci8);
        } catch (Exception e) {
            // ignore
            return 0;
        }
        return totalCpu;
    }

    /**
     * 获取应用占用的CPU时间
     *
     * @return the app cpu time
     */
    public static long getProcessCpuTime() {
        String[] cpuInfos = null;
        try {
            int pid = android.os.Process.myPid();
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    new FileInputStream("/proc/" + pid + "/stat"), "UTF-8"),
                            1000);
            String load = reader.readLine();
            reader.close();
            if (null == load) {
                return 0;
            }
            cpuInfos = load.split(" ");
            if (cpuInfos == null) {
                return 0;
            }
        } catch (IOException e) {
            ZLog.w("getAppCpuTime error:" + e.getMessage());
            return 0;
        }
        String ci13 = cpuInfos[13]; // utime 该任务在用户态运行的时间
        String ci14 = cpuInfos[14]; // stime 该任务在核心态运行的时间
        String ci15 = cpuInfos[15]; // cutime 所有已死线程在用户态运行的时间
        String ci16 = cpuInfos[16]; // cstime 所有已死在核心态运行的时间

        if (ci13 == null || ci13.equals("")) {
            ci13 = "0";
        } else if (ci14 == null || ci14.equals("")) {
            ci14 = "0";
        } else if (ci15 == null || ci15.equals("")) {
            ci15 = "0";
        } else if (ci16 == null || ci16.equals("")) {
            ci16 = "0";
        }
        // 进程CPU总时间 processCpuTime = utime + stime + cutime + cstime
        // 线程CPU时间 threadCpuTime = utime + stime
        long appCpuTime =
                Long.parseLong(ci13)
                        + Long.parseLong(ci14)
                        + Long.parseLong(ci15)
                        + Long.parseLong(ci16);
        return appCpuTime;
    }

    /**
     * 获取CPU温度
     *
     * @return the cpu temperature
     */
    public static long getCPUTemperature() {
        long temp = -1;
        byte[] buffer = new byte[128];
        FileInputStream is = null;
        String file = null;
        try {
            is = new FileInputStream("/sys/devices/virtual/thermal/thermal_zone0/temp");
            int len = is.read(buffer);
            if (len > 0) {
                int i;
                for (i = 0; i < len && i < 128; i++) {
                    if (buffer[i] == '\n') {
                        break;
                    }
                }
                file = new String(buffer, 0, i, "UTF-8");
            }
            if (file != null) {
                temp = Long.parseLong(file);
                ZLog.i("temp a is:" + temp);
                if (temp >= 100 && temp < 1000) {
                    temp = temp / 10;
                } else if (temp >= 1000 && temp < 10000) {
                    temp = temp / 100;
                } else if (temp >= 10000 && temp < 100000) {
                    temp = temp / 1000;
                }
            }
        } catch (Exception e) {
            // ignore
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    ZLog.e("close stream error:" + e.getMessage());
                }
            }
        }
        return temp;
    }
}
