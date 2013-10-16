package com.cc.wifiapp.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cc.wifiapp.R;

public class SocketActivity extends Activity {
	final static String TAG = "wifiapp";
	final static int CLIENT_OK = 100;

	
    //发送内容

    String data = "Hello";
    
    Button send= null;
    
    SocketServerThread mServerThread = null; 
    
    ClientThread mClientThread;
    Handler mHandler=null;
    
    
    
	class ChatThread extends Thread {

		Socket mSocket;

		OutputStream os = null;

		InputStream is = null;

		boolean run = false;

		public ChatThread(SocketServerThread parent, Socket socket) {

			mSocket = socket;
			Log.d(TAG, "服务器：建立一个连接");

			run = true;

			start();

		}

		void onStop() {
			run = false;
			try {
				// 关闭流和连接

				os.close();

				is.close();

				mSocket.close();

			} catch (Exception e) {

			}

			Log.d(TAG, "服务器：关闭一个连接");
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[] b = new byte[1024];

			try {
				os = mSocket.getOutputStream();
				is = mSocket.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while (run) {
				int n;
				try {
					n = is.read(b);
					// is.reset();
					// 输出
					if (n > 0) {
						Log.d(TAG, "服务器：接收内容为：" + new String(b, 0, n));

						// 向客户端发送反馈内容

						os.write(b, 0, n);
						os.flush();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "服务器：OutputStream closed");
					e.printStackTrace();
				}

			}

		}

	}

	class ClientThread extends Thread {
		
	    String serverIP = null;;

	    //服务器端端口号

	    int port = 10000;
	    

		Socket mSocket;

		OutputStream os = null;

		InputStream is = null;

		boolean run = false;
		
		Handler mhandler = null;

		public ClientThread(Socket socket) {

			mSocket = socket;
			try {
				os = mSocket.getOutputStream();
				is = mSocket.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "客户端：Stream fail");
			}
			Log.d(TAG, "客户端：开始侦听");

			run = true;

			start();

		}
		
		public ClientThread(Handler handler,String ip,int pt){
			run = true;
			mhandler = handler;
			if(ip!=null){
				serverIP = ip;
			}else{
				serverIP = "127.0.0.1";
			}
			port = pt;
			
			
			start();
			Log.d(TAG, "客户端：开始侦听");
		}

		void onStop() {
			run = false;
			if(mSocket!=null){
				try {
					// 关闭流和连接
	
					os.close();
	
					is.close();
	
					mSocket.close();
	
				} catch (Exception e) {
	
				} finally {
					Log.d(TAG, "客户端：断开连接");
				}
			}

		}

		void sendMessage(String data) {
			if(run ){
				try {
					os.write(data.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		@Override
		public void run() {

		    if(mSocket == null){
		        try {
		        	
		        	mSocket = new Socket(serverIP,port);
		        	Log.d(TAG,"客户端：建立连接");
					os = mSocket.getOutputStream();
					is = mSocket.getInputStream();
					
					Message m = mhandler.obtainMessage(CLIENT_OK);
										
					mhandler.sendMessage(m);
					
			        
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "客户端 :UnknownHost");
					run = false;
	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d(TAG, "客户端:Stream fail");
					run = false;
				}
		    }
			// TODO Auto-generated method stub
			byte[] b = new byte[1024];
			
			
			while (run) {
				int n;
				try {
					n = is.read(b);
					if (n > 0) {

						Log.d(TAG, "客户端：接收内容为：" + new String(b, 0, n));
					}

				} catch (IOException e) {

				}

			}
			
		}

	}
    
    
    class SocketServerThread extends Thread{

        //监听端口号
    	ServerSocket serverSocket = null;
        int port = 10000;
        boolean run = false;
        
        ArrayList<ChatThread> list= new ArrayList<ChatThread>();
        
        public SocketServerThread(int pt){
        	run = true;
        	port = pt;
        	
        	start();
        }
        
		void onStop(){
			
			run = false;
			

			for (ChatThread ct : list) {

				ct.onStop();
			}
			
			try {
				// 关闭流和连接
				serverSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Log.d(TAG, "服务器:关闭");

			list.clear();
        }
		
		void closeChild(ChatThread ct){
			
			ct.onStop();
			list.remove(ct);		
		}
        
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			
            
            Log.d(TAG,"服务器:启动" );

            Socket socket = null;

            try {

                     //建立连接
                 serverSocket = new ServerSocket(port);

            } catch (Exception e) {

            	Log.d(TAG,"服务器:ServerSocket fail" );

            }
         	

            
            while(run){
            	if(serverSocket == null){
            		break;
            	}
                //获得连接
                try {
					socket = serverSocket.accept();
	                ChatThread ct= new ChatThread(this,socket);
	                if(! list.contains(ct)){
	               	 list.add(ct);
	                }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					 Log.d(TAG,"服务器:ServerSocket= null" );
				}   
            }
            
           
 		}
     	
     }
    
//	class CreateAsyncTask extends AsyncTask<URL, Socket, Integer> {
//		
//
//		@Override
//		protected Integer doInBackground(URL... params) {
//			// TODO Auto-generated method stub
//		    //服务器端IP地址
//
//		    String serverIP = "127.0.0.1";
//
//		    //服务器端端口号
//
//		    int port = 10000;
//		    
//	        try {
//	        	
//	        	Socket st = new Socket(serverIP,port);
//	        	Log.d(TAG,"客户端：建立连接");
//	        	publishProgress(st);
//		        
//			} catch (UnknownHostException e) {
//				// TODO Auto-generated catch block
//				Log.d(TAG, "UnknownHost");
//				e.printStackTrace();
//				return null;
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return null;
//			}
//	        
//	        return null;
//	       				
//		}
//
//		@Override
//		protected void onProgressUpdate(Socket... values) {
//			
//			mClientThread = new ClientThread(values[0]);
//	        
//			super.onProgressUpdate(values);
//		}
//
//		@Override
//		protected void onPostExecute(Integer result) {
//			// TODO Auto-generated method stub  
//
//			send.setEnabled(true);
//			
//			super.onPostExecute(result);
//		}
//
//		@Override
//		protected void onPreExecute() {
//			// TODO Auto-generated method stub
//			send.setEnabled(false);
//			super.onPreExecute();
//		}
//		
//
//	};
	
	//*******************************************************************************************************************************//
      
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.socket_activity);
		
		send = (Button) findViewById(R.id.button_socket_send); 
		
		send.setOnClickListener(new View.OnClickListener() {
				
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mClientThread != null){
					mClientThread.sendMessage(data);
				}
			}
		});
		
		send.setEnabled(false);
		
		mHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch(msg.what){
					case CLIENT_OK:
						send.setEnabled(true);
						break;
					default:
						break;
					
				}
			}
			
		};
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		mServerThread = new SocketServerThread(10000);
		mClientThread = new ClientThread(mHandler,"127.0.0.1",10000);
		//new CreateAsyncTask().execute(null,null,null);
		
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.d(TAG,"wifiapp:关闭" );
		
		mServerThread.onStop();
		mServerThread= null;
		
		mClientThread.onStop();
		mClientThread=null;
		
		super.onStop();

	}
	

}
