package com.alllen.wifiapp;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alllen.mylibrary.IpEditView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

;

public class ActivityListener extends Activity {
    static final String TAG = "ActivityListener";

    private String mName = Build.DEVICE;
    private String mSN = Build.SERIAL;

    private TextView mInfoView;
    private TextView mStateView;
    private IpEditView mIpEditView;
    private EditText  mPortView;
    private Button mLauncherButton;

    private boolean mListening = false;
    private String mTarAddress = null;
    private int mTarPort = 0;
    private StringBuffer mInfo = null;

    static final String IDENTIFY_ID = "phicomm";
    static final String CMD_REQ = "req";
    static final String CMD_ECHO = "echo";

    static final int ID_HOST = 1;
    static final int ID_CLIENT = 2;
    static final int ID = ID_HOST;

    static final String GROUP_BROADCAST_ADDR = "224.0.0.251";
    static final int HOST_UDP_GROUP_PORT = 10000;
    static final int CLIENT_UDP_GROUP_PORT = 5353;
    static final int HOST_UDP_PORT = 10002;
    static final int CLIENT_UDP_PORT = 10003;
    static final int HOST_SCOKET_PORT = 20000;
    static final int CLIENT_SCOKET_PORT = 20001;
    static final int MAX_SEND_COUNT = 10;

    static final int MESSAGE_INIT = 1;
    static final int MESSAGE_LISTEN = 2;
    static final int MESSAGE_SEND_IP = 3;
    static final int MESSAGE_REQUEST_IP = 4;
    static final int MESSAGE_UPDATE_INFO = 5;
    static final int MESSAGE_CANCEL_LISTEN = 6;
    static final int MESSAGE_CREATE_SOCKET = 7;


    private BroadcastAcceptThread mBroadcastAcceptThread;
    private GroupAcceptThread mGroupAcceptThread;
    WifiManager.MulticastLock mWifiLock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listener);

        mInfoView = (TextView) findViewById(R.id.id_text);
        mIpEditView = (IpEditView) findViewById(R.id.ip_addr);
        mPortView= (EditText) findViewById(R.id.port);
        mStateView = (TextView) findViewById(R.id.id_state);
        mLauncherButton = (Button) findViewById(R.id.button);
        mLauncherButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(mListening){
                    mHandler.sendEmptyMessage(MESSAGE_CANCEL_LISTEN);
                    updateState(false);
                }else{
                    mTarAddress = mIpEditView.getIpAddress();
                    String portString = mPortView.getText().toString();
                    mTarPort = -1;
                    if(portString != null && !portString.isEmpty()){
                        try {
                            Integer port = Integer.parseInt(portString);
                            if(port< 0 || port > 65535) {
                                Toast.makeText(ActivityListener.this, "please input correct port", Toast.LENGTH_SHORT).show();
                            }else{
                                mTarPort= port;
                            }
                        }catch (NumberFormatException e){
                            Toast.makeText(ActivityListener.this, "please input correct port", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(ActivityListener.this, "please input port", Toast.LENGTH_SHORT).show();
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
        WifiManager manager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
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


    private void updateInfo(String line) {
        mInfo.append(line);
        mInfo.append("\n");
        mInfoView.setText(mInfo);
    }

    private void updateState(boolean listening){
        mListening = listening;
        if(mListening){
            mStateView.setText(R.string.state_listening);
            mIpEditView.setEnabled(false);
            mPortView.setEnabled(false);
            mLauncherButton.setText(R.string.state_stop);
        }else{
            mStateView.setText(R.string.state_stop);
            mIpEditView.setEnabled(true);
            mPortView.setEnabled(true);
            mLauncherButton.setText(R.string.btn_start);
        }
    }
    private void initIp() {
        mInfoView.setText(mInfo);
        mInfo.append(mName + "\n");
        mInfo.append("\n");
        mInfo.append("SN:"+mSN);
        mInfo.append("\n");

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

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
                    mGroupAcceptThread = new GroupAcceptThread(mTarAddress, mTarPort);
                    mGroupAcceptThread.start();
                    mBroadcastAcceptThread = new BroadcastAcceptThread(mTarPort);
//                    mBroadcastAcceptThread.start();
                    break;
                }
                case MESSAGE_UPDATE_INFO:
                {
                    String info = (String) msg.obj;
                    updateInfo(info);
                    break;
                }
                case MESSAGE_CANCEL_LISTEN:
                    if (mGroupAcceptThread != null) {
                        mGroupAcceptThread.cancel();
                    }

                    if(mBroadcastAcceptThread != null){
                        mBroadcastAcceptThread.cancel();
                    }
                    break;
                case MESSAGE_CREATE_SOCKET:

                    break;
                default:
                    break;
            }
        }
    };


    private void postUpdateInfo(String info) {
        Message msg = mHandler.obtainMessage(MESSAGE_UPDATE_INFO);
        msg.obj = info;
        msg.sendToTarget();
    }

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
                    Log.d(TAG, "Broadcast receive:" + strMsg);
                    postUpdateInfo("Broadcast receive:" + strMsg);
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

    class GroupAcceptThread extends Thread {
        String mAddr;
        int mPort;
        boolean mRunning = true;
        InetAddress receiveAddress;
        MulticastSocket mMulticastSocket;

        GroupAcceptThread(String addr, int port) {
            mAddr = addr;
            mPort = port;
            mRunning = true;
            try {
                mMulticastSocket = new MulticastSocket(mPort);
                receiveAddress = InetAddress.getByName(mAddr);
                mMulticastSocket.joinGroup(receiveAddress);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                Log.e(TAG, "mMulticastSocket fail");
                mRunning = false;
            }
        }

        @Override
        public void run() {
            postUpdateInfo("tar:"+mTarAddress+"::"+mTarPort);
            if(mRunning) {
                postUpdateInfo("group begin listening");
            }
            byte buf[] = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, 1024);
            while (mRunning) {
                try {
                    mMulticastSocket.receive(dp);
                    String data = new String(buf, 0, dp.getLength());
                    Log.d(TAG, "group receive:" + data);
                    postUpdateInfo("group receive:" + data);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "group receive fail");
                }
            }
        }

        public void cancel() {
            postUpdateInfo("group cancel listening");
            if(mMulticastSocket != null) {
                try {
                    mMulticastSocket.leaveGroup(receiveAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!mMulticastSocket.isClosed()) {
                    mMulticastSocket.close();
                }
            }
            mRunning = false;
        }
    }

    public class SocketThread extends Thread {
        Socket socket;
        BufferedWriter bw;
        BufferedReader br;

        public SocketThread(Socket s) {
            this.socket = s;
            try {
                bw = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream(), "utf-8"));
                br = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), "utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void out(String out) {
            try {
                bw.write(out + "\n");
                bw.flush();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println("客户端发来数据："+line);
                }
                br.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
