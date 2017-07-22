package com.allen.mdns.discover;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import com.allen.mdns.Welcome;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

/**
 * Author: allen.z
 * Date  : 2017-06-06
 * last modified: 2017-06-06
 */
public class JmdnsPresenter implements DiscoverContract.Presenter {

    private ServiceListener mDiscoveryListener;
    private JmDNS mJmdns;
    private String mServiceName;
    private DiscoverContract.View mView;
    private boolean isSearching = false;
    private ArrayList<NsdServiceInfo> mServices= null;
    private ExecutorService mExecutor;
    private InetAddress mIpAddress;
    private  String mType = null;

    JmdnsPresenter(DiscoverContract.View view, String ip) {
        this(view,ip, Welcome.DEFAULT_TYPE);
    }
    JmdnsPresenter(DiscoverContract.View view, String ip, String type) {

        mView = view;
        mServices = new ArrayList<NsdServiceInfo>();
        mView.setPresenter(this);
        if(type != null){
            mType = type;
        }else{
            mType = Welcome.DEFAULT_TYPE;
        }
        mType = mType+"local.";

        mExecutor = Executors.newSingleThreadExecutor();
        try {
            mIpAddress = InetAddress.getByName(ip);
            mView.updateInfo("host:"+ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            mView.updateInfo("unkown ip:"+ip);
        }
        initDiscoverListener();

    }


    @Override
    public void start() {

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mJmdns = JmDNS.create(mIpAddress);
                    mView.updateInfo("JmDNS create:"+mIpAddress.getHostAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                    mView.updateInfo("JmDNS create fail");
                }

                registerService("phicomm", mType, 10379);

            }
        });
    }

    @Override
    public void stop() {
        if (isSearching) {
            mJmdns.removeServiceListener(mType, mDiscoveryListener);
        }
        mJmdns.unregisterAllServices();
        mExecutor.shutdown();
    }

    @Override
    public void startResearch() {
        if (!isSearching) {
            startDiscover(mType);
        }
    }

    @Override
    public void stopResearch() {
        if (isSearching) {
            mJmdns.removeServiceListener(mType, mDiscoveryListener);
        }
    }

    @Override
    public ArrayList<NsdServiceInfo> getServices() {
        return mServices;
    }

    public void registerService(String name, String type, int port) {
        mView.updateInfo("registerService[name:" + name + ", type:" + type + ", port:" + port + "]");
        // Register a service
        ServiceInfo serviceInfo = ServiceInfo.create(type, name, port, "path=index.html");
        try {
            mJmdns.registerService(serviceInfo);
            ServiceInfo[] services = mJmdns.list(serviceInfo.getType());
            if(services.length > 0) {
                mServiceName = services[0].getName();
                mView.updateInfo("registerService ok:"+mServiceName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            mView.updateInfo("registerService fail" );
        }
    }

    void startDiscover(String serviceType) {
        mView.updateInfo("startDiscover-> serviceType:" + serviceType);
        mJmdns.addServiceListener(mType, mDiscoveryListener);
    }

    public void initDiscoverListener() {
        mView.updateInfo("initDiscoverListener");
        mDiscoveryListener = new ServiceListener () {

            @Override
            public void serviceAdded(ServiceEvent event) {
                mView.updateInfo("serviceAdded:[name:" + event.getName()+"  type:"+event.getType()+"]");
                if(event.getName().equals(mServiceName)){
                    mView.updateInfo("self event");
                    return;
                }
                mJmdns.requestServiceInfo(event.getType(), event.getName(), 1);
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {
                mView.updateInfo("serviceAdded:" + event);
                NsdServiceInfo lost = null;
                for(NsdServiceInfo service :mServices){
                    if(service.getServiceName().equals(event.getName())){
                        lost = service;
                        break;
                    }
                }
                mServices.remove(lost);
                mView.updateServices();
            }

            @Override
            public void serviceResolved(ServiceEvent event) {
                mView.updateInfo("serviceResolved:" + event);

                for(NsdServiceInfo oldInfo: mServices){
                    if(oldInfo.getServiceName().equals(event.getName())){
                        return;
                    }
                }
                NsdServiceInfo info = new NsdServiceInfo();
                ServiceInfo sinfo = event.getInfo();
                info.setServiceName(sinfo.getName());
                info.setServiceType(sinfo.getType());

                info.setHost(sinfo.getInetAddresses()[0]);
                info.setPort(sinfo.getPort());
                mServices.add(info);
                mView.updateServices();
            }
        };
    }

}
