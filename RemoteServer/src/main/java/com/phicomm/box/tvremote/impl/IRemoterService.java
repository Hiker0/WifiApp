package com.phicomm.box.tvremote.impl;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.phicomm.box.tvremote.beans.ApplicationInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * Author: xufeng02.zhou
 * Date  : 2017-06-30
 * last modified: 2017-06-30
 */
public interface IRemoterService {
    /**
     * @return : name of box
     */
    String getName();

    /**
     * @return the Strength of wifi currently connecting
     */
    int getWifiStrength();

    /**
     * @return the running time from box be opened
     */
    long getOnlineTime();

    /**
     * get the unique identification of box
     *
     * @return wifi mac,bt mac or other
     */
    String getSN();

    /**
     * @return all apps visible on launcher
     */
    ArrayList<ApplicationInfo> getAppList();

    Drawable getApplicationIcon(String packageName);

    /**
     * post a keyevent
     *
     * @param keycode   keycode
     * @param longClick
     */
    void onKeyEvent(int keycode, boolean longClick);

    /**
     * shut down
     */
    void onShutDown();

    /**
     * previous play
     */
    void onPre();

    /**
     * next play
     */
    void onNext();

    /**
     * start an application
     *
     * @param packageName package name
     * @param clasName    class name
     */
    void openApplication(String packageName, String clasName);

    /**
     * @param voiceSource voice file from remoter
     */
    void onVoiceCommand(File voiceSource);

    /**
     * @return jpg file
     */
    Bitmap onScreenShot();
}
