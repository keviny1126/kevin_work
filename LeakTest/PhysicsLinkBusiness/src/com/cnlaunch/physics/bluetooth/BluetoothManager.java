package com.cnlaunch.physics.bluetooth;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.LinkParameters;
import com.cnlaunch.physics.RomoteLocalSwitch;
import com.cnlaunch.physics.bluetooth.ble.BluetoothBLEManager;
import com.cnlaunch.physics.bluetooth.remote.BluetoothManagerImpl;
import com.cnlaunch.physics.bluetooth.remote.IRemoteBluetoothManager;
import com.cnlaunch.physics.impl.IAssitsPhysics;
import com.cnlaunch.physics.impl.IAssitsPhysicsMatcher;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.remote.IRemoteDeviceFactoryManager;
import com.cnlaunch.physics.remote.IRemoteDeviceFactoryManagerCallBack;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.Tools;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 蓝牙连接、管理代理类
 * 因为历史原因，该命名没有加入Proxy
 * @author xiefeihong
 * @version 1.0
 * @date 2014-3-8
 * 
 * 
 */
public class BluetoothManager implements IPhysics, IAssitsPhysics {
	private static final String TAG = "BluetoothManagerProxy";	
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
	
	private Context mContext;	
	IRemoteBluetoothManager mRemoteBluetoothManager;
	BluetoothManagerImpl mBluetoothManagerImpl;
	BluetoothBLEManager mBluetoothBLEManager;
	InputStream inputStream;
	OutputStream outputStream;
	boolean mIsBleMode;

	//DHC 增加蓝牙的辅助连接的处理
    private IAssitsPhysicsMatcher mAssitsPhysicsMatcher;

