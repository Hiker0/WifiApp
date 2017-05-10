package com.example.udpsender;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.FormatException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alllen.mylibrary.IpEditView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class BroadcastSenderTest extends Activity {
    static final String TAG = "ActivityListener";

    private String mName = Build.DEVICE;
    private String mSN = Build.SERIAL;

    private TextView mInfoView;
    private EditText mNumView;
    private TextView mStateView;
    private EditText  mPortView;
    private Button mLauncherButton;

    private boolean mListening = false;
    private String mTarAddress = null;
    private int mTarPort = 0;
    private int mTarNum = 0;
    private int mSendNum = 0;
    private boolean sending;
    private StringBuffer mInfo = null;
    private IpEditView mIpEditView;

    static final String GROUP_DEFAULT_ADDR = "255.255.255.255";
    static final int GROUP_DEFAULT_PORT = 2525;


    static final int MESSAGE_INIT = 1;
    static final int MESSAGE_LISTEN = 2;
    static final int MESSAGE_SEND_IP = 3;
    static final int MESSAGE_REQUEST_IP = 4;
    static final int MESSAGE_UPDATE_INFO = 5;
    static final int MESSAGE_CANCEL_LISTEN = 6;
    static final int MESSAGE_CREATE_SOCKET = 7;
    static final int MESSAGE_CRC_SEND = 8;

    private UdpSender mSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        |WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        |WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        |WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        |WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mNumView = (EditText) findViewById(R.id.num);
        mInfoView = (TextView) findViewById(R.id.id_text);
        mPortView= (EditText) findViewById(R.id.port);
        mStateView = (TextView) findViewById(R.id.id_state);
        mLauncherButton = (Button) findViewById(R.id.button);
        mIpEditView = (IpEditView) findViewById(R.id.ip_addr);
        try {
            mIpEditView.setIpAddress(GROUP_DEFAULT_ADDR);
        }catch (FormatException e){

        }
        mSender = new UdpSender();

        mLauncherButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(sending) {
                    sending = false;
                    mLauncherButton.setText("SEND");
                    mHandler.removeMessages(MESSAGE_CRC_SEND);
                    updateNum();
                    mTarNum = 0;
                }else{
                    mSendNum = 0;
                    mLauncherButton.setText("STOP");
                    mTarAddress = mIpEditView.getIpAddress();
                    String portString = mPortView.getText().toString();
                    mTarPort = -1;
                    if (portString != null && !portString.isEmpty()) {
                        try {
                            Integer port = Integer.parseInt(portString);
                            if (port < 0 || port > 65535) {
                                Toast.makeText(BroadcastSenderTest.this, "please input correct port", Toast.LENGTH_SHORT).show();
                            } else {
                                mTarPort = port;
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(BroadcastSenderTest.this, "please input correct port", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(BroadcastSenderTest.this, "please input port", Toast.LENGTH_SHORT).show();
                    }

                    mTarNum = 0;
                    String numString = mNumView.getText().toString();
                    if (numString != null && !numString.isEmpty()) {
                        try {
                            Integer num = Integer.parseInt(numString);
                            mTarNum = num;
                        } catch (NumberFormatException e) {
                            Toast.makeText(BroadcastSenderTest.this, "please input correct num", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(BroadcastSenderTest.this, "please input num", Toast.LENGTH_SHORT).show();
                    }

                    mHandler.sendEmptyMessage(MESSAGE_CRC_SEND);
                    sending = true;
                }
            }
        });

        mInfo = new StringBuffer();
        mHandler.obtainMessage(MESSAGE_INIT).sendToTarget();
        WifiManager manager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected void onStop() {
        mHandler.obtainMessage(MESSAGE_CANCEL_LISTEN).sendToTarget();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onDestroy() {
        mSender.close();
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

    private void updateNum() {
        mInfoView.setText(mInfo+": "+mSendNum+"/" +mTarNum);
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
                case MESSAGE_UPDATE_INFO:
                {
                    String info = (String) msg.obj;
                    updateInfo(info);
                    break;
                }
                case MESSAGE_CREATE_SOCKET:

                    break;
                case MESSAGE_CRC_SEND:
                    if(mSendNum < mTarNum){
                        mSendNum ++;
                        mSender.sendUdp("test");
                        mHandler.sendEmptyMessageDelayed(MESSAGE_CRC_SEND,200);
                    }
                    updateNum();

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

    class UdpSender {
        private HandlerThread mThread;
        private Handler mHandler;

        final int MSG_SEND_UDP = 2;
        final int MSG_SEND_BROADCAST = 3;

        class MyHandler extends  Handler{
            MyHandler(Looper looper){
                super(looper);
            }

            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case MSG_SEND_BROADCAST:
                        broadcastInner((String)msg.obj);
                        break;
                }
            }
        }
        UdpSender() {
            mThread = new HandlerThread("send thread");
            mThread.start();
            mHandler = new MyHandler(mThread.getLooper());
        }

        public void close(){
            mHandler.removeCallbacksAndMessages(null);
            mThread.quitSafely();
        }

        public void sendUdp(String data){
            Message msg = mHandler.obtainMessage(MSG_SEND_BROADCAST);
            msg.obj = data;
            mHandler.sendMessage(msg);
        }

        private void broadcastInner(String info){
            String msgStr;
            if(info == null || info.isEmpty()){
                msgStr = "123";
            }else{
                msgStr = info;
            }
//            postUpdateInfo("broadcast->"+msgStr);
            int server_port = mTarPort;
            DatagramSocket s = null;
            try {
                s = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            InetAddress local = null;
            try {
                local = InetAddress.getByName(mTarAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            int msg_length = msgStr.length();
            byte[] messageByte = msgStr.getBytes();
            DatagramPacket p = new DatagramPacket(messageByte, msg_length, local,
                    server_port);
            try {

                s.send(p);
                s.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
