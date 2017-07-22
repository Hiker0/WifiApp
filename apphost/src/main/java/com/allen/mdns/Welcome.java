package com.allen.mdns;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.allen.mdns.R;
import com.allen.mdns.discover.DiscoverActivity;
import com.allen.mdns.discover.DiscoverJmdnsActivity;

public class Welcome extends AppCompatActivity {

    public final static String DEFAULT_TYPE = "_phibox._tcp.";

    EditText mTypeET = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mTypeET = (EditText) this.findViewById(R.id.et_type);
        mTypeET.setText(DEFAULT_TYPE);

        Button nsdBtn = (Button) findViewById(R.id.btn_nsd);

        if (nsdBtn != null) {
            nsdBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String type = mTypeET.getText().toString();
                    if(type == null || type.isEmpty()){
                        type = DEFAULT_TYPE;
                    }

                    Intent intent = new Intent();
                    intent.putExtra("type",type);
                    intent.setClass(Welcome.this, DiscoverActivity.class);
                    startActivity(intent);
                }
            });
        }

        Button jmdnsBtn = (Button) findViewById(R.id.btn_jmdns);

        if (jmdnsBtn != null) {
            jmdnsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String type = mTypeET.getText().toString();
                    if(type == null || type.isEmpty()){
                        type = DEFAULT_TYPE;
                    }

                    Intent intent = new Intent();
                    intent.putExtra("type",type);
                    intent.setClass(Welcome.this, DiscoverJmdnsActivity.class);
                    startActivity(intent);
                }
            });
        }

    }



}
