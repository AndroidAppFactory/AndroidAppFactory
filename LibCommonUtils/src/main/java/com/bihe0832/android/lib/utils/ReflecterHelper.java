package com.bihe0832.android.lib.utils;

import com.bihe0832.android.lib.log.ZLog;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射工具类
 * 
 * @author zixie code@bihe0832.com
 * Created on 2025-01-15.
 * 
 * Description: 提供常用的反射操作功能，包括：
 * - 类的动态加载和实例化
 * - 字段的读取和设置（支持静态和实例字段）
 * - 方法的动态调用（支持静态和实例方法）
 * - 线程安全的类状态管理
 * 
 * 注意：为了提高效率，采用了ThreadLocal的工作方式，确保线程安全
 */
public final class ReflecterHelper {

    private static final String TAG = "ReflecterHelper";

    /**
     * 使用ThreadLocal存储当前类，确保线程安全
     */
    private static final ThreadLocal<Class<?>> mCurrentClass = new ThreadLocal<>();

    /**
     * 设置当前线程要操作的类
     *
     * @param name 类的完整路径（如：com.example.MyClass）
     * @return 是否设置成功，true表示类加载成功，false表示类未找到
     */
    public final static boolean setClass(String name) {
        if (name == null || name.isEmpty()) {
            ZLog.e(TAG, "setClass failed: class name is null or empty");
            return false;
        }
        
        Class<?> tmpClass = null;
        try {
            tmpClass = Class.forName(name);
        } catch (ClassNotFoundException e) {
            ZLog.e(TAG, "setClass failed: class not found - " + name + ", error: " + e.getMessage());
        }

        mCurrentClass.set(tmpClass);
        return tmpClass != null;
    }

