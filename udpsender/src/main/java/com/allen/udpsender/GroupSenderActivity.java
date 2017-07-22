package com.allen.udpsender;

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
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

;

public class GroupSenderActivity extends Activity {
    static final String TAG = "ActivityListener";

    private String mName = Build.DEVICE;
    private String mSN = Build.SERIAL;

    private TextView mInfoView;
    private TextView mDataView;
    private IpEditView mIpEditView;
    private EditText mPortView;
    private Button mSendButton;
    private Button mClearButton;

    private boolean mListening = false;
    private InetAddress mTarAddress = null;
    private int mTarPort = 0;
    private StringBuffer mInfo = null;
    private GroupSender mGroupSender;


    static final String GROUP_DEFAULT_ADDR = "224.0.0.251";
    static final int GROUP_DEFAULT_PORT = 5353;


    static final int MESSAGE_INIT = 1;
    static final int MESSAGE_LISTEN = 2;
    static final int MESSAGE_SEND_IP = 3;
    static final int MESSAGE_REQUEST_IP = 4;
    static final int MESSAGE_UPDATE_INFO = 5;
    static final int MESSAGE_CANCEL_LISTEN = 6;
    static final int MESSAGE_CREATE_SOCKET = 7;

    private WifiManager.MulticastLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");

        mInfo = new StringBuffer();
        WifiManager manager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
        mWifiLock = manager.createMulticastLock("test wifi");

        mGroupSender = new GroupSender();

        mDataView = (EditText) findViewById(R.id.id_data);
        mInfoView = (TextView) findViewById(R.id.id_text);
        mIpEditView = (IpEditView) findViewById(R.id.ip_addr);
        try {
            mIpEditView.setIpAddress(GROUP_DEFAULT_ADDR);
        } catch (FormatException e) {

        }
        mPortView = (EditText) findViewById(R.id.port);
        mPortView.setText(Integer.toString(GROUP_DEFAULT_PORT));
        mClearButton = (Button) findViewById(R.id.button_clear);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInfo = new StringBuffer();
                mInfoView.setText(mInfo);
            }
        });

        mSendButton = (Button) findViewById(R.id.btn_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String address = mIpEditView.getIpAddress();
                try {
                    mTarAddress = InetAddress.getByName(address);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Toast.makeText(GroupSenderActivity.this, "please correct address", Toast.LENGTH_SHORT).show();
                    return;
                }

                String portString = mPortView.getText().toString();
                mTarPort = -1;
                if (portString != null && !portString.isEmpty()) {
                    try {
                        Integer port = Integer.parseInt(portString);
                        if (port < 0 || port > 65535) {
                            Toast.makeText(GroupSenderActivity.this, "please input correct port", Toast.LENGTH_SHORT).show();
                        } else {
                            mTarPort = port;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(GroupSenderActivity.this, "please input correct port", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(GroupSenderActivity.this, "please input port", Toast.LENGTH_SHORT).show();
                    return;
                }
                String msg = mDataView.getText().toString();
                mGroupSender.sendGroupBroadcast(mTarAddress, mTarPort, msg);
            }
        });

        mHandler.sendEmptyMessage(MESSAGE_INIT);
    }

    @Override
    protected void onStop() {
        mWifiLock.release();
        mWakeLock.release();
        mGroupSender.stop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWifiLock.acquire();
        mWakeLock.acquire();
        mGroupSender.start();
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

    private void initIp() {
        mInfoView.setText(mInfo);
        mInfo.append(mName + "\n");
        mInfo.append("\n");
        mInfo.append("SN:" + mSN);
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
                case MESSAGE_UPDATE_INFO: {
                    String info = (String) msg.obj;
                    updateInfo(info);
                    break;
                }
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

    class GroupSender {

        private ExecutorService mExecutor;

        GroupSender() {

        }

        public void start() {
            mExecutor = Executors.newSingleThreadExecutor();
        }

        public void stop() {
            mExecutor.shutdown();
        }

        public void sendGroupBroadcast(final InetAddress ip, final int port, final String data) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    broadcastGroupUdp(ip, port, data);
                }
            });
        }


        private void broadcastGroupUdp(final InetAddress ip, final int port, String info) {
            String msgStr;
            if (info == null || info.isEmpty()) {
                msgStr = "empty data";
            } else {
                msgStr = info;
            }
            postUpdateInfo("Group-> [" + ip + ":" + port + "]" + msgStr);

            DatagramPacket dataPacket = null;
            MulticastSocket multicastSocket = null;
            try {
                multicastSocket = new MulticastSocket();
                multicastSocket.joinGroup(ip);
                multicastSocket.setTimeToLive(4);
                byte[] data = msgStr.getBytes();
                dataPacket = new DatagramPacket(data, data.length, ip, port);
                multicastSocket.send(dataPacket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.e(TAG, "broadcastUdp fail");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "broadcastUdp fail");
            } finally {
                if (multicastSocket != null && !multicastSocket.isClosed()) {
                    multicastSocket.close();
                }
            }
        }
    }

}
