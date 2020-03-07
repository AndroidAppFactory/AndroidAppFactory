package com.bihe0832.android.lib.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;


import com.bihe0832.android.lib.utils.ConvertUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author code@bihe0832.com
 */

public class ConfigManager {
    private static final String TAG = "ConfigManager";
    private static volatile ConfigManager instance = null;

    //配置文件配置
    private Properties mLocalConfig = null;
    //内存中的配置
    private HashMap<String, String> mConfigInfoInCache = new HashMap<>();
    //云端的配置
    private SharedPreferences mConfigSP = null;

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
        if (ctx == null) {
            Log.w(TAG, "context is null");
            return;
        }
        try {
            loadFile(ctx, file, isDebug);
            mConfigSP = ctx.getSharedPreferences(file.toUpperCase(), Context.MODE_PRIVATE);
            if (isDebug) {
                Log.d(TAG, "================== config ================");
                Log.d(TAG, "local config:");
                Set<Map.Entry<Object, Object>> entrySet = mLocalConfig.entrySet();//返回的属性键值对实体
                for (Map.Entry<Object, Object> entry : entrySet) {
                    Log.d(TAG, entry.getKey() + "=" + entry.getValue());
                }
                Log.d(TAG, "local config:");
                Map<String, ?> allContent = mConfigSP.getAll();
                for (Map.Entry<String, ?> entry : allContent.entrySet()) {
                    Log.d(TAG, entry.getKey() + "=" + entry.getValue());
                }
                Log.d(TAG, "================== config ================");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR: config file");
        }
    }

    protected void loadFile(Context ctx, String file, boolean isDebug) {
        if (ctx == null) {
            Log.w(TAG, "context is null");
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
                Log.d(TAG, "================== config ================");
                Log.d(TAG, "local config:");
                Set<Map.Entry<Object, Object>> entrySet = mLocalConfig.entrySet();//返回的属性键值对实体
                for (Map.Entry<Object, Object> entry : entrySet) {
                    Log.d(TAG, entry.getKey() + "=" + entry.getValue());
                }
                Log.d(TAG, "================== config ================");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR: config file");
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
                Log.d(TAG, "key value is empty: " + key);
                return value;
            }
            return value.trim();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "readLocalConfig failed");
            return value;
        }
    }

    // 从云端推送保存到本地SharedPreferences的config，云端配置读取不使用默认值
    private String readCloudConfig(String key) {
        String value = null;
        try {
            if (null != mConfigSP && mConfigSP.contains(key)) {
                return mConfigSP.getString(key, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "readCloudConfig failed");
        }
        return value;
    }

    protected String readConfig(String key, String defValue) {
        String value = null;
        if (mConfigInfoInCache.containsKey(key)) {
            value = mConfigInfoInCache.get(key);
        }
        if (!TextUtils.isEmpty(value)) {
            Log.w(TAG, "use cache value:" + value);
            return value;
        }
        value = readCloudConfig(key);
        if (TextUtils.isEmpty(value)) {
            Log.w(TAG, "read local value");
            value = readLocalConfig(key);
        }
        Log.w(TAG, "read cloud value");
        if (value == null || value.length() == 0) {
            value = defValue;
        }
        mConfigInfoInCache.put(key, value);
        Log.w(TAG, "readConfig: key=" + key + ";value=" + value);
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

    protected boolean writeConfig(String key, String value, boolean saveToLocal) {
        Log.d(TAG, "writeConfig, key is :" + key + ";value is:" + value);
        try {
            if (TextUtils.isEmpty(value)) {
                Log.d(TAG, "writeConfig, value is null:" + key);
                value = "";
            }
            if (null != mConfigInfoInCache) {
                mConfigInfoInCache.put(key, value);
            }

            if (!saveToLocal) {
                return true;
            } else {
                if (null == mConfigSP) {
                    Log.d(TAG, "writeConfig, sp is null:");
                    return false;
                }
                SharedPreferences.Editor editor = mConfigSP.edit();
                editor.putString(key, value);
                boolean result = editor.commit();
                Log.d(TAG, "writeConfig result:" + result);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean writeConfig(String key, boolean value, boolean saveToLocal) {
        return writeConfig(key, value ? Config.VALUE_SWITCH_ON : Config.VALUE_SWITCH_OFF, saveToLocal);
    }

    protected boolean writeConfigs(Map<String, String> configs, boolean saveToLocal) {
        boolean result = true;
        try {
            Log.d(TAG, "writeConfig, " + configs);
            if (null == mConfigInfoInCache) {
                Log.d(TAG, "writeConfig ConfigInfoInCache is null");
                return false;
            }

            if (null == mConfigSP && saveToLocal) {
                Log.d(TAG, "writeConfig, sp is null:");
                return false;
            }
            SharedPreferences.Editor editor = null;
            if (configs != null && configs.size() > 0) {
                if (saveToLocal) {
                    editor = mConfigSP.edit();
                }
                Set<Map.Entry<String, String>> entrySet = configs.entrySet();
                for (Map.Entry<String, String> entry : entrySet) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                        if (saveToLocal && null != editor) {
                            editor.putString(key, value);
                        }
                        mConfigInfoInCache.put(key, value);
                    }
                }
                if (saveToLocal) {
                    result = editor.commit();
                    Log.d(TAG, "writeConfig result:" + result);
                    return result;
                } else {
                    return true;
                }
            } else {
                Log.d(TAG, "writeConfig, configs is null:");
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
            Log.d(TAG, "writeConfig, " + configs);
            if (null == mConfigInfoInCache) {
                Log.d(TAG, "writeConfig ConfigInfoInCache is null");
                return false;
            }

            if (null == mConfigSP && saveToLocal) {
                Log.d(TAG, "writeConfig, sp is null:");
                return false;
            }
            SharedPreferences.Editor editor = null;
            if (configs != null) {
                if (saveToLocal) {
                    editor = mConfigSP.edit();
                }
                Iterator<String> keys = configs.keys();
                while (keys.hasNext()) {
                    // 获得key
                    String key = keys.next();
                    String value = configs.getString(key);
                    Log.d(TAG,"key :" + key  + " ;value: "+ value);
                    if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                        if (saveToLocal && null != editor) {
                            editor.putString(key, value);
                        }
                        mConfigInfoInCache.put(key, value);
                    }
                }
                if (saveToLocal) {
                    result = editor.commit();
                    Log.d(TAG, "writeConfig result:" + result);
                    return result;
                } else {
                    return true;
                }
            } else {
                Log.d(TAG, "writeConfig, configs is null:");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
