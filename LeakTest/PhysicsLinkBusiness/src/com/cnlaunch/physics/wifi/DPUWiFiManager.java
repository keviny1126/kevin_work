package com.cnlaunch.physics.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.DPULinkSettingsInformation;
import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.io.PhysicsInputStreamWrapper;
import com.cnlaunch.physics.io.PhysicsOutputStreamWrapper;
import com.cnlaunch.physics.listener.OnWiFiModeListener;
import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.Constants;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.NetworkUtil;
import com.cnlaunch.physics.utils.Tools;
import com.cnlaunch.physics.utils.remote.ReadByteDataStream;
import com.cnlaunch.physics.wifi.custom.CustomWiFiControlForDualWiFi;
import com.cnlaunch.physics.wifi.settings.WiFiControlManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

public class DPUWiFiManager implements IPhysics {
    private static final String TAG = "DPUWiFiManager";
    private Context mContext;
    /**
     * wifi 热点约定ip地址与端口号
     */
    private static final String WIFI_CONNECT_ADDRESS = "192.168.16.254";
    private static final int WIFI_CONNECT_PORT = 8080;
    /**
     * wifi通讯模式建立连接超时时间
     */
    private static final int WIFI_CONNECT_TIMEOUT = 4000; //4S
    /**
     * 接头wifi以sta模式工作的时候，通过udp广播方式获取接头ip地址参数
     * DPU_CONNECTOR_UDP_SEND_AND_RECEIVER_TIMEOUT 				广播发送与接收超时时间
     * DPU_CONNECTOR_UDP_PORT 									广播发送与接收端口参数
     * DPU_CONNECTOR_UDP_SEND_AND_RECEIVER_TRY_MAXNUM_COUNT 	广播发送与接收失败重复次数
     */
    private static final int DPU_CONNECTOR_UDP_SEND_AND_RECEIVER_TIMEOUT = 5000;  //5S
    private static final int DPU_CONNECTOR_UDP_REMOTE_PORT = 988;
    private static final int DPU_CONNECTOR_UDP_LOCAL_PORT = 0;
    private static final int DPU_CONNECTOR_UDP_SEND_AND_RECEIVER_TRY_MAXNUM_COUNT = 5;

    private Socket wifiSocket;
    private ConnectThread mConnectThread;
    private ReadByteDataStream mReadByteDataStreamThread;
    private String mReadData;
    private int mState;
    private boolean commandWait = true;

    private int mMode; //wifi使用模式
    private boolean isFix;
    private DeviceFactoryManager mDeviceFactoryManager;

    private OnWiFiModeListener mOnWiFiModeListener;
    private String mSerialNo;
    private boolean mIsTruckReset;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean mIsRemoteClientDiagnoseMode;
    private boolean mIsSupportOneRequestMoreAnswerDiagnoseMode;
    private boolean mIsBroadcastActionDpuDeviceConnectDisconnected;

    public DPUWiFiManager(DeviceFactoryManager deviceFactoryManager, Context context, boolean isFix, String serialNo) {
        //引用diagnoseactivity 会使使用activity作为接头的fragment释放不掉
        mContext = context.getApplicationContext();
        this.isFix = isFix;
        mDeviceFactoryManager = deviceFactoryManager;
        mConnectThread = null;
        wifiSocket = null;
        mReadByteDataStreamThread = null;
        mOnWiFiModeListener = null;
        mSerialNo = serialNo;
        mState = STATE_NONE;
        mIsTruckReset = false;
        inputStream = null;
        outputStream = null;
        mIsRemoteClientDiagnoseMode = false;
        mIsSupportOneRequestMoreAnswerDiagnoseMode = false;
    }

    //加入wifi测试支持响应端
    public void setOnWiFiModeListener(OnWiFiModeListener onWiFiModeListener) {
        mOnWiFiModeListener = onWiFiModeListener;
    }

