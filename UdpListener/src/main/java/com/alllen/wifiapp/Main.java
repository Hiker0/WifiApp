package com.alllen.wifiapp;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Main extends Activity implements View.OnClickListener {
    static final String TAG = "udplistener";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button groupButton = (Button) findViewById(R.id.btn_group);
        Button broadcastButton = (Button) findViewById(R.id.btn_broadcast);
        Button testbutton = (Button) findViewById(R.id.btn_test);
        Button tcpbutton = (Button) findViewById(R.id.btn_tcp);

        groupButton.setOnClickListener(this);
        broadcastButton.setOnClickListener(this);
        testbutton.setOnClickListener(this);
        tcpbutton.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = new Intent();
        if(id == R.id.btn_group){
            intent.setClass(this,GroupListenerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if(id == R.id.btn_broadcast){
            intent.setClass(this,BroadcastListenerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if(id == R.id.btn_test){
            intent.setClass(this,UdpEchoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if(id == R.id.btn_tcp){
            intent.setClass(this,TcpListenerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
//        finish();
    }
}
