package com.cnlaunch.physics;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.bluetooth.BluetoothManager;
import com.cnlaunch.physics.ethernet.DPUEthernetManager;
import com.cnlaunch.physics.impl.IAssitsPhysics;
import com.cnlaunch.physics.impl.IAssitsPhysicsMatcher;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.remote.IRemoteDeviceFactoryManager;
import com.cnlaunch.physics.remote.IRemoteDeviceFactoryManagerCallBack;
import com.cnlaunch.physics.serialport.SerialPortManager;
import com.cnlaunch.physics.simulator.ISimulatorDataProcessor;
import com.cnlaunch.physics.simulator.SimulatorManager;
import com.cnlaunch.physics.smartlink.DPUSocketManager;
import com.cnlaunch.physics.usb.Connector;
import com.cnlaunch.physics.usb.DPUUSBDevice;
import com.cnlaunch.physics.usb.DPUUSBManager;
import com.cnlaunch.physics.utils.Constants;
import com.cnlaunch.physics.utils.LocalServerSocketThread;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.Tools;
import com.cnlaunch.physics.utils.remote.DiagnoseDataPackage;
import com.cnlaunch.physics.wifi.DPUWiFiManager;
import com.cnlaunch.physics.wifi.IDPUWiFiModeSettings;
import com.power.baseproject.utils.EasyPreferences;

import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 设备管理类
 *
 * @author xiefeihong
 */

public class DeviceFactoryManager {
    private static final String TAG = "DeviceFactoryManager";
    /* 接头连接模式 */
    public static final int LINK_MODE_UNKNOWN = -1;// 连接模式->未知
    public static final int LINK_MODE_BLUETOOTH = 0;//连接模式->Bluetooth
    public static final int LINK_MODE_WIFI = 1;// 连接模式->WIFI
    public static final int LINK_MODE_COM = 2;// 连接模式->SerialPort
    public static final int LINK_MODE_USB = 3;// 连接模式->USB
    public static final int LINK_MODE_USB_WITH_STAND_ALONE_CHIP = 4;// 连接模式->USB 独立芯片
    public static final int LINK_MODE_USB_WITH_STAND_ALONE_CHIP_ETHERNET = 5;// 连接模式->USB 独立芯片 网卡
    public static final int LINK_MODE_SIMULATOR = 6;// 模拟诊断
    public static final int LINK_MODE_SMART_LINK_SOCKET = 7;//smartlink的网络连接

    public static final String DEVICE_INFORMATION_KEY = "device_information_key";
    public static final String DEVICE_INFORMATION_VALUE = "device_information_value";
    public static final String DEVICE_INFORMATION_LINKMODE = "device_information_linkmode";
    public static final String DEVICE_INFORMATION_STATUS = "device_information_status";
    public static final String DEVICE_INFORMATION_DISCONNECT = "device_information_disconnect";
    public static final String DEVICE_INFORMATION_RESET = "device_information_reset";
    public static final String DEVICE_INFORMATION_CHANGE_DEVICE = "device_information_change_device";
    public static final String DEVICE_INFORMATION_RECONNECT = "device_information_reconnect";
    public static final String DEVICE_NOT_QUIT_CAR_KEY = "device_not_quit_car_key";

    public static final int GET_DEVICE_INFORMATION_DELAYTIME = 1000;
    public static final int GET_DEVICE_INFORMATION_TRYTIMES = 60;

    public static final int STD_NOTIFY_DEVICE_INFORMATION_UNKNOWN = -1;// STD notify unknown id
    public static final int STD_NOTIFY_DEVICE_INFORMATION_RESET = 0;// STD notify reset connector id
    public static final int STD_NOTIFY_DEVICE_INFORMATION_CHANGE_DEVICE = 1;// STD notify reset connector id
    public static final int STD_NOTIFY_DEVICE_INFORMATION_RECONNECT = 2;// STD notify reconnect connector id

    public static final String DOWNLOAD_BIN_FIX_KEY = "download_bin_fix";
    /**
     * 当通讯用于固件修复模式时，子项扩展为其他用于
     * FIRMWARE_FIX_SUB_MODE_FOR_NO_FIX 非固件修复模式
     * FIRMWARE_FIX_SUB_MODE_FOR_FIX  表示固件修复
     * FIRMWARE_FIX_SUB_MODE_FOR_WIFI_MODE_SETTINGS  表示通过蓝牙设定wifi模式
     */
    public static final int FIRMWARE_FIX_SUB_MODE_FOR_NO_FIX = 0;
    public static final int FIRMWARE_FIX_SUB_MODE_FOR_FIX = 1;  //固件修复
    public static final int FIRMWARE_FIX_SUB_MODE_FOR_WIFI_MODE_SETTINGS = 2;     //wifi设定
    public static final int FIRMWARE_FIX_SUB_MODE_FOR_DIAGNOSE = 3; //诊断过程中的固件升级
    public static final int FIRMWARE_FIX_SUB_MODE_FOR_USB_MODE_SETTINGS = 4;     //SMARTBOX30 usb通讯模式设定
    public static final int FIRMWARE_FIX_SUB_MODE_FOR_OTA_UPGRADE = 5;   //SMARTBOX30 ota升级
    public static final int FIRMWARE_FIX_SUB_MODE_FOR_VEHICLE_VOLTAGE = 6;   //预留为车辆电压检测

    private static DeviceFactoryManager deviceFactoryManager = null;
    private LocalServerSocketThread mLocalServerSocketThread;
    private IPhysics mCurrentDevice;
    private boolean mIsResetStatus;
    private int mCurrenLinkMode;
    private boolean mNeedChangeLinkmode;//变更连接方式
    private boolean mIsReconnect;//设备是否在重连状态，也包括诊断菜单进入后需要的第一次连接
    private Dialog mSelectModeDialog;
    private boolean mIsNoQuitStatus = false;
    private int mFirmwareFixSubMode;
    //记录是否存在设备连接管理服务
    private boolean mIsRemoteMode;
    private IDPUWiFiModeSettings mDPUWiFiModeSettings;
    private ISimulatorDataProcessor mSimulatorDataProcessor;
    private boolean mIsNeedExcludeVoltageValidCheck;
    /**
     * 用于与DPULinkManagerServices验证的内部发布版本号，默认每次递增1
     */
    private static final int DPULMS_INTERNAL_RELEASE_VERSION_CODE = 2;
    private static final int MESSAGE_DISPLAY_DPULMS_CONFLICT = 0X5010;
    private Context mDPULMSContext;
    private IRemoteDataCallBack mRemoteDataCallBack;

