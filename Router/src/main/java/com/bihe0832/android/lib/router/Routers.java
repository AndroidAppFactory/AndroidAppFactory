package com.bihe0832.android.lib.router;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zixie code@bihe0832.com
 *         Created on 2019-07-19.
 *         Description: Description
 */
public class Routers {

    public static final String ROUTERS_KEY_RAW_URL = "com.bihe0832.router.URI";

    public static final String ROUTER_FLAG = "zixie_router_flag";
    public static final String ROUTERS_KEY_PARSE_SOURCE_KEY = "com.bihe0832.router.source";
    public static final String ROUTERS_VALUE_PARSE_SOURCE = "zixie_router_outer";
    public static final String HTTP_REQ_ENTITY_MERGE = "=";
    public static final String HTTP_REQ_ENTITY_JOIN = "&";

    private static void initIfNeed(String format) {
        if (!RouterMappingManager.getInstance().getRouterMapping().isEmpty() && RouterMappingManager.getInstance()
                .getRouterMapping().containsKey(format)) {
            return;
        }
        RouterMappingManager.getInstance().initMapping(format);
    }

    public static boolean open(Context context, String source, String url) {
        return open(context, Uri.parse(url), source, Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    public static boolean open(Activity activity, String source, String url) {
        return open(activity, Uri.parse(url), source, Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    public static boolean open(Fragment fragment, String source, String url) {
        return open(fragment, Uri.parse(url), source, Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    public static boolean open(Context context, String url, String source, int startFlag) {
        return open(context, Uri.parse(url), source, startFlag);
    }

    public static boolean open(Activity activity, String url, String source, int startFlag) {
        return open(activity, Uri.parse(url), source, startFlag);
    }

    public static boolean open(Fragment fragment, String url, String source, int startFlag) {
        return open(fragment, Uri.parse(url), source, startFlag);
    }

    public static boolean open(Context context, String url, String source, int startFlag,
            RouterContext.RouterCallback callback) {
        return open(context, Uri.parse(url), source, startFlag, callback);
    }

    public static boolean open(Activity activity, String url, String source, int startFlag,
            RouterContext.RouterCallback callback) {
        return open(activity, Uri.parse(url), source, startFlag, callback);
    }

    public static boolean open(Fragment fragment, String url, String source, int startFlag,
            RouterContext.RouterCallback callback) {
        return open(fragment, Uri.parse(url), source, startFlag, callback);
    }

    public static boolean open(Context context, Uri uri, String source, int startFlag) {
        return open(context, uri, source, startFlag, RouterContext.INSTANCE.getGlobalRouterCallback());
    }

    public static boolean open(Activity activity, Uri uri, String source, int startFlag) {
        return open(activity, uri, source, startFlag, RouterContext.INSTANCE.getGlobalRouterCallback());
    }

    public static boolean open(Fragment fragment, Uri uri, String source, int startFlag) {
        return open(fragment, uri, source, startFlag, RouterContext.INSTANCE.getGlobalRouterCallback());
    }

    public static boolean open(Context context, Uri uri, String source, int startFlag,
            RouterContext.RouterCallback callback) {
        return open(context, null, null, uri, source, -1, startFlag, callback);
    }

    public static boolean open(Activity activity, Uri uri, String source, int startFlag,
            RouterContext.RouterCallback callback) {
        return open(activity, activity, null, uri, source, -1, startFlag, callback);
    }

    public static boolean open(Fragment fragment, Uri uri, String source, int startFlag,
            RouterContext.RouterCallback callback) {
        return open(fragment.getContext(), null, fragment, uri, source, -1, startFlag, callback);
    }

    public static boolean openForResult(Activity activity, String url, String source, int requestCode, int startFlag) {
        return openForResult(activity, Uri.parse(url), source, requestCode, startFlag);
    }

    public static boolean openForResult(Fragment fragment, String url, String source, int requestCode, int startFlag) {
        return openForResult(fragment, Uri.parse(url), source, requestCode, startFlag);
    }

    public static boolean openForResult(Activity activity, String url, String source, int requestCode, int startFlag,
            RouterContext.RouterCallback callback) {
        return openForResult(activity, Uri.parse(url), source, requestCode, startFlag, callback);
    }

    public static boolean openForResult(Fragment fragment, String url, String source, int requestCode, int startFlag,
            RouterContext.RouterCallback callback) {
        return openForResult(fragment, Uri.parse(url), source, requestCode, startFlag, callback);
    }

    public static boolean openForResult(Activity activity, Uri uri, String source, int requestCode, int startFlag) {
        return openForResult(activity, uri, source, requestCode, startFlag,
                RouterContext.INSTANCE.getGlobalRouterCallback());
    }

    public static boolean openForResult(Fragment fragment, Uri uri, String source, int requestCode, int startFlag) {
        return openForResult(fragment, uri, source, requestCode, startFlag,
                RouterContext.INSTANCE.getGlobalRouterCallback());
    }

    public static boolean openForResult(Activity activity, Uri uri, String source, int requestCode, int startFlag,
            RouterContext.RouterCallback callback) {
        return open(activity, activity, null, uri, source, requestCode, startFlag, callback);
    }

    public static boolean openForResult(Fragment fragment, Uri uri, String source, int requestCode, int startFlag,
            RouterContext.RouterCallback callback) {
        return open(fragment.getContext(), null, fragment, uri, source, requestCode, startFlag, callback);
    }

    private static boolean open(Context context, Activity activity, Fragment fragment, Uri uri, String source,
            int requestCode, int startFlag, RouterContext.RouterCallback callback) {
        boolean success = false;
        if (callback != null) {
            if (callback.beforeOpen(context, uri, source)) {
                return false;
            }
        }

        try {
            success = doOpen(context, activity, fragment, uri, source, requestCode, startFlag);
        } catch (Throwable e) {
            e.printStackTrace();
            if (callback != null) {
                callback.error(context, uri, source, e);
            }
        }

        if (callback != null) {
            if (success) {
                callback.afterOpen(context, uri, source);
            } else {
                callback.notFound(context, uri, source);
            }
        }
        return success;
    }

    public static Intent resolve(Context context, String url) {
        return resolve(context, Uri.parse(url));
    }

    public static Intent resolve(Context context, Uri uri) {
        initIfNeed(uri.getHost());
        for (Map.Entry<String, Class<? extends Activity>> entry : RouterMappingManager.getInstance().getRouterMapping()
                .entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            if (entry.getKey().equalsIgnoreCase(uri.getHost()) && null != entry.getValue()) {
                Intent intent = new Intent(context, entry.getValue());
                intent.putExtras(parseExtras(uri));
                intent.putExtra(ROUTERS_KEY_RAW_URL, uri.toString());
                return intent;
            }
        }
        return null;
    }

    private static boolean doOpen(Context context, Activity activity, Fragment fragment, Uri uri, String source,
            int requestCode, int startFlag) {
        initIfNeed(uri.getHost());
        if (RouterMappingManager.getInstance().getRouterMapping().containsKey(uri.getHost())) {
            Class<? extends Activity> activityClass = RouterMappingManager.getInstance().getRouterMapping()
                    .get(uri.getHost());
            if (null != activityClass) {
                try {
                    Intent intent = new Intent(context, activityClass);
                    intent.putExtras(parseExtras(uri));
                    intent.putExtra(ROUTERS_KEY_RAW_URL, uri.toString());
                    intent.putExtra(ROUTERS_KEY_PARSE_SOURCE_KEY, source);
                    if (startFlag > 0) {
                        intent.addFlags(startFlag);
                    }
                    if (requestCode >= 0) {
                        if (null != fragment) {
                            fragment.startActivityForResult(intent, requestCode);
                        } else {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivityForResult(intent, requestCode);
                        }
                    } else {
                        if (null != fragment) {
                            context.startActivity(intent);
                        } else {
                            context.startActivity(intent);
                        }

                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        } else {
            try {
                Intent intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                intent.setSelector(null);
                System.out.println("jumpToOtherApp url:" + uri.toString() + ",intent:" + intent.toString());
                if (startFlag > 0) {
                    intent.addFlags(startFlag);
                }

                if (requestCode >= 0) {
                    if (null != fragment) {
                        fragment.startActivityForResult(intent, requestCode);
                    } else {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivityForResult(intent, requestCode);
                    }
                } else {
                    if (null != fragment) {
                        context.startActivity(intent);
                    } else {
                        context.startActivity(intent);
                    }

                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    public static Bundle parseExtras(Uri uri) {
        Bundle bundle = new Bundle();

        String query = uri.getEncodedQuery();
        if (TextUtils.isEmpty(query)) {
            return bundle;
        }
        int start = 0;
        do {
            int next = query.indexOf(HTTP_REQ_ENTITY_JOIN, start);
            int end = (next == -1) ? query.length() : next;
            int separator = query.indexOf(HTTP_REQ_ENTITY_MERGE, start);
            if (separator > end || separator == -1) {
                separator = end;
            }
            String name = query.substring(start, separator);
            if (separator - start == name.length() && query.regionMatches(start, name, 0, name.length())) {
                if (separator == end) {
                    bundle.putString(name, "");
                } else {
                    bundle.putString(name, query.substring(separator + 1, end));
                }
                start = end + 1;
            }
        } while (start < query.length());

        return bundle;
    }


    public static List<Class<? extends Activity>> getMainActivityList() {
        return RouterMappingManager.getInstance().getActivityIsMain();
    }

    public static ConcurrentHashMap<String, Class<? extends Activity>> getRouterMappings() {
        return RouterMappingManager.getInstance().getRouterMapping();
    }
}
