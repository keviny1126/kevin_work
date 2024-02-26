package com.cnlaunch.physics.utils;

import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.cnlaunch.physics.impl.IPhysics;

/**
 * 写数据流工具
 * [A brief description]
 * 
 * @author bichuanfeng
 * @version 1.0
 * @date 2014-3-10
 * 
 *
 */
public class DownloadBinWriteByte implements Runnable {
	private OutputStream outputStream;
	private byte[] buffer;
	private String TAG = "WriteByteData";
	private Timer timer;
	private IPhysics mIPhysics = null;
	public static int DELAY = 8000;
	private Bridge mBridge;
	public DownloadBinWriteByte(Bridge bridge,byte[] buffer, int delay, IPhysics pIPhysics) {
		super();
		this.buffer = buffer;
		mIPhysics = pIPhysics;
		DELAY = delay;
		mBridge = bridge;
	}

	public DownloadBinWriteByte(Bridge bridge,byte[] buffer, IPhysics pIPhysics) {
		super();
		this.buffer = buffer;
		mIPhysics = pIPhysics;
		mBridge = bridge;
	}

	@Override
	public void run() {
		try {
			outputStream = mIPhysics.getOutputStream();
			outputStream.write(buffer);
			timer = null;
			timer = new Timer();
			timer.schedule(new MyTimerTasks(), DELAY);			
			mBridge.getData();
		} catch (Exception e) {
			Log.e(TAG, "Exception during write", e);
		}
	}

	class MyTimerTasks extends TimerTask {
		@Override
		public void run() {
			mBridge.putData();
		}
	}
}
