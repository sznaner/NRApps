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


    private Context context;
    public List<String> installApps = new ArrayList<>();

    private NRApps() {
        init();
    }
    private static volatile NRApps mInstance = null;
    public static NRApps getInstance() {
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
        installApps.clear();
        List<ApplicationInfo> list = getInstallAppInfo(false);
        for (ApplicationInfo app : list){
            installApps.add(app.packageName);
        }
    }

    public static void init(Context context){
        NRApps.getInstance().context = context;
    }
    public static Boolean has(String packageName){
       return NRApps.getInstance().installApps.contains(packageName);
    }


    private List<ApplicationInfo> getInstallAppInfo(boolean hasSystemApp) {
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
