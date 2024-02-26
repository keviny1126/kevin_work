package com.cnlaunch.physics;

import android.util.Log;

import com.cnlaunch.physics.downloadbin.util.Analysis;
import com.cnlaunch.physics.downloadbin.util.MyFactory;
import com.cnlaunch.physics.entity.AnalysisData;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.Tools;
import com.cnlaunch.physics.wifi.Smartbox30DPUWiFiModeSettings;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

/**
 * smartbox 3.0系统及应用升级逻辑实现
 * Created by xiefeihong on 2019/3/6.
 */

public class Smartbox30Update {
    private final static String TAG = Smartbox30Update.class.getSimpleName();

    public static class Smartbox30Version {
        private String SystemVersion;
        private String ApplicationVersion;

        public Smartbox30Version() {
            SystemVersion = null;
            ApplicationVersion = null;
        }

        public String getSystemVersion() {
            return SystemVersion;
        }

        public void setSystemVersion(String systemVersion) {
            SystemVersion = systemVersion;
        }

        public String getApplicationVersion() {
            return ApplicationVersion;
        }

        public void setApplicationVersion(String applicationVersion) {
            ApplicationVersion = applicationVersion;
        }
    }

    public static Smartbox30Version queryVersion() {
        return queryVersion(5, 5000);
    }
    /**
     *
     * @param maxTries
     * @param perOneReceiveTimeoutWithMillseconds
     * @param requestBuffer
     * @param host default is IPhysics.SMARTBOX30_WIFI_CONNECT_ADDRESS
     * @param port default is  IPhysics.SMARTBOX30_DPU_SYSTEM_UDP_REMOTE_PORT
     * @return
     */
    private static byte[] sendDatagram(int maxTries, int perOneReceiveTimeoutWithMillseconds,byte[] requestBuffer,String host,int port) {
       byte[] responseBuffer = null;
        byte[] buf = new byte[1024];
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
            socket = new DatagramSocket(0);
            // 广播的形式标明端口
            InetAddress broadcastInetAddress = InetAddress.getByName(host);
            if (MLog.isDebug) {
                MLog.d(TAG, "broadcast Inet Address :" + broadcastInetAddress.toString());
            }
            SocketAddress socketAddr = null;
            socketAddr = new InetSocketAddress(broadcastInetAddress, port);
            // 定义用来发送数据的DatagramPacket实例
            dp_send = new DatagramPacket(requestBuffer, requestBuffer.length, socketAddr);
            // 定义用来接收数据的DatagramPacket实例
            dp_receive = new DatagramPacket(buf, 1024);
            socket.setSoTimeout(perOneReceiveTimeoutWithMillseconds);
        } catch (Exception e1) {
            if (MLog.isDebug) {
                MLog.e(TAG, "DatagramSocket initialize error,exception :" + e1.getMessage());
            }
            return responseBuffer;
        }
        // 直到接收到数据，或者重发次数达到预定值，则退出循环
        while (!receivedResponse && tries < maxTries) {
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
                if (MLog.isDebug) {
                    MLog.e(TAG, "send broadcast error,exception :" + e.getMessage() + " ; " + (5 - tries) + "  more tries...");
                }
                continue;
            }
            try {
                // 接收从接头发送回来的数据
                socket.receive(dp_receive);
                // 验证数据正确性
                if (MLog.isDebug) {
                    MLog.d(TAG, "DatagramSocket.receive  buffer=" + ByteHexHelper.bytesToHexStringWithSearchTable(dp_receive.getData(), 0, 1024));
                    MLog.d(TAG, "dp_receive ip:" + dp_receive.getAddress().getHostAddress());
                    MLog.d(TAG, "dp_receive port:" + dp_receive.getPort());
                }
                Analysis analysis = MyFactory.creatorForAnalysis();
                AnalysisData analysisData = analysis.analysisSmartbox30LinuxCommand(requestBuffer, dp_receive.getData());
                if (analysisData.getState()) {
                    byte[] receiveBuffer = analysisData.getpReceiveBuffer();
                    if (receiveBuffer != null) {
                        responseBuffer = receiveBuffer;
                        receivedResponse = true;
                        if (MLog.isDebug) {
                            MLog.d(TAG, " analysisData state is true");
                        }
                    }
                }
            } catch (Exception e) {
                Arrays.fill(buf, (byte) 0);
                dp_receive.setData(buf);
                tries++;
                e.printStackTrace();
                Log.e(TAG, "receive broadcast error,exception :" + e.getMessage());
                continue;
            }
        }
        if (socket != null) {
            socket.close();
        }
        return responseBuffer;
    }
    public static Smartbox30Version queryVersion(int maxTries, int perOneReceiveTimeoutWithMillseconds) {
        if (MLog.isDebug) {
            MLog.d(TAG, "start querySystemVersion()");
        }
        Smartbox30Version smartbox30SystemVersion = null;
        byte[] requestBuffer  = Smartbox30DPUWiFiModeSettings.generateDPUWiFiDatagramRequestCommand2C29();
        if (MLog.isDebug) {
            MLog.d(TAG, "DatagramSocket.send  requestBuffer=" + ByteHexHelper.bytesToHexStringWithSearchTable(requestBuffer));
        }
        byte[] receiveBuffer = sendDatagram(maxTries, perOneReceiveTimeoutWithMillseconds, requestBuffer, IPhysics.SMARTBOX30_WIFI_CONNECT_ADDRESS, IPhysics.SMARTBOX30_DPU_SYSTEM_UDP_REMOTE_PORT);
        if (receiveBuffer != null) {
            String data = new String(receiveBuffer);
            smartbox30SystemVersion = new Smartbox30Version();
            int index = data.lastIndexOf("=");
            if (index != -1) {
                smartbox30SystemVersion.setSystemVersion(data.substring(index + 1, data.length()));
            }
            int indexApp = data.indexOf("=");
            if (indexApp != -1) {
                smartbox30SystemVersion.setApplicationVersion(data.substring(indexApp + 1, indexApp + 1 + 6));
            }
            if (MLog.isDebug) {
                MLog.d(TAG, String.format("Smartbox30SystemVersion mLinuxVersion=%s ,mAppVersion=%s ", smartbox30SystemVersion.getSystemVersion(), smartbox30SystemVersion.getApplicationVersion()));
            }
        }
        return smartbox30SystemVersion;
    }

    /**
     * 获取VCI序列号 2A
     * UDP查询VCI序列号
     * UDP查询应用和系统版本信息
     * UDP广播包端口：22536
     *
     * 子命令：0x2A
     * 返回格式： <字符串长度(2个字节)><字符串> 注：字符串长度 高位在前，低位在后…
     * 主端申请：2C 2A
     * 从端应答：6C 2A  00  0C
     * 示例：
     * Send:55 AA F1 F8 00 03 02 2C 2A 0E
     * Ans :55 AA F8 F1 00 11 02 6C 2A 00 0C 39 38 39 38 39 30 30 30 30 30 36 31 5E //序列号为989890000061
     * @return
     */
    public static String querySerialNo() {
        return querySerialNo(5, 5000);
    }
    public static String querySerialNo(int maxTries, int perOneReceiveTimeoutWithMillseconds) {
        if (MLog.isDebug) {
            MLog.d(TAG, "start querySerialNo()");
        }
        String serialNo="" ;
        byte[] requestBuffer  = MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x2A}, null);;
        if (MLog.isDebug) {
            MLog.d(TAG, "DatagramSocket.send  requestBuffer=" + ByteHexHelper.bytesToHexStringWithSearchTable(requestBuffer));
        }
        byte[] receiveBuffer = sendDatagram(maxTries, perOneReceiveTimeoutWithMillseconds, requestBuffer, IPhysics.SMARTBOX30_WIFI_CONNECT_ADDRESS, IPhysics.SMARTBOX30_DPU_SYSTEM_UDP_REMOTE_PORT);
        if (receiveBuffer != null && receiveBuffer.length>=14) {
            serialNo = new String(Arrays.copyOfRange(receiveBuffer,2,receiveBuffer.length));
            if (MLog.isDebug) {
                MLog.d(TAG, String.format("querySerialNo  serialNo=%s ", serialNo));
            }
        }
        return serialNo;
    }
    public static boolean sendUpdateCommand(IPhysics iPhysics) {
        int maxWaitTime = 6000;
        byte[] dpuWiFiModeSettingsOrder = null;
        byte[] dpuWiFiModeReceiveBuffer = null;
        dpuWiFiModeSettingsOrder = MyFactory.creatorForOrderMontage().generateSmartbox30LinuxCommonCommand(new byte[]{0x2c, 0x07}, new byte[]{});
        dpuWiFiModeReceiveBuffer = Tools.dpuSmartbox30CommandOperation(iPhysics, dpuWiFiModeSettingsOrder, maxWaitTime);
        if (dpuWiFiModeReceiveBuffer != null && dpuWiFiModeReceiveBuffer.length >= 1) {
            if (dpuWiFiModeReceiveBuffer[0] == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
