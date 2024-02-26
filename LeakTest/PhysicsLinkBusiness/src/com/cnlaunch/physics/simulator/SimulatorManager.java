package com.cnlaunch.physics.simulator;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.io.PhysicsInputStreamWrapper;
import com.cnlaunch.physics.io.PhysicsOutputStreamWrapper;
import com.cnlaunch.physics.utils.ByteBufferStream;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.remote.ReadByteDataStream;

/**
 * [虚拟DPU连接管理]
 * * @author 谢飞虹
 * @version 1.0
 * @date 2019-7-10
 **/
public class SimulatorManager implements IPhysics {
	private static final String TAG = "SimulatorManager";
	private static String mReadData = "";
	private Context mContext;
	private ReadByteDataStream mReadByteDataStreamThread;
	private boolean mIsTruckReset;
	private int mState;
	private boolean commandWait = true;
	private boolean isFix;
	private DeviceFactoryManager mDeviceFactoryManager;
	private String mSerialNo;
	private InputStream inputStream;
	private OutputStream outputStream;
	private DataByteBufferStream mNetworkAnswerDataByteBufferStream;
	private DataByteBufferStream mDiagnoseRequestDataByteBufferStream;
	private StreamThread mStreamThread;
	private boolean mIsRemoteClientDiagnoseMode;
	private boolean mIsSupportOneRequestMoreAnswerDiagnoseMode;
	public SimulatorManager(DeviceFactoryManager deviceFactoryManager,
			Context context, boolean isFix,String serialNo) {
		// 引用diagnoseactivity
		// 会使使用activity作为接头的fragment释放不掉
		mContext = context.getApplicationContext();
		this.isFix = isFix;
		mDeviceFactoryManager = deviceFactoryManager;
		mReadByteDataStreamThread = null;
		mNetworkAnswerDataByteBufferStream = null;
		mDiagnoseRequestDataByteBufferStream = null;
		mStreamThread = null;
		mState = STATE_NONE;
		mSerialNo = serialNo;
		inputStream = null;
		outputStream = null;
		mIsTruckReset = false;
		mIsRemoteClientDiagnoseMode = false;
		mIsSupportOneRequestMoreAnswerDiagnoseMode = false;
	}

