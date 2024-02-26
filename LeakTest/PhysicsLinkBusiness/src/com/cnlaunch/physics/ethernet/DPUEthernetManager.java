package com.cnlaunch.physics.ethernet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.io.PhysicsInputStreamWrapper;
import com.cnlaunch.physics.io.PhysicsOutputStreamWrapper;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.remote.ReadByteDataStream;
import com.cnlaunch.physics.utils.Tools;
import com.power.baseproject.utils.SystemPropertiesInvoke;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DPUEthernetManager implements IPhysics {
	private static final String TAG = "DPUEthernetManager";
	private Context mContext;
	/**
	 * Ethernet约定ip地址与端口号
	 */
	private static final String SMARTBOX30_ETHERNET_CONNECT_ADDRESS ="192.168.100.1";
	private static final int    SMARTBOX30_ETHERNET_CONNECT_PORT = 22488;
	private static final int    SMARTBOX30_ETHERNET_CONNECT_SYSTEM_PORT = 22400;
	//UDP广播包端口：22534
	//下位机TCP Server端 透传端口22488
	/**
	 *  网络通讯模式建立连接超时时间
	 */
	private static final int  ETHERNET_CONNECT_TIMEOUT  = 10000; //10S

	private Socket ethernetSocket;
	private ConnectThread mConnectThread;
	private ReadByteDataStream mReadByteDataStreamThread;
	private String mReadData;
	private int mState;
	private boolean commandWait = true;
	
	private int mMode; //wifi使用模式
	private boolean isFix;
	private DeviceFactoryManager mDeviceFactoryManager;
	private String mSerialNo;
	private boolean mIsTruckReset;
	private String mPermisson;
	private InputStream inputStream;
	private OutputStream outputStream;
	private boolean mIsRemoteClientDiagnoseMode;
	private boolean mIsSupportOneRequestMoreAnswerDiagnoseMode;
	Semaphore mySemaphore;
	public DPUEthernetManager(DeviceFactoryManager deviceFactoryManager,Context context,boolean isFix,String serialNo) {
		//引用diagnoseactivity 会使使用activity作为接头的fragment释放不掉
		mContext = context.getApplicationContext();
		this.isFix = isFix;
		mDeviceFactoryManager = deviceFactoryManager;
		mConnectThread = null;
		ethernetSocket = null;
		mReadByteDataStreamThread = null;
		mSerialNo = serialNo;
		mState = STATE_NONE;
		mIsTruckReset = false;
		String APP_USB_PERMISSION = mContext.getPackageName();
		APP_USB_PERMISSION += ".USB_PERMISSION";
		mPermisson = APP_USB_PERMISSION;
		inputStream = null;
		outputStream = null;
		mIsRemoteClientDiagnoseMode = false;
		mIsSupportOneRequestMoreAnswerDiagnoseMode = false;
		mySemaphore = new Semaphore(0);
	}	

	@Override
	protected void finalize() {
		try {
			MLog.e(TAG, "finalize DPUEthernetManager");
			mHandler=null;
			ethernetSocket=null;
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	/**
	 * 获取上下文
	 */
	@Override
	public Context getContext(){
		return mContext;
	}
	@Override
	public synchronized boolean getCommand_wait() {
		return commandWait;
	}

	@Override
	public synchronized void setCommand_wait(boolean wait) {
		commandWait = wait;
	}
	
	@Override
	public String getDeviceName() {
		return null;
	}

	@Override
	public void closeDevice() {
		try{
			mContext.unregisterReceiver(mBroadcastReceiver);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		MLog.d(TAG, "stop ethernet ConnectThread");
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if (mReadByteDataStreamThread != null) {
			mReadByteDataStreamThread.cancel();
			mContext.sendBroadcast(new Intent(ACTION_DIAG_UNCONNECTED));
			mReadByteDataStreamThread = null;
		}
		setState(STATE_NONE);
	}
	private void setState(int state) {
		mState = state;
	}

	@Override
	public int getState() {
		return mState;
	}

	@Override
	public String getCommand() {
		return mReadData;
	}

	@Override
	public void setCommand(String command) {
		mReadData = command;
		mDeviceFactoryManager.send(command);
	}

	@Override
	public void setCommand(String command, boolean isSupportSelfSend) {
		if(isSupportSelfSend) mReadData = command;
		else setCommand(command);
	}

	@Override
	public InputStream getInputStream() {
		try {
			if(inputStream == null){
				inputStream = new PhysicsInputStreamWrapper(ethernetSocket.getInputStream());
			}
			return inputStream;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public OutputStream getOutputStream() {
		try {
			if(outputStream == null){
				outputStream = new PhysicsOutputStreamWrapper(ethernetSocket.getOutputStream(),mDeviceFactoryManager.getIPhysicsOutputStreamBufferWrapper());
			}
			return outputStream;
		} catch (Exception e) {
			return null;
		}
	}
	private boolean semaphoreAcquire(long millTimeout){
		boolean isSuccess=false;
		try {
			if (MLog.isDebug) {
				MLog.d(TAG, "semaphoreAcquire start" );
			}
			isSuccess=mySemaphore.tryAcquire(millTimeout, TimeUnit.MILLISECONDS);
			if (MLog.isDebug) {
				MLog.d(TAG, "semaphoreAcquire end" );
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	private void semaphoreAcquire(){
		try {
			if (MLog.isDebug) {
				MLog.d(TAG, "semaphoreAcquire start" );
			}
			mySemaphore.acquire();
			if (MLog.isDebug) {
				MLog.d(TAG, "semaphoreAcquire end" );
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private void semaphoreRelease(){
		if (MLog.isDebug) {
			MLog.d(TAG, "semaphoreRelease start" );
		}
		mySemaphore.release();
		if (MLog.isDebug) {
			MLog.d(TAG, "semaphoreRelease end" );
		}
	}

	private void startSmartbox30Client(){
		registerSmartbox30ClientIPReceiver();
		Intent intent = new Intent("com.bsk.broadcast.eth.set.dhcp.clients");
		mContext.sendBroadcast(intent);
	}
	private void registerSmartbox30ClientIPReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.bsk.broadcast.eth.service.ip");
		mContext.registerReceiver(mSmartbox30ClientIPBroadcastReceiver, filter);
	}

	private void unRegisterSmartbox30ClientIPReceiver() {
		try{
			mContext.unregisterReceiver(mSmartbox30ClientIPBroadcastReceiver);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	private BroadcastReceiver mSmartbox30ClientIPBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//ip分配状态，系统将发送ip
			//ethServiceIP参数为null或者空，表示ip分配失败
			semaphoreRelease();
		}
	};
	/**
	 * 连接线程
	 * 	
	 */
	private class ConnectThread extends Thread {
		private final Socket mmSocket;
		public ConnectThread() {			
			MLog.e(TAG, "ConnectThread construct");		
			mmSocket= new Socket();		
			try {
				mmSocket.setTcpNoDelay(true);
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		private InetSocketAddress getSmartbox30IP(){
			InetSocketAddress socketAddress = null;
			if(mDeviceFactoryManager!=null && isFix && mDeviceFactoryManager.getFirmwareFixSubMode() == DeviceFactoryManager.FIRMWARE_FIX_SUB_MODE_FOR_OTA_UPGRADE){
				socketAddress = new InetSocketAddress(SMARTBOX30_ETHERNET_CONNECT_ADDRESS, SMARTBOX30_ETHERNET_CONNECT_SYSTEM_PORT);
			}else {
				socketAddress = new InetSocketAddress(SMARTBOX30_ETHERNET_CONNECT_ADDRESS, SMARTBOX30_ETHERNET_CONNECT_PORT);
			}
			return socketAddress ;
		}

		public void run() {
			InetSocketAddress socketAddress = null;
			if (Tools.isMatchSmartbox30SupportSerialnoPrefix(mContext, getSerialNo())) {
				if(SystemPropertiesInvoke.getBoolean("ro.support_mix_doip",false)){
					if(TextUtils.isEmpty(Tools.getX431PRO3V5ProjectBSKEth1IPV4Address())==false){
						socketAddress = getSmartbox30IP();
					}
					else {
						//告知系统为Smartbox30 模式
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								startSmartbox30Client();
							}
						});
						if (semaphoreAcquire(12000) == false) {
							if (MLog.isDebug) {
								MLog.d(TAG, "Smartbox30 Client get ip fail by dhcp ");
							}
						}
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								unRegisterSmartbox30ClientIPReceiver();
							}
						});
						if (TextUtils.isEmpty(Tools.getX431PRO3V5ProjectBSKEth1IPV4Address()) == false) {
							socketAddress = getSmartbox30IP();
						}
					}
				}
				else{
					socketAddress = getSmartbox30IP();
				}
			}

			if (socketAddress == null) {
				if (!interrupted()) {
					connectionFailed(mContext.getResources().getString(R.string.msg_ethernet_connect_state_fail_with_no_ip));
					return;
				}
			}
			try {
				if (!interrupted()) {
					mmSocket.connect(socketAddress, ETHERNET_CONNECT_TIMEOUT);
				}
			} catch (Exception e1) {
				MLog.e(TAG, "unable to connect() exception : " + e1.getMessage());
				//重试一次
				try {
					if (!interrupted()) {
						mmSocket.connect(socketAddress, ETHERNET_CONNECT_TIMEOUT);
					}
				} catch (Exception e2) {
					MLog.e(TAG, "try connect error unable to connect() exception : " + e1.getMessage());
					if (!interrupted()) {
						connectionFailed();
					}
					return;
				}
			}
			// Start the connected thread
			if (!interrupted()) {
				connected(mmSocket);
			}
		}

		public void cancel() {
			MLog.e(TAG, "cancel ConnectThread ");
			try{
				this.interrupt();
				MLog.i(TAG, "mConnectThread.interrupt() for cancel");
			}
			catch(Exception e){
				MLog.i(TAG, "mConnectThread.interrupt() Exception for cancel");
			}
			try {
				if (mmSocket != null && mmSocket.isConnected()) {					
					mmSocket.close();
				}				
			} catch (IOException e) {
				MLog.e(TAG, " close() of Socket connect " );
			}
		}
	}
	
	
	/**
	 * 根据配置信息，连接网络
	 * 
	 */
	public  void connectNetwork() {
		MLog.e(TAG, "connect  Device ");
		connect();
	}
	
	/**
	 * 网络连接
	 */
	private void connect() {
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}
		MLog.e(TAG, "mReadByteDataStreamThread cancel ");
		if (mReadByteDataStreamThread != null) {
			mReadByteDataStreamThread.cancel();
			mReadByteDataStreamThread = null;
		}
		mConnectThread = new ConnectThread();
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}
	/**
	 * 网络连接成功后读取接头数据
	 * 
	 * @param socket	
	 */
	private  void connected(Socket socket) {
		MLog.d(TAG, "connected ");
		// Cancel the thread that completed the connection
		ethernetSocket = socket;
		try {
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();			
			mReadByteDataStreamThread = new ReadByteDataStream(this,inputStream, outputStream);
		} catch (IOException e) { 
			MLog.e(TAG, "ethernet Socket sockets not created" + e.getMessage());
		}
		new Thread(mReadByteDataStreamThread).start();		
		setState(STATE_CONNECTED);
		mHandler.sendEmptyMessage(0);
	}

	/**
	 * 连接失败
	 */
	private void connectionFailed() {
		 connectionFailed(null);
	}
	private void connectionFailed(String message) {
		setState(STATE_NONE);
		// 发送连接失败广播
		Intent intent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_FAIL);
		intent.putExtra(IS_CONNECT_FAIL, true);
		intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
		if( message == null){
			intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.msg_ethernet_connect_state_fail));
		}
		else{
			intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, message);
		}
		mContext.sendBroadcast(intent);
	}
	private Handler mHandler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg) {
			Intent intent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS);
			intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
			intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.msg_ethernet_connect_state_success));
			mContext.sendBroadcast(intent);
			registerNetworkConnectChangedReceiver();
			MLog.e(TAG, "ethernet connected success,starting transfer data ");
			//发送广播通知连接成功
			mContext.sendBroadcast(new Intent(ACTION_DIAG_CONNECTED));
		}
	};
	private void registerNetworkConnectChangedReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		filter.addAction(mPermisson);
		mContext.registerReceiver(mBroadcastReceiver, filter);
	}

	/**
	 * 该广播只有在连接成功后使用
	 */
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {// 设备插入
				MLog.d(TAG, "ACTION_USB_DEVICE_ATTACHED");
			}
			else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {// 设备拔出
				MLog.d(TAG, "ACTION_USB_DEVICE_DETACHED");
				//判断是否为匹配的接头设备
				if(mDeviceFactoryManager.queryIsMatchDevice(mContext,intent)==false){
					return;
				}
				mContext.sendBroadcast(new Intent(ACTION_DPU_DEVICE_CONNECT_DISCONNECTED));
			}
			else if (mPermisson.equals(action)) {// 请求USB 权限，当用户点击授权
				MLog.d(TAG, "Permisson REQUEST");
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
		this.isFix = isFix;
	}	

	@Override
	public void physicalCloseDevice() {
		closeDevice();
	}
	@Override
	public void setIsRemoteClientDiagnoseMode(boolean isRemoteClientDiagnoseMode) {
		this.mIsRemoteClientDiagnoseMode  = isRemoteClientDiagnoseMode;
	}
	@Override
	public boolean getIsRemoteClientDiagnoseMode() {
		return mIsRemoteClientDiagnoseMode;
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
