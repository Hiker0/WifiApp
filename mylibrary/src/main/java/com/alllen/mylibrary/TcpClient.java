package com.alllen.mylibrary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: allen.z
 * Date  : 2017-05-26
 * last modified: 2017-05-26
 */

public class TcpClient {
    String mAddr;
    int mPort;
    Socket mSocket;
    ExecutorService mSingleThreadExecutor;
    BufferedWriter bw;
    BufferedReader br;
    InputStream input;
    OutputStream output;
    Listener mListener;
    PingTask mTask;
    Timer mTimer = null;
    STATUS status = STATUS.init;

    public enum STATUS {init, connecting, connectted, close}

    public TcpClient(String addr, int port) {
        mAddr = addr;
        mPort = port;
        mSocket = new Socket();
        status = STATUS.init;
        mTimer = new Timer();

    }

    public void setListener(Listener listener) {
        mListener = listener;
    }
    public void send(final String out) {
        mSingleThreadExecutor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    bw.write(out + "\n");
                    bw.flush();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void connect() {
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
        mSingleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                status = STATUS.connecting;
                InetSocketAddress addr = new InetSocketAddress(mAddr, mPort);
                try {
                    mSocket.setKeepAlive(true);
                    mSocket.connect(addr, 3000);
                    output = mSocket.getOutputStream();
                    input = mSocket.getInputStream();
                    bw = new BufferedWriter(new OutputStreamWriter(output, "utf-8"));
                    br = new BufferedReader(new InputStreamReader(input, "utf-8"));
                    status = STATUS.connectted;
                    if (mListener != null) {
                        mListener.onConnect();
                    }
                    startListenThread();
                    mTask = new PingTask();
                    mTask.start(mTimer);
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                    disconnect();
                } catch (IOException e) {
                    disconnect();
                    e.printStackTrace();
                }

            }
        });

    }

    public STATUS getStatus() {
        return status;
    }

    private void startListenThread() {
        if (STATUS.connectted != status) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = null;
                try {
                    while ((line = br.readLine()) != null) {
                        if (mListener != null) {
                            mListener.onReceive(line);
                        }
                    }
                } catch (IOException e) {
                    disconnect();
                }
            }
        }).start();
    }

    public void disconnect() {
        if(status == STATUS.close || status == STATUS.init) {
            return;
        }
        status = STATUS.close;

        try {
            if(mSocket != null && !mSocket.isClosed()) {
                mSocket.close();
            }
            if (br != null) {
                br.close();
            }

            if (bw != null) {
                bw.close();
            }

            if (input != null) {
                input.close();
            }

            if (output != null) {
                output.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(mTask != null){
            mTask.cancel();
        }
        if (mSingleThreadExecutor != null && !mSingleThreadExecutor.isShutdown()) {
            mSingleThreadExecutor.shutdown();
        }
        if (mListener != null) {
            mListener.onClose();
        }
    }

    public class PingTask extends TimerTask {

        PingTask(){

        }
        void start(Timer timer){
            timer.schedule(this, 500, 500);
        }

        @Override
        public void run() {
            try{
                mSocket.sendUrgentData(0xFF);
            }catch(Exception ex){
                disconnect();
            }
        }
    }
    public interface Listener {
        void onConnect();

        void onReceive(String msg);

        void onClose();
    }

}
