package com.bihe0832.android.lib.config;

import android.content.Context;
import android.text.TextUtils;

import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.utils.ConvertUtils;
import com.tencent.mmkv.MMKV;
import com.tencent.mmkv.MMKVLogLevel;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import kotlin.jvm.Synchronized;

/**
 * @author code@bihe0832.com
 */
class ConfigManager {
    private static final String TAG = "ConfigManager";
    private static volatile ConfigManager instance = null;

    private boolean hasInit = false;
    //配置文件配置
    private Properties mLocalConfig = null;
    //内存中的配置
    private ConcurrentHashMap<String, String> mConfigInfoInCache = new ConcurrentHashMap<>();

    private CopyOnWriteArrayList<OnConfigChangedListener> mConfigChangedListenerList = new CopyOnWriteArrayList<>();
    private MMKV mMMKVInstance = null;

    protected static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    protected void init(Context ctx, String file, boolean isDebug) {
        if (isDebug) {
            ZLog.setDebug(true);
        }
        if (ctx == null) {
            ZLog.w(TAG, "context is null");
            return;
        }
        hasInit = true;
        try {
            loadFile(ctx, file, isDebug);
        } catch (Exception e) {
            e.printStackTrace();
            ZLog.d(TAG, "ERROR: config file");
        }

        try {
            String rootDir = MMKV.initialize(ctx.getApplicationContext());
            mMMKVInstance = MMKV.mmkvWithID(ctx.getPackageName(), MMKV.MULTI_PROCESS_MODE);
            if (isDebug) {
                MMKV.setLogLevel(MMKVLogLevel.LevelInfo);
            } else {
                MMKV.setLogLevel(MMKVLogLevel.LevelNone);
            }
            ZLog.d(TAG, "mmkv root: " + rootDir);

        } catch (Exception e) {
            e.printStackTrace();
            ZLog.d(TAG, "ERROR: config file");
        }

    }

    protected void addOnConfigChangedListener(OnConfigChangedListener listener) {
        if (listener != null) {
            mConfigChangedListenerList.add(listener);
        }
    }

    protected void removeOnConfigChangedListener(OnConfigChangedListener listener) {
        if (mConfigChangedListenerList.contains(listener)) {
            mConfigChangedListenerList.remove(listener);
        }
    }

    protected boolean hasInit() {
        return hasInit;
    }

