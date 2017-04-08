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
import android.os.Message;;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.ArrayList;

public class MainActivity extends Activity {
    static final String TAG = "allen";

    private int mSendCount = 0;
    private String mName = Build.DEVICE;
    private String mSN = android.os.Build.SERIAL;
    private TextView mInfoView;
    private ListView mListView;
    private BaseAdapter mAdapter;
    private int mIpAddress = 0;
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
    private TcpUdpSender mSender;

    ArrayList<Friend> mFriends;

    class Friend {
        String mIP;
        String mName;
        String mSN;
        Friend(String name, String ip, String sn){
            mIP = ip;
            mName = name;
            mSN = sn;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        mFriends = new ArrayList<Friend>();

        mInfoView = (TextView) findViewById(R.id.id_text);
        mListView = (ListView) findViewById(R.id.id_list);
        mAdapter = new DeviceAdaper();
        mListView.setAdapter(mAdapter);

        mInfo = new StringBuffer();
        mHandler.obtainMessage(MESSAGE_INIT).sendToTarget();
        WifiManager manager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
        mWifiLock = manager.createMulticastLock("test wifi");
        mSender = new TcpUdpSender();
    }

    @Override
    protected void onStop() {
        mWifiLock.release();
        mHandler.obtainMessage(MESSAGE_CANCEL_LISTEN).sendToTarget();
        mHandler.removeMessages(MESSAGE_SEND_IP);
        mHandler.removeMessages(MESSAGE_REQUEST_IP);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWifiLock.acquire();
        mHandler.obtainMessage(MESSAGE_LISTEN).sendToTarget();
        if(ID == ID_HOST){
            mHandler.sendEmptyMessage(MESSAGE_REQUEST_IP);
        }
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
        if (ID == ID_HOST) {
            mInfo.append("I am host");
        } else {
            mInfo.append("I am client");
        }
        mInfo.append("\n");
        mInfo.append("SN:"+mSN);
        mInfo.append("\n");

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        mIpAddress = wifiInfo.getIpAddress();
        String ip = intToIp(mIpAddress);
        updateInfo("local ip:" + ip);
        mSendCount = 0;
        mHandler.sendEmptyMessage(MESSAGE_SEND_IP);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_INIT:
                    initIp();
                    break;
                case MESSAGE_SEND_IP:
                    mSender.sendGroupBroadcast("");
                    mSender.sendBroadcast("");
                    if (mSendCount < MAX_SEND_COUNT) {
                        mSendCount++;
                        mHandler.sendEmptyMessageDelayed(MESSAGE_SEND_IP, 2000);
                    }
                    break;
                case MESSAGE_REQUEST_IP:
                    mSender.sendGroupBroadcast(IDENTIFY_ID+":"+CMD_REQ);
                    mSender.sendBroadcast(IDENTIFY_ID+":"+CMD_REQ);
                    if (mSendCount < MAX_SEND_COUNT) {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_REQUEST_IP, 2000);
                    }
                    break;
                case MESSAGE_LISTEN:
                {
                    int port = (ID == ID_HOST) ? HOST_UDP_GROUP_PORT : CLIENT_UDP_GROUP_PORT;
                    mGroupAcceptThread = new GroupAcceptThread(GROUP_BROADCAST_ADDR, port);
                    mGroupAcceptThread.start();
                    mBroadcastAcceptThread = new BroadcastAcceptThread((ID == ID_HOST) ? HOST_UDP_PORT : CLIENT_UDP_PORT);
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
                    if (mGroupAcceptThread != null) {
                        mGroupAcceptThread.cancel();
                    }

                    if(mBroadcastAcceptThread != null){
                        mBroadcastAcceptThread.cancel();
                    }
                    break;
                case MESSAGE_CREATE_SOCKET:
                    Friend friend = (Friend) msg.obj;
                    createScoket(friend);
                    break;
                default:
                    break;
            }
        }
    };

    private void createScoket(Friend friend){
        for(Friend f : mFriends){
            if(f.mSN.equals(friend.mSN)){
                return;
            }
        }
        mFriends.add(friend);
        mAdapter.notifyDataSetChanged();
        mSendCount = 0;
        mHandler.obtainMessage(MESSAGE_SEND_IP).sendToTarget();
    }

    private void postUpdateInfo(String info) {
        Message msg = mHandler.obtainMessage(MESSAGE_UPDATE_INFO);
        msg.obj = info;
        msg.sendToTarget();
    }

    class TcpUdpSender {
        private HandlerThread mThread;
        private Handler mHandler;
        private MulticastSocket mMulticastSocket;
        private InetAddress sendAddress;

        final int MSG_SEND_GROUP = 1;
        final int MSG_SEND_UDP = 2;
        final int MSG_SEND_BROADCAST = 3;

        class MyHandler extends  Handler{
            MyHandler(Looper looper){
                super(looper);
            }

            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case MSG_SEND_GROUP:
                        broadcastGroupUdp((String)msg.obj);
                        break;
                    case MSG_SEND_BROADCAST:
                        broadcastInner((String)msg.obj);
                        break;
                }
            }
        }
        TcpUdpSender() {
            mThread = new HandlerThread("send thread");
            mThread.start();
            mHandler = new MyHandler(mThread.getLooper());
            try {
                mMulticastSocket = new MulticastSocket();
                sendAddress = InetAddress.getByName(GROUP_BROADCAST_ADDR);
                mMulticastSocket.joinGroup(sendAddress);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "mMulticastSocket fail");
            }
        }

        public void sendGroupBroadcast(String data) {
            Message msg = mHandler.obtainMessage(MSG_SEND_GROUP);
            msg.obj = data;
            mHandler.sendMessage(msg);
        }

        public void sendBroadcast(String data){
            Message msg = mHandler.obtainMessage(MSG_SEND_BROADCAST);
            msg.obj = data;
            mHandler.sendMessage(msg);
        }

        private void broadcastGroupUdp(String info) {
            String msgStr;
            if(info == null || info.isEmpty()){
                msgStr = IDENTIFY_ID + ":"+ CMD_ECHO +":" + mName + ":" + intToIp(mIpAddress) + ":"+mSN+":" + mSendCount;
            }else{
                msgStr = info;
            }
            postUpdateInfo("Group->" + msgStr);

            DatagramPacket dataPacket = null;
            try {
                mMulticastSocket.setTimeToLive(4);
                InetAddress ia = InetAddress.getByName(GROUP_BROADCAST_ADDR);
                byte[] data = msgStr.getBytes();
                int port = ID == ID_HOST ? CLIENT_UDP_GROUP_PORT : HOST_UDP_GROUP_PORT;
                dataPacket = new DatagramPacket(data, data.length, ia, port);
                mMulticastSocket.send(dataPacket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.e(TAG, "broadcastUdp fail");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "broadcastUdp fail");
            }
        }

        private void broadcastInner(String info){
            String msgStr;
            if(info == null || info.isEmpty()){
                msgStr = IDENTIFY_ID + ":" + CMD_ECHO + ":" + mName + ":" + intToIp(mIpAddress) + ":"+mSN+":" + mSendCount;
            }else{
                msgStr = info;
            }
            postUpdateInfo("broadcast->"+msgStr);
            int server_port = ID==ID_HOST? CLIENT_UDP_PORT:HOST_UDP_PORT;
            DatagramSocket s = null;
            try {
                s = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            InetAddress local = null;
            try {
                local = InetAddress.getByName("255.255.255.255");
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
            }
        }

        @Override
        public void run() {
            byte[] message = new byte[512];

            DatagramPacket datagramPacket = new DatagramPacket(message,
                    message.length);
            try {
                while (mRunning) {
                    datagramSocket.receive(datagramPacket);
                    String strMsg = new String(datagramPacket.getData()).trim();
                    Log.d("allen", "Broadcast receive:" + strMsg);
                    postUpdateInfo("Broadcast receive:" + strMsg);
                    parserUdp(strMsg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void parserUdp(String data){
            String[] info = data.split(":");
            if(info[0].equals(IDENTIFY_ID)){
                if(info[1].equals(CMD_REQ)){
                    mHandler.sendEmptyMessage(MESSAGE_SEND_IP);
                }else if(info[1].equals(CMD_ECHO)){
                    Friend friend = new Friend(info[2], info[3], info[4]);
                    Message msg = mHandler.obtainMessage(MESSAGE_CREATE_SOCKET);
                    msg.obj = friend;
                    msg.sendToTarget();
                    postUpdateInfo("get ip:" + info[3]);
                }
            }
        }

        public void cancel() {
            if(!datagramSocket.isClosed()){
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
            }
        }

        @Override
        public void run() {
            postUpdateInfo("group begin listening");
            byte buf[] = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, 1024);
            while (mRunning) {
                try {
                    mMulticastSocket.receive(dp);
                    String data = new String(buf, 0, dp.getLength());
                    Log.d("allen", "group receive:" + data);
                    postUpdateInfo("group receive:" + data);
                    parserUdp(data);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "group receive fail");
                }
            }
        }

        public void parserUdp(String data){
            String[] info = data.split(":");
            if(info[0].equals(IDENTIFY_ID)){
                if(info[1].equals(CMD_REQ)){
                    mHandler.sendEmptyMessage(MESSAGE_SEND_IP);
                }else if(info[1].equals(CMD_ECHO)){
                    Friend friend = new Friend(info[2], info[3], info[4]);
                    Message msg = mHandler.obtainMessage(MESSAGE_CREATE_SOCKET);
                    msg.obj = friend;
                    msg.sendToTarget();
                    postUpdateInfo("get ip:" + info[3]);
                }
            }
        }
        public void cancel() {
            postUpdateInfo("group cancel listening");
            try {
                mMulticastSocket.leaveGroup(receiveAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!mMulticastSocket.isClosed()) {
                mMulticastSocket.close();
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
    class DeviceAdaper extends BaseAdapter {

        @Override
        public int getCount() {
            return mFriends.size();
        }

        @Override
        public Friend getItem(int position) {
            return mFriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            ViewHolder holder;
            View view;
            if(convertView != null){
                view = (View) convertView;
                holder = (ViewHolder) convertView.getTag();
            }else{
                view = inflater.inflate(R.layout.list_item, null);
                holder = new ViewHolder();
                holder.name = (TextView)view.findViewById(R.id.id_name);
                holder.ip = (TextView)view.findViewById(R.id.id_ip);
                holder.sn = (TextView)view.findViewById(R.id.id_time);
                view.setTag(holder);
            }
            Friend friend= getItem(position);
            holder.setName(friend.mName);
            holder.setIp(friend.mIP);
            holder.setTime(friend.mSN);
            return view;
        }

        class ViewHolder {
            TextView name;
            TextView ip;
            TextView sn;

            void setName(String text) {
                name.setText(text);
            }
            void setIp(String text) {
                ip.setText(text);
            }
            void setTime(String text) {
                sn.setText(text);
            }
        }
    }
}