    @Override
    protected void finalize() {
        try {
            MLog.e(TAG, "finalize DPUWiFiManager");
            mHandler = null;
            wifiSocket = null;
            mOnWiFiModeListener = null;
            super.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取上下文
     */
    @Override
    public Context getContext() {
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
        try {
            mContext.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MLog.d(TAG, "stop wifi ConnectThread");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mReadByteDataStreamThread != null) {
            mReadByteDataStreamThread.cancel();
            mContext.sendBroadcast(new Intent(ACTION_DIAG_UNCONNECTED));
            mReadByteDataStreamThread = null;
        }
        if (Tools.isSupportDualWiFi(mContext)) {
            CustomWiFiControlForDualWiFi.getInstance(mContext).stopMonitor();
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
        if (isSupportSelfSend) mReadData = command;
        else setCommand(command);
    }

    @Override
    public InputStream getInputStream() {
        try {
            if (inputStream == null) {
                inputStream = new PhysicsInputStreamWrapper(wifiSocket.getInputStream());
            }
            return inputStream;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream() {
        try {
            if (outputStream == null) {
                outputStream = new PhysicsOutputStreamWrapper(wifiSocket.getOutputStream(), mDeviceFactoryManager.getIPhysicsOutputStreamBufferWrapper());
            }
            return outputStream;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 连接线程
     */
    private class ConnectThread extends Thread {
        private Socket mmSocket;

        public ConnectThread() {
            MLog.e(TAG, "ConnectThread construct");
            init();
        }

        //初始化socket
        private void init() {
            mmSocket = new Socket();
            try {
                mmSocket.setTcpNoDelay(true);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            if (Tools.isSupportDualWiFi(mContext)) {
                //wifi链路连接建立尝试两次，如果
                boolean isForceEnabled = false;
                for (int tryCount = 0; tryCount < 2; tryCount++) {
                    if (tryCount == 0) {
                        isForceEnabled = false;
                    } else {
                        if (MLog.isDebug) {
                            MLog.d(TAG, "Support Dual WiFi Force Enabled true");
                        }
                        isForceEnabled = true;
                    }
                    int conntectState = CustomWiFiControlForDualWiFi.getInstance(mContext).getState(mSerialNo);
                    if (MLog.isDebug) {
                        MLog.d(TAG, String.format("Tools.isSupportDualWiFi  conntectState=%d", conntectState));
                    }
                    if (conntectState == STATE_CONNECTING) {
                        while (conntectState == STATE_CONNECTING) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            conntectState = CustomWiFiControlForDualWiFi.getInstance(mContext).getState(mSerialNo);
                        }
                    } else {
                        if (MLog.isDebug) {
                            MLog.d(TAG, String.format("Tools.isSupportDualWiFi not connected ,start to manualConnectNetwork ssid=%s,password=%s", mSerialNo, Constants.SMARTBOX30_AP_PASSWORD));
                        }
                        if (mMode == Constants.WIFI_WORK_MODE_WITH_AP) {
                            CustomWiFiControlForDualWiFi.getInstance(mContext).manualConnectNetwork(mSerialNo, Constants.SMARTBOX30_AP_PASSWORD, isForceEnabled);
                        } else {
                            String ssid = DPULinkSettingsInformation.getInstance().getSSID(mSerialNo);
                            String password = DPULinkSettingsInformation.getInstance().getPassword(mSerialNo);
                            CustomWiFiControlForDualWiFi.getInstance(mContext).manualConnectNetwork(ssid, password, isForceEnabled);
                        }
                    }
                    conntectState = CustomWiFiControlForDualWiFi.getInstance(mContext).getState(mSerialNo);
                    if (conntectState != STATE_CONNECTED) {
                        if (tryCount == 0) {
                            continue;
                        } else {
                            connectionFailed();
                            return;
                        }
                    } else {
                        break;
                    }
                }
            } else if (Tools.isSupportWiFiPriority(mContext)) {
                WiFiControlManager wiFiControlManager = WiFiControlManager.getInstance(mContext);
                if (wiFiControlManager.isWifiConnectedWithMatchedSSID(mSerialNo) == false) {
                    boolean isEnable = wiFiControlManager.connectWPA2Network(mSerialNo, Constants.SMARTBOX30_AP_PASSWORD);
                    if (MLog.isDebug) {
                        MLog.d(TAG, "isSupportWiFiPriority TRUE connectWPA2Network SSID=" + mSerialNo + " isEnable=" + isEnable);
                    }
                    int tryCheckCount = 6;
                    while (wiFiControlManager.isWifiConnectedWithMatchedSSID(mSerialNo) == false && (tryCheckCount--) > 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (MLog.isDebug) {
                        MLog.d(TAG, "isSupportWiFiPriority TRUE tryCheckCount=" + tryCheckCount + " isWifiConnected()=" + WiFiControlManager.getInstance(mContext).isWifiConnected());
                    }
                    if (wiFiControlManager.isWifiConnectedWithMatchedSSID(mSerialNo) == false) {
                        connectionFailed();
                        return;
                    }
                }
            }
            InetSocketAddress socketAddress = null;
            if (mMode == Constants.WIFI_WORK_MODE_WITH_AP) {
                if (Tools.isMatchSmartbox30SupportSerialnoPrefix(mContext, getSerialNo())) {
                    if (mDeviceFactoryManager != null && isFix && mDeviceFactoryManager.getFirmwareFixSubMode() == DeviceFactoryManager.FIRMWARE_FIX_SUB_MODE_FOR_OTA_UPGRADE) {
                        socketAddress = new InetSocketAddress(SMARTBOX30_WIFI_CONNECT_ADDRESS, SMARTBOX30_WIFI_CONNECT_SYSTEM_PORT);
                    } else {
                        socketAddress = new InetSocketAddress(SMARTBOX30_WIFI_CONNECT_ADDRESS, SMARTBOX30_WIFI_CONNECT_DIAGNOSE_PORT);
                    }
                } else {
                    socketAddress = new InetSocketAddress(WIFI_CONNECT_ADDRESS, WIFI_CONNECT_PORT);
                }
            } else {
                if (!interrupted()) {
                    IDPUWiFiModeSettings dpuWiFiModeSettings = mDeviceFactoryManager.getDPUWiFiModeSettings();
                    if (dpuWiFiModeSettings == null || mMode == Constants.WIFI_WORK_MODE_WITH_STA_MODE_NO_INTERACTION) {
                        dpuWiFiModeSettings = new DPUWiFiModeSettingsImpl();
                    }
                    boolean isWifiApEnabled = NetworkUtil.isWifiApEnabled(mContext);
                    if (isWifiApEnabled &&
                            (Tools.isSupportDualWiFi(mContext) == false && Tools.isSupportWiFiPriority(mContext) == false)) {
                        ArrayList<String> ipList = NetworkUtil.getConnectedIP();
                        if (MLog.isDebug) {
                            MLog.e(TAG, "Connected IP  " + ipList.toString());
                        }
                        if (ipList.size() > 0) {
                            if (mDeviceFactoryManager != null && isFix && mDeviceFactoryManager.getFirmwareFixSubMode() == DeviceFactoryManager.FIRMWARE_FIX_SUB_MODE_FOR_OTA_UPGRADE) {
                                socketAddress = new InetSocketAddress(ipList.get(0), SMARTBOX30_WIFI_CONNECT_SYSTEM_PORT);
                            } else {
                                socketAddress = new InetSocketAddress(ipList.get(0), SMARTBOX30_WIFI_CONNECT_DIAGNOSE_PORT);
                            }
                        }
                    } else {
                        socketAddress = (InetSocketAddress) dpuWiFiModeSettings.getDPUSocketAddressWithUDPBroadcast();
                    }
                }
                if (socketAddress == null) {
                    if (!interrupted()) {
                        connectionFailed(mContext.getResources().getString(R.string.msg_wifi_connect_state_fail_with_no_ip));
                        return;
                    }
                }
            }
            try {
                if (!interrupted()) {
                    mmSocket.connect(socketAddress, WIFI_CONNECT_TIMEOUT);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
                MLog.e(TAG, "unable to connect() exception : " + e1.getMessage());
                //重试一次
                try {
                    if (!interrupted()) {
                        if (mmSocket.isClosed() == false) {
                            mmSocket.close();
                        }
                        init();
                        //等待3秒后重新连接
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mmSocket.connect(socketAddress, WIFI_CONNECT_TIMEOUT);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    MLog.e(TAG, "try connect error unable to connect() exception : " + e2.getMessage());
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
            try {
                this.interrupt();
                MLog.i(TAG, "mConnectThread.interrupt() for cancel");
            } catch (Exception e) {
                MLog.i(TAG, "mConnectThread.interrupt() Exception for cancel");
            }
            try {
                if (mmSocket != null && mmSocket.isConnected()) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                MLog.e(TAG, " close() of Socket connect ");
            }
        }
    }


    /**
     * 根据配置信息，连接网络
     */
    public void connect(int mode) {
        MLog.e(TAG, "connect  Device ");
        mMode = mode;
        if (Tools.isSupportDualWiFi(mContext)) {
            CustomWiFiControlForDualWiFi.getInstance(mContext).stopMonitor();
            connect();
        } else if (Tools.isSupportWiFiPriority(mContext)) {

            boolean isEnable = WiFiControlManager.getInstance(mContext).isWifiEnabled();
            if (MLog.isDebug) {
                MLog.d(TAG, "wifi isEnable  =" + isEnable);
            }
            if (isEnable == false) {
                WiFiControlManager.getInstance(mContext).setWifiEnabled(true);
            }
            connect();
        } else {
            boolean isWifiApEnabled = NetworkUtil.isWifiApEnabled(mContext);
            if (NetworkUtil.getConnectWIFI(mContext) == false && isWifiApEnabled == false) {
                connectionFailed();
            } else {
                if (MLog.isDebug) {
                    if (isWifiApEnabled) {
                        ArrayList<String> ipList = NetworkUtil.getConnectedIP();
                        MLog.e(TAG, "Connected IP  " + ipList.toString());
                    }
                }
                connect();
            }
        }
    }

    /**
     * 蓝牙连接
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
    private void connected(Socket socket) {
        MLog.d(TAG, "connected ");
        // Cancel the thread that completed the connection
        wifiSocket = socket;
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            mReadByteDataStreamThread = new ReadByteDataStream(this, inputStream, outputStream);
        } catch (IOException e) {
            MLog.e(TAG, "wifi Socket sockets not created" + e.getMessage());
        }
        new Thread(mReadByteDataStreamThread).start();
        if (Tools.isSupportDualWiFi(mContext)) {
            CustomWiFiControlForDualWiFi.getInstance(mContext).monitorConnectedState(mSerialNo);
        }
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
        if (message == null) {
            if (Tools.isSupportDualWiFi(mContext)) {
                intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.msg_wifi_state_no_active));
            } else if (Tools.isSupportWiFiPriority(mContext)) {
                intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.msg_wifi_connect_state_fail_for_wifi_priority));
            } else {
                intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.msg_wifi_connect_state_fail));
            }
        } else {
            intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, message);
        }
        mContext.sendBroadcast(intent);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Intent intent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS);
            intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
            intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.msg_wifi_connect_state_success));
            mContext.sendBroadcast(intent);
            registerNetworkConnectChangedReceiver();
            MLog.e(TAG, "wifi connected success,starting transfer data ");
            //发送广播通知连接成功
            mContext.sendBroadcast(new Intent(ACTION_DIAG_CONNECTED));
        }
    };

    private void registerNetworkConnectChangedReceiver() {
        IntentFilter filter = new IntentFilter();
        if (Tools.isSupportDualWiFi(mContext)) {
            filter.addAction(IPhysics.ACTION_CUSTOM_WIFI_CONNECT_DISCONNECTED);
        } else {
            if (NetworkUtil.isWifiApEnabled(mContext) == false) {
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            }
        }
        mIsBroadcastActionDpuDeviceConnectDisconnected = false;
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    /**
     * 该广播只有在连接成功后使用
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (NetworkUtil.getConnectWIFI(context) == false && !mIsBroadcastActionDpuDeviceConnectDisconnected) {
                    mIsBroadcastActionDpuDeviceConnectDisconnected = true;
                    mContext.sendBroadcast(new Intent(ACTION_DPU_DEVICE_CONNECT_DISCONNECTED));
                }
            } else if (intent.getAction().equals(IPhysics.ACTION_CUSTOM_WIFI_CONNECT_DISCONNECTED)) {
                mContext.sendBroadcast(new Intent(ACTION_DPU_DEVICE_CONNECT_DISCONNECTED));
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

    private class DPUWiFiModeSettingsImpl implements IDPUWiFiModeSettings {
        /**
         * 通过广播方式获取网卡模式接头的ip地址,与端口
         *
         * @return
         */
        @Override
        public SocketAddress getDPUSocketAddressWithUDPBroadcast() {
            byte[] buf = new byte[1024];
            boolean isMatchSmartbox30SupportSerialnoPrefix = Tools.isMatchSmartbox30SupportSerialnoPrefix(mContext, getSerialNo());
            DPUWiFiModeConfig dpuWiFiModeConfig = new DPUWiFiModeConfig();
            dpuWiFiModeConfig.setSerialNo(mSerialNo);
            byte[] requestBuffer = null;
            if (isMatchSmartbox30SupportSerialnoPrefix) {
                requestBuffer = Smartbox30DPUWiFiModeSettings.generateDPUWiFiDatagramRequestCommand(dpuWiFiModeConfig);
            } else {
                requestBuffer = StandardDPUWiFiModeSettings.generateDPUWiFiDatagramRequestCommand(dpuWiFiModeConfig);
            }
            if (MLog.isDebug) {
                MLog.d(TAG, "DatagramSocket.send  requestBuffer=" + ByteHexHelper.bytesToHexStringWithSearchTable(requestBuffer));
            }
            // 重发数据的次数
            int tries = 0;
            // 是否接收到数据的标志位
            boolean receivedResponse = false;
            String hostAddress = "";
            int port = 0;
            DatagramSocket socket = null;
            DatagramPacket dp_send = null;
            DatagramPacket dp_receive = null;
            try {
                // 广播的形式,并绑定本地动态端口0
                socket = new DatagramSocket(DPU_CONNECTOR_UDP_LOCAL_PORT);
                // 广播的形式标明端口
                InetAddress broadcastInetAddress = NetworkUtil.getBroadcastAddress(mContext);
                if (MLog.isDebug)
                    MLog.d(TAG, "broadcast Inet Address :" + broadcastInetAddress.toString());
                SocketAddress socketAddr = null;
                if (isMatchSmartbox30SupportSerialnoPrefix) {
                    socketAddr = new InetSocketAddress(broadcastInetAddress, SMARTBOX30_DPU_CONNECTOR_UDP_REMOTE_PORT);
                } else {
                    socketAddr = new InetSocketAddress(broadcastInetAddress, DPU_CONNECTOR_UDP_REMOTE_PORT);
                }
                // 定义用来发送数据的DatagramPacket实例
                dp_send = new DatagramPacket(requestBuffer, requestBuffer.length, socketAddr);
                // 定义用来接收数据的DatagramPacket实例
                dp_receive = new DatagramPacket(buf, 1024);
                socket.setSoTimeout(DPU_CONNECTOR_UDP_SEND_AND_RECEIVER_TIMEOUT);
            } catch (Exception e1) {
                MLog.e(TAG, "DatagramSocket initialize error,exception :" + e1.getMessage());
                return null;
            }
            // 直到接收到数据，或者重发次数达到预定值，则退出循环
            while (!receivedResponse
                    && tries < DPU_CONNECTOR_UDP_SEND_AND_RECEIVER_TRY_MAXNUM_COUNT) {
                // 发送数据
                try {
                    socket.send(dp_send);
                    tries++;
                } catch (Exception e) {
                    e.printStackTrace();
                    // 如果接收数据时阻塞超时，等待1秒重发并减少一次重发的次数
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if (MLog.isDebug)
                        MLog.e(TAG, "send broadcast error,exception :" + e.getMessage() + " ; " + (DPU_CONNECTOR_UDP_SEND_AND_RECEIVER_TRY_MAXNUM_COUNT - tries) + "  more tries...");
                    continue;
                }
                try {
                    // 接收从接头发送回来的数据
                    socket.receive(dp_receive);
                    // 验证数据正确性
                    if (MLog.isDebug) {
                        MLog.d(TAG, "DatagramSocket.receive  buffer=" + ByteHexHelper.bytesToHexStringWithSearchTable(dp_receive.getData(), 0, 1024));
                        MLog.e(TAG, "dp_receive ip:" + dp_receive.getAddress().getHostAddress());
                        MLog.e(TAG, "dp_receive port:" + dp_receive.getPort());
                    }
                    boolean isVailidState = false;
                    if (isMatchSmartbox30SupportSerialnoPrefix) {
                        isVailidState = Smartbox30DPUWiFiModeSettings.analysisDPUWiFiDatagramAnswerCommand(getSerialNo(), requestBuffer, dp_receive.getData());
                    } else {
                        isVailidState = StandardDPUWiFiModeSettings.analysisDPUWiFiDatagramAnswerCommand(requestBuffer, dp_receive.getData());
                    }
                    if (isVailidState) {
                        hostAddress = dp_receive.getAddress().getHostAddress();
                        port = dp_receive.getPort();
                        // 如果接收到数据。则将receivedResponse标志位改为true，从而退出循环
                        receivedResponse = true;
                        break;
                    } else {
                        tries++;
                        Arrays.fill(buf, (byte) 0);
                        dp_receive.setData(buf);
                    }
                } catch (Exception e) {
                    Arrays.fill(buf, (byte) 0);
                    dp_receive.setData(buf);
                    tries++;
                    e.printStackTrace();
                    MLog.e(TAG, "receive broadcast error,exception :" + e.getMessage());
                    continue;
                }
            }
            if (socket != null) {
                socket.close();
            }
            if (receivedResponse) {
                if (isMatchSmartbox30SupportSerialnoPrefix) {
                    return new InetSocketAddress(hostAddress, SMARTBOX30_WIFI_CONNECT_DIAGNOSE_PORT);
                } else {
                    return new InetSocketAddress(hostAddress, WIFI_CONNECT_PORT);
                }
            } else {
                return null;
            }
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
