package com.cc.wifiapp.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cc.wifiapp.R;

public class UDPtestActivity extends Activity {

	final static String TAG = "wifiapp";
	final static int SEARCH=200;
	
	
	 class Protocal{
		 
		final static byte TYPE_UNKNOWN = -1;
		final static byte TYPE_ASK = 0;
		final static byte TYPE_ANSWER = 1;
		final static byte TYPE_INFO = 2;
		
		final static byte HOST_UNKNOWN = -1;
		final static byte HOST_HOST = 0;
		final static byte HOST_GUEST = 1;
		
		final static byte STATE_UNKNOWN = 1;
		final static byte STATE_IDLE = 0;
		final static byte STATE_BUSY = 1;
		
		public byte type = TYPE_UNKNOWN;
		public String ipaddress = null;
		public String mInfo = null;
		public byte host = HOST_UNKNOWN;
		public byte state = STATE_UNKNOWN;
		
		
		/***********Protocal************
		 *  Ask
		 *  1BYTE BYTES
		 *  |TYPE|IP...|
		 *  =========================
		 *  Answer
		 *  1BYTE 1BYTE 1BYTE BYTES
		 *  |TYPE|HOST|STATE|IP...|
		 *  =========================
		 *  Info
		 *  1BYTE BYTES
		 *  |TYPE|INFO...|
		 *  
		 *****************************/
		
		
		public Protocal (){
			
		}
		
		public Protocal (byte[] code){
			int len = code.length;
			if(len > 0){
				type = code[0];
				
				switch(type ){
					case TYPE_ASK:
					{
						byte[]  ip = new byte[len-1]; 
						for(int i=0; i<len-1;i++){
							ip[i]=code[i+1];
						}
						
						ipaddress = new String(ip, 0, len-1);
						break;
					}
						
					case TYPE_ANSWER:
					{
						host = code[1];
						state = code[2];
						byte[]  ip = new byte[len-3]; 
						for(int i=0; i<len-3;i++){
							ip[i]=code[i+3];
						}
						
						ipaddress = new String(ip, 0, len-3);
						break;
					}
					
					case TYPE_INFO:
					{
						byte[]  info = new byte[len-1]; 
						for(int i=0; i<len-1;i++){
							info[i]=code[i+1];
						}
						
						mInfo = new String(info, 0, len-1);
						break;
					}
					default:
						break;
					
				}
				
			}
		}
		
		public  byte[] getCode(){
			

				switch(type ){
					case TYPE_ASK:
					{
						
						byte[] ip = ipaddress.getBytes();
						int len = ip.length;
						byte[] code = new byte[len+1]; 
						code[0] = type;
						
						for(int i=0; i<len;i++){
							code[i+1]=ip[i];
						}
						
						return code;
					}
					case TYPE_ANSWER:
					{
						byte[] ip = ipaddress.getBytes();
						int len = ip.length;
						byte[] code = new byte[len+3]; 
						
						code[0] = type;
						code[1] = type;
						code[2] = type;
						
						for(int i=0; i<len;i++){
							code[i+3]=ip[i];
						}
						
						return code;	
					}
					
					case TYPE_INFO:
					{
						
						byte[] info = mInfo.getBytes();
						int len = info.length;
						byte[] code = new byte[len+1]; 
						code[0] = type;
						
						for(int i=0; i<len;i++){
							code[i+1]=info[i];
						}
						
						return code;
					}
					
					default:
						break;
				}
				
				byte[] code = new byte[1]; 
				code[0] = -1;
				return code;
		}

		
		
		public void setCode(byte[] code){
			int len = code.length;
			if(len > 0){
				type = code[0];
				
				switch(type ){
					case TYPE_ASK:
					{
						byte[]  ip = new byte[len-1]; 
						for(int i=0; i<len-1;i++){
							ip[i]=code[i+1];
						}
						
						ipaddress = new String(ip, 0, len-1);
						break;
					}
						
					case TYPE_ANSWER:
					{
						host = code[1];
						state = code[2];
						byte[]  ip = new byte[len-3]; 
						for(int i=0; i<len-3;i++){
							ip[i]=code[i+3];
						}
						
						ipaddress = new String(ip, 0, len-3);
						break;
					}
					
					case TYPE_INFO:
					{
						byte[]  info = new byte[len-1]; 
						for(int i=0; i<len-1;i++){
							info[i]=code[i+1];
						}
						
						mInfo = new String(info, 0, len-1);
						break;
					}
					default:
						break;
					
				}
				
			}
		}
		
	}

	 
	class BroadcastThread extends Thread {
		
