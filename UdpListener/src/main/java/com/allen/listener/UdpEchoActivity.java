package com.allen.listener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.allen.listener.R;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

;
import static android.view.View.*;
import static android.widget.Toast.LENGTH_SHORT;

public class UdpEchoActivity extends Activity {
    static final String TAG = "ActivityListener";

    private String mName = Build.DEVICE;
    private String mSN = Build.SERIAL;

    private Context mContext;
    private TextView mInfoView;
    private TextView mStateView;
    private EditText  mPortView;
    private Button mLauncherButton;

    private boolean mListening = false;
    private String mTarAddress = null;
    private int mTarPort = 0;
    private StringBuffer mInfo = null;
    private int mCount = 0;

    static final int MESSAGE_INIT = 1;
    static final int MESSAGE_LISTEN = 2;
    static final int MESSAGE_SEND_IP = 3;
    static final int MESSAGE_REQUEST_IP = 4;
    static final int MESSAGE_UPDATE_INFO = 5;
    static final int MESSAGE_CANCEL_LISTEN = 6;
    static final int MESSAGE_CREATE_SOCKET = 7;
    static final int MESSAGE_UPDATE_NUM = 8;


    private BroadcastAcceptThread mBroadcastAcceptThread;
    WifiManager.MulticastLock mWifiLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        |WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        |WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        |WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        |WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listener);

        mInfoView = (TextView) findViewById(R.id.id_text);
        mPortView= (EditText) findViewById(R.id.port);
        mStateView = (TextView) findViewById(R.id.id_state);
        mLauncherButton = (Button) findViewById(R.id.button);
        View ipgroup = findViewById(R.id.ip_group);
        ipgroup.setVisibility(View.GONE);

        Button cleanButton = (Button) findViewById(R.id.button_clear);
        cleanButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                mInfo = new StringBuffer();
                mInfoView.setText(mInfo);
            }
        });

        mLauncherButton.setOnClickListener(new OnClickListener(){
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View v) {
                if(mListening){
                    mHandler.sendEmptyMessage(MESSAGE_CANCEL_LISTEN);
                    updateState(false);
                }else{

                    String portString = mPortView.getText().toString();
                    mTarPort = -1;
                    if(portString != null && !portString.isEmpty()){
                        try {
                            Integer port = Integer.parseInt(portString);
                            if(port< 0 || port > 65535) {
                                Toast.makeText(mContext, "please input correct port", LENGTH_SHORT).show();
                            }else{
                                mTarPort= port;
                            }
                        }catch (NumberFormatException e){
                            Toast.makeText(mContext, "please input correct port", LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(mContext, "please input port", LENGTH_SHORT).show();
                    }

                    if(mTarPort>0 && mTarPort < 65535){
                        mHandler.sendEmptyMessage(MESSAGE_LISTEN);
                        updateState(true);
                    }
                }
            }
        });

        mInfo = new StringBuffer();
        mHandler.obtainMessage(MESSAGE_INIT).sendToTarget();
        @SuppressLint("WrongConstant") WifiManager manager = (WifiManager) this.getApplicationContext()
                .getSystemService(WIFI_SERVICE);
        mWifiLock = manager.createMulticastLock("test wifi");
    }

    @Override
    protected void onStop() {
        mWifiLock.release();
        mHandler.obtainMessage(MESSAGE_CANCEL_LISTEN).sendToTarget();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWifiLock.acquire();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }



    private void postUpdateInfo(String info) {
        Message msg = mHandler.obtainMessage(MESSAGE_UPDATE_INFO);
        msg.obj = info;
        msg.sendToTarget();
    }

    private void postUpdateNum() {
        mHandler.sendEmptyMessage(MESSAGE_UPDATE_NUM);
    }

    private void updateNum() {
        mInfoView.setText(mInfo+ ":"+mCount);
    }

    private void updateInfo(String line) {
        mInfo.append(line);
        mInfo.append("\n");
        mInfoView.setText(mInfo+ ":"+mCount);
    }

    private void updateState(boolean listening){
        mListening = listening;
        if(mListening){
            mStateView.setText(R.string.state_listening);
            mPortView.setEnabled(false);
            mLauncherButton.setText(R.string.state_stop);
        }else{
            mStateView.setText(R.string.state_stop);
            mPortView.setEnabled(true);
            mLauncherButton.setText(R.string.btn_start);
        }
    }

    @SuppressLint("WrongConstant")
    private void initIp() {
        mInfoView.setText(mInfo);
        mInfo.append(mName + "\n");
        mInfo.append("\n");
        mInfo.append("SN:"+mSN);
        mInfo.append("\n");

        WifiManager wifiManager;
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        updateInfo("local ip:" + ip);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_INIT:
                    initIp();
                    break;
                case MESSAGE_SEND_IP:
                    break;
                case MESSAGE_REQUEST_IP:
                    break;
                case MESSAGE_LISTEN:
                {
                    mCount = 0;
                    mBroadcastAcceptThread = new BroadcastAcceptThread(mTarPort);
                    mBroadcastAcceptThread.start();
                    break;
                }
                case MESSAGE_UPDATE_INFO:
                {
                    String info = (String) msg.obj;
                    updateInfo(info);
                    break;
                }
                case MESSAGE_CANCEL_LISTEN:
                    if(mBroadcastAcceptThread != null){
                        mBroadcastAcceptThread.cancel();
                    }
                    break;
                case MESSAGE_CREATE_SOCKET:

                    break;
                case MESSAGE_UPDATE_NUM:
                {
                    updateNum();
                    break;
                }
                default:
                    break;
            }
        }
    };

    class BroadcastAcceptThread extends Thread {
        int mPort;
        boolean mRunning = true;
        DatagramSocket datagramSocket;

        BroadcastAcceptThread(int port) {
            mPort = port;

            try {
                datagramSocket = new DatagramSocket(mPort);
                datagramSocket.setBroadcast(true);
            } catch (SocketException e) {
                e.printStackTrace();
                Log.d(TAG, "Broadcast DatagramSocket fail" );
            }
        }

        @Override
        public void run() {
            postUpdateInfo("Start Listenning port=" + mPort);
            byte[] message = new byte[512];

            DatagramPacket datagramPacket = new DatagramPacket(message,
                    message.length);
            if(datagramSocket == null){
                mRunning = false;
            }

            try {
                while (mRunning) {
                    datagramSocket.receive(datagramPacket);
                    String strMsg = new String(datagramPacket.getData()).trim();
                    if(strMsg.equals("test")){
                        mCount ++;
                        postUpdateNum();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void cancel() {
            if(datagramSocket != null && !datagramSocket.isClosed()){
                datagramSocket.close();
            }
            postUpdateInfo("Broadcast cancel listening");
            mRunning = false;
        }
    }

}
