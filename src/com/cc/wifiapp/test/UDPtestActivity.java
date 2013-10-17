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
import android.view.View;
import android.widget.Button;

import com.cc.wifiapp.R;

public class UDPtestActivity extends Activity {

	final static String TAG = "wifiapp";
	final static int SEARCH=200;
    String data = "Hello";
    
    Button send= null;
	
	private BroadcastThread mBroadcastThread=null;
	private ReceiverThread  mReceiverThread =null;
	private Handler mHandler = new Handler(){
		
	}; 
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.udp_test_activity);
		
		send = (Button) findViewById(R.id.button_socket_send); 
		
		send.setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mBroadcastThread != null){
					Protocal mpt = new Protocal();
					mpt.type = Protocal.TYPE_INFO;
					mpt.mInfo = data;
					mBroadcastThread.sendMessage(mpt.getCode(), "255.255.255.255", 10000);
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		mBroadcastThread = new BroadcastThread(10001);
		mReceiverThread  = new ReceiverThread(mHandler,10000);
	
	  super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.d(TAG,"UDP:stop");
		mBroadcastThread.onStop();
		mReceiverThread.onStop();
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
	
	private int getLocalWifiIPAddress(){
		WifiManager wifim =(WifiManager)this.getSystemService(WIFI_SERVICE);
		if(wifim != null){
			WifiInfo  wi = wifim.getConnectionInfo();
			return wi.getIpAddress();
		}
		
		return 0;
	}
	
	private byte[] intToByteArray(final int integer) {
		int byteNum = (40 -Integer.numberOfLeadingZeros (integer < 0 ? ~integer : integer))/ 8;
		byte[] byteArray = new byte[4];

		for (int n = 0; n < byteNum; n++)
		byteArray[3 - n] = (byte) (integer>>> (n * 8));

		return (byteArray);
		}


	private int byteArrayToInt(byte[] b, int offset) {
	       int value= 0;
	       for (int i = 0; i < 4; i++) {
	           int shift= (4 - 1 - i) * 8;
	           value +=(b[i + offset] & 0x000000FF) << shift;
	       }
	       return value;
	 }
	
	
	/***************************************************************************************************************/
	
	
	
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
			public int ipaddress = 0;
			public String mInfo = null;
			public byte host = HOST_UNKNOWN;
			public byte state = STATE_UNKNOWN;
			
			
			
			/***********Protocal************
			 *  Ask
			 *  1BYTE BYTES
			 *  |TYPE|len|IP...|
			 *  =========================
			 *  Answer
			 *  1BYTE 1BYTE 1BYTE BYTES
			 *  |TYPE|len|HOST|STATE|IP...|
			 *  =========================
			 *  Info
			 *  1BYTE BYTES
			 *  |TYPE|len|INFO...|
			 *  
			 *****************************/
			
			
			public Protocal (){
				
			}
			
			public Protocal (byte[] code){
				
				if(code.length >= 2){
					type = code[0];
					
					switch(type ){
						case TYPE_ASK:
						{
							ipaddress = byteArrayToInt(code,2);
							break;
						}
							
						case TYPE_ANSWER:
						{
							byte len = code[1];
							host = code[2];							
							state = code[3];
							byte[]  ip = new byte[len-4]; 
							for(int i=0; i<len-4;i++){
								ip[i]=code[i+4];
							}
							
							ipaddress  = byteArrayToInt(code,4);
							break;
						}
						
						case TYPE_INFO:
						{
							byte len = code[1];
							mInfo = new String(code, 2, len-2);
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
							
							byte[] ip = intToByteArray(ipaddress);
							byte len = (byte) ip.length;
							byte[] code = new byte[len+4]; 
							code[0] = type;
							code[1] = (byte) (len+2); 
							
							for(int i=0; i<len;i++){
								code[i+2]=ip[i];
							}
							
							return code;
						}
						case TYPE_ANSWER:
						{
							byte[] ip = intToByteArray(ipaddress);
							byte len = (byte) ip.length;
							byte[] code = new byte[len+4]; 
							
							code[0] = type;
							code[1] = (byte) (len+2);		
							code[2] = type;
							code[3] = type;
							
							for(int i=0; i<len;i++){
								code[i+4]=ip[i];
							}
							
							return code;	
						}
						
						case TYPE_INFO:
						{
							
							byte[] info = mInfo.getBytes();
							byte len = (byte) info.length;
							byte[] code = new byte[len+2]; 
							code[0] = type;
							code[1] = (byte) (len+2);
							
							for(int i=0; i<len;i++){
								code[i+2]=info[i];
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
				
				if(code.length >= 2){
					type = code[0];
					
					switch(type ){
						case TYPE_ASK:
						{
							ipaddress = byteArrayToInt(code,2);
							break;
						}
							
						case TYPE_ANSWER:
						{
							byte len = code[1];
							host = code[2];							
							state = code[3];
							byte[]  ip = new byte[len-4]; 
							for(int i=0; i<len-4;i++){
								ip[i]=code[i+4];
							}
							
							ipaddress  = byteArrayToInt(code,4);
							break;
						}
						
						case TYPE_INFO:
						{
							byte len = code[1];
							mInfo = new String(code, 2, len-2);
							break;
						}
						default:
							break;
						
					}
					
				}
			}
			public String getInfo(){
				return mInfo;
			}
			public int getIP(){
				return ipaddress;
			}
			
		}

		 
	class BroadcastThread extends Thread {

		
		final static int STATE_IDLE = 0;
		final static int STATE_NEED = 1;
		final static int STATE_SENDING = 2;
		
		private int mSendPort;
		private boolean run = false;
		
		
		byte[]  mData = null;
		String  recAddress = null;
		int     recPort = -1;		
		int     state = STATE_IDLE;
  
		public BroadcastThread(int port) {
			mSendPort = port;
			run = true;
			start();
		}
		
		void onStop() {
			Log.d(TAG," Broadcast:stop");
			run = false;
		}
		
		public void sendMessage(byte[] message, String ipAddress, int port){
			
			mData = message;
			recAddress = ipAddress;
			recPort = port;		
			state = STATE_NEED;
			
		}
		
		private int sendMessageInter() {
				
			int success = 1;
			if (state == STATE_NEED) {
				state = STATE_IDLE;
				success = 0 ;
				try {

					DatagramSocket socket = new DatagramSocket(mSendPort);

					InetAddress serverAdd = null;

					try {

						serverAdd = InetAddress.getByName(recAddress);

					} catch (UnknownHostException e) {
						e.printStackTrace();
						success = 2;
					}

					DatagramPacket packet = new DatagramPacket(mData,
							mData.length, serverAdd, recPort);

					try {

						socket.send(packet);

						socket.close();

					} catch (IOException e) {

						// TODO Auto-generated catch block

						e.printStackTrace();
						success = 3;
					}

				} catch (SocketException e) {

					// TODO Auto-generated catch block
					e.printStackTrace();
					success = 4;
				}
				

			}
			return success;
		}



		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			while(run){
				sendMessageInter() ;
				try {
					sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Log.d(TAG," Broadcast:stoped");
		}
	}
		
		
	class ReceiverThread extends Thread {

		private int port = 10000;
		private Handler parentHandler = null;
		private boolean run = false;

		DatagramSocket udpSocket = null;

		private byte[] data = new byte[256];
		private DatagramPacket udpPacket = new DatagramPacket(data, 256);

		public ReceiverThread(Handler handler, int pt) {
			port = pt;
			parentHandler = handler;
			run = true;

			start();

		}

		void onStop() {
			
			run = false;
			udpSocket.close();
			Log.d(TAG," ReceiverThread:stop");
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				udpSocket = new DatagramSocket(port);
			} catch (SocketException e) {
				e.printStackTrace();
			}

			while (run) {
				try {
					udpSocket.receive(udpPacket);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (udpPacket.getLength() != 0) {
					String codeString = new Protocal(data).getInfo();
					if(codeString != null){
						Log.d(TAG, codeString);
					}
				}
				
				Log.d(TAG," ReceiverThread:stoped");

			};
		}
	}
		
		

}
