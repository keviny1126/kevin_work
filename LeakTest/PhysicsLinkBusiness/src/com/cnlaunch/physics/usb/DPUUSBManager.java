package com.cnlaunch.physics.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.remote.ReadByteDataStream;

import java.io.InputStream;
import java.io.OutputStream;

public class DPUUSBManager implements IPhysics {
    private final static String TAG = "DPUUSBManager";
    private DPUUSBDevice mUsbDevice;
    private String mPermisson;
    private Context mContext;
    private ReadByteDataStream mReadByteDataStreamThread;
    private boolean isFix;
    private DeviceFactoryManager mDeviceFactoryManager;
    private String mReadData;
    private USBInputStream mUSBInputStream;
    private USBOutputStream mUSBOutputStream;
    private String mSerialNo;
    private boolean mIsTruckReset;
    private boolean mIsRemoteClientDiagnoseMode;
    private boolean mIsSupportOneRequestMoreAnswerDiagnoseMode;
    private boolean mbIsSetCommandOfMainLink = false;

    /**
     * 初始化dpu usb设备管理类
     *
     * @param context
     */
    public DPUUSBManager(DeviceFactoryManager deviceFactoryManager, Context context, boolean isFix, String serialNo) {
        mDeviceFactoryManager = deviceFactoryManager;
        mContext = context.getApplicationContext();
        this.isFix = isFix;
        mReadByteDataStreamThread = null;
        String APP_USB_PERMISSION = mContext.getPackageName();
        APP_USB_PERMISSION += ".USB_PERMISSION";
        mPermisson = APP_USB_PERMISSION;
        mUsbDevice = new DPUUSBDevice(mContext, APP_USB_PERMISSION);
        registerBoardcastReciver();
        mUSBInputStream = null;
        mUSBOutputStream = null;
        mSerialNo = serialNo;
        mIsTruckReset = false;
        mIsRemoteClientDiagnoseMode = false;
        mIsSupportOneRequestMoreAnswerDiagnoseMode = false;
    }

