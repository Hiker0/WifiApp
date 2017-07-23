package com.phicomm.box.tvremote.beans;

import com.google.gson.annotations.SerializedName;

/**
 * Author: xufeng02.zhou
 * Date  : 2017-06-30
 * last modified: 2017-06-30
 */
public class ApplicationInfo {
    @SerializedName("name")
    private String mName;
    @SerializedName("package")
    private String mPackageName;
    @SerializedName("activity")
    private String mClassName;
    @SerializedName("appid")
    private String mAppid;

    public ApplicationInfo(String name, String packageName, String className, String appid){
        this.mName =name;
        this.mPackageName = packageName;
        this.mClassName = className;
        this.mAppid = appid;
    }

    public String getPackageName(){
        return mPackageName;
    }

    public String getClassName(){
        return mClassName;

    }
}
