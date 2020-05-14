package android.os;


public class SystemProperties
{
  public static final int PROP_NAME_MAX = 31;
  public static final int PROP_VALUE_MAX = 255;

  public static native String native_get(String paramString);

  public static native String native_get(String paramString1, String paramString2);

  public static native int native_get_int(String paramString, int paramInt);

  public static native long native_get_long(String paramString, long paramLong);

  public static native boolean native_get_boolean(String paramString, boolean paramBoolean);

  public static native void native_set(String paramString1, String paramString2);

  public static String get(String key)
  {
    if (key.length() > 31) {
      throw new IllegalArgumentException("key.length > 31");
    }
    return native_get(key);
  }

  public static String get(String key, String def) {
    if (key.length() > 31) {
      throw new IllegalArgumentException("key.length > 31");
    }
    return native_get(key, def);
  }

  public static int getInt(String key, int def) 
  {
    if (key.length() > 31) {
      throw new IllegalArgumentException("key.length > 31");
    }
    return native_get_int(key, def);
  }

  public static long getLong(String key, long def) {
    if (key.length() > 31) {
      throw new IllegalArgumentException("key.length > 31");
    }
    return native_get_long(key, def);
  }

  public static boolean getBoolean(String key, boolean def) {
    if (key.length() > 31) {
      throw new IllegalArgumentException("key.length > 31");
    }
    return native_get_boolean(key, def);
  }

  public static void set(String key, String val) {
    if (key.length() > 31) {
      throw new IllegalArgumentException("key.length > 31");
    }
    if ((val != null) && (val.length() > 255)) {
      throw new IllegalArgumentException("val.length > 255");
    }
    native_set(key, val);
  }
}