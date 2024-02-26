package com.cnlaunch.physics.bluetooth.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.RomoteLocalSwitch;
import com.cnlaunch.physics.bluetooth.BluetoothsNeedDirectLinkManager;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.utils.ConnectWaitTimer;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.remote.ReadByteDataStream;

/**
 * 蓝牙连接、管理类
 * 跨进程类，请不要在里边加入应用配置相关的内容，以及显示特有内容
 * @author xiefeihong
 * @version 1.0
 * @date 2014-3-8
 * 
 * 
 */
public class BluetoothManagerImpl implements IPhysics {
	private static final String TAG = "BluetoothManagerImpl";
	//根据测试，蓝牙连接4次是比较好的体验
	private static final int AUTO_RECONNECT_COUNT = 4; 	
	//306ecbb4-615f-11e7-907b-a6006ad3dba0
	private final UUID PUBLIC_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



	public static final int BT_DEVICE_ADD = 100; // 搜索到了新的设备
	public static final int BT_DEVICE_LIST_REFERSH = 110; // 刷新蓝牙显示列表
	public static final int BT_DEVICE_CON_CONING = 120;// 正在连接UI更新
	public static final int BT_DEVICE_CON_SUCCESS = 130;// 连接成功UI更新
	public static final int BT_DEVICE_CON_FAIL = 140;// 连接失败UI更新
	public static final int BT_DEVICE_CON_LOST = 150;// 连接丢失UI更新
	public static final int BT_DEVICE_SCAN = 160;// 开始扫描蓝牙设备列表
	public static final int BT_DEVICE_SCAN_FINISH = 170;// 开始扫描蓝牙设备列表
	public static final int BT_DEVICE_MORE_CON_FAIL = 180;// 连接失败UI更新
	
	// 蓝牙自定义状态广播
	public static final String ACTION_BT_DEVICE_CON_CONING = "action.bt.device.con.coning";// 正在连接UI更新
	public static final String ACTION_BT_DEVICE_CON_SUCCESS = "action.bt.device.con.success";// 连接成功UI更新
	public static final String ACTION_BT_DEVICE_CON_FAIL = "action.bt.device.con.fail";// 连接失败UI更新
	public static final String ACTION_BT_DEVICE_CON_LOST = "action.bt.device.con.lost";// 连接丢失UI更新
	
	private boolean mAutoReConnectBoolean = true;
	private int mAutoReConnect;
	private BluetoothSocket mBluetoothSocket;
	private BluetoothDevice mBluetoothDevice;

	private Context mContext;
	private BluetoothAdapter mBluetoothAdapter;


	private ConnectThread mConnectThread;
	private ReadByteDataStream mReadByteDataStreamThread;

	private int mState;
	private String mReadData;

	private boolean mIsAutoConnect = false;
	private ConnectWaitTimer mConnectWaitTimer;
	
	private boolean isFix;
	private String mSerialNo;
	private boolean mIsTruckReset;
	private boolean mIsRemoteClientDiagnoseMode;
	private boolean mIsSupportOneRequestMoreAnswerDiagnoseMode;
	private boolean mbIsSetCommandOfMainLink = true;
	private boolean mIsTpmsManager = false;

	/**
	 * 初始化蓝牙管理类
	 * 
	 * @param context
	 */
	public BluetoothManagerImpl(Context context,boolean isFix,String serialNo,boolean isTpmsManager) {
		mContext = context.getApplicationContext();//modify by zhangshengda 2016.06.21 引用diagnoseactivity 会使使用activity作为接头的fragment释放不掉
		this.isFix = isFix;
		mBluetoothSocket = null;
		mConnectThread = null;
		mReadByteDataStreamThread = null;
		mAutoReConnectBoolean = true;
		mAutoReConnect = AUTO_RECONNECT_COUNT;
		mConnectWaitTimer = new ConnectWaitTimer();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(!mBluetoothAdapter.isEnabled()){
			mBluetoothAdapter.enable();
		}
		mState = IPhysics.STATE_NONE;
		regeisterDiagnoseBroadcast();
		mSerialNo = serialNo;
		this.mIsTpmsManager = isTpmsManager;
		mIsTruckReset = false;
		mIsRemoteClientDiagnoseMode = false;
		mIsSupportOneRequestMoreAnswerDiagnoseMode = false;
	}
	
