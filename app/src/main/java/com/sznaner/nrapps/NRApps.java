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
        List<ApplicationInfo> apps = getInstallAppInfos(context, hasSystemApp,new ArrayList<String>());
        List<String> list = new ArrayList<>();
        try {
            for (ApplicationInfo app : apps) {
                if(app == null){
                    continue;
                }
                if(app.packageName==null){
                    continue;
                }
                list.add(app.packageName);
            }
        }catch (Exception e){
            Log.e("NRApps","getInstallApps_error");
        }
        return list;
    }


    public static List<ApplicationInfo> getInstallAppInfos(Context context, boolean hasSystemApp,List<String> excludePackageNames) {
        if(context == null){
            Log.e("NRApps","请先初始化:NRApps.init(context)");
            return new ArrayList<>();
        }
        List<ApplicationInfo> filterApplicationInfos = new ArrayList<>();
        try {
            PackageManager packageManager = context.getPackageManager();
            if(packageManager == null){
                Log.e("NRApps","packageManager == null");
                return filterApplicationInfos;
            }
            String selfPackageName = context.getPackageName();
            if(selfPackageName == null){
                Log.e("NRApps","selfPackageName == null");
                selfPackageName = "";
            }
            List<PackageInfo> packageInfos = packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
            if(packageInfos != null){
                for (PackageInfo packageInfo : packageInfos) {
                    if (packageInfo == null) {
                        continue;
                    }
                    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                    String packageName = packageInfo.packageName;
                    if(applicationInfo == null){
                        continue;
                    }
                    if(packageName == null){
                        continue;
                    }
                    if (packageName.equals(selfPackageName)) {//排除自身
                        continue;
                    }
                    if(excludePackageNames != null){
                        if(excludePackageNames.contains(packageName)){//排除指定app
                            continue;
                        }
                    }
                    if (!applicationInfo.enabled) {
                        continue;
                    }
                    Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
                    if (launchIntent == null) {
                        continue;
                    }

                    if (!hasSystemApp) {
                        if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
                            filterApplicationInfos.add(applicationInfo);
                        }
                    } else {
                        filterApplicationInfos.add(applicationInfo);
                    }
                }
            }
        }catch (Exception e){
            Log.e("NRApps","getInstallAppInfos_error");
        }
        return filterApplicationInfos;
    }



}