	/**
	 * 初始化蓝牙管理类
	 * 该类变更为蓝牙管理代理类，因为历史关系，为减少代码变动，命名不变更
	 * 日志TAG变更为BluetoothManagerProxy
	 * @param context
	 */
	public BluetoothManager(Context context,boolean isFix,String serialNo,boolean isBleMode) {
		//modify by zhangshengda 2016.06.21 引用diagnoseactivity 会使使用activity作为接头的fragment释放不掉
		mContext = context.getApplicationContext();
		mIsBleMode = isBleMode;
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				IRemoteDeviceFactoryManagerCallBack remoteDeviceFactoryManagerCallBack = DeviceFactoryManager.getInstance().getRemoteDeviceFactoryManagerCallBack();
				IRemoteDeviceFactoryManager remoteDeviceFactoryManager = DeviceFactoryManager.getInstance().getRemoteDeviceFactoryManager();
				try {
					mRemoteBluetoothManager = remoteDeviceFactoryManager.getRemoteBluetoothManager(serialNo, isFix, remoteDeviceFactoryManagerCallBack);
					remoteDeviceFactoryManager.setDPUType(serialNo, Tools.isTruck(mContext, serialNo), Tools.isCarAndHeavyduty(mContext, serialNo));
					inputStream = new BluetoothInputStreamProxy(mRemoteBluetoothManager);
					outputStream = new BluetoothOutputStreamProxy(mRemoteBluetoothManager, DeviceFactoryManager.getInstance().getIPhysicsOutputStreamBufferWrapper());
					mBluetoothManagerImpl = null;
					mBluetoothBLEManager = null;
				} catch (Exception e) {
					e.printStackTrace();
					//定义本地蓝牙管理对象
					RomoteLocalSwitch.getInstance().setRemoteMode(false);
					mRemoteBluetoothManager = null;
					mBluetoothBLEManager = null;
					inputStream = null;
					outputStream = null;
					mBluetoothManagerImpl = new BluetoothManagerImpl(context, isFix, serialNo,false);
				}
			} else {
				mRemoteBluetoothManager = null;
				mBluetoothBLEManager = null;
				inputStream = null;
				outputStream = null;
				mBluetoothManagerImpl = new BluetoothManagerImpl(context, isFix, serialNo,false);
			}
		}
		else{
			mRemoteBluetoothManager = null;
			mBluetoothManagerImpl = null;
			inputStream = null;
			outputStream = null;
			mBluetoothBLEManager = new BluetoothBLEManager(DeviceFactoryManager.getInstance(),context, isFix, serialNo);
		}
	}

	@Override
	protected void finalize() {
		try {
			MLog.e(TAG, "finalize BluetoothManagerProxy");	
			mBluetoothManagerImpl = null;
			mBluetoothBLEManager = null;
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
		MLog.e(TAG, "auto Bluetooth Connect serialNo=" + serialNo + "deviceAddress=" + deviceAddress);
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					if (mRemoteBluetoothManager.getState() == STATE_CONNECTED) {
						mRemoteBluetoothManager.userInteractionWhenDPUConnected();
						return;
					}
					mRemoteBluetoothManager.autoConnectBluetooth(serialNo, deviceAddress);
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					if (mBluetoothManagerImpl.getState() == STATE_CONNECTED) {
						mBluetoothManagerImpl.userInteractionWhenDPUConnected();
						return;
					}
					mBluetoothManagerImpl.autoBluetoothConnect(serialNo, deviceAddress);
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				if (mBluetoothBLEManager.getState() == STATE_CONNECTED) {
					mBluetoothBLEManager.userInteractionWhenDPUConnected();
					return;
				}
				mBluetoothBLEManager.autoBluetoothConnect(serialNo, deviceAddress);
			}
		}
	}

	/**
	 * 判断是否为重新连接阶段
	 * 
	 * @return
	 */
	public boolean isAutoReConnect() {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					return mRemoteBluetoothManager.isAutoReConnect();
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
					return false;
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					return mBluetoothManagerImpl.isAutoReConnect();
				} else {
					return false;
				}
			}
		}
		else{
			return false;
		}
	}
	/**
	 * 判断是否为自动连接
	 * 
	 * @return
	 */
	public boolean isAutoConnect() {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					return mRemoteBluetoothManager.isAutoConnect();
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
					return false;
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					return mBluetoothManagerImpl.isAutoConnect();
				} else {
					return false;
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				return mBluetoothBLEManager.isAutoConnect();
			} else {
				return false;
			}
		}
	}

	/**
	 * 根据传过来蓝牙对象连接
	 */
	public void connectBluetoothDevice(BluetoothDevice device) {
		MLog.e(TAG, "connect Bluetooth Device ");
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					if (mRemoteBluetoothManager.getState() == STATE_CONNECTED) {
						mRemoteBluetoothManager.userInteractionWhenDPUConnected();
						return;
					}
					mRemoteBluetoothManager.connectBluetooth(device.getAddress());
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					if (mBluetoothManagerImpl.getState() == STATE_CONNECTED) {
						mBluetoothManagerImpl.userInteractionWhenDPUConnected();
						return;
					}
					mBluetoothManagerImpl.connectBluetoothDevice(device);
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				if (mBluetoothBLEManager.getState() == STATE_CONNECTED) {
					mBluetoothBLEManager.userInteractionWhenDPUConnected();
					return;
				}
				mBluetoothBLEManager.connect(device);
			}
		}
	}
	/**
	 * 获取设备连接状态
	 * 
	 * @return
	 */
	@Override
	public int getState() {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					int state = mRemoteBluetoothManager.getState();
					MLog.e(TAG, "current state is " + state);
					return state;
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
					return STATE_NONE;
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					return mBluetoothManagerImpl.getState();
				} else {
					return STATE_NONE;
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				return mBluetoothBLEManager.getState();
			} else {
				return STATE_NONE;
			}
		}
	}

	/**
	 * 获取读取到的完整指令
	 * 
	 * @return
	 */
	@Override
	public  String getCommand() {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					return mRemoteBluetoothManager.getCommand();
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
					return "";
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					return mBluetoothManagerImpl.getCommand();
				} else {
					return "";
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				return mBluetoothBLEManager.getCommand();
			} else {
				return "";
			}
		}
	}

	/**
	 * 获取蓝牙写数据流
	 * 
	 * @return
	 */
	@Override
	public InputStream getInputStream() {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				return inputStream;
			} else {
				if (mBluetoothManagerImpl != null) {
					if (inputStream == null) {
						inputStream = new BluetoothInputStreamProxy(mBluetoothManagerImpl.getInputStream());
					}
					return inputStream;
				} else {
					return null;
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				if (inputStream == null) {
					inputStream = mBluetoothBLEManager.getInputStream();
				}
				return inputStream;
			} else {
				return null;
			}
		}
	}
	
	/**
	 * 获取蓝牙读数据流
	 * 
	 * @return
	 */
	@Override
	public OutputStream getOutputStream() {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				return outputStream;
			} else {
				if (mBluetoothManagerImpl != null) {
					if (outputStream == null) {
						outputStream = new BluetoothOutputStreamProxy(mBluetoothManagerImpl.getOutputStream(), DeviceFactoryManager.getInstance().getIPhysicsOutputStreamBufferWrapper());
					}
					return outputStream;
				} else {
					return null;
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				if (outputStream == null) {
					outputStream = mBluetoothBLEManager.getOutputStream();
				}
				return outputStream;
			} else {
				return null;
			}
		}
	}
	
	/**
	 * 获取接头命令阻塞标志
	 */
	@Override
	public  boolean getCommand_wait() {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					return mRemoteBluetoothManager.getCommand_wait();
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
					return false;
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					return mBluetoothManagerImpl.getCommand_wait();
				} else {
					return false;
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				return mBluetoothBLEManager.getCommand_wait();
			} else {
				return false;
			}
		}
	}
	/**
	 * 设置接头命令阻塞标志
	 */
	@Override
	public  void setCommand_wait(boolean wait) {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					mRemoteBluetoothManager.setCommand_wait(wait);
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.setCommand_wait(wait);
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.setCommand_wait(wait);
			}
		}
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
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					mRemoteBluetoothManager.closeDevice();
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.closeDevice();
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.closeDevice();
			}
		}
	}	
	
	@Override
	public String getDeviceName() {
		String deviceName = "";
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					deviceName = mRemoteBluetoothManager.getDeviceName();
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					deviceName = mBluetoothManagerImpl.getDeviceName();
				}
			}
		}
		else {
			if (mBluetoothBLEManager != null) {
				deviceName = mBluetoothBLEManager.getDeviceName();
			}
		}
		return deviceName;
	}
	/**
	 * 设置获取到的接头返回命令
	 * 代理类无法设置,直接使用无任何效果
	 * @throws IOException 
	 */
	@Override
	@Deprecated	
	public  void setCommand(String command) {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
			} else {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.setCommand(command);
				}
			}
		}
		else {
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.setCommand(command);
			}
		}
	}

	@Override
	public void setCommand(String command, boolean isSupportSelfSend) {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
			} else {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.setCommand(command,isSupportSelfSend);
				}
			}
		}
		else {
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.setCommand(command,isSupportSelfSend);
			}
		}
	}

	/**
	 * 代理类无法设置
	 */
	@Override
	public void setSerialNo(String serialNo) {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					mRemoteBluetoothManager.setSerialNo(serialNo);
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.setSerialNo(serialNo);
				}
			}
		}
		else {
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.setSerialNo(serialNo);
			}
		}
	}

	@Override
	public String getSerialNo() {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					return mRemoteBluetoothManager.getSerialNo();
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
					return null;
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					return mBluetoothManagerImpl.getSerialNo();
				} else {
					return null;
				}
			}
		}
		else {
			if (mBluetoothBLEManager != null) {
				return mBluetoothBLEManager.getSerialNo();
			} else {
				return null;
			}
		}
	}
	@Override
	public  void setIsTruckReset(boolean isTruckReset) {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					mRemoteBluetoothManager.setIsTruckReset(isTruckReset);
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.setIsTruckReset(isTruckReset);
				}
			}
		}
		else {
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.setIsTruckReset(isTruckReset);
			}
		}
	}

	@Override
	public  boolean isTruckReset() {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					return mRemoteBluetoothManager.isTruckReset();
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
					return false;
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					return mBluetoothManagerImpl.isTruckReset();
				} else {
					return false;
				}
			}
		}
		else {
			if (mBluetoothBLEManager != null) {
				return mBluetoothBLEManager.isTruckReset();
			} else {
				return false;
			}
		}
	}

	@Override
	public void userInteractionWhenDPUConnected() {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					mRemoteBluetoothManager.userInteractionWhenDPUConnected();
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.userInteractionWhenDPUConnected();
				}
			}
		}
		else {
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.userInteractionWhenDPUConnected();
			}
		}
	}

	@Override
	public void setIsFix(boolean isFix) {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					mRemoteBluetoothManager.setIsFix(isFix);
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.setIsFix(isFix);
				}
			}
		}
		else {
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.setIsFix(isFix);
			}
		}
	}
	/**
	 * 当连接管理服务出错时的逻辑处理
	 */
	private void onRemoteException(){
		RomoteLocalSwitch.getInstance().setRemoteMode(false);
		//提示连接错误提示
		Intent bluetoothIntent=new Intent(ACTION_DPU_DEVICE_CONNECT_DISCONNECTED);
		//固件升级时无需处理该逻辑
		bluetoothIntent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, false);
		mContext.sendBroadcast(bluetoothIntent);
	}

	@Override
	public void physicalCloseDevice() {
		MLog.d(TAG, "physical close Device");
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode()) {
				try {
					mRemoteBluetoothManager.physicalCloseDevice();
				} catch (Exception e) {
					e.printStackTrace();
					onRemoteException();
				}
			} else {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.physicalCloseDevice();
				}
			}
		}
		else {
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.physicalCloseDevice();
			}
		}
	}
	/**
	 * 只返回非远程模式下的蓝牙mac地址
	 * @return
	 */
	private String getBluetoothDeviceAddress(){
		MLog.d(TAG, "get no remote mode Bluetooth Device address");	
		String address = null;
		if(mIsBleMode==false) {
			if (DeviceFactoryManager.getInstance().isRemoteMode() == false) {
				if (mBluetoothManagerImpl != null) {
					BluetoothDevice bluetoothDevice = mBluetoothManagerImpl.getBluetoothDevice();
					if (bluetoothDevice != null) {
						address = bluetoothDevice.getAddress();
					}
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				BluetoothDevice bluetoothDevice = mBluetoothBLEManager.getBluetoothDevice();
				if (bluetoothDevice != null) {
					address = bluetoothDevice.getAddress();
				}
			}
		}
		return address ;
	}
	
	/**
	 * 获取连接蓝牙设备
	 */
	public static String getBluetoothDeviceAddress(Context context,boolean isFix, String serialNo) {
		MLog.d(TAG, "get Bluetooth Device address");
		String address = null;
		if(DeviceFactoryManager.getInstance().getIsBLEMode()==false) {
			if (DeviceFactoryManager.getInstance().isRemoteMode()) {
				IRemoteDeviceFactoryManagerCallBack remoteDeviceFactoryManagerCallBack = DeviceFactoryManager.getInstance().getRemoteDeviceFactoryManagerCallBack();
				IRemoteDeviceFactoryManager remoteDeviceFactoryManager = DeviceFactoryManager.getInstance().getRemoteDeviceFactoryManager();
				try {
					IRemoteBluetoothManager remoteBluetoothManager = remoteDeviceFactoryManager.getRemoteBluetoothManager(serialNo, isFix, remoteDeviceFactoryManagerCallBack);
					remoteDeviceFactoryManager.setDPUType(serialNo, Tools.isTruck(context, serialNo), Tools.isCarAndHeavyduty(context, serialNo));
					address = remoteBluetoothManager.getBluetoothDeviceAddress();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					IPhysics iPhysics = DeviceFactoryManager.getInstance().getCurrentDevice();
					BluetoothManager bluetoothManager = (iPhysics instanceof BluetoothManager) ? ((BluetoothManager) iPhysics) : null;
					if (bluetoothManager != null && bluetoothManager.getSerialNo().equals(serialNo)) {
						address = bluetoothManager.getBluetoothDeviceAddress();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			try {
				IPhysics iPhysics = DeviceFactoryManager.getInstance().getCurrentDevice();
				BluetoothManager bluetoothManager = (iPhysics instanceof BluetoothManager) ? ((BluetoothManager) iPhysics) : null;
				if (bluetoothManager != null && bluetoothManager.getSerialNo().equals(serialNo)) {
					address = bluetoothManager.getBluetoothDeviceAddress();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return address;
	}

	@Override
	public void setIsRemoteClientDiagnoseMode(boolean isRemoteClientDiagnoseMode) {
		MLog.d(TAG, "setIsRemoteClientDiagnoseMode call");
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode() == false) {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.setIsRemoteClientDiagnoseMode(isRemoteClientDiagnoseMode);
				}
			} else {
				try {
					mRemoteBluetoothManager.setIsRemoteClientDiagnoseMode(isRemoteClientDiagnoseMode);
				} catch (Exception e) {
					e.printStackTrace();
					//onRemoteException();
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.setIsRemoteClientDiagnoseMode(isRemoteClientDiagnoseMode);
			}
		}
	}

	@Override
	public boolean getIsRemoteClientDiagnoseMode() {
		MLog.d(TAG, "getIsRemoteClientDiagnoseMode call");
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode() == false) {
				if (mBluetoothManagerImpl != null) {
					return mBluetoothManagerImpl.getIsRemoteClientDiagnoseMode();
				} else {
					return false;
				}
			} else {
				try {
					return mRemoteBluetoothManager.getIsRemoteClientDiagnoseMode();
				} catch (Exception e) {
					e.printStackTrace();
					//onRemoteException();
					return  false;
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				return mBluetoothBLEManager.getIsRemoteClientDiagnoseMode();
			} else {
				return false;
			}
		}
	}

	@Override
	public void setIsSupportOneRequestMoreAnswerDiagnoseMode(boolean isSupportOneRequestMoreAnswerDiagnoseMode){
		MLog.d(TAG, "setIsSupportOneRequestMoreAnswerDiagnoseMode");
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode() == false) {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.setIsSupportOneRequestMoreAnswerDiagnoseMode(isSupportOneRequestMoreAnswerDiagnoseMode);
				}
			} else {
				try {
					mRemoteBluetoothManager.setIsSupportOneRequestMoreAnswerDiagnoseMode(isSupportOneRequestMoreAnswerDiagnoseMode);
				} catch (Exception e) {
					e.printStackTrace();
					//onRemoteException();
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.setIsSupportOneRequestMoreAnswerDiagnoseMode(isSupportOneRequestMoreAnswerDiagnoseMode);
			}
		}
	}

	@Override
	public boolean getIsSupportOneRequestMoreAnswerDiagnoseMode() {
		MLog.d(TAG, "getIsSupportOneRequestMoreAnswerDiagnoseMode call");
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode() == false) {
				if (mBluetoothManagerImpl != null) {
					return mBluetoothManagerImpl.getIsSupportOneRequestMoreAnswerDiagnoseMode();
				} else {
					return false;
				}
			} else {
				try {
					return mRemoteBluetoothManager.getIsSupportOneRequestMoreAnswerDiagnoseMode();
				} catch (Exception e) {
					e.printStackTrace();
					//onRemoteException();
					return  false;
				}
			}
		}
		else{
			if (mBluetoothBLEManager != null) {
				return mBluetoothBLEManager.getIsSupportOneRequestMoreAnswerDiagnoseMode();
			} else {
				return false;
			}
		}
	}

    @Override
    public int getLinkMode() {
        return DeviceFactoryManager.LINK_MODE_BLUETOOTH;
    }

    @Override
    public void setLinkParameters(LinkParameters linkParameters) {

    }

    @Override
    public IPhysics getPhysics() {
        return this;
    }

    @Override
    public void setAssitsPhysicsMatcher(IAssitsPhysicsMatcher assitsPhysicsMatcher) {
        mAssitsPhysicsMatcher = assitsPhysicsMatcher;
    }

    @Override
    public IAssitsPhysicsMatcher getAssitsPhysicsMatcher() {
        return mAssitsPhysicsMatcher;
    }

	/**
	 * @param isSetCommandOfMainLink 设置数据是否传给蓝牙主连接（针对蓝牙一对多的项目）
	 */
	@Override
	public void setCommandStatus(boolean isSetCommandOfMainLink) {
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode() ) {
			} else {
				if (mBluetoothManagerImpl != null) {
					mBluetoothManagerImpl.setCommandStatus(isSetCommandOfMainLink);
				}
			}
		} else {
			if (mBluetoothBLEManager != null) {
				mBluetoothBLEManager.setCommandStatus(isSetCommandOfMainLink);
			}
		}
		
	}

	@Override
	public boolean getCommandStatus() {
		boolean bCurrentStatus;
		if(mIsBleMode==false) {
			if (RomoteLocalSwitch.getInstance().isRemoteMode() ) {
				bCurrentStatus = true;
			} else {
				if (mBluetoothManagerImpl != null) bCurrentStatus =  mBluetoothManagerImpl.getCommandStatus();
				else bCurrentStatus = true;
			}
		}
		else {
			if (mBluetoothBLEManager != null) bCurrentStatus = mBluetoothBLEManager.getCommandStatus();
			else bCurrentStatus = true;
		}
		return bCurrentStatus;
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