	@Override
	protected void finalize() {
		try {
			MLog.e(TAG, "finalize SerialPortManager");
			mReadByteDataStreamThread = null;
			mNetworkAnswerDataByteBufferStream = null;
			mDiagnoseRequestDataByteBufferStream = null;
			mStreamThread = null;
			mHandler = null;
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取读取到的完整指令
	 * 
	 * @return
	 */
	public  String getCommand() {
		MLog.e(TAG, "获取读取到的完整指令" + mReadData);
		return mReadData;
	}

	/**
	 * 设置完整指令内容
	 */
	public  void setCommand(String command) {
		mReadData = command;
		mDeviceFactoryManager.send(command);
	}

	public void setCommand(String command, boolean isSupportSelfSend) {
		if(isSupportSelfSend) mReadData = command;
		else setCommand(command);
	}

	/**
	 * 获取读数据流
	 */
	public InputStream getInputStream() {
		if(inputStream == null){
			inputStream = new SimulatorInputStream(this);
		}
		return inputStream;
	}

	/**
	 * 获取写数据流
	 */
	public OutputStream getOutputStream() {
		if(outputStream == null){
			outputStream = new SimulatorOutputStream(this,mDeviceFactoryManager.getIPhysicsOutputStreamBufferWrapper());
		}
		return outputStream;
	}

	private void setState(int state) {
		mState = state;
	}

	@Override
	public int getState() {
		return mState;
	}

	@Override
	public synchronized boolean getCommand_wait() {
		return commandWait;
	}

	@Override
	public synchronized void setCommand_wait(boolean wait) {
		commandWait = wait;
	}

	/**
	 * 获取上下文
	 */
	@Override
	public Context getContext() {
		return mContext;
	}

	@Override
	public String getDeviceName() {
		return null;
	}

	@Override
	public void closeDevice() {
		if (mReadByteDataStreamThread != null) {
			mReadByteDataStreamThread.cancel();
			mReadByteDataStreamThread = null;
		}
		if(mNetworkAnswerDataByteBufferStream !=null){
			mNetworkAnswerDataByteBufferStream.close();
		}
		if(mDiagnoseRequestDataByteBufferStream != null){
			mDiagnoseRequestDataByteBufferStream.close();
		}
		if(mStreamThread != null){
			mStreamThread.stopThread();
		}
		setState(STATE_NONE);
	}

	/**
	 * 打开串口，立即返回连接状态
	 * @return
	 */
	public int connect() {
		if (mReadByteDataStreamThread != null) {
			mReadByteDataStreamThread.cancel();
			mReadByteDataStreamThread = null;
		}

		if (mNetworkAnswerDataByteBufferStream != null) {
			mNetworkAnswerDataByteBufferStream.close();
		}
		if (mDiagnoseRequestDataByteBufferStream != null) {
			mDiagnoseRequestDataByteBufferStream.close();
		}
		if (mStreamThread != null) {
			mStreamThread.stopThread();
		}
		mNetworkAnswerDataByteBufferStream = new DataByteBufferStream();
		mDiagnoseRequestDataByteBufferStream = new DataByteBufferStream();
		mStreamThread = new StreamThread(this);
		mStreamThread.start();

		mReadByteDataStreamThread = new ReadByteDataStream(this, getInputStream(), getOutputStream());
		new Thread(mReadByteDataStreamThread).start();
		MLog.d(TAG, "simulator connected success,starting transfer data ");
		mHandler.sendEmptyMessage(0);
		setState(STATE_CONNECTED);
		//监听诊断板状态
		return getState();
	}


	private void connectionFailed(String message) {
		setState(STATE_NONE);
		//辅助通讯设备不能通过发送广播与外界通讯
		// 发送连接失败广播
		Intent intent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_FAIL);
		intent.putExtra(IS_CONNECT_FAIL, true);
		intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
		if (message == null) {
			intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.msg_simulator_connect_state_fail));
		} else {
			intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, message);
		}
		mContext.sendBroadcast(intent);
	}

	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				//辅助通讯设备不能通过发送广播与外界通讯
					Intent intent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS);
					intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
					intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.msg_simulator_connect_state_success));
					mContext.sendBroadcast(intent);
					// 发送广播通知连接成功
					mContext.sendBroadcast(new Intent(ACTION_DIAG_CONNECTED));

			} else if (msg.what == 1) {
					Intent disconnectIntent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_DISCONNECTED);
					disconnectIntent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
					mContext.sendBroadcast(disconnectIntent);

			}
		}
	};
	@Override
	public void setSerialNo(String serialNo) {
		mSerialNo = serialNo;		
	}
	@Override
	public String getSerialNo() {		
		return mSerialNo;
	}
	@Override
	public synchronized void setIsTruckReset(boolean isTruckReset) {
		mIsTruckReset = 	isTruckReset;
	}

	@Override
	public synchronized boolean isTruckReset() {
		return mIsTruckReset;
	}

	@Override
	public void userInteractionWhenDPUConnected() {
		if(mHandler!=null){
			Message message =mHandler.obtainMessage(0, 0, 0);
			mHandler.sendMessage(message);
		}
	}

	@Override
	public void setIsFix(boolean isFix) {
		this. isFix = isFix;
	}

	@Override
	public void physicalCloseDevice() {
		closeDevice();
	}


	@Override
	public void setIsRemoteClientDiagnoseMode(boolean isRemoteClientDiagnoseMode) {
		mIsRemoteClientDiagnoseMode = isRemoteClientDiagnoseMode;
	}

	@Override
	public boolean getIsRemoteClientDiagnoseMode() {
		return mIsRemoteClientDiagnoseMode;
	}

	public int read(byte[] readbuffer) {
		return read(readbuffer,0,readbuffer.length);
	}
	public int read(byte[] readbuffer,int offset, int length) {
		return mNetworkAnswerDataByteBufferStream.read(readbuffer,offset,length);
	}
	public void write(byte[] b, int offset, int length) {
		mDiagnoseRequestDataByteBufferStream.write(b, offset, length);
	}
	public static class DataByteBufferStream extends ByteBufferStream {
		private static final String TAG = "NetworkAnswerDataByteBufferStream";
		private final Lock mNotificationLock;
		private final Condition mNotificationCondition;

		public DataByteBufferStream() {
			super();
			mNotificationLock = new ReentrantLock();
			mNotificationCondition = mNotificationLock.newCondition();
		}

		public void close() {
			mNotificationLock.lock();
			try {
				mNotificationCondition.signal();
			} finally {
				mNotificationLock.unlock();
			}
		}

		@Override
		public void write(byte[] writeBuffer, int offset, int length) {
			mNotificationLock.lock();
			try {
				super.write(writeBuffer, offset, length);
				mNotificationCondition.signal();
			} finally {
				mNotificationLock.unlock();
			}
		}

		public int read(byte[] readbuffer,int offset, int length) {
			int count;
			mNotificationLock.lock();
			try {
				if(this.length<=0){
					mNotificationCondition.await();
				}
				count = super.readBytes(readbuffer,offset,length);
			} catch (InterruptedException e) {
				count = 0;
				e.printStackTrace();
			}
			mNotificationLock.unlock();
			return count;
		}
	}

	public DataByteBufferStream getNetworkAnswerDataByteBufferStream() {
		return mNetworkAnswerDataByteBufferStream;
	}

	public DataByteBufferStream getDiagnoseRequestDataByteBufferStream() {
		return mDiagnoseRequestDataByteBufferStream;
	}
	public void outputStreamClose() {
		mDiagnoseRequestDataByteBufferStream.close();
	}
	public void inputStreamClose() {
		mNetworkAnswerDataByteBufferStream.close();
	}
	@Override
	public void setIsSupportOneRequestMoreAnswerDiagnoseMode(boolean isSupportOneRequestMoreAnswerDiagnoseMode){
		mIsSupportOneRequestMoreAnswerDiagnoseMode=isSupportOneRequestMoreAnswerDiagnoseMode;
	}
	@Override
	public boolean getIsSupportOneRequestMoreAnswerDiagnoseMode(){
		return mIsSupportOneRequestMoreAnswerDiagnoseMode;
	}

	/**
	 * @param isSetCommandOfMainLink 设置数据是否传给蓝牙主连接（针对蓝牙一对多的项目）
	 */
	@Override
	public void setCommandStatus(boolean isSetCommandOfMainLink) {

	}

	@Override
	public boolean getCommandStatus() {
		return false;
	}
	private String dataType;
	@Override
	public void setDataType(String type) {
		dataType = type;
	}

	@Override
	public String getDataType() {
		return dataType;
	}
}