		public Handler mHandler; 
		
		public BroadcastThread(){
			
		}
		
		
		class BroadcastHandler extends Handler{
			
            public void handleMessage(Message msg) {  
                // process incoming messages here  
           	 switch(msg.what){
           	 	case SEARCH:
           	 		break;
           	 	default:
           	 		break;
           	 }
            }  
		}
		
		boolean sendMessage(byte[] message, String ipAddress, int port){
			boolean success = false;
			
			try {

			    DatagramSocket socket = new DatagramSocket(9090);

			        InetAddress serverAdd = null;

			        try {

			            serverAdd = InetAddress.getByName(ipAddress);

			        } catch (UnknownHostException e) {
			        	e.printStackTrace();
			            return success;   
			        }

			    DatagramPacket packet = new DatagramPacket(message,message.length,serverAdd,port);

			    try {
			    	
			        socket.send(packet);

			        socket.close();


			    } catch (IOException e) {

			        // TODO Auto-generated catch block

			        e.printStackTrace();
			        return success;   
			    }

			} catch (SocketException e) {

			    // TODO Auto-generated catch block
			    e.printStackTrace();
			    return success;   
			}
			
			return true;   
		}
		
		void onStop(){
			Looper.myLooper().quit();
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Looper.prepare();
			
	        mHandler = new BroadcastHandler();
	         
			Looper.loop();	
		}
		
		
		
	}
	
	
	class ReceiverThread extends Thread {
		
		private int port = 10000;
		private Handler parentHandler = null;
		private boolean run = false;
		
		
		DatagramSocket udpSocket =null;
		
		private  byte[] data = new byte[256];
		private  DatagramPacket udpPacket = new DatagramPacket( data, 256 );
		
		public ReceiverThread(Handler handler, int pt){
			port = pt;
			parentHandler = handler; 
			run = true;
			
			start();
			
		}
		
		void onStop(){
			
			run = false;
			
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				   udpSocket = new DatagramSocket( port );
			} catch (SocketException e) {
				   e.printStackTrace();
			}
			
			while(run){
			   try {    
					udpSocket.receive(udpPacket);
			   } catch (Exception e) {
				   e.printStackTrace();
			   }
			   
			   if( udpPacket.getLength() != 0 ){
				   udpPacket.getSocketAddress()
				    String codeString = new String( data, 0, udpPacket.getLength() );
				    Log.d(TAG,codeString );
			   }
				
			};
		}		
	}
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.udp_test_activity);
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
	  BroadcastThread td= new BroadcastThread();
	  td.start();
		
	  super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	
	
//	private String getLocalIPAddress() {
//		try {
//			for (Enumeration<NetworkInterface> en = NetworkInterface
//					.getNetworkInterfaces(); en.hasMoreElements();) {
//				NetworkInterface intf = en.nextElement();
//				for (Enumeration<InetAddress> enumIpAddr = intf
//						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//					InetAddress inetAddress = enumIpAddr.nextElement();
//					if (!inetAddress.isLoopbackAddress()) {
//						Log.d(TAG, "IP:"+inetAddress.getHostAddress());
//						return inetAddress.getHostAddress();
//					}
//				}
//			}
//		} catch (SocketException ex) {
//			Log.e(TAG, ex.toString());
//		}
//		return null;
//	}
	
	private String intToIp(int i)  {
		return (i & 0xFF)+ "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) +"."+((i >> 24 ) & 0xFF );
	}
	
	private String getLocalWifiIPAddress(){
		WifiManager wifim =(WifiManager)this.getSystemService(WIFI_SERVICE);
		if(wifim != null){
			WifiInfo  wi = wifim.getConnectionInfo();
			int ip=wi.getIpAddress();
			return intToIp(ip);
		}
		
		return null;
	}

}
