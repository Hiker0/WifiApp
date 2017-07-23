package com.phicomm.box.tvremote.impl;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.phicomm.box.tvremote.beans.ApplicationInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * Author:
 * Date  : 2017-06-30
 * last modified: 2017-06-30
 */
public class RemoterServiceImpl implements IRemoterService {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getWifiStrength() {
        return 0;
    }

    @Override
    public long getOnlineTime() {
        return 0;
    }

    @Override
    public String getSN() {
        return null;
    }

    @Override
    public ArrayList<ApplicationInfo> getAppList() {
        return null;
    }

    @Override
    public Drawable getApplicationIcon(String packageName) {
        return null;
    }

    @Override
    public void onKeyEvent(int keycode, boolean longClick) {

    }

    @Override
    public void onShutDown() {

    }

    @Override
    public void onPre() {

    }

    @Override
    public void onNext() {

    }

    @Override
    public void openApplication(String packageName, String clasName) {

    }

    @Override
    public void onVoiceCommand(File voiceSource) {

    }

    @Override
    public Bitmap onScreenShot() {
        return null;
    }
}
