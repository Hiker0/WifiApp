package com.cc.wifiapp.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cc.wifiapp.R;

public class SocketActivity extends Activity {
	final static String TAG = "wifiapp";
	
	Socket socket = null;
	
	
    //��������

    String data = "Hello";
    
    Button send= null;
    
    SocketServerThread mServerThread = null; 
    
    ClientThread mClientThread;
    
    
    
    class ChatThread extends Thread{
    	
    	Socket mSocket;
    	
        OutputStream os = null;

        InputStream is = null;
    	
    	public ChatThread(SocketServerThread parent,Socket socket){
    		
    		mSocket = socket;
			try {
				os = mSocket.getOutputStream();
				is = mSocket.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(TAG,"������������һ������");
			
			start();
	
    	}
    	
		void onStop(){
			try{			
			        //�ر���������
			
	            os.close();
	
	            is.close();
	            
	            mSocket.close();
			
			 }catch(Exception e){
				 
			 }finally{
				Log.d(TAG,"���������ر�һ������");
				finish(); 
			 }	
        }
		

		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[] b = new byte[1024];
			while(true){
				 int n;
				try {
					n = is.read(b);
					//is.reset();
	                 //���

	                 Log.d(TAG,"����������������Ϊ��" + new String(b,0,n));

	                 //��ͻ��˷��ͷ�������

	                 os.write(b, 0, n);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			

			}
			
			
		}
    	
    	
    	
    }

    class ClientThread extends Thread{
    	
    	Socket mSocket;
    	
        OutputStream os = null;

        InputStream is = null;
    	
    	public ClientThread(Socket socket){
    		
    		mSocket = socket;
			try {
				os = mSocket.getOutputStream();
				is = mSocket.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.d(TAG,"�ͻ��ˣ���ʼ����");
			
			start();
	
    	}
    	
		void onStop(){
			try{			
			        //�ر���������
			
	            os.close();
	
	            is.close();
	            
	            mSocket.close();
			
			 }catch(Exception e){
				 
			 }finally{
				Log.d(TAG,"�ͻ��ˣ��Ͽ�����");
				finish(); 
			 }	
        }
		
		void sendMessage(String data){
		
			 try {
				os.write(data.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		@Override
		public void run() {
			// TODO Auto-generated method stub
			byte[] b = new byte[1024];
			while(true){
				 int n;
				try {
					n = is.read(b);
					
	                Log.d(TAG,"�ͻ��ˣ���������Ϊ��" + new String(b,0,n));

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			

			}
			
			
		}
    	
    	
    	
    }
    
    
    class SocketServerThread extends Thread{
    	ServerSocket serverSocket = null;
        //�����˿ں�

        int port = 10000;
        ArrayList<ChatThread> list= new ArrayList<ChatThread>();
        
        public SocketServerThread(){
        	start();
        }
        
		void onStop(){
			try{			
			        //�ر���������	
	            serverSocket.close();			
			 }catch(Exception e){
				 
			 }
			
            for(ChatThread ct:list){
            	
            	ct.onStop();   	
            }
            list.clear();
            
            finish();
        }
		
		void closeChild(ChatThread ct){
			
			ct.onStop();
			list.remove(ct);		
		}
        
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			
            
            Log.d(TAG,"������:����" );


            Socket socket = null;

            try {

                     //��������
                 serverSocket = new ServerSocket(port);

                 
                 while(true){
                	 
                     //�������
                     socket = serverSocket.accept();
                     
                     ChatThread ct= new ChatThread(this,socket);
                     if(! list.contains(ct)){
                    	 list.add(ct);
                     }
                     
                 }

            } catch (Exception e) {

                     e.printStackTrace();

            }finally{

            }
         	
 			super.run();
 		}
     	
     }
    
	class CreateAsyncTask extends AsyncTask<URL, Socket, Integer> {
		

		@Override
		protected Integer doInBackground(URL... params) {
			// TODO Auto-generated method stub
		    //��������IP��ַ

		    String serverIP = "127.0.0.1";

		    //�������˶˿ں�

		    int port = 10000;
		    
	        try {
	        	
	        	Socket st = new Socket(serverIP,port);
	        	Log.d(TAG,"�ͻ��ˣ���������");
	        	publishProgress(st);
		        
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, "UnknownHost");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	        
	        return null;
	       				
		}

		@Override
		protected void onProgressUpdate(Socket... values) {
			
			mClientThread = new ClientThread(values[0]);
	        
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub  

			send.setEnabled(true);
			
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			send.setEnabled(false);
			super.onPreExecute();
		}
		

	};
	
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
		
		mServerThread = new SocketServerThread();
		
		new CreateAsyncTask().execute(null,null,null);
		
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mServerThread.onStop();
		
		mClientThread.onStop();

	}
	

}