    protected void loadFile(Context ctx, String file, boolean isDebug) {
        if (ctx == null) {
            ZLog.w(TAG, "context is null");
            return;
        }
        InputStream inputStream = null;
        try {
            Properties localConfig = new Properties();
            if (!TextUtils.isEmpty(file)) {
                inputStream = ctx.getResources().getAssets().open(file);
                localConfig.load(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            }
            if (null == mLocalConfig) {
                mLocalConfig = localConfig;
            } else {
                Set<Map.Entry<Object, Object>> entrySet = localConfig.entrySet();//返回的属性键值对实体
                for (Map.Entry<Object, Object> entry : entrySet) {
                    mLocalConfig.setProperty((String) entry.getKey(), (String) entry.getValue());
                }
            }
            if (isDebug) {
                ZLog.d(TAG, "================== config ================");
                ZLog.d(TAG, "local config:");
                Set<Map.Entry<Object, Object>> entrySet = mLocalConfig.entrySet();//返回的属性键值对实体
                for (Map.Entry<Object, Object> entry : entrySet) {
                    ZLog.d(TAG, entry.getKey() + "=" + entry.getValue());
                }
                ZLog.d(TAG, "================== config ================");
            }
        } catch (IOException e) {
            e.printStackTrace();
            ZLog.d(TAG, "ERROR: config file");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 从缓存的Properties获取编译apk预置的config
    private String readLocalConfig(String key) {
        if (null == mLocalConfig || mLocalConfig.containsKey(key) != true) {
            return null;
        }

        String value = null;
        try {
            value = mLocalConfig.getProperty(key, null);
            if (value == null || value.length() == 0) {
                ZLog.d(TAG, "key value is empty: " + key);
                return value;
            }
            return value.trim();
        } catch (Exception e) {
            e.printStackTrace();
            ZLog.d(TAG, "readLocalConfig failed");
            return value;
        }
    }

    // 从云端推送保存到本地SharedPreferences的config，云端配置读取不使用默认值
    private String readCloudConfig(String key) {
        try {
            if (null != mMMKVInstance && mMMKVInstance.containsKey(key)) {
                return mMMKVInstance.decodeString(key, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ZLog.d(TAG, "readCloudConfig failed");
        }
        return null;
    }

    protected String readConfig(String key, String defValue) {
        String value = null;
        if (mConfigInfoInCache.containsKey(key)) {
            value = mConfigInfoInCache.get(key);
        }
        if (value != null) {
            ZLog.d(TAG, "readConfig: key=" + key + ";use cache value:" + value);
            return value;
        }
        value = readCloudConfig(key);
        if (value == null) {
            ZLog.d(TAG, "read local value");
            value = readLocalConfig(key);
        }
        if (value != null) {
            mConfigInfoInCache.put(key, value);
        }

        ZLog.d(TAG, "read cloud value");
        if (value == null) {
            value = defValue;
        }

        ZLog.d(TAG, "readConfig: key=" + key + ";value=" + value);
        return value;
    }

    protected boolean isSwitchEnabled(String switchKey, boolean defValue) {
        String value = readConfig(switchKey, String.valueOf(defValue));
        if (!TextUtils.isEmpty(value)) {
            if (Config.VALUE_SWITCH_ON.equalsIgnoreCase(value)) {
                return true;
            } else if (Config.VALUE_SWITCH_OFF.equalsIgnoreCase(value)) {
                return false;
            } else {
                return defValue;
            }
        } else {
            return defValue;
        }
    }

    protected int readConfig(String key, int defaultValue) {
        String configInterval = readConfig(key, String.valueOf(defaultValue));
        return ConvertUtils.parseInt(configInterval, defaultValue);
    }

    protected long readConfig(String key, long defaultValue) {
        String configInterval = readConfig(key, String.valueOf(defaultValue));
        return ConvertUtils.parseLong(configInterval, defaultValue);
    }

    protected float readConfig(String key, float defaultValue) {
        String configInterval = readConfig(key, String.valueOf(defaultValue));
        return ConvertUtils.parseFloat(configInterval, defaultValue);
    }

    protected double readConfig(String key, double defaultValue) {
        String configInterval = readConfig(key, String.valueOf(defaultValue));
        return ConvertUtils.parseDouble(configInterval, defaultValue);
    }

    protected boolean writeConfig(String key, boolean value, boolean saveToLocal) {
        return writeConfig(key, value ? Config.VALUE_SWITCH_ON : Config.VALUE_SWITCH_OFF, saveToLocal);
    }

    @Synchronized
    protected boolean writeConfig(String key, String value, boolean saveToLocal) {
        ZLog.d(TAG, "writeConfig, key is :" + key + ";value is:" + value);
        try {
            if (TextUtils.isEmpty(key)) {
                ZLog.d(TAG, "writeConfig, key is null:" + key);
                return false;
            }

            if (TextUtils.isEmpty(value)) {
                ZLog.d(TAG, "writeConfig, value is null:" + key);
                value = "";
            }

            if (null != mConfigInfoInCache) {
                String preData = "";
                if (mConfigInfoInCache.containsKey(key)) {
                    preData = mConfigInfoInCache.get(key);
                }
                mConfigInfoInCache.put(key, value);
                notifyValueChange(key, preData, value);
            }

            if (!saveToLocal) {
                return true;
            } else {
                if (null == mMMKVInstance) {
                    ZLog.d(TAG, "writeConfig, sp is null:");
                    return false;
                }
                boolean result = mMMKVInstance.encode(key, value);
                ZLog.d(TAG, "writeConfig result:" + result);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean writeConfigs(Map<String, String> configs, boolean saveToLocal) {
        boolean result = true;
        try {
            ZLog.d(TAG, "writeConfig, " + configs);
            if (null == mConfigInfoInCache) {
                ZLog.d(TAG, "writeConfig ConfigInfoInCache is null");
                return false;
            }

            if (null == mMMKVInstance && saveToLocal) {
                ZLog.d(TAG, "writeConfig, sp is null:");
                return false;
            }


            if (configs != null && configs.size() > 0) {
                for (Map.Entry<String, String> entry : configs.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    ZLog.d(TAG, "key :" + key + " ;value: " + value);
                    boolean tempResult = writeConfig(key, value, saveToLocal);
                    if (tempResult) {
                        mConfigInfoInCache.put(key, value);
                    } else {
                        result = false;
                    }
                }
                return result;
            } else {
                ZLog.d(TAG, "writeConfig, configs is null:");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean writeConfigs(JSONObject configs, boolean saveToLocal) {
        boolean result = true;
        try {
            ZLog.d(TAG, "writeConfig, " + configs);
            if (null == mConfigInfoInCache) {
                ZLog.d(TAG, "writeConfig ConfigInfoInCache is null");
                return false;
            }

            if (null == mMMKVInstance && saveToLocal) {
                ZLog.d(TAG, "writeConfig, sp is null:");
                return false;
            }
            if (configs != null) {
                Iterator<String> keys = configs.keys();
                while (keys.hasNext()) {
                    // 获得key
                    String key = keys.next();
                    String value = configs.getString(key);
                    ZLog.d(TAG, "key :" + key + " ;value: " + value);
                    boolean tempResult = writeConfig(key, value, saveToLocal);
                    if (tempResult) {
                        mConfigInfoInCache.put(key, value);
                    } else {
                        result = false;
                    }
                }
                return result;
            } else {
                ZLog.d(TAG, "writeConfig, configs is null:");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void notifyValueChange(String key, String preData, String newData) {
        if (mConfigChangedListenerList.size() > 0) {
            boolean isSame = preData.equals(newData);
            for (OnConfigChangedListener listener : mConfigChangedListenerList) {
                if (isSame) {
                    listener.onValueAgain(key, newData);
                } else {
                    listener.onValueChanged(key, newData);
                }
            }
        }
    }
}
