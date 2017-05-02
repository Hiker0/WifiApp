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
        Button GroupButton = (Button) findViewById(R.id.btn_group);
        Button BroadcastButton = (Button) findViewById(R.id.btn_broadcast);
        GroupButton.setOnClickListener(this);
        BroadcastButton.setOnClickListener(this);
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
        }
//        finish();
    }
}
