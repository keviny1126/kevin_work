package com.cnlaunch.physics.wifi.custom;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.cnlaunch.physics.downloadbin.util.MyFactory;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.Constants;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.wifi.Smartbox30DPUWiFiModeSettings;
import com.power.baseproject.utils.EasyPreferences;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.cnlaunch.physics.impl.IPhysics.STATE_CONNECTED;
import static com.cnlaunch.physics.impl.IPhysics.STATE_CONNECTING;
import static com.cnlaunch.physics.impl.IPhysics.STATE_NONE;

/**
 * 双wifi中的自定义wifi控制
 * Created by xiefeihong on 2018/4/21.
 */
public class CustomWiFiControlForDualWiFi implements ISecondWiFiManager {
    private static final String TAG = "CustomWiFiControl";
    private Context mContext;
    private ConcurrentHashMap<String, Integer> mSSIDState;
    private MonitorThread monitorThread;
    private ISecondWiFiManager mSecondWiFiManager;
    private boolean isNewDualWifiSupport;
    private boolean isObtainingIpSetting;

    public static enum WPAState {
        NONE, CONNECTING, CONNECTED
    }

    public static class WiFiState {
        public String ssid;
        public WPAState wpaState;

        public WiFiState() {
            reset();
        }

        private void reset() {
            ssid = "";
            wpaState = WPAState.NONE;
        }
    }

    private static CustomWiFiControlForDualWiFi mCustomWiFiControlForDualWiFi = null;

    public static CustomWiFiControlForDualWiFi getInstance(Context context) {
        if (mCustomWiFiControlForDualWiFi == null) {
            mCustomWiFiControlForDualWiFi = new CustomWiFiControlForDualWiFi(context);
        }
        return mCustomWiFiControlForDualWiFi;
    }

    private CustomWiFiControlForDualWiFi(Context context) {
        mContext = context;
        monitorThread = null;
        isNewDualWifiSupport = EasyPreferences.Companion.getInstance().get(Constants.IS_NEW_DUAL_WIFI_SUPPORT, false);
        mSSIDState = new ConcurrentHashMap<String, Integer>();
        if (isNewDualWifiSupport == false) {
            mSecondWiFiManager = new OldSecondWiFiManager(mContext);
        } else {
            mSecondWiFiManager = new TrebleSecondWiFiManager(mContext);
        }
    }

    private synchronized void setState(String ssid, int state) {
        mSSIDState.put(ssid, state);
    }

    public synchronized int getState(String ssid) {
        Integer state = mSSIDState.get(ssid);
        if (state != null) {
            return state;
        } else {
            return STATE_NONE;
        }
    }

    private void waitWiFiConnectStateAfterSelectNetwork(String ssid, boolean isManual) {
        waitWiFiConnectStateAfterSelectNetwork(ssid, "", "", isManual);
    }

