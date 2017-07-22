package com.allen.mdns.discover;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.allen.mdns.R;
import com.alllen.mylibrary.utils.ActivityUtils;

/**
 * Author: allen.z
 * Date  : 2017-06-05
 * last modified: 2017-06-05
 */
public class DiscoverActivity extends AppCompatActivity {

    NsdPresenter mDiscoverPresenter;
    WifiManager.MulticastLock mWifiLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initWindow();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mvp);

        String type = getIntent().getStringExtra("type");

        DiscoverFragment discoverFragment = (DiscoverFragment) getSupportFragmentManager().findFragmentById(R.id.layout_fragment_container);
        if (discoverFragment == null) {
            discoverFragment = DiscoverFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), discoverFragment, R.id.layout_fragment_container);
        }
        NsdManager nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        mDiscoverPresenter = new NsdPresenter(discoverFragment, nsdManager,type);

        WifiManager manager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
        mWifiLock = manager.createMulticastLock("test wifi");
        mWifiLock.acquire();
    }


    void initWindow() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        mWifiLock.release();
        super.onDestroy();
    }
}
