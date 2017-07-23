package com.phicomm.box.tvremote.beans;

import com.google.gson.annotations.SerializedName;

/**
 * Author: xufeng02.zhou
 * Date  : 2017-06-30
 * last modified: 2017-06-30
 */
public class Status {
    @SerializedName("name")
    private String mName;
    @SerializedName("sn")
    private String mSn;
    @SerializedName("online")
    private long onLine;
    @SerializedName("strength")
    private int mWifiStrength;

    public Status(String name, String sn, int strength, long online){
        this.mName = name;
        this.mSn = sn;
        mWifiStrength = strength;
        onLine = online;
    }
}