    /**
     * 等待wifi网络设置后的连接状态
     */
    private void waitWiFiConnectStateAfterSelectNetwork(String ssid, String routeRage, String gateway, boolean isManual) {
        int connectCount = 0;
        //因为状态上报存在延时,最长控制为10秒
        int repertCount;
        if (isManual) {
            repertCount = 10;
        } else {
            repertCount = 10;
        }
        WiFiState wiFiState = getCurrentWiFiState(ssid);
        while (wiFiState.wpaState != WPAState.CONNECTED && connectCount < repertCount) {
            String whileState = String.format("waitWiFiConnectStateAfterSelectNetwork Tools.isSupportDualWiFi wiFiState=%s connectCount=%d", wiFiState.wpaState, connectCount);
            if (MLog.isDebug) {
                MLog.d(TAG, whileState);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            wiFiState = getCurrentWiFiState(ssid);
            connectCount++;
        }
        if (wiFiState.wpaState == WPAState.CONNECTED) {
            boolean isSuccess;
            boolean isDHCPRequest;
            if (TextUtils.isEmpty(routeRage) == false && TextUtils.isEmpty(gateway) == false) {
                isDHCPRequest = true;
            } else {
                isDHCPRequest = false;
            }

            int requestRetryCount = 0;
            int maxRetryCount = 3;

            if (isDHCPRequest) {
                //动态分配ip说明不属于工作接头，需要重新进行ip路由设定
                isObtainingIpSetting = false;
                //动态ip分配
                requestRetryCount = 0;
                while (requestRetryCount < maxRetryCount) {
                    isSuccess = requestIPWithDHCP();
                    if (isSuccess) {
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    requestRetryCount++;
                }
                if (requestRetryCount >= maxRetryCount) {
                    setState(ssid, STATE_NONE);
                    return;
                }
                //deleteIpRule();
                requestRetryCount = 0;
                while (requestRetryCount < maxRetryCount) {
                    isSuccess = setIpRule(routeRage, gateway);
                    if (isSuccess) {
                        break;
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    requestRetryCount++;
                }
                if (requestRetryCount >= maxRetryCount) {
                    //什么都不用做
                    //setState(ssid, STATE_NONE);
                    //return;
                }
            } else {
                if (!isObtainingIpSetting) {
                    requestRetryCount = 0;
                    while (requestRetryCount < maxRetryCount) {
                        isSuccess = setStaticIP();
                        if (isSuccess) {
                            break;
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        requestRetryCount++;
                    }
                    if (requestRetryCount >= maxRetryCount) {
                        setState(ssid, STATE_NONE);
                        return;
                    }
                    //deleteIpRule();
                    requestRetryCount = 0;
                    while (requestRetryCount < maxRetryCount) {
                        isSuccess = setIpRule();
                        if (isSuccess) {
                            break;
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        requestRetryCount++;
                    }
                    if (requestRetryCount >= maxRetryCount) {
                        //什么都不用做
                        //setState(ssid, STATE_NONE);
                        //return;
                    }
                    isObtainingIpSetting = true;
                }
            }
            setState(ssid, STATE_CONNECTED);
        } else {
            setState(ssid, STATE_NONE);
        }
    }

    //自动连接wifi
    //必须在线程中执行
    //无需该方法，自动连接需要由wpa_supplicant控制。
    /*public void autoConnectNetwork(final String ssid, final String password) {}*/

    /**
     * 测试用手动连接接口
     *
     * @param ssid
     * @param password
     * @param routeRage
     * @param gateway
     */
    public void manualConnectNetwork(final String ssid, final String password,
                                     final String routeRage, final String gateway) {
        manualConnectNetwork(ssid, password, routeRage, gateway, false);
    }

    //手动连接wifi
    public void manualConnectNetwork(final String ssid, final String password) {
        manualConnectNetwork(ssid, password, "", "", false);
    }

    public void manualConnectNetwork(final String ssid, final String password, boolean isForce) {
        manualConnectNetwork(ssid, password, "", "", isForce);
    }

    public void manualConnectNetwork(final String ssid, final String password, final String routeRage, final String gateway, boolean isForce) {
        if (MLog.isDebug) {
            MLog.d(TAG, String.format("manualConnectNetwork ssid=%s,routeRage=%s,gateway=%s,isForce=%b", ssid, routeRage, gateway, isForce));
        }

        setState(ssid, STATE_CONNECTING);
        WiFiState wifiState = getCurrentWiFiState(ssid);
        if (wifiState.wpaState == WPAState.CONNECTED) {
            waitWiFiConnectStateAfterSelectNetwork(ssid, routeRage, gateway, true);
            return;
        }
        boolean isSuccess = true;
        isObtainingIpSetting = false;
        if (isEnabled() == false) {
            isSuccess = setWifiEnabled(true);
            if (isSuccess == false) {
                setState(ssid, STATE_NONE);
                return;
            }
        } else {
            if (isForce && isNewDualWifiSupport == false) {
                Log.d(TAG, "Force setWifiEnabled true ");
                isSuccess = setWifiEnabled(true);
                if (isSuccess == false) {
                    setState(ssid, STATE_NONE);
                    return;
                }
            }
        }
        isSuccess = addORUpdateNetwork(ssid, password);
        if (isSuccess == false) {
            setState(ssid, STATE_NONE);
            return;
        }
        waitWiFiConnectStateAfterSelectNetwork(ssid, routeRage, gateway, true);
    }

    public synchronized void stopMonitor() {
        if (monitorThread != null) {
            monitorThread.setStop(true);
            monitorThread = null;
        }
    }

    public void monitorConnectedState(final String ssid) {
        if (monitorThread != null) {
            monitorThread.setStop(true);
            monitorThread = null;
        }
        monitorThread = new MonitorThread(ssid);
        monitorThread.start();
    }

    public class MonitorThread extends Thread {
        private String ssid;
        private boolean isStop;

        public MonitorThread(String ssid) {
            this.ssid = ssid;
            isStop = false;
        }

        public synchronized boolean isStop() {
            return isStop;
        }

        public synchronized void setStop(boolean stop) {
            isStop = stop;
        }

        @Override
        public void run() {
            while (isStop() == false) {
                WiFiState wifiState = getCurrentWiFiState(ssid);
                if (CustomWiFiControlForDualWiFi.this.getState(ssid) == STATE_CONNECTED &&
                        (wifiState.wpaState == WPAState.NONE || wifiState.wpaState == WPAState.CONNECTING)) {
                    if (mContext != null) {
                        Intent intent = new Intent(IPhysics.ACTION_CUSTOM_WIFI_CONNECT_DISCONNECTED);
                        mContext.sendBroadcast(intent);
                    }
                    setState(ssid, STATE_NONE);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**********************ISecondWiFiManager overide method**************************/
    @Override
    public boolean isEnabled() {
        return mSecondWiFiManager.isEnabled();
    }

    @Override
    public boolean setWifiEnabled(boolean enabled) {
        return mSecondWiFiManager.setWifiEnabled(enabled);
    }

    @Override
    public boolean startScan() {
        return mSecondWiFiManager.startScan();
    }

    @Override
    public List<AccessPointCustomInterface> getScanResult() {
        return mSecondWiFiManager.getScanResult();
    }

    @Override
    public List<AccessPointCustomInterface> getScanResult(boolean isDebug) {
        return mSecondWiFiManager.getScanResult(isDebug);
    }

    @Override
    public boolean addORUpdateNetwork(String ssid, String password) {
        return mSecondWiFiManager.addORUpdateNetwork(ssid, password);
    }

    @Override
    public boolean requestIPWithDHCP() {
        return mSecondWiFiManager.requestIPWithDHCP();
    }

    @Override
    public boolean setStaticIP() {
        return mSecondWiFiManager.setStaticIP();
    }

    @Override
    public WiFiState getCurrentWiFiState(String ssid) {
        return mSecondWiFiManager.getCurrentWiFiState(ssid);
    }

    @Override
    public boolean setIpRule(String routeRage, String gateway) {
        return mSecondWiFiManager.setIpRule(routeRage, gateway);
    }

    @Override
    public boolean setIpRule() {
        return mSecondWiFiManager.setIpRule();
    }

    @Override
    public boolean deleteIpRule() {
        return mSecondWiFiManager.deleteIpRule();
    }
/**********************ISecondWiFiManager end**************************/

    /**
     * UDP查询VCI序列号
     * UDP广播包端口：22536
     * 子命令：0x2A
     * 返回格式： <字符串长度(2个字节)><字符串> 注：字符串长度 高位在前，低位在后…
     * 主端申请：2C 2A
     * 从端应答：6C 2A  00  0C
     * 示例：
     * Send:55 AA F1 F8 00 03 02 2C 2A 0E
     * Ans :55 AA F8 F1 00 11 02 6C 2A 00 0C 39 38 39 38 39 30 30 30 30 30 36 31 5E //序列号为989890000061
     *
     * @return
     */
    private boolean getCurrentWiFiStateByUdpSniff(String ssid) {
        byte[] buf = new byte[1024];
        byte[] requestBuffer = null;
        requestBuffer = MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x2a}, null);
        if (MLog.isDebug) {
            MLog.d(TAG, "DatagramSocket.send  requestBuffer=" + ByteHexHelper.bytesToHexStringWithSearchTable(requestBuffer));
        }
        // 重发数据的次数
        int tries = 0;
        // 是否接收到数据的标志位
        boolean receivedResponse = false;
        DatagramSocket socket = null;
        DatagramPacket dp_send = null;
        DatagramPacket dp_receive = null;
        try {
            // 广播的形式,并绑定本地动态端口0
            socket = new DatagramSocket();
            // 广播的形式标明端口
            SocketAddress socketAddr = null;
            InetAddress broadcastInetAddress = InetAddress.getByName("192.168.100.255");
            socketAddr = new InetSocketAddress(broadcastInetAddress, 22536);
            // 定义用来发送数据的DatagramPacket实例
            dp_send = new DatagramPacket(requestBuffer, requestBuffer.length, socketAddr);
            // 定义用来接收数据的DatagramPacket实例
            dp_receive = new DatagramPacket(buf, 1024);
            socket.setSoTimeout(250);
        } catch (Exception e1) {
            MLog.e(TAG, "DatagramSocket initialize error,exception :" + e1.getMessage());
            return receivedResponse;
        }
        // 直到接收到数据，或者重发次数达到预定值，则退出循环
        while (!receivedResponse && tries < 3) {
            //发送数据
            try {
                tries++;
                socket.send(dp_send);
            } catch (Exception e) {
                e.printStackTrace();
                //等待500秒重试
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                if (MLog.isDebug) {
                    MLog.e(TAG, "send broadcast error,exception :" + e.getMessage() + " ; " + (3 - tries) + "  more tries...");
                }
                continue;
            }
            try {
                // 接收从接头发送回来的数据
                socket.receive(dp_receive);
                // 验证数据正确性
                if (MLog.isDebug) {
                    MLog.d(TAG, "DatagramSocket.receive  buffer=" + ByteHexHelper.bytesToHexStringWithSearchTable(dp_receive.getData(), 0, dp_receive.getLength()));
                    MLog.e(TAG, "dp_receive ip:" + dp_receive.getAddress().getHostAddress());
                    MLog.e(TAG, "dp_receive port:" + dp_receive.getPort());
                }
                boolean isVailidState = Smartbox30DPUWiFiModeSettings.analysisDPUWiFiDatagramAnswerCommand(ssid, requestBuffer, dp_receive.getData());
                if (isVailidState) {
                    receivedResponse = true;
                }
            } catch (Exception e) {
                //e.printStackTrace();
                Arrays.fill(buf, (byte) 0);
                dp_receive.setData(buf);
                //等待500秒重试
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                if (MLog.isDebug) {
                    MLog.e(TAG, "receive broadcast error,exception :" + e.getMessage() + " ; " + (3 - tries) + "  more tries...");
                }
                continue;
            }
        }
        if (socket != null) {
            socket.close();
        }
        return receivedResponse;
    }
}
