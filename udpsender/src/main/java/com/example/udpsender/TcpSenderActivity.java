package com.example.udpsender;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alllen.mylibrary.IpEditView;
import com.alllen.mylibrary.TcpClient;

public class TcpSenderActivity extends Activity {
    static final String TAG = "ActivityListener";

    private String mName = Build.DEVICE;
    private String mSN = Build.SERIAL;

    private TextView mInfoView;
    private EditText mDataView;
    private EditText mPortView;
    private Button sendButton;
    private Button connectButton;

    private StringBuffer mInfo = null;
    private IpEditView mIpEditView;
    private TcpClient mClient = null;

    static final int MESSAGE_INIT = 1;
    static final int MESSAGE_CONNECT = 2;
    static final int MESSAGE_SEND_IP = 3;
    static final int MESSAGE_REQUEST_IP = 4;
    static final int MESSAGE_UPDATE_INFO = 5;
    static final int MESSAGE_DISCONNECT = 6;
    static final int MESSAGE_CREATE_SOCKET = 7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_connect);
        mInfo = new StringBuffer();
        mHandler.obtainMessage(MESSAGE_INIT).sendToTarget();
        WifiManager manager = (WifiManager) this.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        initView();
    }

    void initView() {
        mInfoView = (TextView) findViewById(R.id.id_text);

        mIpEditView = (IpEditView) findViewById(R.id.ip_addr);
        mPortView = (EditText) findViewById(R.id.port);
        connectButton = (Button) findViewById(R.id.id_connect);

        mDataView = (EditText) findViewById(R.id.id_data);
        sendButton = (Button) findViewById(R.id.button);

        updateState();

        Button cleanButton = (Button) findViewById(R.id.button_clear);
        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInfo = new StringBuffer();
                mInfoView.setText(mInfo);
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClient != null && mClient.getStatus() == TcpClient.STATUS.connectted) {
                    mHandler.sendEmptyMessage(MESSAGE_DISCONNECT);
                } else {
                    mHandler.sendEmptyMessage(MESSAGE_CONNECT);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = mDataView.getText().toString();
                if (mClient != null) {
                    updateInfo("send>>" + msg);
                    mClient.send(msg);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        if (mClient != null) {
            mClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    private void uiUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateState();
            }
        });
    }

    private void updateState() {
        if (mClient == null) {
            mIpEditView.setEnabled(true);
            mPortView.setEnabled(true);
            connectButton.setEnabled(true);
            sendButton.setEnabled(false);
            connectButton.setText("connect");
            return;
        }

        switch (mClient.getStatus()) {
            case init:
                mIpEditView.setEnabled(true);
                mPortView.setEnabled(true);
                sendButton.setEnabled(false);
                connectButton.setText("connect");
                break;
            case connecting:
                mIpEditView.setEnabled(false);
                mPortView.setEnabled(false);
                sendButton.setEnabled(false);
                connectButton.setText("disconnect");
                break;
            case connectted:
                mIpEditView.setEnabled(false);
                mPortView.setEnabled(false);
                sendButton.setEnabled(true);
                connectButton.setText("disconnect");
                break;
            case close:
                mIpEditView.setEnabled(true);
                mPortView.setEnabled(true);
                sendButton.setEnabled(false);
                connectButton.setText("connect");
                break;
        }
    }

    private void initIp() {
        mInfoView.setText(mInfo);
        mInfo.append(mName + "\n");
        mInfo.append("\n");
        mInfo.append("SN:" + mSN);
        mInfo.append("\n");

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        updateInfo("local ip:" + ip);
    }

    void doConnect() {
        String address = mIpEditView.getIpAddress();
        String portString = mPortView.getText().toString();
        int tarPort = -1;
        if (portString != null && !portString.isEmpty()) {
            try {
                Integer port = Integer.parseInt(portString);
                if (port < 0 || port > 65535) {
                    Toast.makeText(TcpSenderActivity.this, "please input correct port", Toast.LENGTH_SHORT).show();
                } else {
                    tarPort = port;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(TcpSenderActivity.this, "please input correct port", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(TcpSenderActivity.this, "please input port", Toast.LENGTH_SHORT).show();
        }

        if (tarPort >= 0) {
            mClient = new TcpClient(address, tarPort);
            mClient.setListener(new StatusListener());
            mClient.connect();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_INIT:
                    initIp();
                    break;
                case MESSAGE_CONNECT:
                    doConnect();
                    break;
                case MESSAGE_SEND_IP:
                    break;
                case MESSAGE_REQUEST_IP:
                    break;
                case MESSAGE_UPDATE_INFO: {
                    String info = (String) msg.obj;
                    updateInfo(info);
                    break;
                }
                case MESSAGE_DISCONNECT:
                    if (mClient != null) {
                        mClient.disconnect();
                    }
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

    class StatusListener implements TcpClient.Listener {

        @Override
        public void onConnect() {
            uiUpdate();
            postUpdateInfo("Connect OK");
        }

        @Override
        public void onReceive(String msg) {
            uiUpdate();
            postUpdateInfo("receive<<" + msg);
        }

        @Override
        public void onClose() {
            uiUpdate();
            postUpdateInfo("Connect Close");
        }
    }
}
