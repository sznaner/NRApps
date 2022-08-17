package com.sznaner.nrapps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NRApps {

    private static String TAG = "NRApps";
    private Map<String,PackageInfo> cachePackageInfoMaps = new HashMap<>();
    private String signSHA256 = "";
    private Context context;

    //私有方法
    private NRApps() {
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


    public static void init(Context context) {
        Log.d(TAG,"init(context)");
        NRApps.getInstance().context = context;
    }

    public static Boolean has(String packageName){
        return has(packageName,true);
    }
    public static Boolean has(String packageName,Boolean useCache) {
        if(packageName == null) return false;
        Boolean res = false;
        if(useCache){
            try {
                PackageInfo packageInfo_cache = NRApps.getInstance().cachePackageInfoMaps.get(packageName);
                if(packageInfo_cache != null){
                    res = true;
                }else {
                    PackageInfo packageInfo = getPackageInfo(packageName);
                    if(packageInfo != null) res = true;
                }
            }catch (Exception e){}
        }else {
            PackageInfo packageInfo = getPackageInfo(packageName);
            if(packageInfo != null) res = true;
        }
        return res;
    }

    public static List<ApplicationInfo> getInstallAppInfos(Boolean hasSystemApp,List<String> excludePackageNames) {
        Log.d(TAG,"getInstallAppInfos_start");
        Context context = NRApps.getInstance().context;
        if(context == null){
            Log.e(TAG,"请先NRApps.init(Context context)");
            return new ArrayList<>();
        }
        List<ApplicationInfo> res_Infos = new ArrayList<>();
        try {
            PackageManager packageManager = context.getPackageManager();
            if(packageManager == null){
                Log.e(TAG,"packageManager == null");
                return res_Infos;
            }
            String selfPackageName = context.getPackageName();
            if(selfPackageName == null){
                Log.e(TAG,"selfPackageName == null");
                selfPackageName = "";
            }

            //增加同步块,防止Package manager has died问题
            synchronized (NRApps.class) {
                List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
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
                                res_Infos.add(applicationInfo);
                            }
                        } else {
                            res_Infos.add(applicationInfo);
                        }
                    }
                }

                Log.i(TAG,"getInstallAppInfos_ok");
            }

        }catch (Exception e){
            Log.e(TAG,"getInstallAppInfos_error");
        }
        Log.i(TAG,"getInstallAppInfos_end");
        return res_Infos;
    }


    public static  Boolean isSPC(){
        //签名
        if(getSignSHA256().equals("2D:37:0C:21:F5:DF:D5:53:D2:A7:96:31:4B:70:92:5F:B3:8A:DE:EF:90:86:4C:92:0B:BB:BB:12:88:7D:35:22")){
            Log.d(TAG,"isSPC");
            return true;
        }
        return false;
    }

    public static Boolean isYHK(){
        //签名
        if(getSignSHA256().equals("FA:F0:03:FF:83:92:C0:2B:AE:3D:C4:CA:48:DA:10:7D:6F:89:7C:A4:5F:98:FB:79:63:93:03:22:51:57:FB:3D")){
            Log.d(TAG,"isYHK");
            return true;
        }
        return false;
    }

    //获取签名的SHA256
    public static String getSignSHA256(){
        if(NRApps.getInstance().signSHA256 != null && NRApps.getInstance().signSHA256.length() > 0){
            return NRApps.getInstance().signSHA256;
        }
        String res =  "";
        Context context = NRApps.getInstance().context;
        if(context == null){
            Log.e(TAG,"请先NRApps.init(Context context)");
            return res;
        }
        try {
            PackageInfo info = getPackageInfo(context.getPackageName());
            if(info != null){
                byte[] cert = info.signatures[0].toByteArray();
                MessageDigest md = MessageDigest.getInstance("SHA256");
                byte[] publicKey = md.digest(cert);
                StringBuffer hexString = new StringBuffer();
                for (int i = 0; i < publicKey.length; i++) {
                    String appendString = Integer.toHexString(0xFF & publicKey[i])
                            .toUpperCase(Locale.US);
                    if (appendString.length() == 1)
                        hexString.append("0");
                    hexString.append(appendString);
                    hexString.append(":");
                }
                String result =hexString.toString();
                String  res_temp = result.substring(0, result.length()-1);
                if(res_temp != null){
                    res = res_temp;
                    NRApps.getInstance().signSHA256 = res;//缓存结果
                }
            }
        }catch (Exception e){}
        return res;
    }

    //获取当前app version name(即版本号)
    public static String getAppVersion(String packageName) {
        String appVersionName = "0.0.1";//防止获取未安装app版本号,造成异常
        PackageInfo packageInfo = getPackageInfo(packageName);
        if(packageInfo != null){
            appVersionName = packageInfo.versionName;
        }
        return appVersionName;
    }
    public static PackageInfo getPackageInfo(String packageName){
        Log.d(TAG,"getPackageInfo_start");
        Context context = NRApps.getInstance().context;
        if(context == null){
            Log.e(TAG,"请先NRApps.init(Context context)");
            return null;
        }
        if(packageName == null) return null;
        PackageInfo info = null;
        try {//增加同步块,防止Package manager has died问题
            synchronized (NRApps.class) {
                PackageManager packageManager = context.getPackageManager();
                if(packageManager != null){
                    info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);//类型PackageManager.GET_SIGNATURES,不要改,获取签名要用到
                    if(info != null){
                        NRApps.getInstance().cachePackageInfoMaps.put(packageName,info);
                    }
                }
                Log.d(TAG,"getPackageInfo_ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG,"getPackageInfo_end");
        return info;
    }


}
