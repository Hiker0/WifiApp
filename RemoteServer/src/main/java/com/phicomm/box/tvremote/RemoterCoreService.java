package com.phicomm.box.tvremote;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.phicomm.box.tvremote.httpserver.RemoterServer;
import com.phicomm.box.tvremote.impl.IRemoterService;
import com.phicomm.box.tvremote.impl.Mockservice;
import com.phicomm.box.tvremote.util.LogUtil;

import java.io.IOException;

/**
 * Author: xufeng02.zhou
 * Date  : 2017-06-30
 * last modified: 2017-06-30
 */
public class RemoterCoreService extends Service {
    public static final String KEY_BSSID = "bssid";
    public static final String KEY_NAME = "name";

    private NsdManager mNsdManager;
    private NsdManager.RegistrationListener mRegistrationListener;
    private String mServiceName;
    private RemoterServer mRemoterServer;
    private IRemoterService mService;
    private WifiStateReceiver mWifiStateReceiver;
    private boolean mRegisted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d("onCreate");

        mService = new Mockservice(this);
        mWifiStateReceiver = new WifiStateReceiver();

        mRemoterServer = new RemoterServer(this, mService);
        mNsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
        initializeRegistrationListener();

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStateReceiver, filter);
    }

    void startRemoteServer() {
        if (mRegisted) {
            return;
        }

        try {
            mRemoterServer.start();
            registerService(mService.getName(), Configs.SERVICE_TYPE, mRemoterServer.getListeningPort(), mService.getSN());
            mRegisted = true;
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e("mRemoterServer fail");
        }
    }

    void stopRemoteServer() {
        if (mRegisted) {
            mNsdManager.unregisterService(mRegistrationListener);
            mRemoterServer.stop();
            mRegisted = false;
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.d("onDestroy");
        stopRemoteServer();
        unregisterReceiver(mWifiStateReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerService(String name, String type, int port) {
        registerService(name, type, port, "unkown");
    }

    private void registerService(String name, String type, int port, String bssid) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(name);
        serviceInfo.setServiceType(type);
        serviceInfo.setPort(port);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            serviceInfo.setAttribute(KEY_BSSID, bssid);
            serviceInfo.setAttribute(KEY_NAME, name);
        }
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

    }

    public void initializeRegistrationListener() {
        LogUtil.d("initializeRegistrationListener");
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = serviceInfo.getServiceName();
                LogUtil.d("onServiceRegistered:" + mServiceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
                LogUtil.d("onRegistrationFailed:" + errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                LogUtil.d("onServiceUnregistered:" + arg0.getServiceName());
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
                LogUtil.d("onUnregistrationFailed:" + errorCode);
            }
        };
    }

    public class WifiStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    LogUtil.d("wifi network disconnect");
                    stopRemoteServer();
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    LogUtil.d("wifi network connect");
                    startRemoteServer();
                }

            } else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
                    LogUtil.d("wifi disable");
                } else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    LogUtil.d("wifi enable");
                }
            }
        }
    }
}
