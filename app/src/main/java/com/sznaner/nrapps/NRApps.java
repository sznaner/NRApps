package com.sznaner.nrapps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.security.AllPermission;
import java.util.ArrayList;
import java.util.List;

public class NRApps {


    public List<String> installApps = new ArrayList<>();


    public static void init(Context context) {
        List<String> apps = getInstallApps(context, true);
        NRApps.getInstance().installApps = apps;
    }
    public static Boolean has(String packageName) {
        return NRApps.getInstance().installApps.contains(packageName);
    }
    public static List<String> apps() {
        return NRApps.getInstance().installApps;
    }


    //私有方法
    private NRApps() {
        init();
    }

    private static volatile NRApps mInstance = null;

    private static NRApps getInstance() {
        if (mInstance == null) {
            synchronized (NRApps.class) {
                if (mInstance == null) {
                    mInstance = new NRApps();
                }
            }
        }
        return mInstance;
    }

    private void init() {

    }

    public static List<String> getInstallApps(Context context, boolean hasSystemApp){
        List<ApplicationInfo> apps = getInstallAppInfos(context, hasSystemApp);
        List<String> list = new ArrayList<>();
        for (ApplicationInfo app : apps) {
            list.add(app.packageName);
        }
        return list;
    }
    private static List<ApplicationInfo> getInstallAppInfos(Context context, boolean hasSystemApp) {
        if(context == null){
            Log.e("NRApps","请先初始化:NRApps.init(context)");
            return new ArrayList<>();
        }
        PackageManager packageManager = context.getPackageManager();
        String selfPackageName = context.getPackageName();
        List<ApplicationInfo> filterApplicationInfos = new ArrayList<>();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        for (PackageInfo packageInfo : packageInfos) {
            if (packageInfo == null) {
                continue;
            }
            if (packageInfo.applicationInfo == null || packageInfo.packageName == null) {
                continue;
            }
            if (packageInfo.packageName.equals(selfPackageName)) {
                continue;
            }
            if (!packageInfo.applicationInfo.enabled) {
                continue;
            }
            Intent launchIntent = packageManager.getLaunchIntentForPackage(packageInfo.packageName);
            if (launchIntent == null) {
                continue;
            }
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if (!hasSystemApp) {
                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
                    filterApplicationInfos.add(applicationInfo);
                }
            } else {
                filterApplicationInfos.add(applicationInfo);
            }
        }
        return filterApplicationInfos;
    }



}
