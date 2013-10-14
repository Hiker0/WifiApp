package com.cc.wifiapp.test; 

import com.cc.wifiapp.R;

import android.app.Activity;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CureentStateActivity extends Activity {
	private WifiManager wifim = null;
	
	private TextView mWifiStatusView,mConnectStatusView,mConnectInfoView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.current_state_activity);
		
		wifim=(WifiManager)this.getSystemService(WIFI_SERVICE);
		mWifiStatusView=(TextView)this.findViewById(R.id.wifi_status);
		mConnectStatusView=(TextView)this.findViewById(R.id.connect_status);
		mConnectInfoView=(TextView)this.findViewById(R.id.connect_info);
		
	}
	
	void getWifiList(){
		
	}
	
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}





	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		refreshWifiStatus();
		refreshConnectInfo();
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





	void refreshWifiStatus(){
		if(wifim != null){
			StringBuilder sb=new StringBuilder();
			Resources rs = this.getResources();
			
			sb.append(rs.getString(R.string.wifi_status));
			if(wifim.isWifiEnabled()){
				sb.append(rs.getString(R.string.text_on));
			}else{
				sb.append(rs.getString(R.string.text_off));
			}
			
			if(mWifiStatusView!=null){
				mWifiStatusView.setText(sb);
			}
		}
	}
	
	private String intToIp(int i)  {
		return (i & 0xFF)+ "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) +"."+((i >> 24 ) & 0xFF );
	}
	
	void refreshConnectInfo(){
		if(wifim != null){
			WifiInfo  wi = wifim.getConnectionInfo();
			if(wi.getNetworkId() == -1){
				StringBuilder sb=new StringBuilder();
				sb.append(getResources().getString(R.string.connect_status));
				sb.append(getResources().getString(R.string.text_off));
				mConnectStatusView.setText(sb);
				mConnectInfoView.setVisibility(View.GONE);
				mConnectStatusView.setVisibility(View.VISIBLE);
				
			}else{
				StringBuilder sb=new StringBuilder();
				sb.append("SSID:"+wi.getSSID()+"\n");
				int ip=wi.getIpAddress();
				sb.append("IP:");
				
				sb.append(intToIp(ip));
				sb.append("\n");
				
				sb.append("MAC:"+wi.getMacAddress()+"\n");
				sb.append("BSSID:"+wi.getBSSID()+"\n");
				sb.append("Speed:"+wi.getLinkSpeed()+"\n");
				sb.append("Rssi:"+wi.getRssi()+"\n");
				
				
				
				mConnectInfoView.setText(sb);
				mConnectInfoView.setVisibility(View.VISIBLE);
				mConnectStatusView.setVisibility(View.GONE);
			}
		}
	}
}