	@Override
	protected void finalize() {
		try {
			MLog.e(TAG, "finalize BluetoothManager");
			mHandler=null;
			mConnectWaitTimer.shutdown();
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * 自动连接
	 * 
	 * @param serialNo
	 * @param deviceAddress
	 */
	public void autoBluetoothConnect(String serialNo, String deviceAddress) {
		MLog.e(TAG, "auto Bluetooth Connect serialNo=" + serialNo
				+ "deviceAddress=" + deviceAddress);

		mIsAutoConnect = true;
		mAutoReConnectBoolean = true;
		mAutoReConnect = AUTO_RECONNECT_COUNT;
		if (TextUtils.isEmpty(deviceAddress) && TextUtils.isEmpty(serialNo)) {
			sendConnectionFailedBroadcast(false);
			return;
		}
		if (!TextUtils.isEmpty(deviceAddress)) {
			connect(deviceAddress);
		} else if (!TextUtils.isEmpty(serialNo)) {
			boolean flag = false;
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			for (BluetoothDevice device : pairedDevices) {
				if (device.getName() != null && device.getName().equals(serialNo)) {
					flag = true;
					connect(device.getAddress());
					break;
				}
			}
			if (!flag) {
				sendConnectionFailedBroadcast(false,REASON_BLUETOOTH_NOPAIRED);
				return;
			}
		}
	}

	/**
	 * 判断是否为重新连接阶段
	 * 
	 * @return
	 */
	public boolean isAutoReConnect() {
		if (mAutoReConnectBoolean 
				&& (mAutoReConnect-1) >= 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断是否为自动连接
	 * 
	 * @return
	 */
	public boolean isAutoConnect() {
		return mIsAutoConnect;
	}
	
	private void regeisterDiagnoseBroadcast() {
		MLog.i(TAG, "BluetoothManager register Receiver");
		IntentFilter filter = new IntentFilter();
		//filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		//filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		//filter.addAction(IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS);
		//filter.addAction(IPhysics.ACTION_DPU_DEVICE_CONNECT_FAIL);
		filter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
		mContext.registerReceiver(mReceiver, filter);
	}
	
	private void unregisterBoardcasetReciver() {
		if (null != mReceiver) {
			try {
				mContext.unregisterReceiver(mReceiver);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	/**
	 * 根据传过来蓝牙对象连接
	 *
	 * @throws Exception
	 */
	public void connectBluetoothDevice(BluetoothDevice device) {
		MLog.e(TAG, "connect Bluetooth Device ");
		mIsAutoConnect = false;
		mAutoReConnectBoolean = true;
		mAutoReConnect = AUTO_RECONNECT_COUNT;
		mBluetoothDevice = device;
		connect();
	}



	/**
	 * 蓝牙连接
	 *
	 */
	private  void connect() {
		if (getBluetoothDevice() != null) {
			setState(IPhysics.STATE_CONNECTING);
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
			MLog.e(TAG, "mReadByteDataStreamThread cancel ");
			if (mReadByteDataStreamThread != null) {
				mReadByteDataStreamThread.cancel();
				mReadByteDataStreamThread = null;
			}
			sendCustomBluetoothStatusBroadcast(mContext,
					ACTION_BT_DEVICE_CON_CONING, BT_DEVICE_CON_CONING,
					mContext.getString(R.string.bluetooth_connecting),
					mBluetoothDevice,mAutoReConnect);
			mConnectThread = new ConnectThread(mBluetoothDevice);
			mConnectThread.start();			
		}
		else{
			sendConnectionFailedBroadcast(true);
		}
	}

	/**
	 * 使用蓝牙地址连接接头
	 * 
	 * @param deviceAddress
	 *            蓝牙地址
	 */
	private void connect(String deviceAddress) {
		mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
		connect();		
	}
	/**
	 *  *蓝牙主动连接成功时，利用该方法初始化BluetoothManagerImpl对象
	 * @param socket
	 * @param device
	 */
	private void connected(BluetoothSocket socket,BluetoothDevice device)
	{
		connected(socket,device,false);
	}
	/**
	 * 蓝牙连接成功后读取蓝牙流数据	
	 * @param socket
	 * @param device
	 */
	public void connected(BluetoothSocket socket,BluetoothDevice device,boolean isListener) {
		MLog.d(TAG, "connected ");
		mAutoReConnect = -1;
		setState(STATE_CONNECTED);		
		BluetoothsNeedDirectLinkManager.getInstance().setBluetoothNeedDirectLinkState(mSerialNo,false);
		// Cancel the thread that completed the connection
		mBluetoothSocket = socket;
		setBluetoothDevice(device);
		try {
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();
			mReadByteDataStreamThread = new ReadByteDataStream(this, inputStream, outputStream);
		} catch (IOException e) { 
			MLog.e(TAG, "remoteSocket sockets not created" + e.getMessage());
		}
		new Thread(mReadByteDataStreamThread).start();
		int listenerState = 0;
		if(isListener){
			listenerState = 1;
		}
		Message message = mHandler.obtainMessage(0, listenerState, 0);
		mHandler.sendMessage(message);
	}

	/**
	 * 设置设备连接状态
	 * 
	 * @param state
	 */
	private synchronized void setState(int state) {
		mState = state;
	}

	/**
	 * 获取设备连接状态
	 * 
	 * @return
	 */
	@Override
	public synchronized int getState() {
		return mState;
	}

	/**
	 * 1秒后自动重新连接蓝牙对象
	 */
	private  void reConnectBluetoothDevice() {
		mHandler.sendEmptyMessageDelayed(2,1000);
		/*MLog.e(TAG, "开始重新连接 剩余次数: " + (mAutoReConnect-1));
		if (mAutoReConnectBoolean == true && (mAutoReConnect-1) > 0) {
			Timer connectTimer = new Timer();
			connectTimer.schedule(new ReConnectTimerTask(), 1000);
		}*/
	}

	/**
	 * 自动重新连接蓝牙对象
	 */
	private void reConnectBluetoothDeviceHandler() {
		MLog.e(TAG, "开始重新连接 剩余次数: " + (mAutoReConnect - 1));
		// 执行前仍需要判断条件是否满足
		if (getBluetoothDevice() == null || (mAutoReConnectBoolean == true && (mAutoReConnect - 1) > 0) == false) {
			return;
		}
		MLog.e(TAG, "ReConnect TimerTask Start");
		mAutoReConnect--;
		connect();
	}
	

	/**
	 * 连接线程
	 * 
	 * @author bichuanfeng
	 * 
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private String mSocketType = "Insecure";
		private String mPhoneModel = "";
		public ConnectThread(BluetoothDevice device) {
			MLog.e(TAG, "ConnectThread construct");
			mmDevice = device;
			BluetoothSocket tmp = null;
			if (android.os.Build.MODEL != null) {
				mPhoneModel = android.os.Build.MODEL;
			}
			//此处逻辑请根据AUTO_RECONNECT_COUNT具体修改
			if(!mPhoneModel.equals("") && mPhoneModel.equalsIgnoreCase("MediaPad 10 LINK"))
			{			
				if (mAutoReConnect >2) {
					tmp = createBluetoothSocket(mmDevice);
				} else {
					try {
						if (Build.VERSION.SDK_INT >= 10) {
							if(mIsRemoteClientDiagnoseMode){
								tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
							}
							else{
								tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(PUBLIC_UUID);
							}
						} else {
							if(mIsRemoteClientDiagnoseMode){
								tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
							}
							else{
								tmp = mmDevice.createRfcommSocketToServiceRecord(PUBLIC_UUID);
							}
						}
					} catch (IOException e) {
						MLog.e(TAG, "Socket Type: " + mSocketType+ " create() failed " + e.getMessage());
						tmp = null;
					}
				}
			}
			else {
				boolean needDirectLink = BluetoothsNeedDirectLinkManager.getInstance().getBluetoothNeedDirectLinkState(mSerialNo);
				//android7.1.1之后不支持蓝牙私有连接方法
				//xfh2019/01/08 增加下列内容
				//实际使用中发现，很多场合蓝牙私有方法建立链接链路会导致未知错误，且公司的pad3平板（android5.1.1）有时出现莫名其妙问题
				//所以把能使用未公开方法建立建立链接链路的android版本提前到android5.0。
				//先前公司产品android4.0时代使用未公开方法建立建立链接链路的确对蓝牙连接体验有帮助，且没有出现问题
				if (((mAutoReConnect % 2) == 0 && needDirectLink == false) || Build.VERSION.SDK_INT >=21) {
					MLog.e(TAG, "connect with public method");
					try {
						BluetoothsNeedDirectLinkManager.getInstance().setBluetoothNeedDirectLinkState(mSerialNo,true);
						if (Build.VERSION.SDK_INT >= 10) {
							if(mIsRemoteClientDiagnoseMode){
								tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
							}
							else{
								tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(PUBLIC_UUID);
							}
						} else {
							if(mIsRemoteClientDiagnoseMode){
								tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
							}
							else{
								tmp = mmDevice.createRfcommSocketToServiceRecord(PUBLIC_UUID);
							}
						}
					} catch (IOException e) {
						MLog.e(TAG, "Socket Type: " + mSocketType+ " create() failed " + e.getMessage());
						tmp = null;
					}
				} else {
					MLog.e(TAG, "connect with private method");
					BluetoothsNeedDirectLinkManager.getInstance().setBluetoothNeedDirectLinkState(mSerialNo,false);
					tmp = createBluetoothSocket(mmDevice);
				}
			}
			mmSocket = tmp;
		}

		/**
		 * add by weizeweie
		 */
		private BluetoothSocket createBluetoothSocket(BluetoothDevice device){
			BluetoothSocket tmp = null;
			try {
				Method method;
				method = device.getClass().getMethod("createRfcommSocket",new Class[] { int.class });
				tmp = (BluetoothSocket) method.invoke(device,Integer.valueOf(1));
			} catch (Exception e) {
				tmp = null;
				Log.e("BluetoothChatService","Could not create Insecure RFComm Connection", e);
			}
			return tmp;
		}

		public void run() {
			MLog.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
			setName("ConnectThread" + mSocketType);
			if (mmSocket == null) {
				connectionFailed();
				return;
			}
			// Always cancel discovery because it will slow down a connection
			mBluetoothAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mConnectWaitTimer.onStart(new FinishListener());
				//sleep(35*1000);
				mmSocket.connect();
				mConnectWaitTimer.onStop();
			}
			/*catch (InterruptedException e) {
				e.printStackTrace();
				mConnectWaitTimer.onStop();
			}*/
			catch (IOException e) {
				// Close the socket
				mConnectWaitTimer.onStop();
				try {
					mmSocket.close();
				} catch (IOException e2) {
					MLog.e(TAG,
							"unable to close() " + mSocketType+ " socket during connection failure"+ e2.getMessage());
				}
				MLog.e(TAG,"unable to connect() " +e.getMessage()+" "+e.getClass().getSimpleName()+" "+e.toString());
				if(!interrupted()){					
					connectionFailed();
				}
				else{
					MLog.e(TAG,"connection thread has interrupted " );
				}
				return;
			}

			// Start the connected thread
			if(!interrupted()){
				connected(mmSocket, mmDevice);
			}
			else{
				cancel();
			}
		}

    	@SuppressLint("NewApi")
		public void cancel() {
			MLog.e(TAG, "cancel ConnectThread ");
			//
			try{
				this.interrupt();
				MLog.i(TAG, "mConnectThread.interrupt() for cancel");
			}
			catch(Exception e){
				MLog.i(TAG, "mConnectThread.interrupt() Exception for cancel");
			}
			try {
				if (mmSocket != null && mmSocket.isConnected()) {
					MLog.d(TAG, "socket close for cancel");
					mmSocket.close();
				}
			} catch (IOException e) {
				MLog.e(TAG, "close() of connect " + mSocketType+ " socket failed" + e.getMessage());
			}
		}
	}

	/**
	 * 发送蓝牙连接状态广播
	 * 
	 * @param broadCast
	 */
	private static void sendBluetoothStatusBoradcast(Context context,
			String broadCast) {
		context.sendBroadcast(new Intent(broadCast));
	}

	/**
	 * 获取读取到的完整指令
	 * 
	 * @return
	 */
	@Override
	public  String getCommand() {
		return mReadData;
	}

	/**
	 * 获取读取到的完整指令
	 */
	@Override
	public void setCommand(String command) {
		mReadData = command;
		if(mbIsSetCommandOfMainLink)RomoteLocalSwitch.getInstance().setCommand(mSerialNo,command,mIsTpmsManager);
	}

	@Override
	public void setCommand(String command, boolean isSupportSelfSend) {
		if(isSupportSelfSend) mReadData = command;
		else setCommand(command);
	}

	/**
	 * 获取蓝牙写数据流
	 * 
	 * @return
	 */
	@Override
	public InputStream getInputStream() {
		try {
			if (mBluetoothSocket != null)
				return mBluetoothSocket.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取蓝牙读数据流
	 * 
	 * @return
	 */
	@Override
	public OutputStream getOutputStream() {
		try {
			if (mBluetoothSocket != null)
				return mBluetoothSocket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	

	private boolean bluetooth_command_wait = true;

	@Override
	public synchronized boolean getCommand_wait() {
		return bluetooth_command_wait;
	}

	@Override
	public synchronized void setCommand_wait(boolean wait) {
		bluetooth_command_wait = wait;
	}

	/**
	 * 获取上下文
	 */
	@Override
	public Context getContext() {
		return mContext;
	}

	/**
	 * 关闭设备
	 */
	@Override
	public void  closeDevice() {
		MLog.d(TAG, "stop bluetooth ConnectThread");
			setBluetoothDevice(null);
			unregisterBoardcasetReciver();
			mConnectWaitTimer.onStop();
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
			if (mReadByteDataStreamThread != null) {
				mReadByteDataStreamThread.cancel();
				sendBluetoothStatusBoradcast(mContext,ACTION_DIAG_UNCONNECTED);
				mReadByteDataStreamThread = null;
			}
			setState(STATE_NONE);
	}
	

	/**
	 * 连接失败
	 */
	private void connectionFailed() {		
		sendCustomBluetoothStatusBroadcast(mContext, ACTION_BT_DEVICE_CON_FAIL,
				BT_DEVICE_CON_FAIL,
				mContext.getString(R.string.bluetooth_connect_fail),
				mBluetoothDevice,mAutoReConnect);		
		// 发送连接失败广播
		if ((mAutoReConnectBoolean == true && (mAutoReConnect-1) == 0)
				|| mAutoReConnectBoolean == false) {
			sendConnectionFailedBroadcast(true);
			//只有重试次数满足时，才改变连接状态
			setState(STATE_NONE);
			return;
		}
		reConnectBluetoothDevice();
	}
/**
 * 
 * @param context
 * @param action
 * @param type
 * @param content
 * @param bluetoothDevice
 * @param autoReConnect 加入蓝牙连接次数，用于更新蓝牙连接界面
 */
	private void sendCustomBluetoothStatusBroadcast(Context context,
			String action, int type, String content,
			BluetoothDevice bluetoothDevice,int autoReConnect) {
		Intent intent = new Intent(action);
		Bundle bundle = new Bundle();
		bundle.putInt("type", type);
		bundle.putString("status", content);
		bundle.putInt("pair", 12);
		if(action.equalsIgnoreCase(ACTION_BT_DEVICE_CON_CONING))
		{
			bundle.putInt("auto_reconnect_count",AUTO_RECONNECT_COUNT - (autoReConnect-1));
		}
		bundle.putParcelable("bluetoothDevice", bluetoothDevice);
		intent.putExtra("customBluetoothBroadcastIntentExtraBundle", bundle);
		intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
		context.sendBroadcast(intent);
	}
	/**
	 * 诊断蓝牙广播消息接收器
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//BluetoothDevice btDevice=null;
			//String cnLaunchAddress = (mBluetoothDevice!=null)?mBluetoothDevice.getAddress():"";
			MLog.d(TAG, "BluetoothManager  Receiver action"+action);
			if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
				/*btDevice = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				if(btDevice != null &&  btDevice.getAddress().equals(cnLaunchAddress)){
					MLog.i(TAG, "pair BluetoothManager  Receiver action"+action);
				}*/
			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
				//ACTION_ACL_DISCONNECTED 消息不及时，可能会存在问题，所以去掉
				/*btDevice = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(btDevice != null &&  btDevice.getAddress().equals(cnLaunchAddress)){
					MLog.i(TAG, "pair BluetoothManager  Receiver action"+action);
					sendBluetoothStatusBoradcast(mContext,
							ACTION_DIAG_UNCONNECTED);
					setState(STATE_NONE);
				}*/
			} else if (IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS.equals(action)) {
				/*mAutoReConnect = -1;
				Toast.makeText(
						mContext,
						mContext.getString(R.string.bluetooth_connect_success)
								+ ":" + intent.getStringExtra("deviceName"),
						Toast.LENGTH_SHORT).show();
				MLog.i(TAG, "BluetoothManager  Receiver 蓝牙连接成功");
				//发送广播通知蓝牙连接成功
				sendBluetoothStatusBoradcast(mContext,
						ACTION_DIAG_UNCONNECTED);
				*/
			} else if (IPhysics.ACTION_DPU_DEVICE_CONNECT_FAIL.equals(action)) {
				/*mAutoReConnect = -1;
				String deviceName = intent.getStringExtra("deviceName");
				if(deviceName != null){
				Toast.makeText(
						mContext,
						mContext.getString(R.string.bluetooth_connect_fail)
								+ ":" + deviceName,
						Toast.LENGTH_SHORT).show();
				}*/
			}else if(action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
				BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String address = (mBluetoothDevice != null)?mBluetoothDevice.getAddress():"";
				setPin(btDevice,address);
			}
		}
	};
	
	@Override
	public String getDeviceName() {
		String deviceName = "";
		if (mBluetoothDevice != null) {
			MLog.d(TAG, "remoteDevice is not null.");
			deviceName = mBluetoothDevice.getName();
			if(deviceName==null){
				deviceName = "";
			}
		}
		return deviceName;
	}
	private void sendConnectionFailedBroadcast(boolean isConnectFail){
		sendConnectionFailedBroadcast(isConnectFail,REASON_UNKNOWN);
	}
    /**
     * 发送蓝牙连接失败广播
     * @param isConnectFail
     */
    private void sendConnectionFailedBroadcast(boolean isConnectFail,int reason){
    	mAutoReConnect = -1;
    	Intent bluetoothIntent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_FAIL);
		bluetoothIntent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
    	bluetoothIntent.putExtra(IS_CONNECT_FAIL, isConnectFail);
		bluetoothIntent.putExtra(CONNECT_FAIL_REASON,reason);
    	bluetoothIntent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.bluetooth_connect_fail));
		if (mBluetoothDevice != null) {
			String deviceName = mBluetoothDevice.getName();
			bluetoothIntent.putExtra("deviceName", (deviceName!=null)?deviceName:"");
		}
		mContext.sendBroadcast(bluetoothIntent);
    }
    
    /**
	 * 蓝牙连接长时间无响应处理
	 */
	private void connectionFailedWithLongTimes() {
		if (mConnectThread != null) {
			try{
				mConnectThread.interrupt();
				MLog.i(TAG, "mConnectThread.interrupt() for connection Failed With Long Times trigger");
			}
			catch(Exception e){
				MLog.i(TAG, "mConnectThread.interrupt() Exception for connection Failed With Long Times trigger");
				e.printStackTrace();
			}
			mConnectThread = null;
		}
		MLog.i(TAG, "connection Failed With Long Times trigger");
		//如果连接超时，且计划下次连接为直连，做一次重新连接蓝牙操作
		if(BluetoothsNeedDirectLinkManager.getInstance().getBluetoothNeedDirectLinkState(mSerialNo) &&
				mAutoReConnect == AUTO_RECONNECT_COUNT) {
			MLog.i(TAG, "connection Failed With Long Times trigger and do connectionFailed after 15 second");
			//延迟5秒后重新连接
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					connectionFailed();
				}
			},5000);
			return;
		}
		setState(STATE_NONE);
		sendCustomBluetoothStatusBroadcast(mContext, 
				ACTION_BT_DEVICE_CON_FAIL,
				BT_DEVICE_CON_FAIL,
				mContext.getString(R.string.bluetooth_connect_fail),
				mBluetoothDevice,mAutoReConnect);

		// 发送连接失败广播
		sendConnectionFailedBroadcast(true);
	}
    /**
     * 蓝牙连接超时无响应处理
     * @author xiefeihong
     *
     */
    private class FinishListener implements Runnable {
		public FinishListener() {

		}
		public void run() {
			connectionFailedWithLongTimes();
		}
	}

   
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		 /**
	     *  msg.what 0 逻辑连接成功 1 逻辑连接失败 2 逻辑连接失败重试
	     */
		@Override
		public void handleMessage(Message msg) {
			// super.handleMessage(msg);
			if (msg.what == 0) {
				int listenerState = msg.arg1;
				String deviceName = ((mBluetoothDevice != null) ? mBluetoothDevice.getName() : "");
				if(deviceName==null){
					deviceName = "";
				}
				if (listenerState == 0) {
					sendCustomBluetoothStatusBroadcast(mContext, ACTION_BT_DEVICE_CON_SUCCESS, BT_DEVICE_CON_SUCCESS, mContext.getString(R.string.bluetooth_connected), mBluetoothDevice, mAutoReConnect);
					Intent intent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS);
					intent.putExtra("deviceName", deviceName);
					intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
					intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.bluetooth_connect_success));
					mContext.sendBroadcast(intent);
				}
				else{
					//后台连接成功广播，一般指蓝牙回连
					Intent intent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS_WITH_BACKGROUND);
					intent.putExtra("deviceName", deviceName);
					intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
					intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.bluetooth_connect_success));
					mContext.sendBroadcast(intent);
				}
				MLog.e(TAG, "Bluetooth connected success,starting transfer data ");
				Toast.makeText(mContext, mContext.getString(R.string.bluetooth_connect_success) + ":" + deviceName, Toast.LENGTH_SHORT).show();
				MLog.i(TAG, "BluetoothManager  Receiver 蓝牙连接成功");
				// 发送广播通知蓝牙连接成功
				sendBluetoothStatusBoradcast(mContext, ACTION_DIAG_CONNECTED);
			} 
			else if (msg.what == 2) {
				reConnectBluetoothDeviceHandler();
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

	public synchronized BluetoothDevice getBluetoothDevice() {
		return mBluetoothDevice;
	}

	private synchronized void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
		this.mBluetoothDevice = bluetoothDevice;
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
	/**
	 * 设置蓝牙配对密码
	 * @param btDevice 需要配对的设备
	 * @param address   当前使用的蓝牙设备mac地址
	 */
	private void setPin(BluetoothDevice btDevice,String address){
		String strPsw = "0000";		
		int sdk = Build.VERSION.SDK_INT;
		if(MLog.isDebug) {
			MLog.d("BluetoothConnectReceive", "Build.VERSION.SDK_INT = " + sdk);
		}
		if(btDevice==null || !btDevice.getAddress().equals(address)){
			if(MLog.isDebug) {
				MLog.d("BluetoothConnectReceive", "not pair cnlaunch device " + btDevice.getAddress() + "  " + address);
			}
			return;
		}
		//android 6.0后不再自动拦截配对广播，进行自动配对
		if(sdk<23) {
			PairUtils.createDeviceBond(btDevice);
		}
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
		mbIsSetCommandOfMainLink = isSetCommandOfMainLink;
	}

	@Override
	public boolean getCommandStatus() {
		return mbIsSetCommandOfMainLink;
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
