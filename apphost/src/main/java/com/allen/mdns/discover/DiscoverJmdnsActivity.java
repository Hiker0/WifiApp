package com.allen.mdns.discover;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.alllen.mylibrary.utils.ActivityUtils;
import com.allen.mdns.R;

/**
 * Author: allen.z
 * Date  : 2017-06-05
 * last modified: 2017-06-05
 */
public class DiscoverJmdnsActivity extends AppCompatActivity {

    JmdnsPresenter mDiscoverPresenter;
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

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int address = wifiInfo.getIpAddress();

        String ip = intToIp(address);
        mDiscoverPresenter = new JmdnsPresenter(discoverFragment, ip, type);

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

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    @Override
    protected void onDestroy() {
        mWifiLock.release();
        super.onDestroy();
    }
}
