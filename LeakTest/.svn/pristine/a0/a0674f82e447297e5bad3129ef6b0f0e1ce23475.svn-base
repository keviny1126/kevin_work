package com.cnlaunch.physics.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.bluetooth.ble.BluetoothLeScannerManager;
import com.cnlaunch.physics.entity.BluetoothListDto;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.listener.OnBluetoothListener;
import com.cnlaunch.physics.utils.MLog;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothScanManager {
    private static final String TAG = "BluetoothScanManager";
    //去掉所有控制逻辑，只要蓝牙名称符合公司接头命名规则就可以"([0-9]{12})"
    //private static String PRODUCT_TYPE_PORT = "9";// 主序列号前缀
    //private static String PRODUCT_TYPE_PORT_2 = "9";// GoloMasterDiag序列号前缀

    private static Context mmContext;
    private OnBluetoothListener mmListener;
    private ArrayList<BluetoothListDto> mDevicesArrayList;
    private ArrayList<BluetoothListDto> mNoMatchedDevicesArrayList;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mIsBLE;
    private boolean mIsNeedIncludePairedDevice;
    public BluetoothLeScannerManager mBluetoothLeScannerManager;
    private boolean mIsRegeisterBroadcast;
    private boolean mIsStopScan;

    public BluetoothScanManager(Context context) {
        this(context, false);
    }

    public BluetoothScanManager(Context context, boolean isBLE) {
        this(context, isBLE, true);
    }

    public BluetoothScanManager(Context context, boolean isBLE, boolean isNeedIncludePairedDevice) {
        mIsBLE = isBLE;
        mmContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mDevicesArrayList = new ArrayList<BluetoothListDto>();
        mNoMatchedDevicesArrayList = new ArrayList<BluetoothListDto>();
        /*String heavyduty = PhysicsCommonUtils.getProperty(mmContext, "is_heavyduty");
        boolean isHeavyduty = Boolean.parseBoolean(heavyduty);
        PRODUCT_TYPE_PORT = PhysicsCommonUtils.getProperty(mmContext, "serialNo_Prefix");

        String goloMaster = PhysicsCommonUtils.getProperty(mmContext, "isGoloMasterDiag");
        boolean isGoloMaster = Boolean.parseBoolean(goloMaster);
        if (isGoloMaster) {
            PRODUCT_TYPE_PORT_2 = PhysicsCommonUtils.getProperty(mmContext, "serialNo_Prefix_2");
        }
        else {
            PRODUCT_TYPE_PORT_2 = PRODUCT_TYPE_PORT;
        }*/
        if (mIsBLE) {
            mBluetoothLeScannerManager = new BluetoothLeScannerManager(mBluetoothAdapter, mBluetoothLeScannerManagerCallBack);
        } else {
            mBluetoothLeScannerManager = null;
        }
        mIsNeedIncludePairedDevice = isNeedIncludePairedDevice;
        mIsRegeisterBroadcast = false;
        mIsStopScan = false;
    }

    /**
     * 设置蓝牙状态回调监听
     *
     * @param onBluetoothListener
     */
    public void setOnBluetoothListener(OnBluetoothListener onBluetoothListener) {
        mmListener = onBluetoothListener;
        if (mmListener != null) {// 如果适配器为null，则不支持蓝牙
            mmListener.onBluetooth(mBluetoothAdapter, 0, mDevicesArrayList, null);
            return;
        }
    }

    private void regeisterBroadcast() {
        MLog.i(TAG, "BluetoothScanManager register Receiver");
        IntentFilter filter = new IntentFilter();
        if (!mIsBLE) {
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        }
        filter.addAction(BluetoothManager.ACTION_BT_DEVICE_CON_CONING);
        filter.addAction(BluetoothManager.ACTION_BT_DEVICE_CON_SUCCESS);
        filter.addAction(BluetoothManager.ACTION_BT_DEVICE_CON_FAIL);
        filter.addAction(BluetoothManager.ACTION_BT_DEVICE_CON_LOST);
        filter.addAction(IPhysics.ACTION_DPU_DEVICE_CONNECT_FAIL);
        mmContext.registerReceiver(mmReceiver, filter);
    }

    /**
     * 获取蓝牙列表
     *
     * @return
     */
    public ArrayList<BluetoothListDto> getBluetoothList() {
        return mDevicesArrayList;
    }

    /**
     * 开始搜索蓝牙设备
     */
    public void reScanBluetooth() {
        // 蓝牙扫描无需另开线程
        scanBluetoothList();
    }

    public BluetoothLeScannerManager getmBluetoothLeScannerManager(){
        return mBluetoothLeScannerManager;
    }

    public void stopScan() {
        try {
            if (mIsRegeisterBroadcast) {
                mmContext.unregisterReceiver(mmReceiver);
                mIsRegeisterBroadcast = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mIsBLE) {
            mBluetoothLeScannerManager.stopScan();
        } else {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
        mDevicesArrayList.clear();
        mNoMatchedDevicesArrayList.clear();
        mIsStopScan = true;
    }

    /**
     * 搜索蓝牙设备，先将配对过的蓝牙对象保存到列表中
     */
    public void scanBluetoothList() {
        if (!mIsRegeisterBroadcast) {
            mIsRegeisterBroadcast = true;
            regeisterBroadcast();
        }
        mIsStopScan = false;
        if (mIsBLE) {
            mBluetoothLeScannerManager.startScan();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            startDiscovery();
        }
    }

    public boolean isStopScan() {
        return mIsStopScan;
    }

    /**
     * 开始搜索蓝牙设备
     */
    private synchronized void startDiscovery() {
        mDevicesArrayList.clear();
        mNoMatchedDevicesArrayList.clear();
        if (mIsNeedIncludePairedDevice) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices != null) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device != null && matchFilterRule(device)) {
                        if (MLog.isDebug) {
                            MLog.d(TAG, "Bonded Devices name=" + device.getName());
                        }
                        setListDto(device, Short.MIN_VALUE, true, false);
                    }
                }
            }
        }
        MLog.e(TAG, "开始扫描蓝牙设备列表...");
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        // 本身为一个异步方法，不用放在多线程中处理
        mBluetoothAdapter.startDiscovery();
    }

    private void setNoMatchedListDto(BluetoothDevice device, int rssi, boolean isPair, boolean isHasRssi) {
        boolean flag = false;
        for (int i = 0; i < mNoMatchedDevicesArrayList.size(); i++) { // 重复蓝牙地址的不再添加
            if (device.getAddress().equals(mNoMatchedDevicesArrayList.get(i).getBluetoothAddress())) {
                mNoMatchedDevicesArrayList.get(i).setBluetoothName(device.getName());
                if (isHasRssi) {
                    mNoMatchedDevicesArrayList.get(i).setRssi(rssi);
                }
                flag = true;
                break;
            }
        }
        if (!flag) {
            BluetoothListDto bluetoothListDto = new BluetoothListDto();
            bluetoothListDto.setBluetoothName(device.getName());
            bluetoothListDto.setBluetoothAddress(device.getAddress());
            bluetoothListDto.setBluetoothPairStatus(isPair);
            bluetoothListDto.setBluetoothConnStatus(false);
            bluetoothListDto.setBluetoothDevice(device);
            if (isHasRssi) {
                bluetoothListDto.setRssi(rssi);
            }
        }
    }

    /**
     * 将蓝牙连接实体类写入LIST中
     *
     * @param device 蓝牙对象
     * @param isPair 是否配对
     */
    private void setListDto(BluetoothDevice device, int rssi, boolean isPair, boolean isHasRssi) {
        boolean flag = false;
        // 重复蓝牙地址的不再添加
        for (int i = 0; i < mDevicesArrayList.size(); i++) {
            BluetoothListDto tempBluetoothListDto = mDevicesArrayList.get(i);
            if (device.getAddress().equals(tempBluetoothListDto.getBluetoothAddress())) {
                tempBluetoothListDto.setBluetoothName(device.getName());
                if (isHasRssi) {
                    tempBluetoothListDto.setRssi(rssi);
                }
                flag = true;
                break;
            }
        }
        if (!flag) {
            BluetoothListDto bluetoothListDto = new BluetoothListDto();
            bluetoothListDto.setBluetoothName(device.getName());
            bluetoothListDto.setBluetoothAddress(device.getAddress());
            bluetoothListDto.setBluetoothPairStatus(isPair);
            bluetoothListDto.setBluetoothConnStatus(false);
            bluetoothListDto.setBluetoothDevice(device);
            if (isHasRssi) {
                bluetoothListDto.setRssi(rssi);
            } else {
                //从mNoMatchedDevicesArrayList中发现相同设备信号值
                for (int i = 0; i < mNoMatchedDevicesArrayList.size(); i++) { // 重复蓝牙地址的不再添加
                    if (device.getAddress().equals(mNoMatchedDevicesArrayList.get(i).getBluetoothAddress())) {
                        bluetoothListDto.setRssi(mNoMatchedDevicesArrayList.get(i).getRssi());
                        if (MLog.isDebug) {
                            MLog.d(TAG, "find device from mNoMatchedDevicesArrayList name=" + device.getName());
                        }
                        break;
                    }
                }
            }
            bluetoothListDto.setBluetoothConnWait(mmContext.getString(R.string.bluetooth_no_connected));
            mDevicesArrayList.add(bluetoothListDto);
        }
        if (mmListener != null) {
            mmListener.onBluetooth(mBluetoothAdapter, BluetoothManager.BT_DEVICE_ADD, mDevicesArrayList, null);
        }
    }

    /**
     * 设置蓝牙连接对象状态
     *
     * @param device
     * @param conn
     * @param pair
     */
    private void setBTConnectStatus(BluetoothDevice device, String conn, boolean pair) {
        if (device == null) {
            return;
        }
        for (int i = 0; i < mDevicesArrayList.size(); i++) {
            BluetoothDevice tempDev = mDevicesArrayList.get(i).getBluetoothDevice();
            if (tempDev != null && tempDev.getAddress().equals(device.getAddress())) {
                mDevicesArrayList.get(i).setBluetoothPairStatus(pair);
                mDevicesArrayList.get(i).setBluetoothConnWait(conn);
            } else {
                mDevicesArrayList.get(i).setBluetoothConnWait(mmContext.getString(R.string.bluetooth_no_connected));
            }
        }
    }

    /***
     * 设置蓝牙连接对象状态
     *
     * @param deviceName
     * @param conn
     */
    private void setBTConnectStatus(String deviceName, String conn) {
        boolean pair = true;
        for (int i = 0; i < mDevicesArrayList.size(); i++) {
            BluetoothDevice tempDev = mDevicesArrayList.get(i).getBluetoothDevice();
            if (tempDev.getAddress().equals(deviceName)) {
                mDevicesArrayList.get(i).setBluetoothPairStatus(pair);
                mDevicesArrayList.get(i).setBluetoothConnWait(conn);
            } else {
                mDevicesArrayList.get(i).setBluetoothConnWait(mmContext.getString(R.string.bluetooth_no_connected));
            }
        }
    }

    /**
     * 将搜索到的蓝牙对象放到LIST中返回给调用者
     */
    private final BroadcastReceiver mmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MLog.isDebug) {
                MLog.d(TAG, "action=" + action);
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //BluetoothClass deviceClass = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
                String deviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                if (device != null) {
                    boolean isPair;
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        isPair = false;
                    } else {
                        isPair = true;
                    }
                    if (matchFilterRule(device)) {
                        setListDto(device, rssi, isPair, true);
                    } else {
                        //保存ACTION_FOUND设备信号量
                        setNoMatchedListDto(device, rssi, isPair, true);
                    }
                    if (MLog.isDebug) {
                        MLog.d(TAG, String.format(" Devices name=%s address=%s,deviceName=%s, rssi=%d,isPair=%b", device.getName(), device.getAddress(), deviceName, rssi, isPair));
                    }
                }
            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    boolean isPair = false;
                    if (matchFilterRule(device)) {
                        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                            isPair = false;
                        } else {
                            isPair = true;
                        }
                        setListDto(device, Short.MIN_VALUE, isPair, false);
                    }
                    if (MLog.isDebug) {
                        MLog.d(TAG, String.format(" ACTION_NAME_CHANGED Devices name=%s,address=%s,isPair=%b", device.getName(), device.getAddress(), isPair));
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (MLog.isDebug) {
                    MLog.d(TAG, "BluetoothAdapter ACTION_DISCOVERY_FINISHED");
                }
                if (mmListener != null) {
                    mmListener.onBluetoothScanFinish();
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF) == BluetoothAdapter.STATE_ON) {
                    if (MLog.isDebug) {
                        MLog.d(TAG, "BluetoothAdapter ACTION_STATE_CHANGED STATE_ON and EXTRA_PREVIOUS_STATE=" + intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF));
                    }
                    startDiscovery();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                if (MLog.isDebug) {
                    MLog.d(TAG, "BluetoothAdapter ACTION_DISCOVERY_STARTED");
                }
                if (mmListener != null) {
                    mmListener.onBluetoothScanStart();
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (MLog.isDebug) {
                    MLog.d(TAG, "ACTION_BOND_STATE_CHANGED name=" + device.getName() + " address=" + device.getAddress());
                }
            } else if (BluetoothManager.ACTION_BT_DEVICE_CON_CONING.equals(action)
                    || BluetoothManager.ACTION_BT_DEVICE_CON_SUCCESS.equals(action)
                    || BluetoothManager.ACTION_BT_DEVICE_CON_FAIL.equals(action)
                    || BluetoothManager.ACTION_BT_DEVICE_CON_LOST.equals(action)) {
                Bundle bundle = intent.getBundleExtra("customBluetoothBroadcastIntentExtraBundle");
                //蓝牙连接失败，不更新状态
                if (!(BluetoothManager.ACTION_BT_DEVICE_CON_FAIL.equals(action))) {
                    setBTConnectStatus((BluetoothDevice) bundle.getParcelable("bluetoothDevice"), bundle.getString("status"), true);
                }
                if (mmListener != null) {
                    mmListener.onBluetooth(mBluetoothAdapter, bundle.getInt("type"), mDevicesArrayList, null);
                }
                if (BluetoothManager.ACTION_BT_DEVICE_CON_SUCCESS.equals(action)) {
                    if (mmListener != null) {
                        mmListener.onBluetoothConnSuccess("");
                    }
                } else if (BluetoothManager.ACTION_BT_DEVICE_CON_FAIL.equals(action)) {
                } else if (BluetoothManager.ACTION_BT_DEVICE_CON_LOST.equals(action)) {
                } else if (BluetoothManager.ACTION_BT_DEVICE_CON_CONING.equals(action)) {
                }
            } else if (IPhysics.ACTION_DPU_DEVICE_CONNECT_FAIL.equals(action)) {
                setBTConnectStatus(intent.getStringExtra("deviceName"), mmContext.getString(R.string.bluetooth_connect_fail));
                if (mmListener != null) {
                    mmListener.onBluetooth(mBluetoothAdapter, BluetoothManager.BT_DEVICE_MORE_CON_FAIL, null, null);
                }
            }
        }
    };

    private boolean matchFilterRule(BluetoothDevice device) {
        String deviceName = device.getName();
        return deviceName != null && deviceName.startsWith("ES");
				/*(deviceName.startsWith(PRODUCT_TYPE_PORT) || deviceName.startsWith(PRODUCT_TYPE_PORT_2)
						|| deviceName.startsWith("98999") || Tools.isTruck(mmContext, deviceName) 
						|| Tools.isMatchNewCarPrefix(mmContext, deviceName) || Tools.isCarAndHeavyduty(mmContext, deviceName)*/
    }

    private BluetoothLeScannerManager.BluetoothLeScannerManagerCallBack mBluetoothLeScannerManagerCallBack
            = new BluetoothLeScannerManager.BluetoothLeScannerManagerCallBack() {

        @Override
        public void onStartScan() {
            MLog.i(TAG, "BLE_SCAN_STARTED");
            if (mmListener != null)
                mmListener.onBluetoothScanStart();

        }

        @Override
        public void onStopScan() {
            if (MLog.isDebug) {
                MLog.d(TAG, "BLE_SCAN_FINISHED");
            }
            if (mmListener != null)
                mmListener.onBluetoothScanFinish();
        }

        @Override
        public void onScanResult(BluetoothDevice bluetoothDevice, int rssi) {
            if (MLog.isDebug) {
                MLog.d(TAG, "onScanResult =bluetoothDevice " + bluetoothDevice.getName());
            }
            if (bluetoothDevice != null && matchFilterRule(bluetoothDevice)) {
                setListDto(bluetoothDevice, rssi, false, true);
                if (MLog.isDebug) {
                    MLog.d(TAG, "no Bonded Devices name=" + bluetoothDevice.getName() + " address=" + bluetoothDevice.getAddress());
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            if (MLog.isDebug) {
                MLog.d(TAG, "BLE_SCAN_FINISHED WITH ERROR=" + errorCode);
            }
            if (mmListener != null)
                mmListener.onBluetoothScanFinish();
        }
    };
}
