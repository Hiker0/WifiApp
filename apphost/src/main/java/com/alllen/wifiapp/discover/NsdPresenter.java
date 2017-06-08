package com.alllen.wifiapp.discover;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.ArrayList;

/**
 * Author: allen.z
 * Date  : 2017-06-06
 * last modified: 2017-06-06
 */
public class NsdPresenter implements DiscoverContract.Presenter {

    private static final  String SERVICE_TYPE = "_tv._tcp.";
    private NsdManager.RegistrationListener mRegistrationListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager mNsdManager;
    private String mServiceName;
    private DiscoverContract.View mView;
    private boolean isSearching = false;
    private ArrayList<NsdServiceInfo> mServices= null;

    NsdPresenter(DiscoverContract.View view, NsdManager manager) {
        mNsdManager = manager;
        mView = view;
        mServices = new ArrayList<NsdServiceInfo>();
        mView.setPresenter(this);
        initializeRegistrationListener();
        initDiscoverListener();
    }


    @Override
    public void start() {
        registerService("phicomm", SERVICE_TYPE, 10379);
    }

    @Override
    public void stop() {
        if (isSearching) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
        mNsdManager.unregisterService(mRegistrationListener);
    }

    @Override
    public void startResearch() {
        if (!isSearching) {
            startDiscover(SERVICE_TYPE);
        }
    }

    @Override
    public void stopResearch() {
        if (isSearching) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
    }

    @Override
    public ArrayList<NsdServiceInfo> getServices() {
        return mServices;
    }

    public void registerService(String name, String type, int port) {
        mView.updateInfo("registerService[name:" + name + ", type:" + type + ", port:" + port + "]");

        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(name);
        serviceInfo.setServiceType(type);
        serviceInfo.setPort(port);
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
//        mNsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
    }

    void startDiscover(String serviceType) {
        mView.updateInfo("startDiscover-> serviceType:" + serviceType);
        mNsdManager.discoverServices(
                serviceType, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void initDiscoverListener() {
        mView.updateInfo("initDiscoverListener");
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                mView.updateInfo("onStartDiscoveryFailed:" + errorCode);
                isSearching = false;
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                mView.updateInfo("onStopDiscoveryFailed:" + errorCode);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                mView.updateInfo("onDiscoveryStarted");
                isSearching = true;
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                mView.updateInfo("onDiscoveryStopped");
                isSearching = false;
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                mView.updateInfo("onServiceFound:" + serviceInfo);
                if (!serviceInfo.getServiceType().equals(SERVICE_TYPE)){
                    mView.updateInfo("Unknown service type: " + serviceInfo.getServiceType());
                } else if (serviceInfo.getServiceName().equals(mServiceName)){
                    mView.updateInfo("self service");
                } else {
                    resolveService(serviceInfo);
                }

            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                mView.updateInfo("onServiceLost:" + serviceInfo);
                NsdServiceInfo lost = null;
                for(NsdServiceInfo service :mServices){
                    if(service.getServiceName().equals(serviceInfo.getServiceName())){
                        lost = service;
                        break;
                    }
                }
                mServices.remove(lost);
                mView.updateServices();
            }
        };
    }

    public void initializeRegistrationListener() {
        mView.updateInfo("initializeRegistrationListener");
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = serviceInfo.getServiceName();
                mView.updateInfo("onServiceRegistered:" + serviceInfo);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
                mView.updateInfo("onRegistrationFailed:" + errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                mView.updateInfo("onServiceUnregistered:" + arg0);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
                mView.updateInfo("onUnregistrationFailed");
            }
        };
    }

    public void resolveService(NsdServiceInfo serviceInfo) {
        NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener(){

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                mView.updateInfo("onResolveFailed:" + errorCode);
                switch (errorCode) {
                    case NsdManager.FAILURE_ALREADY_ACTIVE:
                        resolveService(serviceInfo);
                        break;
                    case NsdManager.FAILURE_INTERNAL_ERROR:
                        break;
                    case NsdManager.FAILURE_MAX_LIMIT:
                        break;
                }
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                mView.updateInfo("onServiceResolved:" + serviceInfo);
                for(NsdServiceInfo oldInfo: mServices){
                    if(oldInfo.getServiceName().equals(serviceInfo.getServiceName())){
                        return;
                    }
                }
                mServices.add(serviceInfo);
                mView.updateServices();
            }
        };

        mNsdManager.resolveService(serviceInfo,resolveListener);
    }
}
