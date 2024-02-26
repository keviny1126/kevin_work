package com.cnlaunch.physics.bluetooth.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.bluetooth.remote.BluetoothManagerImpl;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.utils.ByteBufferStream;
import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.remote.ReadByteDataStream;
import com.power.baseproject.utils.EasyPreferences;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * [BLE蓝牙控制类]
 *
 * @author 谢飞虹
 * @version 4.0
 * @date 2017-08-01
 **/
public class BluetoothBLEManager implements IPhysics {
    private static final String TAG = "BluetoothBLEManager";

    /**
     * 蓝牙地址
     */
    public static final String BluetoothAddress = "bluetooth_address";
    /**
     * 蓝牙名称
     */
    public static final String BluetoothName = "bluetooth_name";
    private static String mReadData = "";
    private Context mContext;
    private ReadByteDataStream mReadByteDataStreamThread;
    private int mState;
    private boolean commandWait = true;
    private boolean isFix;
    private DeviceFactoryManager mDeviceFactoryManager;
    private String mSerialNo;
    private boolean mIsTruckReset;
    public static BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private String UUID_SERVER_UART = "0000fff0-0000-1000-8000-00805f9b34fb";
    private static String UUID_CHAR_READ_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";
    private static final int AUTO_RECONNECT_COUNT = 3;
    private int mBLEMTU;
    private BleReceiveDataByteBufferStream mBleReceiveDataByteBufferStream;
    private BleSendDataByteBufferStream mBleSendDataByteBufferStream;
    private SendDataThread mSendDataThread;
    private BluetoothBLEInputStream mBluetoothBLEInputStream;
    private BluetoothBLEOutputStream mBluetoothBLEOutputStream;
    private boolean mIsRemoteClientDiagnoseMode;
    private BluetoothDevice mBluetoothDevice;
    private boolean mIsAutoConnect;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScannerManager mBluetoothLeScannerManager;
    private int mAutoReConnect;
    private boolean mIsSupportOneRequestMoreAnswerDiagnoseMode;
    //DHC add
    private boolean mbIsSetCommandOfMainLink = true;
    private boolean mIsStop;//是否停止当前所有的读写操作

    public BluetoothBLEManager(DeviceFactoryManager deviceFactoryManager, Context context, boolean isFix, String serialNo) {
        // 引用diagnoseactivity
        // 会使使用activity作为接头的fragment释放不掉
        mContext = context.getApplicationContext();
        this.isFix = isFix;
        mDeviceFactoryManager = deviceFactoryManager;
        mReadByteDataStreamThread = null;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mState = STATE_NONE;
        mSerialNo = serialNo;
        mIsTruckReset = false;
        mBluetoothGatt = null;
        if (TextUtils.isEmpty(deviceFactoryManager.getBLEUARTServicesUUID()) == false) {
            UUID_SERVER_UART = deviceFactoryManager.getBLEUARTServicesUUID();
        }

        mBLEMTU = 20;
        mBleReceiveDataByteBufferStream = new BleReceiveDataByteBufferStream();
        mBleSendDataByteBufferStream = new BleSendDataByteBufferStream();
        mSendDataThread = null;
        mBluetoothBLEInputStream = new BluetoothBLEInputStream(this);
        mBluetoothBLEOutputStream = new BluetoothBLEOutputStream(this, deviceFactoryManager.getIPhysicsOutputStreamBufferWrapper());
        mIsAutoConnect = false;
        mAutoReConnect = AUTO_RECONNECT_COUNT;
        mBluetoothLeScannerManager = null;
        mIsRemoteClientDiagnoseMode = false;
        mIsSupportOneRequestMoreAnswerDiagnoseMode = false;
    }

