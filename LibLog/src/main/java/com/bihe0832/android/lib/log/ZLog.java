package com.bihe0832.android.lib.log;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import kotlin.jvm.Synchronized;
import org.jetbrains.annotations.NotNull;

/**
 * @author hardyshi code@bihe0832.com
 *         Created on 2020/7/9.
 *         Description:日志工具类的各类日志打印方式
 */
public final class ZLog {

    private static boolean sDebug;

    private static final String TAG = "ZLog";

    private static final int STACK_TRACE_DEEP = 6;
    private static final int LOG_LENGTH = 3000;

    private static final ConcurrentHashMap<String, LogImpl> logImplList = new ConcurrentHashMap<String, LogImpl>() {{
        put(LogImplForLogcat.INSTANCE.getName(), LogImplForLogcat.INSTANCE);
    }};

    private static final String getTag() {
        return getTag(null, STACK_TRACE_DEEP);
    }

    private static final String getTag(String subTag, int index) {
        String tag = "";
        StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        if (index < 0 || index >= traces.length) {
            return tag;
        }
        String clsName = traces[index].getClassName();
        String methodName = traces[index].getMethodName();
        String shortClsName = "";
        int dot = clsName.lastIndexOf('.');
        if (dot != -1) {
            shortClsName = clsName.substring(dot + 1);
        }

        if (TextUtils.isEmpty(subTag)) {
            tag = TAG + " " + shortClsName + "." + methodName;
        } else {
            tag = TAG + "->" + subTag + " " + shortClsName + "."
                    + methodName;
        }
        return tag;
    }

    public static void info(String log) {
        info(getTag(), log);
    }

    public static void info(String tag, String log) {
        if (log.length() > LOG_LENGTH) {
            for (int i = 0; i < log.length(); i += LOG_LENGTH) {
                if (i + LOG_LENGTH < log.length()) {
                    info(tag, log.substring(i, i + LOG_LENGTH));
                } else {
                    info(tag, log.substring(i));
                }
            }
        } else {
            for (LogImpl tpl : logImplList.values()) {
                tpl.info(tag, log);
            }
        }
    }

    public static void i(String log) {
        if (sDebug) {
            i(getTag(), log);
        }
    }

    public static void i(String tag, String log) {
        if (sDebug) {
            if (log.length() > LOG_LENGTH) {
                for (int i = 0; i < log.length(); i += LOG_LENGTH) {
                    if (i + LOG_LENGTH < log.length()) {
                        i(tag, log.substring(i, i + LOG_LENGTH));
                    } else {
                        i(tag, log);
                    }
                }
            } else {
                for (LogImpl tpl : logImplList.values()) {
                    tpl.i(tag, log);
                }
            }
        }
    }

    private static void debugLog(String tag, String log) {
        if (log.length() > LOG_LENGTH) {
            for (int i = 0; i < log.length(); i += LOG_LENGTH) {
                if (i + LOG_LENGTH < log.length()) {
                    debugLog(tag, log.substring(i, i + LOG_LENGTH));
                } else {
                    debugLog(tag, log.substring(i));
                }
            }
        } else {
            for (LogImpl tpl : logImplList.values()) {
                tpl.d(tag, log);
            }
        }
    }

    public static void d(String tag, Bundle b) {
        if (sDebug) {
            if (b == null) {
                debugLog(tag, "empty bundle");
                return;
            }

            Set<String> keys = b.keySet();
            for (String key : keys) {
                if (b.get(key) instanceof byte[]) {
                    debugLog(tag, key + ":" + new String(b.getByteArray(key)));
                } else if (b.get(key) instanceof String) {
                    debugLog(tag, key + ":" + b.getString(key));
                } else if (b.get(key) instanceof Long) {
                    debugLog(tag, key + ":" + b.getLong(key));
                } else if (b.get(key) instanceof Integer) {
                    debugLog(tag, key + ":" + b.getInt(key));
                } else {
                    debugLog(tag, key);
                }
            }
        }
    }

    public static void d(String subTag, Intent i) {
        if (sDebug) {
            if (i == null || i.getExtras() == null) {
                debugLog(subTag, "********************** INTENT START **************************");
                debugLog(subTag, "********************** INTENT END **************************");
                return;
            }
            debugLog(subTag, "********************** INTENT START **************************");
            debugLog(subTag, "Action: " + i.getAction());
            debugLog(subTag, "Component: " + i.getComponent());
            debugLog(subTag, "Flags: " + i.getFlags());
            debugLog(subTag, "Scheme: " + i.getScheme());

            Bundle b = i.getExtras();
            Set<String> keys = b.keySet();
            for (String key : keys) {
                if (b.get(key) instanceof byte[]) {
                    debugLog(subTag, key + ":" + new String(b.getByteArray(key)));
                } else if (b.get(key) instanceof String) {
                    debugLog(subTag, key + ":" + b.getString(key));
                } else if (b.get(key) instanceof Long) {
                    debugLog(subTag, key + ":" + b.getLong(key));
                } else if (b.get(key) instanceof Integer) {
                    debugLog(subTag, key + ":" + b.getInt(key));
                } else {
                    debugLog(subTag, key);
                }
            }
            debugLog(subTag, "********************** INTENT END **************************");
        }
    }

    public static void d(String log) {
        if (sDebug) {
            debugLog(getTag(), log);
        }
    }

    public static void d(String tag, String log) {
        if (sDebug) {
            debugLog(tag, log);
        }
    }

    public static void d(Bundle b) {
        if (sDebug) {
            String tag = getTag(null, STACK_TRACE_DEEP);
            d(tag, b);
        }
    }

    public static void d(Intent i) {
        if (sDebug) {
            String tag = getTag(null, STACK_TRACE_DEEP);
            d(tag, i);
        }
    }

    public static void w(String tag, String log) {
        if (sDebug) {
            if (log.length() > LOG_LENGTH) {
                for (int i = 0; i < log.length(); i += LOG_LENGTH) {
                    if (i + LOG_LENGTH < log.length()) {
                        w(tag, log.substring(i, i + LOG_LENGTH));
                    } else {
                        w(tag, log.substring(i));
                    }
                }
            } else {
                for (LogImpl tpl : logImplList.values()) {
                    tpl.w(tag, log);
                }
            }
        }
    }

    public static void w(String log) {
        if (sDebug) {
            w(getTag(), log);
        }
    }

    public static void e(String tag, String log) {
        if (sDebug) {
            if (log.length() > LOG_LENGTH) {
                for (int i = 0; i < log.length(); i += LOG_LENGTH) {
                    if (i + LOG_LENGTH < log.length()) {
                        e(tag, log.substring(i, i + LOG_LENGTH));
                    } else {
                        e(tag, log.substring(i));
                    }
                }
            } else {
                for (LogImpl tpl : logImplList.values()) {
                    tpl.e(tag, log);
                }
            }
        }
    }

    public static void e(String log) {
        if (sDebug) {
            e(getTag(), log);
        }
    }

    public static final void setDebug(boolean debug) {
        sDebug = debug;
    }

    @Synchronized
    public static final void addLogImpl(@NotNull LogImpl impl) {
        if (!logImplList.containsKey(impl.getName())) {
            logImplList.put(impl.getName(), impl);
        }
    }

    @Synchronized
    public static final void removeImpl(@NotNull String name) {
        logImplList.remove(name);
    }

    public static final void closeLogcat() {
        removeImpl(LogImplForLogcat.INSTANCE.getName());
    }
}
