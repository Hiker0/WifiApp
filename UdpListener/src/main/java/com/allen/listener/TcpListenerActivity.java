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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.allen.listener.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

;import static android.view.View.GONE;

public class TcpListenerActivity extends Activity {
    static final String TAG = "Tcp Listener";

    private String mName = Build.DEVICE;
    private String mSN = Build.SERIAL;

    private TextView mInfoView;
    private TextView mStateView;
    private EditText  mPortView;
    private Button mLauncherButton;
    private ListView mSocketView;

    private boolean mListening = false;
    private int mTarPort = 0;
    private StringBuffer mInfo = null;
    private Map<Socket, SocketThread> mSockets;
    private Handler mHandler;
    private SocketAdapter mAdapter;

    static final int MESSAGE_INIT = 1;
    static final int MESSAGE_LISTEN = 2;
    static final int MESSAGE_SEND_IP = 3;
    static final int MESSAGE_REQUEST_IP = 4;
    static final int MESSAGE_UPDATE_INFO = 5;
    static final int MESSAGE_CANCEL_LISTEN = 6;
    static final int MESSAGE_REFRESH_LIST = 7;


    private SocketServerThread socketServerThread;
    WifiManager.MulticastLock mWifiLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_listener);

        mSockets = new HashMap<Socket, SocketThread>();
        mHandler = new MyHandler();
        initView();

        mInfo = new StringBuffer();
        mHandler.obtainMessage(MESSAGE_INIT).sendToTarget();
        @SuppressLint("WrongConstant") WifiManager manager = (WifiManager) this.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        mWifiLock = manager.createMulticastLock("test wifi");
    }


    @Override
    protected void onStop() {
        mWifiLock.release();
        mHandler.obtainMessage(MESSAGE_CANCEL_LISTEN).sendToTarget();
        updateState(false);
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


    @SuppressLint("WrongConstant")
    void initView() {
        mInfoView = (TextView) findViewById(R.id.id_text);
        mPortView = (EditText) findViewById(R.id.port);
        mStateView = (TextView) findViewById(R.id.id_state);
        mLauncherButton = (Button) findViewById(R.id.button);
        View ipgroup = findViewById(R.id.ip_group);
        ipgroup.setVisibility(GONE);


        Button cleanButton = (Button) findViewById(R.id.button_clear);
        cleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInfo = new StringBuffer();
                mInfoView.setText(mInfo);
            }
        });

        mLauncherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListening) {
                    mHandler.sendEmptyMessage(MESSAGE_CANCEL_LISTEN);
                    updateState(false);
                } else {

                    String portString = mPortView.getText().toString();
                    mTarPort = -1;
                    if (portString != null && !portString.isEmpty()) {
                        try {
                            Integer port = Integer.parseInt(portString);
                            if (port < 0 || port > 65535) {
                                Toast.makeText(TcpListenerActivity.this, "please input correct port", Toast.LENGTH_SHORT).show();
                            } else {
                                mTarPort = port;
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(TcpListenerActivity.this, "please input correct port", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TcpListenerActivity.this, "please input port", Toast.LENGTH_SHORT).show();
                    }

                    if (mTarPort > 0 && mTarPort < 65535) {
                        mHandler.sendEmptyMessage(MESSAGE_LISTEN);
                        updateState(true);
                    }
                }
            }
        });

        mSocketView = (ListView) findViewById(R.id.socket_list);
        mAdapter = new SocketAdapter();
        mSocketView.setAdapter(mAdapter);
    }

    ;

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

        @SuppressLint("WrongConstant") WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        updateInfo("local ip:" + ip);
    }

    class MyHandler extends Handler{
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
                    socketServerThread = new SocketServerThread(mTarPort);
                    socketServerThread.start();
                    break;
                }
                case MESSAGE_UPDATE_INFO:
                {
                    String info = (String) msg.obj;
                    updateInfo(info);
                    break;
                }
                case MESSAGE_CANCEL_LISTEN: {
                    if (socketServerThread != null) {
                        socketServerThread.cancel();
                    }

                    for(Map.Entry<Socket, SocketThread>  entity :mSockets.entrySet()){
                        try {
                            entity.getKey().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                }
                case MESSAGE_REFRESH_LIST:
                    mAdapter.refresh();
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

    class SocketServerThread extends Thread {
        int mPort;
        boolean mRunning = true;
        ServerSocket serverSocket;

        SocketServerThread(int port) {
            mPort = port;
            try {
                serverSocket = new ServerSocket(mPort);
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            postUpdateInfo("Start Listenning port=" + mPort);
            if(serverSocket == null){
                mRunning = false;
            }

            try {
                while (mRunning) {
                    Socket socket = serverSocket.accept();
                    Log.d(TAG, "server accept:" + socket.toString());
                    postUpdateInfo("accept socket:[" + socket.getInetAddress().getHostAddress()+"][" + socket.getPort() +"] ");
                    SocketThread thread = new SocketThread(socket);
                    thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            postUpdateInfo("Listenning end");
        }

        public void cancel() {
            if(serverSocket != null && !serverSocket.isClosed()){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            postUpdateInfo("server cancel listening");
            mRunning = false;
        }
    }

    public class SocketThread extends Thread {
        Socket socket;
        BufferedWriter bw;
        BufferedReader br;
        InputStream input;
        OutputStream output;
        String mName = null;

        public SocketThread(Socket s) {
            this.socket = s;
            mSockets.put(socket,this);
            mHandler.sendEmptyMessage(MESSAGE_REFRESH_LIST);
            try {
                output = socket.getOutputStream();
                input = socket.getInputStream();
                bw = new BufferedWriter(new OutputStreamWriter(output , "utf-8"));
                br = new BufferedReader(new InputStreamReader(input, "utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mName = socket.getInetAddress().getHostAddress()+"][" + socket.getPort();
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
            postUpdateInfo("socket:[" + mName +"]  listening");
            try {
                String line = null;
                while ((line = br.readLine()) != null) {
                    postUpdateInfo("socket["+socket.getInetAddress().getHostAddress()+"]:"+line);
                    out(line);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    br.close();
                    bw.close();
                    input.close();
                    output.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mSockets.remove(socket);
                mHandler.sendEmptyMessage(MESSAGE_REFRESH_LIST);
            }
            postUpdateInfo("socket:[" + mName +"]  end");
        }
    }

    class SocketAdapter extends BaseAdapter{
        ArrayList<Socket> sockets ;
        SocketAdapter(){
            sockets = new ArrayList<Socket>();
            sockets.addAll(mSockets.keySet());
        }

        void refresh(){
            sockets.clear();
            sockets.addAll(mSockets.keySet());
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return sockets.size();
        }

        @Override
        public Socket getItem(int position) {
            return sockets.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(TcpListenerActivity.this);
            ViewHolder holder;
            View view;
            if(convertView != null){
                view = (View) convertView;
                holder = (ViewHolder) convertView.getTag();
            }else{
                view = inflater.inflate(R.layout.socket_item, null);
                holder = new ViewHolder();
                holder.addr = (TextView)view.findViewById(R.id.tv_address);
                holder.port = (TextView)view.findViewById(R.id.tv_port);
                view.setTag(holder);
            }
            Socket socket= getItem(position);
            holder.setAddr(socket.getInetAddress().getHostAddress());
            holder.setPort(Integer.toString(socket.getPort()));
            return view;
        }

        class ViewHolder {
            TextView addr;
            TextView port;
            void setAddr(String text) {
                addr.setText(text);
            }
            void setPort(String text) {
                port.setText(text);
            }
        }
    }
}
