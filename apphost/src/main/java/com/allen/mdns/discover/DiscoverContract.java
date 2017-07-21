package com.allen.mdns.discover;

import android.net.nsd.NsdServiceInfo;

import com.alllen.mylibrary.mvp.BasePresenter;
import com.alllen.mylibrary.mvp.BaseView;

import java.util.ArrayList;

/**
 * Author: allen.z
 * Date  : 2017-06-06
 * last modified: 2017-06-06
 */
public interface DiscoverContract {
    interface View extends BaseView<Presenter> {
        void updateInfo(String info);
        void clearInfo();
        void updateServices();
    }

    interface Presenter extends BasePresenter {

        void start();
        void stop();
        void startResearch();
        void stopResearch();
        ArrayList<NsdServiceInfo> getServices();
    }
}