    private boolean mIsRemoteClientDiagnoseMode; //支持ait一类设备远程诊断
    private boolean mIsSupportOneRequestMoreAnswerDiagnoseMode; //支持ait一类设备远程诊断
    private boolean mIsBLEMode; //蓝牙使用ble模式
    private String mBLEUARTServicesUUID; //BLE串口通讯服务UUID
    private boolean mIsFix;
    private PAD3DHCPForDoIP mPAD3DHCPForDoIP;
    private List<IAssitsPhysics> mAssitsPhysicsList;
    private boolean mIsTPMSDiagnoseRequestStatus;
    private LinkParameters.SerialPortParameters mCompositeTPMSSerialPortParameters;
    //判断是否需要检测LocalServerSocket是否运行正常
    private boolean mIsNeedCheckLocalServerSocket;
    private BleServicesDiscoveredCallBack mBleServicesDiscoveredCallBack;

    public static DeviceFactoryManager getInstance() {
        if (deviceFactoryManager == null) {
            deviceFactoryManager = new DeviceFactoryManager();
        }
        return deviceFactoryManager;
    }

    private DeviceFactoryManager() {
        mPAD3DHCPForDoIP = null;
        mDPULMSContext = null;
        mFirmwareFixSubMode = FIRMWARE_FIX_SUB_MODE_FOR_NO_FIX;
        mCurrentDevice = null;
        mIsResetStatus = false;
        mNeedChangeLinkmode = false;
        mIsReconnect = false;
        mSelectModeDialog = null;
        mCurrenLinkMode = DeviceFactoryManager.LINK_MODE_UNKNOWN;
        mIsRemoteMode = false;
        mLocalServerSocketThread = new LocalServerSocketThread(this);
        mLocalServerSocketThread.start();
        mDPUWiFiModeSettings = null;
        mSimulatorDataProcessor = null;
        mRemoteDataCallBack = null;
        mIsRemoteClientDiagnoseMode = false;
        mIsSupportOneRequestMoreAnswerDiagnoseMode = false;
        mIsBLEMode = false;
        mBLEUARTServicesUUID = "";
        mIsFix = false;
        mAssitsPhysicsList = Collections.synchronizedList(new LinkedList<IAssitsPhysics>());
        mIsTPMSDiagnoseRequestStatus = false;
        mCompositeTPMSSerialPortParameters = null;
        mIsNeedExcludeVoltageValidCheck = false;
        mIsNeedCheckLocalServerSocket = true;
        mBleServicesDiscoveredCallBack = null;
    }