    @Override
    protected void finalize() {
        try {
            MLog.e(TAG, "finalize BluetoothBLEManager");
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
    public String getCommand() {
        return mReadData;
    }

    /**
     * 设置完整指令内容
     */
    public void setCommand(String command) {
        mReadData = command;
        if (mbIsSetCommandOfMainLink) mDeviceFactoryManager.send(command);
    }

    public void setCommand(String command, boolean isSupportSelfSend) {
        if (isSupportSelfSend) mReadData = command;
        else setCommand(command);
    }

    /**
     * 获取读数据流
     */
    public InputStream getInputStream() {
        return mBluetoothBLEInputStream;
    }

    /**
     * 获取写数据流
     */
    public OutputStream getOutputStream() {
        return mBluetoothBLEOutputStream;
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
        String deviceName = "";
        if (mBluetoothDevice != null) {
            MLog.d(TAG, "ble remoteDevice is not null.");
            deviceName = mBluetoothDevice.getName();
            if (deviceName == null) {
                deviceName = "";
            }
        }
        return deviceName;
    }

    @Override
    public void closeDevice() {
        if (mReadByteDataStreamThread != null) {
            mReadByteDataStreamThread.cancel();
            mContext.sendBroadcast(new Intent(ACTION_DIAG_UNCONNECTED));
            mReadByteDataStreamThread = null;
        }
        if (mSendDataThread != null) {
            mSendDataThread.stopThead();
            mSendDataThread = null;
        }
        mBleReceiveDataByteBufferStream.close();
        mBleSendDataByteBufferStream.close();
        disconnect();
        close();
        setState(STATE_NONE);
    }

    private void connectionFailed(boolean isConnectFail) {
        connectionFailed(null, isConnectFail);
    }

    private void connectionFailed(String message, boolean isConnectFail) {
        setState(STATE_NONE);
        sendCustomBluetoothStatusBroadcast(mContext,
                BluetoothManagerImpl.ACTION_BT_DEVICE_CON_FAIL,
                BluetoothManagerImpl.BT_DEVICE_CON_FAIL,
                mContext.getString(R.string.bluetooth_connect_fail),
                mBluetoothDevice, mAutoReConnect);
        // 发送连接失败广播
        Intent intent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_FAIL);
        intent.putExtra(IS_CONNECT_FAIL, isConnectFail);
        intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
        if (message == null) {
            intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.bluetooth_connect_fail));
        } else {
            intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, message);
        }
        if (mBluetoothDevice != null) {
            String deviceName = mBluetoothDevice.getName();
            intent.putExtra("deviceName", deviceName);
        }
        mContext.sendBroadcast(intent);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                sendCustomBluetoothStatusBroadcast(mContext,
                        BluetoothManagerImpl.ACTION_BT_DEVICE_CON_SUCCESS,
                        BluetoothManagerImpl.BT_DEVICE_CON_SUCCESS, mContext.getString(R.string.bluetooth_connected), mBluetoothDevice, 0);
                Intent intent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS);
                intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
                String deviceName = ((mBluetoothDevice != null) ? mBluetoothDevice.getName() : "");
                if (deviceName == null) {
                    deviceName = "";
                }
                intent.putExtra("deviceName", deviceName);
                intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.msg_serialport_connect_state_success));
                mContext.sendBroadcast(intent);
                MLog.e(TAG, "ble connected success,starting transfer data ");
                // 发送广播通知连接成功
                mContext.sendBroadcast(new Intent(ACTION_DIAG_CONNECTED));
            } else if (msg.what == 1) {
                Intent disconnectIntent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_DISCONNECTED);
                disconnectIntent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
                String deviceName = ((mBluetoothDevice != null) ? mBluetoothDevice.getName() : "");
                if (deviceName == null) {
                    deviceName = "";
                }
                disconnectIntent.putExtra("deviceName", deviceName);
                mContext.sendBroadcast(disconnectIntent);
            } else if (msg.what == 2) {
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
        if (mHandler != null) {
            Message message = mHandler.obtainMessage(0, 0, 0);
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
        mIsRemoteClientDiagnoseMode = isRemoteClientDiagnoseMode;
    }

    @Override
    public boolean getIsRemoteClientDiagnoseMode() {
        return mIsRemoteClientDiagnoseMode;
    }

    public boolean isAutoConnect() {
        return mIsAutoConnect;
    }

    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    private boolean matchFilterRule(BluetoothDevice device, String destBluetoothName) {
        String deviceName = device.getName();
        return (deviceName != null && deviceName.equalsIgnoreCase(destBluetoothName));
    }

    private class CustomBluetoothLeScannerManagerCallBack implements BluetoothLeScannerManager.BluetoothLeScannerManagerCallBack {
        private String destBluetoothname;

        public CustomBluetoothLeScannerManagerCallBack(String destBluetoothname) {
            this.destBluetoothname = destBluetoothname;
        }

        @Override
        public void onStartScan() {
            MLog.i(TAG, "BLE_SCAN_STARTED");
        }

        @Override
        public void onStopScan() {
            MLog.i(TAG, "BLE_SCAN_FINISHED");
        }

        @Override
        public void onScanResult(BluetoothDevice bluetoothDevice, int rssi) {
            MLog.d(TAG, "onScanResult =bluetoothDevice " + bluetoothDevice.getName());
            if (matchFilterRule(bluetoothDevice, this.destBluetoothname)) {
                MLog.i(TAG, "match Devices name=" + bluetoothDevice.getName() + " address=" + bluetoothDevice.getAddress());
                EasyPreferences.Companion.getInstance().put(BluetoothName, bluetoothDevice.getName());
                EasyPreferences.Companion.getInstance().put(BluetoothAddress, bluetoothDevice.getAddress());
                if (mBluetoothLeScannerManager != null) {
                    mBluetoothLeScannerManager.stopScan();
                }
                mBluetoothDevice = bluetoothDevice;
                connectBluetoothDevice();
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            MLog.d(TAG, "BLE_SCAN_FINISHED WITH ERROR=" + errorCode);
            connectionFailed(false);
        }
    }

    ;

    /**
     * 自动连接
     *
     * @param serialNo
     * @param deviceAddress
     */
    public void autoBluetoothConnect(String serialNo, String deviceAddress) {
        MLog.e(TAG, "auto Bluetooth Connect serialNo=" + serialNo + "deviceAddress=" + deviceAddress);
        mIsAutoConnect = true;
        mAutoReConnect = AUTO_RECONNECT_COUNT;
        if (TextUtils.isEmpty(deviceAddress) && TextUtils.isEmpty(serialNo)) {
            connectionFailed(false);
            return;
        }
        if (!TextUtils.isEmpty(deviceAddress)) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
            if (device != null) {
                mBluetoothDevice = device;
                connectBluetoothDevice();
            } else {
                connectionFailed(false);
            }
        } else if (!TextUtils.isEmpty(serialNo)) {
            if (mBluetoothLeScannerManager == null) {
                mBluetoothLeScannerManager = new BluetoothLeScannerManager(mBluetoothAdapter, new CustomBluetoothLeScannerManagerCallBack(serialNo));
            }
            mBluetoothLeScannerManager.startScan();
        }
    }

    public void connect(final BluetoothDevice device) {
        mIsAutoConnect = false;
        mAutoReConnect = AUTO_RECONNECT_COUNT;
        mBluetoothDevice = device;
        connectBluetoothDevice();
    }

    private void connectBluetoothDevice() {
        if (mReadByteDataStreamThread != null) {
            mReadByteDataStreamThread.cancel();
            mReadByteDataStreamThread = null;
        }
        if (mSendDataThread != null) {
            mSendDataThread.stopThead();
            mSendDataThread = null;
        }
        mBleReceiveDataByteBufferStream.close();
        mBleSendDataByteBufferStream.close();
        sendCustomBluetoothStatusBroadcast(mContext,
                BluetoothManagerImpl.ACTION_BT_DEVICE_CON_CONING, BluetoothManagerImpl.BT_DEVICE_CON_CONING,
                mContext.getString(R.string.bluetooth_connecting),
                mBluetoothDevice, mAutoReConnect);
        if (MLog.isDebug) {
            MLog.d(TAG, "start create a new connection.  ");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        } else {
            // setting the autoConnect parameter to false.
            reflectionUseConnectGatt();
//			mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback);
        }
        if (MLog.isDebug) {
            MLog.d(TAG, "Trying to create a new connection. Gatt: " + mBluetoothGatt);
        }
        if (mBluetoothGatt != null) {
            setState(STATE_CONNECTING);
        } else {
            reConnectionCheckForConnectionFailed();
            return;
        }
    }

    /**
     * 失败失败后重新连接检查
     */
    private void reConnectionCheckForConnectionFailed() {
        sendCustomBluetoothStatusBroadcast(mContext, BluetoothManagerImpl.ACTION_BT_DEVICE_CON_FAIL,
                BluetoothManagerImpl.BT_DEVICE_CON_FAIL,
                mContext.getString(R.string.bluetooth_connect_fail),
                mBluetoothDevice, mAutoReConnect);
        // 发送连接失败广播
        if (mAutoReConnect == 1) {
            connectionFailed(true);
            //只有重试次数满足时，才改变连接状态
            setState(STATE_NONE);
            return;
        }
        mHandler.sendEmptyMessageDelayed(2, 500);
    }

    /**
     * 自动重新连接蓝牙对象
     */
    private void reConnectBluetoothDeviceHandler() {
        MLog.e(TAG, "开始重新连接 剩余次数: " + (mAutoReConnect - 1));
        // 执行前仍需要判断条件是否满足
        if (getBluetoothDevice() == null || mAutoReConnect <= 1) {
            return;
        }
        MLog.e(TAG, "ReConnect TimerTask Start");
        mAutoReConnect--;
        connectBluetoothDevice();
    }

    private void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    private void close() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (MLog.isDebug) {
                MLog.d(TAG, "onConnectionStateChange : " + status + "  newState : " + newState);
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //android 5.1 ble通讯还不成熟，mtu还是采用原来的23字节
                if (Build.VERSION.SDK_INT < 23) {
                    gatt.requestMtu(23);
                } else {
                    gatt.requestMtu(512);
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (getState() == STATE_CONNECTING) {
                    close();
                    reConnectionCheckForConnectionFailed();
                    setState(STATE_NONE);
                }
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            if (MLog.isDebug) {
                MLog.d(TAG, "onMtuChanged MTU: " + mtu + "status: " + status);
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBLEMTU = mtu - 3;
            } else {
                mBLEMTU = 20;
            }
            mBluetoothGatt.discoverServices();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> listBluetoothGattService = mBluetoothGatt.getServices();
            for (BluetoothGattService bluetoothGattService : listBluetoothGattService) {
                if (MLog.isDebug) {
                    MLog.d(TAG, "BluetoothGattService Uuid=" + bluetoothGattService.getUuid().toString());
                }
            }
            DeviceFactoryManager.BleServicesDiscoveredCallBack bleServicesDiscoveredCallBack = mDeviceFactoryManager.getBleServicesDiscoveredCallBack();
            if (bleServicesDiscoveredCallBack != null) {
                String aimServiceUUID = bleServicesDiscoveredCallBack.getAimService(gatt);
                if (TextUtils.isEmpty(aimServiceUUID) == false) {
                    UUID_SERVER_UART = aimServiceUUID;
                }
            }
            bluetoothGattSetting(gatt);
            //mtu设置成功，才认为连接完成
            if (MLog.isDebug) {
                MLog.i(TAG, "ble连接成功开启读取数据的线程 ");
            }
            mReadByteDataStreamThread = new ReadByteDataStream(BluetoothBLEManager.this, getInputStream(), getOutputStream());
            new Thread(mReadByteDataStreamThread).start();
            mSendDataThread = new SendDataThread();
            mSendDataThread.start();
            setState(STATE_CONNECTED);
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (MLog.isDebug) {
                MLog.d(TAG, "onCharacteristicChanged uuid=" + characteristic.getUuid().toString());
            }
            final byte[] characteristicData = characteristic.getValue();
            if (characteristicData != null) {
                if (MLog.isDebug) {
                    MLog.d(TAG, "onCharacteristicChanged characteristicData=" + ByteHexHelper.bytesToHexStringWithSearchTable(characteristicData));
                }
                mBleReceiveDataByteBufferStream.write(characteristicData, 0, characteristicData.length);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
            if (MLog.isDebug) {
                MLog.d(TAG, "onCharacteristicRead status=" + status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (MLog.isDebug) {
                MLog.d(TAG, "onDescriptorWrite");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (MLog.isDebug) {
                MLog.d(TAG, "onCharacteristicWrite status =" + status);
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {

            } else {
                if (MLog.isDebug) {
                    MLog.d(TAG, "onCharacteristicWrite Send failed!");
                }
            }
            mBleSendDataByteBufferStream.sendSignal();
        }
    };

    public void bluetoothGattSetting(BluetoothGatt bluetoothGatt) {
        if (UUID_SERVER_UART.toUpperCase(Locale.ENGLISH).equals(BLEDeviceConfig.MICROCHIP_SERVICE_UUID)) {
            microchipBluetoothGattSetting(bluetoothGatt);
        } else {
            genericBluetoothGattSetting(bluetoothGatt, UUID_SERVER_UART,
                    "",
                    "");
        }
    }

    private void genericBluetoothGattSetting(BluetoothGatt bluetoothGatt, String service_uuid,
                                             String notify_characteristics_uuid,
                                             String write_characteristics_uuid) {
        BluetoothGattService qppService = bluetoothGatt.getService(UUID.fromString(service_uuid));
        if (qppService == null) {
            MLog.d(TAG, "service not found");
            return;
        }
        List<BluetoothGattCharacteristic> gattCharacteristics = qppService.getCharacteristics();
        MLog.d(TAG, "gattCharacteristics size=" + gattCharacteristics.size());
        for (int j = 0; j < gattCharacteristics.size(); j++) {
            BluetoothGattCharacteristic chara = gattCharacteristics.get(j);
            MLog.d(TAG, "char Uuid is " + chara.getUuid().toString() + "chara.getProperties() = " + chara.getProperties());
            if (((chara.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) == BluetoothGattCharacteristic.PROPERTY_WRITE ||
                    (chara.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) &&
                    (TextUtils.isEmpty(write_characteristics_uuid) || chara.getUuid().equals(UUID.fromString(write_characteristics_uuid)))) {
                MLog.d(TAG, "Wr char is " + chara.getUuid().toString());
                mWriteCharacteristic = chara;
                //设置回复形式
                //DHC eidt 传输4k的数据使用WRITE_TYPE_NO_RESPONSE大概率会丢包，经和芯片厂商验证先改为WRITE_TYPE_DEFAULT方式，比之前的方式每包增加200ms时间；总体的固件升级增加三到四秒可接受范围。
                //mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                if (BLEDeviceConfig.MICROCHIP_SERVICE_UUID.equals(UUID_SERVER_UART))
                    mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                else
                    mWriteCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            }
            if ((chara.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == BluetoothGattCharacteristic.PROPERTY_NOTIFY &&
                    (TextUtils.isEmpty(notify_characteristics_uuid) || chara.getUuid().equals(UUID.fromString(notify_characteristics_uuid)))) {
                MLog.d(TAG, "NotiChar UUID is : " + chara.getUuid().toString());
                setCharacteristicNotification(bluetoothGatt, chara, true);
            }
        }
    }

    private void microchipBluetoothGattSetting(BluetoothGatt bluetoothGatt) {
        genericBluetoothGattSetting(bluetoothGatt, BLEDeviceConfig.MICROCHIP_SERVICE_UUID,
                BLEDeviceConfig.MICROCHIP_NOTIFY_CHARACTERISTICS_UUID,
                BLEDeviceConfig.MICROCHIP_WRITE_CHARACTERISTICS_UUID);
    }

    private boolean setCharacteristicNotification(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bluetoothGatt == null) {
            MLog.d(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        try {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(UUID_CHAR_READ_DESCRIPTOR));
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                return (bluetoothGatt.writeDescriptor(descriptor));
            } else {
                MLog.d(TAG, "descriptor is null");
                return false;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return true;
    }

    public int read(byte[] readbuffer) {
        return read(readbuffer, 0, readbuffer.length);
    }

    public int read(byte[] readbuffer, int offset, int length) {
        return mBleReceiveDataByteBufferStream.read(readbuffer, offset, length);
    }

    public void write(byte[] b, int offset, int length) {
        mBleSendDataByteBufferStream.write(b, offset, length);
    }

    public void outputStreamClose() {
        mBleReceiveDataByteBufferStream.close();
    }

    public void inputStreamClose() {
        mBleSendDataByteBufferStream.close();
    }

    /**
     * @param context
     * @param action
     * @param type
     * @param content
     * @param bluetoothDevice
     * @param autoReConnect   加入蓝牙连接次数，用于更新蓝牙连接界面
     */
    private void sendCustomBluetoothStatusBroadcast(Context context,
                                                    String action, int type, String content,
                                                    BluetoothDevice bluetoothDevice, int autoReConnect) {
        Intent intent = new Intent(action);
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        bundle.putString("status", content);
        bundle.putInt("pair", 12);
        if (action.equalsIgnoreCase(BluetoothManagerImpl.ACTION_BT_DEVICE_CON_CONING)) {
            bundle.putInt("auto_reconnect_count", (AUTO_RECONNECT_COUNT - autoReConnect) + 1);
        }
        bundle.putParcelable("bluetoothDevice", bluetoothDevice);
        intent.putExtra("customBluetoothBroadcastIntentExtraBundle", bundle);
        intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
        context.sendBroadcast(intent);
    }

    private class BleReceiveDataByteBufferStream extends ByteBufferStream {
        private static final String TAG = "BleReceiveDataByteBufferStream";
        private final Lock mNotificationLock;
        private final Condition mNotificationCondition;

        public BleReceiveDataByteBufferStream() {
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

        public int read(byte[] readbuffer, int offset, int length) {
            int count;
            mNotificationLock.lock();
            try {
                if (this.length <= 0) {
                    mNotificationCondition.await();
                }
                count = super.readBytes(readbuffer, offset, length);
            } catch (InterruptedException e) {
                count = 0;
                e.printStackTrace();
            }
            mNotificationLock.unlock();
            return count;
        }
    }

    private class BleSendDataByteBufferStream extends ByteBufferStream {
        private static final String TAG = "BleSendDataByteBufferStream";
        private final Lock mSendDataLock;
        private final Condition mSendDataCondition;

        public BleSendDataByteBufferStream() {
            super();
            mSendDataLock = new ReentrantLock();
            mSendDataCondition = mSendDataLock.newCondition();
        }

        public void sendSignal() {
            mSendDataLock.lock();
            try {
                mSendDataCondition.signal();
            } finally {
                mSendDataLock.unlock();
            }
        }

        public void close() {
            mSendDataLock.lock();
            try {
                mSendDataCondition.signal();
            } finally {
                mSendDataLock.unlock();
            }
        }

        @Override
        public void write(byte[] writeBuffer, int offset, int count) {
            synchronized (this) {
                super.write(writeBuffer, offset, count);
            }
        }

        /**
         * 发送缓冲数据
         *
         * @param readbuffer
         * @return
         */
        public int sendData(byte[] readbuffer) {
            int count;
            boolean isSuccessful = true;
            mSendDataLock.lock();
            try {
                synchronized (this) {
                    count = super.readBytes(readbuffer);
                }
                if (count > 0) {
                    if (count == readbuffer.length) {
                        do {
                            mWriteCharacteristic.setValue(readbuffer);
                            if (!mIsStop && mBluetoothGatt != null) {
                                isSuccessful = mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
                            } else {
                                isSuccessful = true;
                            }
                            if (MLog.isDebug && isSuccessful == false) {
                                MLog.d(TAG, "写数据失败 ");
                            }
                        } while (isSuccessful == false);
                    } else {
                        byte tempArray[] = new byte[count];
                        System.arraycopy(readbuffer, 0, tempArray, 0, count);

                        do {
                            mWriteCharacteristic.setValue(tempArray);
                            if (!mIsStop && mBluetoothGatt != null) {
                                isSuccessful = mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
                            } else {
                                isSuccessful = true;
                            }

                            if (MLog.isDebug && isSuccessful == false) {
                                MLog.d(TAG, "写数据失败 in flush");
                            }
                        } while (isSuccessful == false);
                    }

                    mSendDataCondition.await();
                    if (MLog.isDebug) {
                        MLog.d(TAG, "mSendDataCondition.await signal");
                    }
                    //最大超时设置为3秒
                    //测试发现此方法会导致ble数据发送阻塞，所以改用mSendDataCondition.await()方法
                    //mSendDataCondition.await(3,TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                count = 0;
                e.printStackTrace();
            }
            mSendDataLock.unlock();
            return count;
        }
    }


    private class SendDataThread extends Thread {
        //		private boolean mIsStop;
        public SendDataThread() {
            mIsStop = false;
        }

        public void stopThead() {
            mIsStop = true;
            mBleSendDataByteBufferStream.close();
        }

        @Override
        public void run() {
            byte[] readbuffer = new byte[mBLEMTU];
            while (mIsStop == false) {
                int count = mBleSendDataByteBufferStream.sendData(readbuffer);
                if (count == 0) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 是否支持一问多答数据收发模式
     */
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

    //DHC add 外部可自定义UUID 使用TpmsGunManager类时会赋值
    public void setSelfUUID(String selfUUID) {
        UUID_SERVER_UART = selfUUID;
    }

    //DHC add 联想一款5.1.1的平板只能使用BluetoothDevice.TRANSPORT_LE方式才能连接低功耗蓝牙
    private void reflectionUseConnectGatt() {
        try {
            Class[] clzParams = {Context.class, boolean.class, BluetoothGattCallback.class, int.class};
            Method method = mBluetoothDevice.getClass().getDeclaredMethod("connectGatt", clzParams);
            Object[] ss = new Object[]{mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE};
            mBluetoothGatt = (BluetoothGatt) method.invoke(mBluetoothDevice, ss);
        } catch (Exception e) {
            MLog.e(TAG, e.toString());
        } finally {
            if (mBluetoothGatt == null)
                mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback);
        }
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