    /**
     * 获取静态int字段的值
     *
     * @param name 字段名称
     * @param defvalue 默认值（获取失败时返回）
     * @return 字段的int值，失败时返回defvalue
     */
    public final static int getStaticIntValue(String name, int defvalue) {
        int result = defvalue;
        Field field = getField(name);

        if (field != null) {
            try {
                result = field.getInt(null);
            } catch (IllegalArgumentException e) {
                ZLog.e(TAG, "getStaticIntValue failed: illegal argument - " + name + ", error: " + e.getMessage());
            } catch (IllegalAccessException e) {
                ZLog.e(TAG, "getStaticIntValue failed: illegal access - " + name + ", error: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * 获取实例对象的int字段值
     *
     * @param owner 对象实例
     * @param name 字段名称
     * @param defvalue 默认值（获取失败时返回）
     * @return 字段的int值，失败时返回defvalue
     */
    public final static int getIntValue(Object owner, String name, int defvalue) {
        if (owner == null) {
            ZLog.e(TAG, "getIntValue failed: owner is null");
            return defvalue;
        }
        
        int result = defvalue;
        setClass(owner.getClass().getName());
        Field field = getField(name);

        if (field != null) {
            try {
                result = field.getInt(owner);
            } catch (IllegalArgumentException e) {
                ZLog.e(TAG, "getIntValue failed: illegal argument - " + name + ", error: " + e.getMessage());
            } catch (IllegalAccessException e) {
                ZLog.e(TAG, "getIntValue failed: illegal access - " + name + ", error: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * 获取当前类的字段对象
     *
     * @param name 字段名称
     * @return Field对象，失败时返回null
     */
    public final static Field getField(String name) {
        Class<?> currentClass = mCurrentClass.get();
        if (currentClass == null) {
            ZLog.e(TAG, "getField failed: current class is null, please call setClass first");
            return null;
        }
        
        Field field = null;
        try {
            field = currentClass.getDeclaredField(name);
            field.setAccessible(true);
        } catch (SecurityException e) {
            ZLog.e(TAG, "getField failed: security exception - " + name + ", error: " + e.getMessage());
        } catch (NoSuchFieldException e) {
            ZLog.e(TAG, "getField failed: no such field - " + name + ", error: " + e.getMessage());
        }

        return field;
    }

    /**
     * 获取某个类的静态属性值
     *
     * @param className 类的完整路径
     * @param fieldName 字段名称
     * @return 字段值，失败时返回null
     */
    public static Object getStaticProperty(String className, String fieldName) {
        if (!setClass(className)) {
            return null;
        }
        
        Field field = getField(fieldName);
        Object result = null;

        if (field != null) {
            try {
                result = field.get(null);
            } catch (IllegalArgumentException e) {
                ZLog.e(TAG, "getStaticProperty failed: illegal argument - " + fieldName + ", error: " + e.getMessage());
            } catch (IllegalAccessException e) {
                ZLog.e(TAG, "getStaticProperty failed: illegal access - " + fieldName + ", error: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 设置某个类的静态属性值
     *
     * @param className 类的完整路径
     * @param fieldName 字段名称
     * @param value 要设置的值
     */
    public static void setStaticProperty(String className, String fieldName, Object value) {
        if (!setClass(className)) {
            return;
        }
        
        Field field = getField(fieldName);

        if (field != null) {
            try {
                field.set(null, value);
            } catch (IllegalArgumentException e) {
                ZLog.e(TAG, "setStaticProperty failed: illegal argument - " + fieldName + ", error: " + e.getMessage());
            } catch (IllegalAccessException e) {
                ZLog.e(TAG, "setStaticProperty failed: illegal access - " + fieldName + ", error: " + e.getMessage());
            } catch (Exception e) {
                ZLog.e(TAG, "setStaticProperty failed: " + fieldName + ", error: " + e.getMessage());
            }
        }
    }

    /**
     * 实例化对象（带参数构造函数）
     * 注意：只支持没有null对象和只有String对象是null的情况
     *
     * @param className 类的完整路径
     * @param args 构造函数参数数组
     * @return 实例化的对象
     * @throws Exception 实例化失败时抛出异常
     */
    static public Object newInstance(String className, Object[] args) throws Exception {
        Class<?> newoneClass = Class.forName(className);
        Constructor<?> cons = newoneClass.getDeclaredConstructor(getArgsClasses(args));
        cons.setAccessible(true);
        return cons.newInstance(args);
    }

    /**
     * 实例化对象（无参构造函数）
     *
     * @param className 类的完整路径
     * @return 实例化的对象
     * @throws Exception 实例化失败时抛出异常
     */
    static public Object newInstance(String className) throws Exception {
        return newInstance(className, null);
    }

    /**
     * 执行某对象的方法（带参数）
     * 注意：只支持没有null对象和只有String对象是null的情况
     *
     * @param owner 对象实例
     * @param methodName 方法名称
     * @param args 方法参数数组
     * @return 方法返回值
     * @throws Exception 方法调用失败时抛出异常
     */
    static public Object invokeMethod(Object owner, String methodName, Object[] args)
            throws Exception {
        return invokeMethod(owner, methodName, getArgsClasses(args), args);
    }

    /**
     * 执行某对象的无参方法
     *
     * @param owner 对象实例
     * @param methodName 方法名称
     * @return 方法返回值
     * @throws Exception 方法调用失败时抛出异常
     */
    static public Object invokeMethod(Object owner, String methodName) throws Exception {
        return invokeMethod(owner, methodName, null);
    }

    /**
     * 执行某对象的方法（指定参数类型）
     * 如果当前类找不到方法，会尝试在父类中查找
     *
     * @param owner 对象实例
     * @param methodName 方法名称
     * @param argsClass 参数类型数组
     * @param args 方法参数数组
     * @return 方法返回值
     * @throws Exception 方法调用失败时抛出异常
     */
    static public Object invokeMethod(Object owner, String methodName, Class<?> argsClass[],
            Object[] args) throws Exception {
        Class<?> ownerClass = owner.getClass();
        Method method = null;
        try {
            method = ownerClass.getDeclaredMethod(methodName, argsClass);
        } catch (Exception e) {
            if (method == null && ownerClass.getSuperclass() != null) {
                method = ownerClass.getSuperclass().getDeclaredMethod(methodName, argsClass);
            }
            if (method == null) {
                throw e;
            }
        }

        method.setAccessible(true);
        return method.invoke(owner, args);
    }

    /**
     * 获取某对象的属性值
     *
     * @param owner 对象实例
     * @param fieldName 字段名称
     * @return 字段值
     * @throws Exception 获取失败时抛出异常
     */
    public static Object getProperty(Object owner, String fieldName) throws Exception {
        Class<?> ownerClass = owner.getClass();

        Field field = ownerClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object property = field.get(owner);

        return property;
    }

    /**
     * 设置某对象的属性值
     *
     * @param owner 对象实例
     * @param fieldName 字段名称
     * @param value 要设置的值
     * @throws Exception 设置失败时抛出异常
     */
    public static void setProperty(Object owner, String fieldName, Object value)
            throws Exception {
        Class<?> ownerClass = owner.getClass();
        Field field = ownerClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(owner, value);
    }

    /**
     * 实例化对象（指定参数类型）
     *
     * @param className 类的完整路径
     * @param args 构造函数参数数组
     * @param argsClass 参数类型数组
     * @return 实例化的对象
     * @throws Exception 实例化失败时抛出异常
     */
    public static Object newInstance(String className, Object args[], Class<?> argsClass[])
            throws Exception {
        Class<?> newoneClass = Class.forName(className);
        Constructor<?> cons = newoneClass.getDeclaredConstructor(argsClass);
        cons.setAccessible(true);
        return cons.newInstance(args);
    }

    /**
     * 调用静态方法（指定参数类型）
     * 如果当前类找不到方法，会尝试在父类中查找
     *
     * @param className 类的完整路径
     * @param methodName 方法名称
     * @param args 方法参数数组
     * @param argsClass 参数类型数组
     * @return 方法返回值
     * @throws Exception 方法调用失败时抛出异常
     */
    public static Object invokeStaticMethod(String className, String methodName, Object args[],
            Class<?> argsClass[]) throws Exception {
        Class<?> cls = Class.forName(className);
        Method staticMethod = null;
        Object ret = null;
        try {
            staticMethod = cls.getDeclaredMethod(methodName, argsClass);
        } catch (Exception e) {
            if (staticMethod == null && cls.getSuperclass() != null) {
                staticMethod = cls.getSuperclass().getDeclaredMethod(methodName, argsClass);
            }
            if (staticMethod == null) {
                throw e;
            }
        }
        staticMethod.setAccessible(true);
        ret = staticMethod.invoke(cls, args);
        return ret;
    }

    /**
     * 调用静态方法（带参数）
     * 注意：只支持没有null对象和只有String对象是null的情况
     *
     * @param className 类的完整路径
     * @param methodName 方法名称
     * @param args 方法参数数组
     * @return 方法返回值
     * @throws Exception 方法调用失败时抛出异常
     */
    public static Object invokeStaticMethod(String className, String methodName, Object args[])
            throws Exception {
        return invokeStaticMethod(className, methodName, args, getArgsClasses(args));
    }

    /**
     * 调用静态方法（无参）
     *
     * @param className 类的完整路径
     * @param methodName 方法名称
     * @return 方法返回值
     * @throws Exception 方法调用失败时抛出异常
     */
    public static Object invokeStaticMethod(String className, String methodName) throws Exception {
        return invokeStaticMethod(className, methodName, null);
    }

    /**
     * 获取参数数组的类型数组
     * 自动处理包装类型到基本类型的转换（Integer->int, Boolean->boolean, Long->long）
     * 对于null参数，默认使用String.class
     *
     * @param args 参数数组
     * @return 参数类型数组
     */
    public static Class<?>[] getArgsClasses(Object[] args) {
        Class<?>[] argsClass = null;
        if (args != null) {
            argsClass = new Class<?>[args.length];

            for (int i = 0, j = args.length; i < j; i++) {
                if (args[i] != null) {
                    argsClass[i] = args[i].getClass();
                } else {
                    argsClass[i] = String.class;
                }
                // 包装类型转换为基本类型
                if (argsClass[i] == Integer.class) {
                    argsClass[i] = int.class;
                } else if (argsClass[i] == Boolean.class) {
                    argsClass[i] = boolean.class;
                } else if (argsClass[i] == Long.class) {
                    argsClass[i] = long.class;
                }

            }
        }
        return argsClass;
    }
}