    @Override
    protected void finalize() {
        try {
            MLog.e(TAG, "finalize DeviceFactoryManager");
            // 必须放在stopConnectThread后面
            if (mLocalServerSocketThread != null) {
                mLocalServerSocketThread.stopThread();
                mLocalServerSocketThread = null;
            }
            mPAD3DHCPForDoIP.unregisterBoardcasetReciver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接线程
     */
    public void stopConnectThread() {
        if (MLog.isDebug) {
            MLog.d(TAG, "init stop ConnectThread ,link mode =" + mCurrenLinkMode);
        }
        if (mLocalServerSocketThread != null) {
            mLocalServerSocketThread.stopActiveSocketThread();
        }
        //蓝牙连接因为比较困难，此处需特殊处理,不关闭物理连接
        //wifi需特殊处理,不关闭物理连接
        //串口因为数据流读写不能及时检查到关闭转态，当读写数据时会保持阻塞，所以一个进程只保证串口打开一次，一般
        //使用串口的设备应该是一体化特殊设备，所以此处是合理的。

        //老重卡重新进入有特殊逻辑，需要关闭物理连接
        // 去掉|| mIsBLEMode，ble模式下无需关闭物理连接
        if (((mCurrenLinkMode != DeviceFactoryManager.LINK_MODE_BLUETOOTH) &&
                (mCurrenLinkMode != DeviceFactoryManager.LINK_MODE_WIFI) &&
                (mCurrenLinkMode != DeviceFactoryManager.LINK_MODE_COM))
                || (Tools.isTruck() && !Tools.isCarAndHeavyduty()) || mIsBLEMode) {
            closeCurrentDevice();
        }
        assitsPhysicsListClear();
        setTPMSDiagnoseRequestStatus(false);
        //padv usb wifi特殊逻辑，诊断完成时，需要停止状态监控,需要卸载usb wifi驱动
        //该解决方法会导致更频繁的死机问题，暂时不处理该问题
		/*if(mCurrentDevice!=null) {
			final Context currentDeviceContext = mCurrentDevice.getContext();
			if (currentDeviceContext != null && Tools.isSupportDualWiFi(currentDeviceContext)) {
				if(MLog.isDebug) {
					MLog.d(TAG, "padv usb wifi need uninstall device driver after diagnose");
				}
				CustomWiFiControlForDualWiFi.getInstance(currentDeviceContext).stopMonitor();
				if(CustomWiFiControlForDualWiFi.getInstance(currentDeviceContext).isEnabled()) {
					new Thread() {
						@Override
						public void run() {
							CustomWiFiControlForDualWiFi.getInstance(currentDeviceContext).setWifiEnabled(false);
						}
					}.start();
				}
				closeCurrentDevice();
			}
		}*/
    }

    public int getLinkMode() {
        return mCurrenLinkMode;
    }

    /**
     * 查询连接模式，串口优先
     *
     * @param context
     * @return
     */
    public int queryLinkModeWithSerialportPriority(Context context) {
        return queryLinkMode(true, context, null, true);
    }

    /**
     * 查询连接模式，串口优先
     *
     * @param context
     * @return
     */
    public int queryLinkModeWithSerialportPriority(boolean isFix, Context context, String currentSerialNo) {
        return queryLinkMode(isFix, context, currentSerialNo, true);
    }

    /**
     * 查询当前连接方式 usb优先
     *
     * @return
     */
    public int queryLinkMode(Context context) {
        return queryLinkMode(true, context, null, false);
    }

    public int queryLinkMode(boolean isFix, Context context, String currentSerialNo) {
        return queryLinkMode(isFix, context, currentSerialNo, false);
    }

    private int queryLinkMode(boolean isFix, Context context, String currentSerialNo, boolean isSerialportPriority) {
        //设备连接前，如果连接方式未知，优先检查连接是否为usb模式
        int linkMode = LINK_MODE_BLUETOOTH;
        boolean serialPortSwitch = false;

        boolean smartLinkSocket = EasyPreferences.Companion.getInstance().get(Constants.SMART_LINK_SOCKET, false);
        if (smartLinkSocket) {
            return LINK_MODE_SMART_LINK_SOCKET;
        }

        //usb设定只使用蓝牙通讯
        if (isFix && getFirmwareFixSubMode() == DeviceFactoryManager.FIRMWARE_FIX_SUB_MODE_FOR_USB_MODE_SETTINGS) {
            linkMode = LINK_MODE_BLUETOOTH;
            return linkMode;
        }
        if (mSimulatorDataProcessor != null) {
            linkMode = LINK_MODE_SIMULATOR;
            return linkMode;
        }
        if (isSerialportPriority == false) {
            if (queryUsbDeviceExist(context)) {
                linkMode = LINK_MODE_USB;
            } else {
                serialPortSwitch = EasyPreferences.Companion.getInstance().get(Constants.LINK_MODE_SERIALPORT_SWITCH, false);
                if (serialPortSwitch) {
                    linkMode = LINK_MODE_COM;
                } else {
                    linkMode = getOtherLinkmodeExcludeUSBAndCOM(isFix, context, currentSerialNo);
                }
            }
        } else {
            serialPortSwitch = EasyPreferences.Companion.getInstance().get(Constants.LINK_MODE_SERIALPORT_SWITCH, false);
            if (serialPortSwitch) {
                linkMode = LINK_MODE_COM;
            } else {
                if (queryUsbDeviceExist(context)) {
                    linkMode = LINK_MODE_USB;
                } else {
                    linkMode = getOtherLinkmodeExcludeUSBAndCOM(isFix, context, currentSerialNo);
                }
            }
        }
        return linkMode;
    }

    private int getOtherLinkmodeExcludeUSBAndCOM(boolean isFix, Context context, String currentSerialNo) {
        String serialNo = "";
        if (currentSerialNo == null) {
            serialNo = getSerialNo(context);
        } else {
            serialNo = currentSerialNo;
        }
        boolean wifiSwitch = (DPULinkSettingsInformation.getInstance().getWiFiSwitch(serialNo) ||
                EasyPreferences.Companion.getInstance().get(Constants.LINK_MODE_WIFI_SWITCH_FOR_SIMULATE, false));
        if (wifiSwitch && !(isFix && getFirmwareFixSubMode() == DeviceFactoryManager.FIRMWARE_FIX_SUB_MODE_FOR_WIFI_MODE_SETTINGS)) {
            return LINK_MODE_WIFI;
        } else {
            return LINK_MODE_BLUETOOTH;
        }
    }

    /**
     * 该方法丢弃，请不要再使用
     *
     * @param linkmode
     */
    @Deprecated
    public void setLinkMode(int linkmode) {
        mCurrenLinkMode = linkmode;
    }

    /**
     * 重置连接对象管理属性
     */
    private void resetManager() {
        mIsResetStatus = false;
        mNeedChangeLinkmode = false;
        //不宜放在此处 xfh2019/06/20
        //mIsReconnect = false;
        mSelectModeDialog = null;
        mCurrenLinkMode = DeviceFactoryManager.LINK_MODE_UNKNOWN;
        //mIsRemoteClientDiagnoseMode = false; //连接断开不变更该值。
        //mIsSupportOneRequestMoreAnswerDiagnoseMode = false; //连接断开不变更该值。
        mFirmwareFixSubMode = FIRMWARE_FIX_SUB_MODE_FOR_NO_FIX;
        mIsFix = false;
        mCompositeTPMSSerialPortParameters = null;
    }

    public void closeCurrentDevice() {
        if (mCurrentDevice != null) {
            mCurrentDevice.closeDevice();
            mCurrentDevice = null;
        }
        resetManager();
    }

    /**
     * 物理关闭当前设备连接链路，相对于 closeCurrentDevice()方法，
     * closeCurrentDevice()只在于逻辑上关闭设备，比如针对蓝牙，closeCurrentDevice()
     * 方法不一定会物理上关闭当前连接链路
     */
    public void physicalCloseCurrentDevice() {
        if (mCurrentDevice != null) {
            mCurrentDevice.physicalCloseDevice();
            mCurrentDevice = null;
        }
        resetManager();
    }

    public IPhysics CreateDeviceManagerWithSerialportPriority(Context context, boolean isFix) {
        return CreateDeviceManager(context, isFix, null, true);
    }

    /**
     * 建立连接对象
     *
     * @param context
     * @param isFix
     * @param currentSerialNo
     * @return
     */
    public IPhysics CreateDeviceManagerWithSerialportPriority(Context context, boolean isFix, String currentSerialNo) {
        return CreateDeviceManager(context, isFix, currentSerialNo, true);
    }

    /**
     * 建立连接对象
     *
     * @param context
     * @param isFix
     * @return
     */
    public IPhysics CreateDeviceManager(Context context, boolean isFix) {
        return CreateDeviceManager(context, isFix, null, false);
    }

    /**
     * 建立连接对象
     *
     * @param context
     * @param isFix
     * @param currentSerialNo
     * @return
     */
    public IPhysics CreateDeviceManager(Context context, boolean isFix, String currentSerialNo) {
        return CreateDeviceManager(context, isFix, currentSerialNo, false);
    }

    /**
     * 建立连接对象
     *
     * @param context
     * @param isFix
     * @param isSerialportPriority
     * @return
     */
    private IPhysics CreateDeviceManager(Context context, boolean isFix, String currentSerialNo, boolean isSerialportPriority) {

        //先判断LocalServerSocket是否建立
        if (mIsNeedCheckLocalServerSocket) {
            if (mLocalServerSocketThread != null && mLocalServerSocketThread.getLocalServerSocket() == null) {
                displayABugMessageAndExit(context);
                return null;
            }
        }
        if (mCurrentDevice != null) {
            closeCurrentDevice();
        }
        //建立主连接时，不一定需要删除辅助连接，因为辅助连接可能先于主连接建立
        //assitsPhysicsListClear();
        mIsFix = isFix;
        String serialNo = "";
        if (currentSerialNo == null) {
            serialNo = getSerialNo(context);
        } else {
            serialNo = currentSerialNo;
        }

        boolean smartLinkSocket = EasyPreferences.Companion.getInstance().get(Constants.SMART_LINK_SOCKET, false);
        if (smartLinkSocket) {
            mCurrenLinkMode = LINK_MODE_SMART_LINK_SOCKET;
            //989459000353 产品化后需要修改的参数值
            DPUSocketManager dpuSocketManager = new DPUSocketManager(this, context, "989459000353");
            dpuSocketManager.createConnect();
            mCurrentDevice = dpuSocketManager;
            return mCurrentDevice;
        }

        //设备连接前，如果连接方式未知
        if (isFix && getFirmwareFixSubMode() == DeviceFactoryManager.FIRMWARE_FIX_SUB_MODE_FOR_USB_MODE_SETTINGS) {
            mCurrenLinkMode = LINK_MODE_BLUETOOTH;
        } else if (mSimulatorDataProcessor != null) {
            mCurrenLinkMode = LINK_MODE_SIMULATOR;
        } else if (mCurrenLinkMode == DeviceFactoryManager.LINK_MODE_UNKNOWN || !mNeedChangeLinkmode) {
            if (mCompositeTPMSSerialPortParameters != null) {
                mCurrenLinkMode = LINK_MODE_COM;
                mCurrentDevice = CreateSerialPortManager(context, isFix, serialNo);
                ((SerialPortManager) mCurrentDevice).connect();
                return mCurrentDevice;
            }
            if (isSerialportPriority) {
                boolean serialPortSwitch = EasyPreferences.Companion.getInstance().get(Constants.LINK_MODE_SERIALPORT_SWITCH, false);
                if (serialPortSwitch) {
                    mCurrenLinkMode = LINK_MODE_COM;
                    mCurrentDevice = CreateSerialPortManager(context, isFix, serialNo);
                    int state = ((SerialPortManager) mCurrentDevice).connect();
                    if (state == IPhysics.STATE_CONNECTED) {
                        return mCurrentDevice;
                    }
                }
            }
            if (queryUsbDeviceExist(context)) {
                if (isStandAloneChipEthernet(context)) {
                    DPUEthernetManager DPUEthernetManager = CreateDPUEthernetManager(context, isFix, serialNo);
                    DPUEthernetManager.connectNetwork();
                    mCurrenLinkMode = LINK_MODE_USB;
                    mCurrentDevice = DPUEthernetManager;
                    return mCurrentDevice;
                } else {
                    DPUUSBManager dpuUSBManager = CreateDPUUSBManager(context, isFix, serialNo);
                    mCurrentDevice = dpuUSBManager;
                    int status = dpuUSBManager.open(true);
                    if (status != Connector.STATE_SUCCESS && status != Connector.STATE_NO_PERMISSION) {
                        mCurrenLinkMode = getNoUsbLinkMode(context, isFix, serialNo, isSerialportPriority);
                        dpuUSBManager.closeDevice();
                        dpuUSBManager = null;
                        mCurrentDevice = null;
                    } else {
                        mCurrenLinkMode = LINK_MODE_USB;
                        mCurrentDevice = dpuUSBManager;
                        return mCurrentDevice;
                    }
                }
            } else {
                mCurrenLinkMode = getNoUsbLinkMode(context, isFix, serialNo, isSerialportPriority);
            }
        }


        switch (mCurrenLinkMode) {
            case LINK_MODE_BLUETOOTH:
                mCurrentDevice = CreateBluetoothManager(context, isFix, serialNo);
                break;
            case LINK_MODE_USB:
                mCurrentDevice = CreateDPUUSBManager(context, isFix, serialNo);
                ((DPUUSBManager) mCurrentDevice).open();
                break;
            case LINK_MODE_WIFI:
                int work_mode = Constants.WIFI_WORK_MODE_UNKNOWN;
                if (DPULinkSettingsInformation.getInstance().getWiFiSwitch(serialNo)) {
                    work_mode = DPULinkSettingsInformation.getInstance().getWiFiMode(serialNo);
                } else if (EasyPreferences.Companion.getInstance().get(Constants.LINK_MODE_WIFI_SWITCH_FOR_SIMULATE, false)) {
                    work_mode = Constants.WIFI_WORK_MODE_WITH_STA_MODE_HAVE_INTERACTION;
                }
                mCurrentDevice = CreateDPUWiFiManager(context, isFix, serialNo);
                ((DPUWiFiManager) mCurrentDevice).connect(work_mode);
                break;
            case LINK_MODE_COM:
                mCurrentDevice = CreateSerialPortManager(context, isFix, serialNo);
                int state = ((SerialPortManager) mCurrentDevice).connect();
                if (state == IPhysics.STATE_CONNECTED) {
                    return mCurrentDevice;
                } else {
                    mCurrenLinkMode = LINK_MODE_BLUETOOTH;
                    mCurrentDevice = CreateBluetoothManager(context, isFix, serialNo);
                }
                break;
            case LINK_MODE_SIMULATOR:
                mCurrentDevice = CreateSimulatorManager(context, isFix, serialNo);
                int simulatorConnectState = ((SimulatorManager) mCurrentDevice).connect();
                if (simulatorConnectState == IPhysics.STATE_CONNECTED) {
                    return mCurrentDevice;
                } else {
                    mCurrenLinkMode = LINK_MODE_BLUETOOTH;
                    mCurrentDevice = CreateBluetoothManager(context, isFix, serialNo);
                }
                break;
            default:
                mCurrentDevice = CreateBluetoothManager(context, isFix, serialNo);
                break;
        }
        return mCurrentDevice;
    }

    private int getNoUsbLinkMode(Context context, boolean isFix, String serialNo, boolean isSerialportPriority) {
        boolean wifiSwitch = (DPULinkSettingsInformation.getInstance().getWiFiSwitch(serialNo) ||
                EasyPreferences.Companion.getInstance().get(Constants.LINK_MODE_WIFI_SWITCH_FOR_SIMULATE, false));
        MLog.d(TAG, "wifiSwitch = " + wifiSwitch + " isFix =" + isFix + " getFirmwareFixSubMode()=" + getFirmwareFixSubMode());
        if (wifiSwitch && !(isFix && getFirmwareFixSubMode() == DeviceFactoryManager.FIRMWARE_FIX_SUB_MODE_FOR_WIFI_MODE_SETTINGS)) {
            return LINK_MODE_WIFI;
        } else {
            // 是否使用串口
            boolean serialPortSwitch = EasyPreferences.Companion.getInstance().get(Constants.LINK_MODE_SERIALPORT_SWITCH, false);
            if (serialPortSwitch) {
                return LINK_MODE_COM;
            } else {
                return LINK_MODE_BLUETOOTH;
            }
        }
    }

    public IPhysics getCurrentDevice() {
        return mCurrentDevice;
    }

    private BluetoothManager CreateBluetoothManager(Context context, boolean isFix, String serialNo) {
        if (isRemoteMode()) {
            RomoteLocalSwitch.getInstance().setRemoteMode(true);
        } else {
            RomoteLocalSwitch.getInstance().setRemoteMode(false);
        }
        BluetoothManager bluetoothManager = new BluetoothManager(context, isFix, serialNo, getIsBLEMode());
        bluetoothManager.setIsRemoteClientDiagnoseMode(mIsRemoteClientDiagnoseMode);
        bluetoothManager.setIsSupportOneRequestMoreAnswerDiagnoseMode(mIsSupportOneRequestMoreAnswerDiagnoseMode);
        return bluetoothManager;
    }

    private DPUUSBManager CreateDPUUSBManager(Context context, boolean isFix, String serialNo) {
        RomoteLocalSwitch.getInstance().setRemoteMode(false);
        DPUUSBManager dpuUSBManager = new DPUUSBManager(this, context, isFix, serialNo);
        dpuUSBManager.setIsRemoteClientDiagnoseMode(mIsRemoteClientDiagnoseMode);
        dpuUSBManager.setIsSupportOneRequestMoreAnswerDiagnoseMode(mIsSupportOneRequestMoreAnswerDiagnoseMode);
        return dpuUSBManager;
    }

    private DPUEthernetManager CreateDPUEthernetManager(Context context, boolean isFix, String serialNo) {
        RomoteLocalSwitch.getInstance().setRemoteMode(false);
        DPUEthernetManager dpuUSBEthernetManager = new DPUEthernetManager(this, context, isFix, serialNo);
        dpuUSBEthernetManager.setIsRemoteClientDiagnoseMode(mIsRemoteClientDiagnoseMode);
        dpuUSBEthernetManager.setIsSupportOneRequestMoreAnswerDiagnoseMode(mIsSupportOneRequestMoreAnswerDiagnoseMode);
        return dpuUSBEthernetManager;
    }

    private DPUWiFiManager CreateDPUWiFiManager(Context context, boolean isFix, String serialNo) {
        RomoteLocalSwitch.getInstance().setRemoteMode(false);
        DPUWiFiManager dpuWiFiManager = new DPUWiFiManager(this, context, isFix, serialNo);
        dpuWiFiManager.setIsRemoteClientDiagnoseMode(mIsRemoteClientDiagnoseMode);
        dpuWiFiManager.setIsSupportOneRequestMoreAnswerDiagnoseMode(mIsSupportOneRequestMoreAnswerDiagnoseMode);
        return dpuWiFiManager;
    }

    private SerialPortManager CreateSerialPortManager(Context context, boolean isFix, String serialNo) {
        RomoteLocalSwitch.getInstance().setRemoteMode(false);
        SerialPortManager serialPortManager = new SerialPortManager(this, context, serialNo, null);
        if (mCompositeTPMSSerialPortParameters != null) {
            LinkParameters linkParameters = new LinkParameters();
            linkParameters.setSerialPortParameters(mCompositeTPMSSerialPortParameters);
            serialPortManager.setLinkParameters(linkParameters);
        }
        serialPortManager.setIsRemoteClientDiagnoseMode(mIsRemoteClientDiagnoseMode);
        serialPortManager.setIsSupportOneRequestMoreAnswerDiagnoseMode(mIsSupportOneRequestMoreAnswerDiagnoseMode);
        return serialPortManager;
    }

    private SimulatorManager CreateSimulatorManager(Context context, boolean isFix, String serialNo) {
        RomoteLocalSwitch.getInstance().setRemoteMode(false);
        SimulatorManager simulatorManager = new SimulatorManager(this, context, isFix, serialNo);
        simulatorManager.setIsRemoteClientDiagnoseMode(mIsRemoteClientDiagnoseMode);
        simulatorManager.setIsSupportOneRequestMoreAnswerDiagnoseMode(mIsSupportOneRequestMoreAnswerDiagnoseMode);
        return simulatorManager;
    }

    public String getDeviceName() {
        if (mCurrentDevice != null) {
            String deviceName = mCurrentDevice.getDeviceName();
            if (TextUtils.isEmpty(deviceName) == false) {
                return deviceName;
            } else {
                Context context = mCurrentDevice.getContext();
                if (context != null) {
                    return EasyPreferences.Companion.getInstance().get("serialNo", "");
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    public Context getContext() {
        if (mCurrentDevice != null) {
            return mCurrentDevice.getContext();
        } else {
            return null;
        }
    }

    public void write(byte[] buffer) {
        write(buffer, 0, buffer.length);
    }

    public void write(byte[] buffer, int count) {
        write(buffer, 0, count);
    }

    public void write(byte[] buffer, int offset, int count) {
        try {
            if (mAssitsPhysicsList.size() > 0) {
                if (MLog.isDebug) {
                    MLog.d(TAG, String.format("mAssitsPhysicsList.size()>0 offset=%d count=%d", offset, count));
                    MLog.d(TAG, "write mCurrentDevice= " + mCurrentDevice);
                }
                boolean isMatched = false;
                Iterator<IAssitsPhysics> assitsPhysicsListIterator = mAssitsPhysicsList.iterator();
                while (assitsPhysicsListIterator.hasNext()) {
                    IAssitsPhysics assitsPhysics = assitsPhysicsListIterator.next();
                    IAssitsPhysicsMatcher assitsPhysicsMatcher = assitsPhysics.getAssitsPhysicsMatcher();
                    if (assitsPhysicsMatcher != null && assitsPhysicsMatcher.isMatch(buffer, offset, count)) {
                        if (MLog.isDebug) {
                            MLog.d(TAG, "assitsPhysicsMatcher isMatched ");
                        }
                        OutputStream outputStream = assitsPhysics.getPhysics().getOutputStream();
                        if (outputStream != null) {
                            outputStream.write(buffer, 0, count);
                            outputStream.flush();
                        }
                        isMatched = true;
                        break;
                    }
                }
                if (isMatched == false) {
                    if (mCurrentDevice != null) {
                        OutputStream outputStream = mCurrentDevice.getOutputStream();
                        if (outputStream != null) {
                            outputStream.write(buffer, 0, count);
                            outputStream.flush();
                        }
                    }
                }
            } else {
                if (mCurrentDevice != null) {
                    OutputStream outputStream = mCurrentDevice.getOutputStream();
                    if (outputStream != null) {
                        outputStream.write(buffer, 0, count);
                        outputStream.flush();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String command) {
        if (gearDataCallBack != null) {
            gearDataCallBack.onGearDataReceived(command);
        } else {
            if (mIsRemoteClientDiagnoseMode == false) {
                mLocalServerSocketThread.send(command);
            } else {
                if (mRemoteDataCallBack != null) {
                    mRemoteDataCallBack.onRemoteDataReceived(command);
                }
            }
        }
    }

    public boolean getResetStatus() {
        return mIsResetStatus;
    }

    public void setResetStatus(boolean resetStatus) {
        mIsResetStatus = resetStatus;
    }

    /**
     * 该方法已经过时，因为实际中并没有实现该功能
     *
     * @param needChangeLinkmode
     */
    @Deprecated
    public void setNeedChangeLinkMode(boolean needChangeLinkmode) {
        if (mNeedChangeLinkmode != needChangeLinkmode) {
            mNeedChangeLinkmode = needChangeLinkmode;
            if (mNeedChangeLinkmode) {
                if (mCurrenLinkMode == DeviceFactoryManager.LINK_MODE_BLUETOOTH) {
                    mCurrenLinkMode = DeviceFactoryManager.LINK_MODE_USB;
                } else {
                    mCurrenLinkMode = DeviceFactoryManager.LINK_MODE_BLUETOOTH;
                }
            }
        }
    }

    @Deprecated
    public boolean getNeedChangeLinkMode() {
        return mNeedChangeLinkmode;
    }

    public boolean isReconnect() {
        return mIsReconnect;
    }

    public void setIsReconnect(boolean isReconnect) {
        this.mIsReconnect = isReconnect;
    }

    public Dialog getSelectModeDialog() {
        return mSelectModeDialog;
    }

    public void setSelectModeDialog(Dialog selectModeDialog) {
        mSelectModeDialog = selectModeDialog;
    }

    public boolean getNoQuitCarStatus() {
        return mIsNoQuitStatus;
    }

    public void setNoQuitCarStatus(boolean status) {
        mIsNoQuitStatus = status;
    }

    /**
     * 查询是否匹配dpu设备
     *
     * @return
     */
    public boolean queryUsbDeviceExist(Context context) {
        boolean isExist = false;
        // 直接查询本机usb相关设备，不与 USB Manager对象产生关系
        String APP_USB_PERMISSION = context.getPackageName();
        APP_USB_PERMISSION += ".USB_PERMISSION";
        DPUUSBDevice usbDevice = new DPUUSBDevice(context, APP_USB_PERMISSION);
        int state = usbDevice.queryDevice();
        MLog.d(TAG, "queryUsbDeviceExist STATE = " + state);
        if (state == Connector.STATE_SUCCESS) {
            isExist = true;
        } else {
            isExist = false;
        }
        usbDevice.close();
        usbDevice = null;
        return isExist;
    }

    /**
     * 根据系统广播 UsbManager.ACTION_USB_DEVICE_ATTACHED，
     * UsbManager.ACTION_USB_DEVICE_DETACHED intent
     * 查询是否匹配dpu设备
     */
    public boolean queryIsMatchDevice(Context context, Intent intent) {
        boolean isMatch = false;
        // 直接查询本机usb相关设备，不与 USB Manager对象产生关系
        String APP_USB_PERMISSION = context.getPackageName();
        APP_USB_PERMISSION += ".USB_PERMISSION";
        DPUUSBDevice usbDevice = new DPUUSBDevice(context, APP_USB_PERMISSION);
        isMatch = usbDevice.queryIsMatchDevice(intent);
        usbDevice.close();
        usbDevice = null;
        return isMatch;
    }

    /**
     * 判断是否为带独立芯片的usb设备
     *
     * @return
     */
    public boolean isStandAloneChip(Context context) {
        return isStandAloneChip(context, null);
    }

    /**
     * 判断是否为带独立芯片的usb设备
     *
     * @return
     */
    public boolean isStandAloneChip(Context context, Intent intent) {
        boolean isMatch = false;
        String APP_USB_PERMISSION = context.getPackageName();
        APP_USB_PERMISSION += ".USB_PERMISSION";
        DPUUSBDevice usbDevice = new DPUUSBDevice(context, APP_USB_PERMISSION);
        if (intent != null) {
            isMatch = usbDevice.isStandAloneChip(intent);
        } else {
            isMatch = usbDevice.isStandAloneChip();
        }
        usbDevice.close();
        usbDevice = null;
        return isMatch;
    }

    /**
     * 判断是否为带独立芯片的usb以太网卡设备
     *
     * @return
     */
    public boolean isStandAloneChipEthernet(Context context) {
        return isStandAloneChipEthernet(context, null);
    }

    /**
     * 判断是否为带独立芯片的usb以太网卡设备
     *
     * @return
     */
    public boolean isStandAloneChipEthernet(Context context, Intent intent) {
        boolean isMatch = false;
        String APP_USB_PERMISSION = context.getPackageName();
        APP_USB_PERMISSION += ".USB_PERMISSION";
        DPUUSBDevice usbDevice = new DPUUSBDevice(context, APP_USB_PERMISSION);
        if (intent != null) {
            isMatch = usbDevice.isStandAloneChipEthernet(intent);
        } else {
            isMatch = usbDevice.isStandAloneChipEthernet();
        }
        usbDevice.close();
        usbDevice = null;
        return isMatch;
    }

    /**
     * 处理多应用通讯冲突
     *
     * @param context
     */
    public void displayABugMessageAndExit(Context context) {
        String msg = context.getString(R.string.msg_system_error_tips);
        if (EasyPreferences.Companion.getInstance().get(Constants.IS_NEED_REPLACE_LAUNCH, false)) {
            msg = msg.replaceAll("(?i)launch", "");
        }
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setMessage(msg);
            builder.setPositiveButton(android.R.string.ok, new FinishListener());
            builder.setOnCancelListener(new FinishListener());
            builder.show();
        } catch (Exception e) {
            MLog.d(TAG, "localsocket bind error \n" + msg);
        }
    }

    public final class FinishListener implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

        public FinishListener() {
        }

        @Override
        public void onCancel(DialogInterface dialogInterface) {
            run();
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            run();
        }

        private void run() {
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }

    /**
     * 获取当前连接是否用于固件修复模式
     * 随着需求扩展当前连接为固件修复模式不一定只用于固件修复，
     * firmwareFixSubMode 用于设置固件修复模式时，子项模式扩展
     *
     * @return
     */
    public boolean isFix() {
        return mIsFix;
    }

    /**
     * 设置固件修复模式时，子项扩展模式
     *
     * @param firmwareFixSubMode
     */
    public void setFirmwareFixSubMode(int firmwareFixSubMode) {
        mFirmwareFixSubMode = firmwareFixSubMode;
    }

    public int getFirmwareFixSubMode() {
        return mFirmwareFixSubMode;
    }

    /**
     * 启动接头通讯管理服务,此处必须在使用创建连接对象前使用
     *
     * @param context
     */
    public void startDPULinkManagerService(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.newchip.dpulinkmanager",
                    "com.newchip.dpulinkmanager.DPULinkManagerService"));
            context.startService(intent);
        } catch (Exception e) {
            //设置为本地模式
            e.printStackTrace();
            setRemoteMode(false);
        }
    }

    /**
     * 初始化诊断蓝牙服务,此处必须在使用创建连接对象前使用
     * 后续扩展到的逻辑应该为初始化DeviceFactoryManager xfh2019/04/03
     *
     * @param context
     */
    public void initialRemoteDeviceFactoryManager(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.newchip.dpulinkmanager",
                    "com.newchip.dpulinkmanager.DPULinkManagerService"));
            context.startService(intent);
            context.getApplicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            //设置为本地模式
            e.printStackTrace();
            setRemoteMode(false);
        }
        //注册doip编程相关状态信息
        mDPULMSContext = context;
        mPAD3DHCPForDoIP = new PAD3DHCPForDoIP(mDPULMSContext);
        mPAD3DHCPForDoIP.registerBoardcastReciver();
        String serialNo = EasyPreferences.Companion.getInstance().get("serialNo", "");
        customWiFiAutoConectControlForDualWiFi(context, serialNo);
    }

    /**
     * 停止PAD3 DHCP For DoIP 服务 xfh2019/04/03 加入
     */
    public void stopPAD3DHCPForDoIPServices() {
        if (mPAD3DHCPForDoIP != null) {
            mPAD3DHCPForDoIP.stopPAD3DHCPForDoIPServices();
        }
    }

    /**
     * 获取远程设备工厂管理对象
     *
     * @return
     */
    public IRemoteDeviceFactoryManager getRemoteDeviceFactoryManager() {
        return mRemoteDeviceFactoryManager;
    }

    /**
     * 远程设备工厂管理回调对象
     *
     * @return
     */
    public IRemoteDeviceFactoryManagerCallBack getRemoteDeviceFactoryManagerCallBack() {
        return mCallBack;
    }

    private IRemoteDeviceFactoryManager mRemoteDeviceFactoryManager = null;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mRemoteDeviceFactoryManager = IRemoteDeviceFactoryManager.Stub.asInterface(service);
            setRemoteMode(true);
            try {
                if (mRemoteDeviceFactoryManager.getDPULMSInternalReleaseVersionCode() < DPULMS_INTERNAL_RELEASE_VERSION_CODE) {
                    mHandler.sendEmptyMessage(MESSAGE_DISPLAY_DPULMS_CONFLICT);
                }
            } catch (Exception e) {
                e.printStackTrace();
                //调整当服务绑定成功,但远端调用出错时,不要弹出升级提示
                //mHandler.sendEmptyMessage(MESSAGE_DISPLAY_DPULMS_CONFLICT);
            }
            MLog.d(TAG, "onServiceConnected sucess!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mRemoteDeviceFactoryManager = null;
        }
    };
    IRemoteDeviceFactoryManagerCallBack.Stub mCallBack = new IRemoteDeviceFactoryManagerCallBack.Stub() {
        @Override
        public void send(String command) throws RemoteException {
            DeviceFactoryManager.this.send(command);
        }
    };

    private String getSerialNo(Context context) {
        String serialNo = "";
        if (context != null) {
            serialNo = EasyPreferences.Companion.getInstance().get("serialNo", "");
        }
        return serialNo;
    }

    private synchronized void setRemoteMode(boolean isRemoteMode) {
        this.mIsRemoteMode = isRemoteMode;
    }

    public synchronized boolean isRemoteMode() {
        return mIsRemoteMode;
    }

    /**
     * 处理应用退出时的连接对象释放工作
     */
    public void closeDeviceOnApplicationExit(final Context context) {
        DeviceFactoryManager.getInstance().closeCurrentDevice();
        if (context != null) {
            if (MLog.isDebug) {
                MLog.d(TAG, "closeDeviceOnApplicationExit isRemoteMode()=" + isRemoteMode());
            }
            // RomoteLocalSwitch.getInstance().isRemoteMode() ==false
            // 修改为isRemoteMode()==false 更准确，isRemoteMode()只确定当前存在连接管理服务，并且已经绑定服务
            // 且没有判断服务提供的某个设备连接是否可用
            // 此处还是留个每个设备自行判断更合适，所以注销此处代码
            //if(isRemoteMode()==false){
            //	context.sendBroadcast(new Intent(IPhysics.ACTION_DIAG_UNCONNECTED));
            //}
            //DPULinkManagerServices 绑定成功时，退出应用时解除绑定
            try {
                if (isRemoteMode()) {
                    context.getApplicationContext().unbindService(mServiceConnection);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //padv usb wifi特殊逻辑，诊断完成时，需要停止状态监控,需要卸载usb wifi驱动
            //该解决方法会导致更频繁的死机问题，暂时不处理该问题
			/*if(Tools.isSupportDualWiFi(context)) {
				if(MLog.isDebug) {
					MLog.d(TAG, "padv usb wifi need uninstall device driver after diagnose");
				}
				CustomWiFiControlForDualWiFi.getInstance(context).stopMonitor();
				if(CustomWiFiControlForDualWiFi.getInstance(context).isEnabled()) {
					new Thread() {
						@Override
						public void run() {
							CustomWiFiControlForDualWiFi.getInstance(context).setWifiEnabled(false);
						}
					}.start();
				}
			}*/
        }
    }

    /**
     * 处理IPhysics.ACTION_DIAG_UNCONNECTED广播
     */
    public void onDeviceClosedBroadcastReciver(Context context) {
        if (context != null) {
            context.sendBroadcast(new Intent(IPhysics.ACTION_DIAG_CONNECTED));
        }
    }

    /**
     * 设置扩展的wifi设定
     *
     * @param dpuWiFiModeSettings
     */
    public void setDPUWiFiModeSettings(IDPUWiFiModeSettings dpuWiFiModeSettings) {
        mDPUWiFiModeSettings = dpuWiFiModeSettings;
    }

    /**
     * 获取设置扩展的wifi设定
     */
    public IDPUWiFiModeSettings getDPUWiFiModeSettings() {
        return mDPUWiFiModeSettings;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_DISPLAY_DPULMS_CONFLICT) {
                if (mDPULMSContext != null) {
                    try {
                        displayDPULinkManagerServicesConflictMessage(mDPULMSContext);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    /**
     * 处理验证与DPULinkManagerServices的内部发布版本号提示
     *
     * @param context
     */
    private void displayDPULinkManagerServicesConflictMessage(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(android.R.string.dialog_alert_title);
        builder.setMessage(context.getString(R.string.msg_system_dpulms_error_tips));
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnCancelListener(null);
        builder.show();
    }

    /**
     * 查找当前蓝牙对象mac地址,该函数必须做到不影响其他功能执行
     *
     * @param context
     * @param isFix
     * @param serialNo
     * @return
     */
    public String getBluetoothDeviceAddress(Context context, boolean isFix, String serialNo) {
        return BluetoothManager.getBluetoothDeviceAddress(context, isFix, serialNo);
    }

    //ait远程诊断支持
    public void setIsRemoteClientDiagnoseMode(boolean isRemoteClientDiagnoseMode) {
        mIsRemoteClientDiagnoseMode = isRemoteClientDiagnoseMode;
    }

    public boolean getIsRemoteClientDiagnoseMode() {
        return mIsRemoteClientDiagnoseMode;
    }

    public interface IRemoteDataCallBack {
        void onRemoteDataReceived(String command);
    }

    ;

    public void setRemoteDataReceiveCallBack(IRemoteDataCallBack remoteDataCallBack) {
        mRemoteDataCallBack = remoteDataCallBack;
    }

    public void removeRemoteDataReceiveCallBack() {
        mRemoteDataCallBack = null;
    }

    public boolean getIsBLEMode() {
        return mIsBLEMode;
    }

    /**
     * 方法已经不推荐使用，请使用setIsBLEMode(boolean isBLEMode,String bleUARTServicesUUID)方法代替
     */
    @Deprecated
    public void setIsBLEMode(boolean isBLEMode) {
        setIsBLEMode(isBLEMode, "");
    }

    public String getBLEUARTServicesUUID() {
        return this.mBLEUARTServicesUUID;
    }

    public interface BleServicesDiscoveredCallBack {
        String getAimService(BluetoothGatt gatt);
    }

    public void setBleServicesDiscoveredCallBack(BleServicesDiscoveredCallBack bleServicesDiscoveredCallBack) {
        mBleServicesDiscoveredCallBack = bleServicesDiscoveredCallBack;
    }

    public BleServicesDiscoveredCallBack getBleServicesDiscoveredCallBack() {
        return mBleServicesDiscoveredCallBack;
    }

    /**
     * 设置BLE通讯方式及串口通讯服务UUID
     *
     * @param isBLEMode           是否为BLE通讯
     * @param bleUARTServicesUUID BLE串口通讯服务UUID
     */
    public void setIsBLEMode(boolean isBLEMode, String bleUARTServicesUUID) {
        this.mIsBLEMode = isBLEMode;
        this.mBLEUARTServicesUUID = bleUARTServicesUUID;
    }

    private IGearDataCallBack gearDataCallBack;

    public interface IGearDataCallBack {
        void onGearDataReceived(String command);
    }

    public void setGearDataCallBack(IGearDataCallBack gearDataCallBack) {
        this.gearDataCallBack = gearDataCallBack;
    }

    /**
     * 支持双wifi，或双通讯通道的设备特有逻辑
     * 自定义wifi控制
     * 去掉通讯初始化时去掉双wifi操作逻辑
     *
     * @param context
     * @param serialNo
     */
    private void customWiFiAutoConectControlForDualWiFi(Context context, String serialNo) {
		/*if(Tools.isSupportDualWiFi(context) && DPULinkSettingsInformation.getInstance().getWiFiSwitch(serialNo)){
			if(DPULinkSettingsInformation.getInstance().getWiFiMode(serialNo)==Constants.WIFI_WORK_MODE_WITH_AP) {
				CustomWiFiControlForDualWiFi.getInstance(context).autoConnectNetwork(serialNo, Constants.SMARTBOX30_AP_PASSWORD);
			}
			else{
				String ssid = DPULinkSettingsInformation.getInstance().getSSID(serialNo);
				String password = DPULinkSettingsInformation.getInstance().getPassword(serialNo);
				CustomWiFiControlForDualWiFi.getInstance(context).autoConnectNetwork(ssid, password);
			}
		}*/
    }

    //通讯输出流写入包包装器
    private IPhysicsOutputStreamBufferWrapper mIPhysicsOutputStreamBufferWrapper = new IPhysicsOutputStreamBufferWrapper() {
        @Override
        public boolean isNeedWrapper() {
            return false;
        }

        @Override
        public byte[] writeBufferWrapper(byte[] buffer) {
            if (buffer == null) {
                return null;
            } else {
                return writeBufferWrapper(buffer, 0, buffer.length);
            }
        }

        @Override
        public byte[] writeBufferWrapper(byte[] buffer, int offset, int length) {
            if (buffer == null) {
                return null;
            } else {
                DiagnoseDataPackage mDiagnoseDataPackage = new DiagnoseDataPackage();
                if (mDiagnoseDataPackage.framePackageForMCU(buffer, offset, length, buffer[6])) {
                    return mDiagnoseDataPackage.toBytes();
                } else {
                    return buffer;
                }
            }
        }
    };

    public IPhysicsOutputStreamBufferWrapper getIPhysicsOutputStreamBufferWrapper() {
        return mIPhysicsOutputStreamBufferWrapper;
    }

    public List<IAssitsPhysics> getAssitsPhysicsList() {
        return mAssitsPhysicsList;
    }

    /**
     * 关闭连接时，需要关闭辅助连接设备
     */
    public void assitsPhysicsListClear() {
        if (mAssitsPhysicsList.size() > 0) {
            Iterator<IAssitsPhysics> assitsPhysicsListIterator = mAssitsPhysicsList.iterator();
            while (assitsPhysicsListIterator.hasNext()) {
                IAssitsPhysics assitsPhysics = assitsPhysicsListIterator.next();
                assitsPhysics.getPhysics().closeDevice();
            }
            mAssitsPhysicsList.clear();
        }
    }

    /**
     * 判断是否为 胎压模块需要执行诊断状态
     *
     * @return
     */
    public boolean isTPMSDiagnoseRequestStatus() {
        return mIsTPMSDiagnoseRequestStatus;
    }

    /**
     * 设置是否为 胎压模块需要执行诊断状态
     *
     * @return
     */
    public void setTPMSDiagnoseRequestStatus(boolean isTPMSDiagnoseRequestStatus) {
        this.mIsTPMSDiagnoseRequestStatus = isTPMSDiagnoseRequestStatus;
    }

    /**
     * 设置混合TPMS 串口作为主连接支持，一般用于与诊断无关的工作，比如固件升级
     * 参数包括串口连接参数，一般为特殊情况下使用，比如TPMS需要用到胎压模块和诊断接头。正常情况可以通过获取产品或项目类型来设置。
     */
    public void setCompositeTPMSLinkParameters(LinkParameters linkParameters) {
        mCompositeTPMSSerialPortParameters = linkParameters.getSerialPortParameters();
    }

    public ISimulatorDataProcessor getSimulatorDataProcessor() {
        return mSimulatorDataProcessor;
    }

    public void setSimulatorDataProcessor(ISimulatorDataProcessor simulatorDataProcessor) {
        this.mSimulatorDataProcessor = simulatorDataProcessor;
    }

    public boolean isSimulatorDiagnose() {
        if (mSimulatorDataProcessor != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSupportOneRequestMoreAnswerDiagnoseMode() {
        return mIsSupportOneRequestMoreAnswerDiagnoseMode;
    }

    public void setIsSupportOneRequestMoreAnswerDiagnoseMode(boolean isSupportOneRequestMoreAnswerDiagnoseMode) {
        this.mIsSupportOneRequestMoreAnswerDiagnoseMode = isSupportOneRequestMoreAnswerDiagnoseMode;
    }

    public boolean isNeedExcludeVoltageValidCheck() {
        return mIsNeedExcludeVoltageValidCheck;
    }

    public void setIsNeedExcludeVoltageValidCheck(boolean isNeedExcludeVoltageValidCheck) {
        this.mIsNeedExcludeVoltageValidCheck = isNeedExcludeVoltageValidCheck;
    }

    public boolean isNeedCheckLocalServerSocket() {
        return mIsNeedCheckLocalServerSocket;
    }

    public void setIsNeedCheckLocalServerSocket(boolean isNeedCheckLocalServerSocket) {
        this.mIsNeedCheckLocalServerSocket = isNeedCheckLocalServerSocket;
    }
}
