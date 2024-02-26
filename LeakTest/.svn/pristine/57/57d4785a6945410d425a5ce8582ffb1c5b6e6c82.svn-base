package com.cnlaunch.physics.utils;
import java.io.IOException;

import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.impl.IPhysics;

import android.net.LocalServerSocket;
import android.net.LocalSocket;

public class LocalServerSocketThread extends Thread {
	private static String TAG =  "LocalServerSocketThread";	
	LocalServerSocket server;
	LocalSocketAcceptThread acceptThread;
	private boolean isStop;
	private DeviceFactoryManager mDeviceFactoryManager;
	public LocalServerSocketThread(DeviceFactoryManager deviceFactoryManager){
		mDeviceFactoryManager = deviceFactoryManager;
		acceptThread = null;
		isStop = false;
		try {
			server = new LocalServerSocket("com.local.socket");
			MLog.d(TAG, "server create success");
		} catch (IOException e) {
			server = null;
			e.printStackTrace();
		}
		catch (Exception e) {
			server = null;
			e.printStackTrace();
		}
	}
	public LocalServerSocket getLocalServerSocket(){
		return server;
	}
	@Override
	public void run() {	
		LocalSocket tempConnect;
		while(getStopFlag() == false){				
				try {
					tempConnect = server.accept();
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				catch (Exception e) {
					e.printStackTrace();
					break;
				}
				if(tempConnect == null){
					MLog.d(TAG,"accept null socket");
					continue;
				}
				else{
					if(acceptThread!=null){
						acceptThread.stopThread();
						acceptThread = null;
					}
					acceptThread = new LocalSocketAcceptThread(tempConnect,mDeviceFactoryManager);
					acceptThread.start();
				}
		}
	}
	private synchronized boolean getStopFlag(){
		return isStop;
	}
	/**
	 * 停止当前连接的活动的socket
	 */
	public synchronized void stopActiveSocketThread(){
		if(acceptThread!=null){	
			MLog.d(TAG,"acceptThread is stop");
			acceptThread.stopThread();			
			acceptThread = null;
			
		}
	}
	public synchronized void stopThread(){
		try {	
			if(acceptThread!=null){
				MLog.d(TAG,"acceptThread is stop");
				acceptThread.stopThread();
				acceptThread = null;				
			}
			if (server != null){
				MLog.d(TAG,"server is close");
				server.close();
				server = null;				
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
		isStop = true;
	}
	public void send(String command) {
		if(acceptThread!=null){
			acceptThread.send(command);
		}
	}
}

