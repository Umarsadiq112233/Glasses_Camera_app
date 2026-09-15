package com.oudmon.ble.base.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * @author gs ,
 * @date swatch_device_text8/13,
 * <p>
 * "佛主保佑,
 * 永无bug"
 **/
public class AppUtil {
    public static boolean isBackground(Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                    .getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(context.getPackageName())) {
                    if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isApplicationBroughtToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }


    public static String getSystemInfo(){
        return Build.MODEL+"_" + Build.VERSION.SDK_INT+"_" + Build.VERSION.RELEASE;
    }

    /**
     * 获取版本号
     *
     * @param context 上下文
     *
     * @return 版本号
     */
    public static String getVersionName(Context context) {

        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "1.0.0.0";
    }

    /**
     * 获取package
     *
     * @param context 上下文
     *
     * @return 版本号
     */
    public static String getPackageName(Context context) {

        PackageManager pm  = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "com.swatchdevice";
    }


}
