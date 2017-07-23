package com.phicomm.box.tvremote.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.phicomm.box.tvremote.R;
import com.phicomm.box.tvremote.beans.ApplicationInfo;
import com.phicomm.box.tvremote.util.Utils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Author: xufeng02.zhou
 * Date  : 2017-06-30
 * last modified: 2017-06-30
 */
public class Mockservice implements IRemoterService {
    private static final String TAG = "Remoter";
    Context mContext;

    public Mockservice(Context context) {
        mContext = context;
    }

    @Override
    public String getName() {
        return "PhicomBox_1";
    }

    @Override
    public int getWifiStrength() {
        return 99;
    }

    @Override
    public long getOnlineTime() {
        return 15555;
    }

    @Override
    public String getSN() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int mIpAddress = wifiInfo.getIpAddress();

        String ip = Utils.intToIp(mIpAddress);
        return ip;
    }

    @Override
    public ArrayList<ApplicationInfo> getAppList() {
        ArrayList<ApplicationInfo> list = new ArrayList<ApplicationInfo>();
        ApplicationInfo info = new ApplicationInfo("launcher", "com.android,launcher", "LauncherActivity", "com.android,launcher");
        list.add(info);
        ApplicationInfo info1 = new ApplicationInfo("setting", "com.android,setting", "SettingsActivity", "com.android,setting");
        list.add(info1);
        return list;
    }

    @Override
    public Drawable getApplicationIcon(String packageName) {

        Drawable drawable = mContext.getResources().getDrawable(R.drawable.android);
        return drawable;
    }

    @Override
    public void onKeyEvent(int keycode, boolean longClick) {
        Log.d(TAG, "onKeyEvent keycode=" + keycode + ",longClick=" + longClick);
    }

    @Override
    public void onShutDown() {
        Log.d(TAG, "do onShutDown");
    }

    @Override
    public void onPre() {
        Log.d(TAG, "do onPre");
    }

    @Override
    public void onNext() {
        Log.d(TAG, "do onNext");
    }

    @Override
    public void openApplication(String packageName, String className) {
        Log.d(TAG, "openApplication:" + packageName + "," + className);
    }

    @Override
    public void onVoiceCommand(File voiceSource) {
        Log.d(TAG, "do onVoiceCommand");
    }

    @Override
    public Bitmap onScreenShot() {

        InputStream fis = this.getClass().getClassLoader().getResourceAsStream("assets/11.jpg");
        Bitmap bitmap = BitmapFactory.decodeStream(fis);
        return bitmap;
    }
}
