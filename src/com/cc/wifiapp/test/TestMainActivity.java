package com.cc.wifiapp.test;


import com.cc.wifiapp.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class TestMainActivity extends Activity  {
	Context mContext = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.test_main_activity);
		
		mContext = this;
		
		this.findViewById(R.id.button_go_state).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent in = new Intent();
				
				in.setClass(getApplicationContext(), CureentStateActivity.class);
				mContext.startActivity(in);
			}
		});
		
		this.findViewById(R.id.button_go_socket).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent in = new Intent();
				
				in.setClass(getApplicationContext(), SocketActivity.class);
				mContext.startActivity(in);
			}
		});
		
		this.findViewById(R.id.button_go_udp).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent in = new Intent();
				
				in.setClass(getApplicationContext(), UDPtestActivity.class);
				mContext.startActivity(in);
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

}
