package com.phicomm.box.tvremote.beans;

import java.util.ArrayList;

/**
 * Author: allen.z
 * Date  : 2017-07-03
 * last modified: 2017-07-03
 */
public class ApplicationList {
    private int count;
    ArrayList<ApplicationInfo> apps;

    public ApplicationList(int length, ArrayList<ApplicationInfo> list){
        count = length;
        apps = list;
    }
}