    @Override
    protected void finalize() {
        try {
            MLog.e(TAG, "finalize DPUUSBManager");
            super.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private void setState(int state) {

    }

    @Override
    public int getState() {
        int customState = mUsbDevice.getStatus();
        MLog.d(TAG, "UsbDevice State =" + customState);
        int state = IPhysics.STATE_NONE;
        switch (customState) {
            case Connector.STATE_RUNNING:
                state = IPhysics.STATE_CONNECTED;
                break;
            case Connector.STATE_CONNECTED:
                state = IPhysics.STATE_CONNECTED;
                break;
            case Connector.STATE_CONNECTING:
                state = IPhysics.STATE_CONNECTING;
                break;
            default:
                state = IPhysics.STATE_NONE;
                break;
        }
        return state;
    }

    @Override
    public String getCommand() {
        return mReadData;
    }

    @Override
    public void setCommand(String command) {
        mReadData = command;
        if (mbIsSetCommandOfMainLink) mDeviceFactoryManager.send(command);
    }

    @Override
    public void setCommand(String command, boolean isSupportSelfSend) {
        if (isSupportSelfSend) mReadData = command;
        else setCommand(command);
    }

    @Override
    public InputStream getInputStream() {
        return mUSBInputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return mUSBOutputStream;
    }

    private boolean commandWait = true;

    @Override
    public synchronized boolean getCommand_wait() {
        return commandWait;
    }

    @Override
    public synchronized void setCommand_wait(boolean wait) {
        commandWait = wait;
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public String getDeviceName() {
        String deviceName = "";
        if (mUsbDevice != null) {
            MLog.d(TAG, "mUsbDevice is not null.");
            deviceName = mUsbDevice.getDeviceName();
        }
        return deviceName;
    }

    private void registerBoardcastReciver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(mPermisson);
        MLog.d(TAG, "mUeventBroadcastReceiver registerReceiver=." + mUeventBroadcastReceiver.toString());
        mContext.registerReceiver(mUeventBroadcastReceiver, filter);
    }

    private void unregisterBoardcasetReciver() {
        MLog.d(TAG, "mUeventBroadcastReceiver=." + mUeventBroadcastReceiver.toString());
        if (null != mUeventBroadcastReceiver) {
            try {
                mContext.unregisterReceiver(mUeventBroadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private BroadcastReceiver mUeventBroadcastReceiver = new BroadcastReceiver() {
        // 以下3个广播可以完成：设备查找、获取设备访问权限、打开设备、关闭设备
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int state = Connector.STATE_UNKNOWN;
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {// 设备插入
                MLog.d(TAG, "ACTION_USB_DEVICE_ATTACHED");
                //判断是否为匹配的接头设备

                if (mDeviceFactoryManager.getNeedChangeLinkMode() || mDeviceFactoryManager.getResetStatus()) {
                    if (!DPUUSBManager.this.queryIsMatchDevice(intent)) {
                        return;
                    }
                    state = connect(intent);// 连接设备(自动调用查询设备、获取权限、如果前2步成功则自动调用打开设备)
                    doConnectAction(state, false);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {// 设备拔出
                MLog.d(TAG, "ACTION_USB_DEVICE_DETACHED");
                //判断是否为匹配的接头设备
                if (!DPUUSBManager.this.queryIsMatchDevice(intent)) {
                    return;
                }
                MLog.d(TAG, "DEVICE_DETACHED before status=" + mUsbDevice.getStatus());
                //此处状态值不确定，只用于测试，只在usb测试放开
                //if(Connector.STATE_RUNNING == mUsbDevice.getStatus())
                {
                    state = mUsbDevice.disconnect(intent);// 断开设备(自动调用关闭设备)
                    doDisconnectAction(state);
                }
            } else if (mPermisson.equals(action)) {// 请求USB 权限，当用户点击授权
                MLog.d(TAG, "Permisson REQUEST");
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    MLog.d(TAG, "Permisson REQUEST TRUE");
                    state = openAferPermissonRequest();// 用户授权后，打开设备
                    doConnectWithPermissonRequestAction(state);
                } else {
                    doConnectWithPermissonRequestAction(Connector.STATE_NO_PERMISSION);
                }

            }
        }

        ;
    };

    private String getConnectorStateText(boolean isConnectAction, int state) {
        String message = "";
        switch (state) {
            case Connector.STATE_NO_DEVICE_DETECTED:
                message = mContext.getResources().getString(
                        R.string.msg_usb_state_no_device_detected);
                break;
            case Connector.STATE_NO_PERMISSION:
                message = mContext.getResources().getString(
                        R.string.msg_usb_state_no_permission);
                break;
            case Connector.STATE_NO_EXCLUSIVE_ACCESS:
                message = mContext.getResources().getString(
                        R.string.msg_usb_state_no_exclusive_access);
                break;
            case Connector.STATE_DEVICE_NOT_SUPPORT:
                message = mContext.getResources().getString(
                        R.string.msg_usb_state_device_not_support);
                break;
            case Connector.STATE_SUCCESS:
                if (isConnectAction)
                    message = mContext.getResources().getString(
                            R.string.msg_usb_connect_state_success);
                else
                    message = mContext.getResources().getString(
                            R.string.msg_usb_disconnect_state_success);
                break;
            default:
                if (isConnectAction)
                    message = mContext.getResources().getString(
                            R.string.msg_usb_connect_state_fail);
                else
                    message = mContext.getResources().getString(
                            R.string.msg_usb_disconnect_state_fail);
                break;
        }
        return message;
    }


    /**
     * 关闭设备
     */
    @Override
    public void closeDevice() {
        if (mReadByteDataStreamThread != null) {
            mReadByteDataStreamThread.cancel();
            sendStatusBoradcast(mContext, ACTION_DIAG_UNCONNECTED);
            mReadByteDataStreamThread = null;
        }

        mUsbDevice.close();
        unregisterBoardcasetReciver();

    }

    private void closeDeviceWithResetOrNeedChangeLinkMode() {
        if (mReadByteDataStreamThread != null) {
            mReadByteDataStreamThread.cancel();
            sendStatusBoradcast(mContext, ACTION_DIAG_UNCONNECTED);
            mReadByteDataStreamThread = null;
        }

        mUsbDevice.close();
    }

    /**
     * 打开接头<br/>
     *
     * @return 参见STATE_xxx, xxx为具体的状态
     */
    public int open(boolean isTest) {
        MLog.d(TAG, "open before status=" + mUsbDevice.getStatus());
        if (Connector.STATE_RUNNING == mUsbDevice.getStatus()) {
            return Connector.STATE_RUNNING;
        }
        int state = mUsbDevice.open();
        doConnectAction(state, isTest);
        return state;
    }

    public int open() {
        return open(false);
    }

    public int openAferPermissonRequest() {
        MLog.d(TAG, "open Afer Permisson Request before status=" + mUsbDevice.getStatus());
        if (Connector.STATE_RUNNING == mUsbDevice.getStatus()) {
            return Connector.STATE_RUNNING;
        }
        int state = mUsbDevice.open();
        return state;
    }

    private int connect(Intent intent) {
        MLog.d(TAG, "connect before status=" + mUsbDevice.getStatus());
        if (Connector.STATE_RUNNING == mUsbDevice.getStatus()) {
            return Connector.STATE_RUNNING;
        }
        int state = mUsbDevice.connect(intent);
        return state;
    }

    private void doConnectAction(int state, boolean isTest) {
        switch (state) {
            case Connector.STATE_SUCCESS:
                MLog.d(TAG, "Connect SUCCESS");
                createReadByteDataStreamThread();
                Intent successIntent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS);
                successIntent.putExtra(IPhysics.CONNECT_TYPE, "USB");
                successIntent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, getConnectorStateText(true, state));
                mContext.sendBroadcast(successIntent);
                break;
            case Connector.STATE_NO_PERMISSION:
                break;
            case Connector.STATE_RUNNING:
                break;
            default:
                if (isTest == false) {
                    Intent failIntent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_FAIL);
                    failIntent.putExtra(IS_CONNECT_FAIL, true);
                    failIntent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
                    failIntent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, getConnectorStateText(true, state));
                    mContext.sendBroadcast(failIntent);
                }
                break;
        }
    }

    private void doDisconnectAction(int state) {
        if (state == Connector.STATE_SUCCESS) {
            closeDeviceWithResetOrNeedChangeLinkMode();
            Intent successIntent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_DISCONNECTED);
            successIntent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
            mContext.sendBroadcast(successIntent);
        }
    }

    private void doConnectWithPermissonRequestAction(int state) {
        switch (state) {
            case Connector.STATE_SUCCESS:
                MLog.d(TAG, "Connect With Permisson Request SUCCESS");
                createReadByteDataStreamThread();
                Intent successIntent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS);
                successIntent.putExtra(IPhysics.CONNECT_TYPE, "USB");
                successIntent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, getConnectorStateText(true, state));
                mContext.sendBroadcast(successIntent);
                break;
            case Connector.STATE_RUNNING:
                break;
            default:
                Intent failIntent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_FAIL);
                failIntent.putExtra(IS_CONNECT_FAIL, true);
                failIntent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
                failIntent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, getConnectorStateText(true, state));
                mContext.sendBroadcast(failIntent);
                break;
        }
    }

    private static void sendStatusBoradcast(Context context,
                                            String broadCast) {
        context.sendBroadcast(new Intent(broadCast));
    }

    private void createReadByteDataStreamThread() {
        mUSBInputStream = new USBInputStream(mUsbDevice);
        mUSBOutputStream = new USBOutputStream(mUsbDevice, mDeviceFactoryManager.getIPhysicsOutputStreamBufferWrapper());
        mReadByteDataStreamThread = new ReadByteDataStream(this, mUSBInputStream, mUSBOutputStream);

        new Thread(mReadByteDataStreamThread).start();
        sendStatusBoradcast(mContext, ACTION_DIAG_CONNECTED);

    }

    /**
     * 查询是否匹配dpu设备
     *
     * @return
     */
    public boolean queryUsbDeviceExist() {
        if (mUsbDevice != null) {
            int state = mUsbDevice.queryDevice();
            MLog.d(TAG, "queryUsbDeviceExist STATE = " + state);
            if (state == Connector.STATE_SUCCESS) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 根据系统广播 UsbManager.ACTION_USB_DEVICE_ATTACHED，UsbManager.ACTION_USB_DEVICE_DETACHED intent
     * 查询是否匹配dpu设备
     */
    public boolean queryIsMatchDevice(Intent intent) {
        if (mUsbDevice != null) {
            return mUsbDevice.queryIsMatchDevice(intent);
        } else {
            return false;
        }
    }

    /**
     * 用于usb有线测试主动关闭有线连接，测试时请放开该内容
     */
	/*public void usbDeviceReset(){
		MLog.d(TAG,"usbDeviceReset before status="+mUsbDevice.getStatus());
		if(Connector.STATE_RUNNING == mUsbDevice.getStatus()){
			int state = mUsbDevice.close();
			doDisconnectAction(state);
		}
	}*/
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
        mIsTruckReset = isTruckReset;
    }

    @Override
    public synchronized boolean isTruckReset() {
        return mIsTruckReset;
    }

    @Override
    public void userInteractionWhenDPUConnected() {

    }

    @Override
    public void setIsFix(boolean isFix) {
        this.isFix = isFix;
    }

    @Override
    public void physicalCloseDevice() {
        closeDevice();
    }

    /**
     * 判断是否为带独立芯片的usb设备
     *
     * @return
     */
    public boolean isStandAloneChip(Intent intent) {
        if (mUsbDevice != null) {
            return mUsbDevice.isStandAloneChip(intent);
        } else {
            return false;
        }
    }

    public boolean isStandAloneChip() {
        if (mUsbDevice != null) {
            return mUsbDevice.isStandAloneChip();
        } else {
            return false;
        }
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
    public void setIsSupportOneRequestMoreAnswerDiagnoseMode(boolean isSupportOneRequestMoreAnswerDiagnoseMode) {
        mIsSupportOneRequestMoreAnswerDiagnoseMode = isSupportOneRequestMoreAnswerDiagnoseMode;
    }

    @Override
    public boolean getIsSupportOneRequestMoreAnswerDiagnoseMode() {
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
