package com.cnlaunch.physics.utils;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import com.cnlaunch.physics.DeviceFactoryManager;
import android.net.LocalSocket;

public class LocalSocketAcceptThread extends Thread {
	private static String TAG = "LocalSocketAcceptThread";
	private LocalSocket mConnect;
	private DeviceFactoryManager mDeviceFactoryManager;
	private boolean isStop;

	public LocalSocketAcceptThread(LocalSocket connect,
			DeviceFactoryManager deviceFactoryManager) {
		mConnect = connect;
		isStop = false;
		mDeviceFactoryManager = deviceFactoryManager;
	}

	@Override
	public void run() {
		try {
			InputStream inputStream = mConnect.getInputStream();			
			//MLog.d(TAG, "read data loop");
			int readcount = 0;
			int bufferSize = 1024*5+250;//5120,最大数据帧长度不超过5500，所以定为5120;
			if(mDeviceFactoryManager != null) {
				if(DeviceFactoryManager.LINK_MODE_SMART_LINK_SOCKET
						== mDeviceFactoryManager.getLinkMode()) {
					bufferSize = 612;//中转服务器限制
				}
			}
			byte[] totalbuffer = new byte[bufferSize];
			//数据发送到蓝牙或其他设备可以不用检查帧完整性，下位机会检查帧的正确性
			while (getStopFlag() == false) {
				try {
					//MLog.d(TAG, "get command start");
					readcount = inputStream.read(totalbuffer);
					//MLog.d(TAG, "get command waiting");					
					while (readcount == bufferSize && inputStream.available() > 0) {
						write(totalbuffer, bufferSize);
						if(MLog.isDebug)MLog.d(TAG, "get sucess command buffer=" + ByteHexHelper.bytesToHexStringWithSearchTable(totalbuffer, 0, readcount));
						//控制日志输出，主要是一些平台资源受限 比如ait2（MT6739芯片）
						//if(MLog.isDebug)MLog.d(TAG, "get sucess command readcount=" + readcount);
						readcount = inputStream.read(totalbuffer);
					}					
					if (readcount > 0 ) {
						write(totalbuffer, readcount);
						if(MLog.isDebug)MLog.d(TAG, "get sucess command buffer=" +ByteHexHelper.bytesToHexStringWithSearchTable(totalbuffer, 0, readcount));
						//控制日志输出，主要是一些平台资源受限 比如ait2（MT6739芯片）
						//if(MLog.isDebug)MLog.d(TAG, "get sucess command readcount=" + readcount);
					}
					else{
						if(mConnect!=null && !mConnect.isConnected()){
							//MLog.d(TAG, "mConnect is  unConnected");
							break;
						}
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} catch (IOException readIOException) {
					readIOException.printStackTrace();
					MLog.d(TAG, "get command IOException");
					continue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized boolean getStopFlag() {
		return isStop;
	}

	public synchronized void stopThread() {
		try {
			MLog.d(TAG, "connect is close");
			if (mConnect != null && mConnect.isConnected()) {				
				mConnect.getInputStream().close();
				mConnect.getOutputStream().close();
				mConnect.close();
			}
			mConnect = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		isStop = true;
	}

	public void send(String command) {
		if (mConnect == null) {
			return;
		}
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(mConnect.getOutputStream()));
			bufferedWriter.write(command);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			//控制日志输出，主要是一些平台资源受限 比如ait2（MT6739芯片）
			/*if(MLog.isDebug) {
				MLog.d(TAG, "send sucess command=" + command);
			}*/
		} catch (IOException e) {
			e.printStackTrace();
			MLog.d(TAG, "send fail command=" + command);
		}
	}

	private void write(byte[] buffer, int count) {
		if (mDeviceFactoryManager != null) {
			mDeviceFactoryManager.write(buffer, count);
		}
	}
}
