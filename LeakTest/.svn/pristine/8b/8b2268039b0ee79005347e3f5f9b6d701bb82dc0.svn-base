package com.cnlaunch.physics.smartlink;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.text.TextUtils;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.utils.DataTools;
import com.cnlaunch.physics.utils.MLog;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static java.net.InetAddress.getByName;

public class PeerTask implements SendInterface {
    public static final String TAG = "PeerTask";
    private static final int BUFF_SIZE = 4096;

    public static final int GET_MY_TASK = 1;        ////web端查询任务
    public static final int GET_PEER_INFO = 2;        //UDP服务器查询对端信息
    public static final int PEER_UDP_CONNECT = 3;     //UDP直连
    public static final int GET_SERVER_LIST = 4;      //等待用户选择服务器
    public static final int PEER_TCP_CONNECT = 5;     //TCP中转

    public static final int PEER_UDP = 0;   //UDP直连
    public static final int PEER_TCP = 1;   //TCP中转

    public static final int PEER_HANDSHAKE = 0;          //双方握手--身份通知
    public static final int PEER_CONFIG_SMART_TASK_FIRE = 2;  //智能任务点火开关提醒
    public static final int PEER_CONFIG_SMART_XML = 3;        //智能任务生成XML配置文件
    public static final int PEER_CONFIG_WAIT_FIRE = 6;       //等待用户确认点火开关打开
    public static final int PEER_CONFIG_AUTO = 7;            //波特率自识别
    public static final int PEER_CONFIG_ANSWER = 10;      //接收到应答

    InetSocketAddress udp_serv_addr;//UDP服务器地址
    InetSocketAddress udp_peer_addr;//对端地址
    static PeerTask mPeerTask;
    PeerLink mPeerLink;
    Socket mTcpSocket;
    Messenger mClient;
    byte[] saveUdpReceivaData = new byte[0];
    int saveUdpReceiveDataLen = 0;
    boolean isUdpFristPackage = true;
    Lock udpReadLock = new ReentrantLock();

    byte[] saveTcpReceivaData = new byte[0];
    int saveTcpReceiveDataLen = 0;
    boolean isTcpFristPackage = true;
    Lock tcpReadLock = new ReentrantLock();
    String mUdpIp;
    int mUdpPort;

    int peer_udp_fd = -1;              //UDP连接
    int peer_tcp_fd = -1;              //中转服务器

    int peer_mode = PEER_UDP;             //B端与C端通信方式
    int peer_task_state = GET_MY_TASK;

    boolean peer_udp_server_state = false;   //与UDP服务器连接状态
    boolean peer_connect_state = false;      //B与C端之間建立起连接(包括中转服务器方式)

    int get_peer_info_state = 0;          //0-未发送  1-已发送   2-接收到应答
    int peer_task_cnt = 0;                //B端建立UDP服务器连接计数器
    int peer_task_timeout = 0;            //任务超时时间
    int peer_heartbeat_cnt = 0;           //对端心跳
    int udp_heartbeat_cnt = 0;            //UDP服务器心跳
    int tcp_heartbeat_cnt = 0;            //TCP服务器心跳

    public String local_ip;
    public String peer_ip;
    int local_port = 0;
    int peer_port = 0;
    public String tcp_server_ip = "proxy_remote.x431.com";        //域名或IP,兼容旧版本，若B端通知未带服务器地址，则使用默认服务器
    int tcp_server_port = 20168;
    int tcp_server_connect_times = 0;    //中转服务器连接次数

    int peer_tcp_task_state = 0;           //0-未发送 1-已发送   2-接收应答  3-匹配成功,进行车型配置 4-诊断数据传输
    int peer_request_config_state = PEER_HANDSHAKE;     //请求车辆配置信息  0-未发送 1-已发送 2-接收应答 3-配置M7 4-获取VIN码 5-通知对端配置成功，带VIN码 6-接收到应答
    int local_config_state = 0;            //本机车辆配置结果  0-失败  1-成功
    int peer_config_state = 0;             //对端车辆配置结果  0-失败  1-成功
    int peer_udp_reconnect_times = 0;      //UDP直连重连次数
    boolean peer_handshake_request = false;   //收到对方握手请求
    boolean peer_handshake_ack = false;       //收到对方握手请求应答

    byte linkb_type = (byte) 0xB0;             //默认硬B端

    boolean get_all_server_td_flag = false;

    static boolean global_net_state = false;    //检测eth0网卡是否有IP
    byte ip_limit_flag = 0;
    DatagramSocket mUdpSocket;
    Context mContext;
    long send_bytes = 0;
    long recv_bytes = 0;
    boolean network_tatus = true;
    boolean mIsNotExit = false;
    TaskInfo mTaskInfo;
    int car_volt;
    boolean reset_arm = false;
    int request_peer_test_state = 0;      //0-未发送  1-已发送   2-接收到应答  (向UDP服务器发起远程诊断请求)
    public String mSerialNo;
    boolean peer_get_all_server_td_flag = false;
    boolean request_server_td_finish = false;
    int get_tcp_server_info_state = 0; //0-获取服务器列表  1-请求C端发送服务器列表速率  2-等待用户选择服务器 3-用户选择完服务器
    boolean switch_btn_state = false;         //切换服务器按钮标志
    int tcp_notify_test_end_state = 0;     //0-未开始  1-通知C端  2-收到C端应答
    ArrayList<ServerInfos> mServerInfos;
    boolean request_select_server_flag = false;
    int select_server_index = 0;
    long start_diagnose_time = 0;
    int net_time = 0;
    boolean reconnect_status = false;

    public static PeerTask getInstance() {
        if (mPeerTask == null) {
            synchronized (PeerTask.class) {
                if (mPeerTask == null) {
                    mPeerTask = new PeerTask();
                }
            }
        }
        return mPeerTask;
    }

    PeerTask() {
        try {
            mUdpSocket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setSn(String sn) {
        mSerialNo = sn;
    }

    boolean get_peer_get_all_server_td_flag() {
        return peer_get_all_server_td_flag;
    }

    void set_peer_get_all_server_td_flag(boolean value) {
        peer_get_all_server_td_flag = value;
    }

    void handshake_request_recv() {
        peer_handshake_request = true;
        byte[] payload = new byte[512];
        int size = mPeerLink.send_peer_ack(payload, PacketQueue.PEER_ID, 0x07, 0x12, 0x01);
        if (peer_mode == PEER_UDP) {
            peer_udp_send_to_peer(peer_udp_fd, payload, size);
        } else {
            peer_send(peer_tcp_fd, payload, size);
        }
        SendQueue.getInstance().clear();
        RecvQueue.getInstance().clear();
    }

    void handshake_ack_recv() {
        if(peer_request_config_state == PEER_CONFIG_ANSWER) {
            return;
        }
        peer_handshake_ack = true;
        peer_request_config_state = PEER_CONFIG_ANSWER;
        start_diagnose_time = System.currentTimeMillis();
        local_config_state = 1;
        peer_config_state = 1;

        if(!reconnect_status) {
            byte[] payload = new byte[128];
            byte[] data = new byte[1];
            data[0] = 0x13;
            MLog.d(TAG, "SEND 2534");
            int size = mPeerLink.pack_peer_frame(payload, 01, (byte) 0x07, data, 1);
            if (peer_mode == PEER_UDP) {
                peer_udp_send_to_peer(peer_udp_fd, payload, size);
            } else {
                peer_send(peer_tcp_fd, payload, size);
            }
        }

       // peer_task_state = 5 ;
        PacketQueue.getMcuSendQueue().clear();
        PacketQueue.getPeerTcpQueue().clear();
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent successIntent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS);
        successIntent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, false);
        successIntent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, "connect smartlink successful");
        boolean status = reconnect_status;
        successIntent.putExtra("reconnect_smartlink", status);
        mContext.sendBroadcast(successIntent);
        reconnect_status = false;
        //LocalBroadcastManager.getInstance(mContext).sendBroadcast(successIntent);
    }

    public boolean is_handshake_ok() {
        if (peer_handshake_ack && peer_handshake_request) {
            return true;
        }

        return false;
    }

    public boolean is_match_ok() {
/*        if ((peer_request_config_state == PEER_CONFIG_ANSWER) && (local_config_state == 1) && (peer_config_state == 1)) {
            return true;
        }*/
        if(peer_request_config_state == PEER_CONFIG_ANSWER) {
            return true;
        }
        return false;
    }


    public int get_peer_mode() {
        return peer_mode;
    }

    void clear_heartbeat(int src) {
        if (src == 0x00) {
            if (peer_task_state == PEER_TCP_CONNECT) {
                tcp_heartbeat_cnt = 0;
            } else {
                udp_heartbeat_cnt = 0;
            }
        } else {
            peer_heartbeat_cnt = 0;
        }
    }

    boolean is_open_wifi_sta_mode() {
        return false;
    }

    //对端车辆配置通知结果
    void set_peer_config_result(byte src, int value) {
        byte[] payload = new byte[512];
        int size = 0;

        peer_config_state = value;

        if (is_open_wifi_sta_mode()) {
            //system("wl PM 0 &");
        }

        //memset(payload, 0, sizeof(payload));
        if (peer_mode == PEER_UDP) {
            size = mPeerLink.send_peer_ack(payload, src, 0x07, 0x14, 0x01);
            peer_udp_send_to_peer(peer_udp_fd, payload, size);
        } else {
            size = mPeerLink.send_peer_ack(payload, src, 0x07, 0x14, 0x01);
            peer_send(peer_tcp_fd, payload, size);
        }
    }

    //本方发送车辆配置通知后收到对端应答
    void set_peer_request_config_state(int value) {
        peer_request_config_state = value;
    }

    int get_peer_task_state() {
        return peer_task_state;
    }

    synchronized int peer_send(int fd, byte[] buf, int length) {
        int size = 0;
        int n = 0;
        int remain = length;
        int times = 0;

        if (fd < 0) {
            MLog.e(TAG,"peer not yet connect");
            return -1;
        }

        if (length <= 0) {
            MLog.e(TAG,"send size error");
            return -1;
        }
        byte[] sendData = new byte[length];
        System.arraycopy(buf, 0, sendData, 0 ,length);
        try {
            mTcpSocket.getOutputStream().write(sendData);
            if (MLog.isDebug) {
                String datastr = "TCP发送数据***:" + DataTools.bytesToHexString(sendData, 0, length);
                MLog.d(TAG, datastr);
            } else {
                MLog.d(TAG, "tcp 发送长度="+sendData.length);
            }
            //last_send_timer = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return n;
    }

    synchronized int peer_udp_send(int fd, InetSocketAddress dest_addr, byte[] buf, int length) {
        int size = 0;

        if (fd < 0) {
            MLog.e(TAG,"peer not yet connect");
            return -1;
        }

        if (length <= 0) {
            MLog.e(TAG,"send size error");
            return -1;
        }

        if(mUdpSocket == null) {
            try {
                mUdpSocket = new DatagramSocket();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            DatagramPacket sendDp = new DatagramPacket(buf, length, dest_addr);
            mUdpSocket.send(sendDp);
            MLog.d(TAG, "udp 发送长度="+length);
            //last_send_timer = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
            //MLog.e(TAG, "udp发送数据-失败:" + DataTools.bytesToHexString(buf, 0, length));
            return 0;
        }
        if (MLog.isDebug) {
            //MLog.d(TAG, "udp发送数据:" + DataTools.bytesToHexString(buf, 0, length));
        }
        return size;
    }

    int peer_udp_send_to_server(int fd, byte[] buf, int length) {
        return peer_udp_send(fd, udp_serv_addr, buf, length);
    }

    int peer_udp_send_to_peer(int fd, byte[] buf, int length) {
        return peer_udp_send(fd, udp_peer_addr, buf, length);
    }

    int peer_send_to_peer(byte[] buf, int length) {
        if (peer_mode == PEER_UDP) {
            return peer_udp_send_to_peer(peer_udp_fd, buf, length);
        } else {
            return peer_send(peer_tcp_fd, buf, length);
        }
    }

    void peer_link_check() {
        mPeerLink.check_triptime();

        if (udp_heartbeat_cnt > 3) {
            clear_get_info_state();
            udp_heartbeat_cnt = 0;
        }

        if (tcp_heartbeat_cnt > 3) {
            peer_tcp_task_clear();
            tcp_heartbeat_cnt = 0;
        }

        if (peer_heartbeat_cnt > 3) {
            if(is_match_ok()) {
                Intent intent = new Intent("SHOW_RECONNECT_DIAGLOG");
                //LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                mContext.sendBroadcast(intent);
            }

            if (peer_task_state == PEER_TCP_CONNECT) {
                peer_tcp_task_clear();
            } else if (peer_task_state == PEER_UDP_CONNECT) {
                peer_udp_reconnect_times++;
                clear_get_info_state();
                peer_task_state = GET_PEER_INFO;
                peer_task_timeout = 0;
                peer_connect_state = false;
            }
            start_diagnose_time = 0;
            peer_heartbeat_cnt = 0;
        }
    }

    void peer_udp_send_thread() {
        SendQueue sd = SendQueue.getInstance();
        while (mIsNotExit) {
            if (!is_match_ok()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            }

            if (peer_mode == PEER_UDP) {
                //long time = 1;
                if (peer_udp_fd > 0) {
                    sd.getPacket();
                } else {
                    // time = 1000;
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } else {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void peer_tcp_send_thread() {
        while (mIsNotExit) {
            if (!is_match_ok()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            }

            if (peer_mode == PEER_TCP) {
                if (peer_tcp_fd > 0) {
                    PacketQueue.getPeerTcpQueue().run();
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void peer_heartbeat_thread() {
        long now_timer = 0;
        int size = 0;
        byte[] buf = new byte[512];
        int ret = 0;
        int number = 0;
        while (mIsNotExit) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Arrays.fill(buf, (byte) 0);

            //心跳发送逻辑
            switch (peer_task_state) {
                case GET_PEER_INFO:
                    if (peer_udp_server_state) {
                        size = mPeerLink.heartbeat(buf, 0x00, 0);
                        ret = peer_udp_send_to_server(peer_udp_fd, buf, size);
                        if (ret >= 0)
                            udp_heartbeat_cnt++;
                    }
                    break;
                case PEER_UDP_CONNECT:
                    if (is_handshake_ok()) {
                        size = mPeerLink.peer_heartbeat(buf);
                        ret = peer_udp_send_to_peer(peer_udp_fd, buf, size);
                        if (ret >= 0)
                            peer_heartbeat_cnt++;
                    }
                    break;
                case PEER_TCP_CONNECT:
                    if (peer_connect_state && (!is_handshake_ok())) {
                        size = mPeerLink.heartbeat(buf, 0x00, 1);
                        ret = peer_send(peer_tcp_fd, buf, size);
                        if (ret >= 0)
                            tcp_heartbeat_cnt++;
                    }

                    if (is_handshake_ok()) {
                        size = mPeerLink.peer_heartbeat(buf);
                        ret = peer_send(peer_tcp_fd, buf, size);
                        if (ret >= 0)
                            peer_heartbeat_cnt++;
                    }
                    break;
                default:
                    break;
            }

            peer_link_check();
        }
    }

    void peer_tcp_recv_thread() {
        byte[] recBuf = new byte[BUFF_SIZE];
        while (mIsNotExit) {
            if (peer_tcp_fd <= 0) {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            }

            try {
                int recLen = mTcpSocket.getInputStream().read(recBuf);
                if (MLog.isDebug) {
                    String datastr = "TCP read:" + DataTools.bytesToHexString(recBuf, 0, recLen);
                    MLog.d(TAG, datastr);
                }
                if(recLen > 0) {
                    receiveTcpData(recBuf, recLen);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void paraseUdpData(byte[] receiveData, int dataLen) {
        int version = receiveData[0] & 0xFF;
        if ((version != 0x01)) {
            MLog.e("XEE", "***不支持的协议版本*：" + version);//目前中转和WEB端会发01或80版本
            //readLock.unlock();
            return;
        }
        if (dataLen < 3) {
            saveUdpReceivaData = new byte[dataLen];
            System.arraycopy(receiveData, 0, saveUdpReceivaData, 0, dataLen);
            isUdpFristPackage = false;
            saveUdpReceiveDataLen = dataLen;
            MLog.d(TAG, "发送的字节长度小于4个");
            //readLock.unlock();
            return;
        }
        int pkgLen = DataTools.parseShortFromArrayAsLittle(receiveData, 1) + 3;//包的长度不包括头三个字节（协议及长度） 因此要加上
        if (pkgLen <= dataLen) {//1个或以上的包数据
            if (pkgLen != dataLen) { //如果这个包的数据大于一个包 即1个以上的包数据 则
                byte[] packageBytes = new byte[pkgLen];
                System.arraycopy(receiveData, 0, packageBytes, 0, pkgLen);
                mPeerLink.peer_msg_recv(packageBytes, pkgLen);
                int len = dataLen - pkgLen;
                byte[] newTempData = new byte[len];
                System.arraycopy(receiveData, pkgLen, newTempData, 0, len);
                paraseUdpData(newTempData, len);
                MLog.d(TAG, "---1个以上的包数据*");
            } else {
                mPeerLink.peer_msg_recv(receiveData, dataLen);
                isUdpFristPackage = true;
                //readLock.unlock();
            }
        } else {
            saveUdpReceivaData = new byte[dataLen];
            System.arraycopy(receiveData, 0, saveUdpReceivaData, 0, dataLen);
            saveUdpReceiveDataLen = dataLen;
            isUdpFristPackage = false;
            MLog.d(TAG, "不是一个完整的包，开始等待拼包:" + DataTools.bytesToHexString(saveUdpReceivaData, 0, saveUdpReceiveDataLen));
            // readLock.unlock();
        }
    }


    void receiveUdpData(byte[] receiveData, int dataLen) {
        if(dataLen <= 0) {
            return;
        }
        udpReadLock.lock();
        try {
            if (isUdpFristPackage) {
                paraseUdpData(receiveData, dataLen);
            } else {
                int newDataLen = saveUdpReceiveDataLen + dataLen;
                byte[] tempData = new byte[newDataLen];
                System.arraycopy(saveUdpReceivaData, 0, tempData, 0, saveUdpReceiveDataLen);
                System.arraycopy(receiveData, 0, tempData, saveUdpReceiveDataLen, dataLen);
                MLog.d(TAG, "---开始拼包:" +tempData.length);//+ DataTools.bytesToHexString(tempData, 0 , tempData.length));
                paraseUdpData(tempData, newDataLen);
            }
        } finally {
            udpReadLock.unlock();
        }
    }


    void receiveTcpData(byte[] receiveData, int dataLen) {
        tcpReadLock.lock();
        try {
            if (isTcpFristPackage) {
                paraseTcpData(receiveData, dataLen);
            } else {
                int newDataLen = saveTcpReceiveDataLen + dataLen;
                byte[] tempData = new byte[newDataLen];
                System.arraycopy(saveTcpReceivaData, 0, tempData, 0, saveTcpReceiveDataLen);
                System.arraycopy(receiveData, 0, tempData, saveTcpReceiveDataLen, dataLen);
                MLog.d(TAG, "---开始拼包:" +tempData.length);// + DataTools.bytesToHexString(tempData, 0 , tempData.length));
                paraseTcpData(tempData, newDataLen);
            }
        } finally {
            tcpReadLock.unlock();
        }
    }

    void paraseTcpData(byte[] receiveData, int dataLen) {
        int version = receiveData[0] & 0xFF;
        if ((version != 0x01)) {
            MLog.e("XEE", "***不支持的协议版本*：" + version);//目前中转和WEB端会发01或80版本
            //readLock.unlock();
            return;
        }
        if (dataLen < 3) {
            saveTcpReceivaData = new byte[dataLen];
            System.arraycopy(receiveData, 0, saveTcpReceivaData, 0, dataLen);
            isTcpFristPackage = false;
            saveTcpReceiveDataLen = dataLen;
            MLog.d(TAG, "发送的字节长度小于4个");
            //readLock.unlock();
            return;
        }
        int pkgLen = DataTools.parseShortFromArrayAsLittle(receiveData, 1) + 3;//包的长度不包括头三个字节（协议及长度） 因此要加上
        if (pkgLen <= dataLen) {//1个或以上的包数据
            if (pkgLen != dataLen) { //如果这个包的数据大于一个包 即1个以上的包数据 则
                byte[] packageBytes = new byte[pkgLen];
                System.arraycopy(receiveData, 0, packageBytes, 0, pkgLen);
                mPeerLink.peer_msg_recv(packageBytes, pkgLen);
                int len = dataLen - pkgLen;
                byte[] newTempData = new byte[len];
                System.arraycopy(receiveData, pkgLen, newTempData, 0, len);
                paraseUdpData(newTempData, len);
                MLog.d(TAG, "---1个以上的包数据*");
            } else {
                mPeerLink.peer_msg_recv(receiveData, dataLen);
                isTcpFristPackage = true;
                //readLock.unlock();
            }
        } else {
            saveTcpReceivaData = new byte[dataLen];
            System.arraycopy(receiveData, 0, saveTcpReceivaData, 0, dataLen);
            saveTcpReceiveDataLen = dataLen;
            isTcpFristPackage = false;
            MLog.d(TAG, "不是一个完整的包，开始等待拼包:" + DataTools.bytesToHexString(saveUdpReceivaData, 0, saveTcpReceiveDataLen));
            // readLock.unlock();
        }
    }

    void peer_udp_recv_thread() {
        byte[] recBuf = new byte[BUFF_SIZE];
        DatagramPacket receiveDp = new DatagramPacket(recBuf, BUFF_SIZE);
        while (mIsNotExit) {
            if (peer_udp_fd <= 0) {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            }

            try {
                mUdpSocket.receive(receiveDp);
                if (MLog.isDebug) {
                   // String temp = "UDP receive:" + DataTools.bytesToHexString(receiveDp.getData(), 0, receiveDp.getLength());
                   // MLog.d(TAG, temp);
                }
                receiveUdpData(receiveDp.getData(), receiveDp.getLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void peer_task_thread() {
        while (mIsNotExit) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            peer_task();
        }
    }

    void timer_task() {
        while (mIsNotExit) {
            try {
                Thread.sleep(5);
            } catch (Exception e) {
                e.printStackTrace();
            }

            SendQueue.getInstance().run();
            RecvQueue.getInstance().run();
        }
    }

    void peer_init(Messenger clent, Context context) {
        mClient = clent;
        mContext = context;
        if(mIsNotExit) {
            return;
        }
        mIsNotExit = true;
        mPeerLink = new PeerLink();
        new Thread() {
            @Override
            public void run() {
                peer_udp_recv_thread();
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                peer_task_thread();
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                peer_udp_send_thread();
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                peer_tcp_send_thread();
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                peer_heartbeat_thread();
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                peer_tcp_recv_thread();
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                timer_task();
            }
        }.start();
        SendQueue.getInstance().set_output(this);
        RecvQueue.getInstance().set_output(this);
    }

    void peer_state_clear() {
        tcp_server_connect_times = 0;
        peer_mode = PEER_UDP;
        peer_task_state = GET_MY_TASK;
        peer_udp_server_state = false;
        peer_connect_state = false;
        get_peer_info_state = 0;
        request_peer_test_state = 0;
        peer_tcp_task_state = 0;
        peer_request_config_state = 0;
        local_config_state = 0;
        peer_config_state = 0;
        peer_udp_reconnect_times = 0;
        peer_task_timeout = 0;
        peer_task_cnt = 0;
        peer_heartbeat_cnt = 0;
        udp_heartbeat_cnt = 0;
        tcp_heartbeat_cnt = 0;
        ip_limit_flag = 0;
        linkb_type = (byte) 0xB0;
        close_fd(peer_udp_fd);
        close_fd(peer_tcp_fd);
        peer_handshake_request = false;
        peer_handshake_ack = false;
        mUdpIp = null;
        mUdpPort = 0;
        car_volt = 0;
        get_tcp_server_info_state = 0;
        reset_arm = false;
        SendQueue.getInstance().clear();
        RecvQueue.getInstance().clear();
    }

    void peer_task() {
        int page = 0;
        int mode = 0;
        MLog.d(TAG,"peer_task =" + peer_task_state);
        switch (peer_task_state) {
            case GET_MY_TASK:
                if(mIsNotExit) {
                    mTaskInfo = ConfigUtil.getMyTask(mSerialNo);
                    if(mTaskInfo != null) {
                        peer_task_state = GET_PEER_INFO;
                    } else {
                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case GET_PEER_INFO:
                get_peer_info();
                break;
            case PEER_UDP_CONNECT:
                peer_link_udp();
                break;
            case GET_SERVER_LIST:
                get_server_list_info();
                break;
            case PEER_TCP_CONNECT:
                peer_link_tcp();
                break;
            default:
                break;
        }
    }

    void get_server_list_info() {
        int ret = -1;
        int size = 0;
        byte[] payload = new byte[512];

        //通知获取C端与服务器间速率
        if(!peer_get_all_server_td_flag){
            size = mPeerLink.send_udp_pass_msg(payload, (byte) 0x01, null, 0);
            peer_udp_send_to_server(peer_udp_fd, payload, size);
        }

        if(!get_all_server_td_flag) {
            mServerInfos = ConfigUtil.request_server_list(mContext);
            if(mServerInfos != null){
                get_all_server_speed(mServerInfos);
                get_all_server_td_flag = true;
                peer_task_timeout = 0;
                get_tcp_server_info_state = 1;
            }
        }

        if(get_all_server_td_flag && (!request_server_td_finish)) {
            peer_task_timeout++;   //前后兼容考虑，若C端一直没有回复，则认为C端没有此功能，以B端速率为主

            if((peer_get_all_server_td_flag) || (peer_task_timeout >= 35)) {
                peer_task_timeout = 0;
                sort_tcp_server_by_speed();
                request_server_td_finish = true;
                //set_page(SERVER_PAGE);
                select_page();
            }
        }

        if(request_select_server_flag) {
            request_select_server_flag = false;
            set_select_server(select_server_index);
        }

        if(get_tcp_server_info_state == 3) {
            peer_task_state = GET_PEER_INFO;
        }
    }

    void sort_tcp_server_by_speed() {
        Collections.sort(mServerInfos, new Comparator<ServerInfos>() {
            @Override
            public int compare(ServerInfos o1, ServerInfos o2) {
                return (int)((o1.ctd + o1.btd) - (o2.ctd + o2.btd));
            }
        });
    }

    void select_page() {
        if(mServerInfos == null) {
            return;
        }
        Intent intent = new Intent("SHOW_SELECT_SERVER_LIST");
        mContext.sendBroadcast(intent);
        //LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    int set_select_server(int index) {
        if(index >= mServerInfos.size()) {
            return -1;
        }

        tcp_server_ip = mServerInfos.get(index).domain;
        tcp_server_port = Integer.parseInt(mServerInfos.get(index).port);

        String temp = String.format("select server, name=%s, ip=%s, port=%d", mServerInfos.get(index).name, tcp_server_ip, tcp_server_port);
        MLog.d(TAG, temp);

        if(!switch_btn_state) {
            peer_tcp_task_clear();
            get_tcp_server_info_state = 3;
            peer_task_state = GET_PEER_INFO;
        }else{
            tcp_notify_test_end_state = 1;
            switch_btn_state = false;
        }
        return 0;
    }

    void get_peer_info() {
        int fd = -1;
        int size = 0;
        int ret = 0;
        int offset = 0;
        byte[] buf = new byte[4096];
        // byte[] data = new byte[4096];

        peer_task_cnt++;
        peer_task_timeout++;


        if (!peer_udp_server_state) {
            if(TextUtils.isEmpty(mUdpIp) || mUdpPort == 0) {
                ConfigUtil.getUdpServerInfo(mSerialNo);
            }
            if(!TextUtils.isEmpty(mUdpIp)) {
                try {
                    udp_serv_addr = new InetSocketAddress(getByName(mUdpIp), mUdpPort);
                    peer_udp_server_state = true;
                    peer_udp_fd = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if ((peer_udp_fd > 0) && (get_peer_info_state == 0)) {
            //memset(buf, 0, sizeof(buf));
            size = mPeerLink.connect_link_by_b(buf, 0);
            if (size > 0) {
                peer_udp_send_to_server(peer_udp_fd, buf, size);
                get_peer_info_state = 1;
                peer_task_cnt = 0;
            }
        }

        if(get_peer_info_state == 2) {
            if(request_peer_test_state == 0) {
                size = mPeerLink.send_test_request(buf, peer_mode, tcp_server_ip, tcp_server_port);
                if(size > 0) {
                    peer_udp_send_to_server(peer_udp_fd, buf, size);
                    request_peer_test_state = 1;
                    peer_task_cnt = 0;
                }
            }
        }


        if (peer_task_cnt >= 3) {
            MLog.d(TAG,"error. udp server no response, state=" + get_peer_info_state);
            if (get_peer_info_state == 1) {
                peer_task_cnt = 0;
                get_peer_info_state = 0;
            } else if(request_peer_test_state == 1) {
                request_peer_test_state = 0;
                peer_task_cnt = 0;
            }
        }

        if (peer_task_timeout >= 60) {
            MLog.d(TAG,"get peer info timeout");
            peer_state_clear();
        }

        if(request_peer_test_state == 2) {
            peer_task_cnt = 0;
            peer_task_timeout = 0;
            if(peer_mode == PEER_UDP) {
                peer_task_state = PEER_UDP_CONNECT;
            }else{
                peer_task_state = PEER_TCP_CONNECT;
            }
        }
    }

    void clear_get_info_state() {
        MLog.d(TAG,"clear_get_info_state");
        get_peer_info_state = 0;
        request_peer_test_state = 0;
        close_fd(peer_udp_fd);
        peer_udp_server_state = false;
        peer_connect_state = false;
        peer_handshake_request = false;
        peer_handshake_ack = false;
        peer_request_config_state = 0;
        peer_heartbeat_cnt = 0;
        udp_heartbeat_cnt = 0;
        local_config_state = 0;
        peer_config_state = 0;
        peer_task_cnt = 0;
        linkb_type = (byte) 0xB0;
        reset_arm = false;
    }

    void peer_link_udp() {
        peer_task_cnt++;
        peer_task_timeout++;

        if (!peer_connect_state) {
            try {
                udp_peer_addr = new InetSocketAddress(getByName(peer_ip), peer_port);
                peer_connect_state = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
       // response_server_speed(0);

        request_m7_config();

        if (peer_handshake_ack && peer_handshake_request) {
            //B端和C端握手成功
            peer_task_cnt = 0;
            peer_task_timeout = 0;
            peer_udp_reconnect_times = 0;
        }

        if (peer_task_cnt > 5) {
            MLog.d(TAG,"peer_task_cnt = " + peer_task_cnt);
            peer_task_cnt = 0;
            if(peer_udp_reconnect_times >= 1) {
                get_all_server_td_flag = false;
                peer_get_all_server_td_flag = false;
                request_server_td_finish = false;
                get_tcp_server_info_state = 0;
                peer_task_state = GET_SERVER_LIST;
                peer_mode = PEER_TCP;
                peer_task_timeout = 0;
                peer_task_cnt = 0;
                peer_heartbeat_cnt = 0;
                udp_heartbeat_cnt = 0;
                tcp_heartbeat_cnt = 0;
                peer_connect_state = false;
                peer_tcp_task_state = 0;
                request_peer_test_state = 0;
                peer_request_config_state = 0;
                local_config_state = 0;
                peer_config_state = 0;
                peer_handshake_request = false;
                peer_handshake_ack = false;
            }else{
                peer_connect_state = false;
                peer_udp_reconnect_times++;
                clear_get_info_state();
                peer_task_state = GET_PEER_INFO;
            }
        }
    }

    void close_fd(int fd) {
        try {
            if (fd == 2) {
                if (mTcpSocket != null) {
                    mTcpSocket.close();
                    mTcpSocket = null;
                }
                peer_tcp_fd = 0;
            } else if (fd == 1) {
                if(mUdpSocket != null) {
                    mUdpSocket.close();
                    mUdpSocket = null;
                }
                peer_udp_fd = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void peer_tcp_task_clear() {
        peer_task_cnt = 0;
        peer_heartbeat_cnt = 0;
        tcp_heartbeat_cnt = 0;
        linkb_type = (byte) 0xB0;
        close_fd(peer_tcp_fd);
        peer_connect_state = false;
        peer_tcp_task_state = 0;
        local_config_state = 0;
        peer_config_state = 0;
        peer_request_config_state = 0;
        peer_handshake_request = false;
        peer_handshake_ack = false;
        tcp_server_connect_times = 0;
        reset_arm = false;
        tcp_notify_test_end_state = 0;
        //TCP断开重连后重新通知
        clear_get_info_state();
        peer_task_state = GET_PEER_INFO;
    }

    void peer_link_tcp() {
        int fd = -1;
        byte[] buf = new byte[512];
        int ret = -1;
        byte[] payload = new byte[512];
        int size = 0;
        int page = 0;

        peer_task_timeout++;
        peer_task_cnt++;

        //PRINT_DEBUG("peer_tcp_task_state=%d, local_config_state=%d, peer_config_state=%d", peer_tcp_task_state, local_config_state, peer_config_state);

        if (!peer_connect_state) {
            tcp_server_connect_times++;
            try {
                if (mTcpSocket != null) {
                    mTcpSocket.close();
                }
                MLog.d(TAG, "***connect***:" + tcp_server_ip + ",port=" + tcp_server_port);
                Socket socket = new Socket();
                socket.setTcpNoDelay(true);
                InetSocketAddress address = new InetSocketAddress(getByName(tcp_server_ip), tcp_server_port);
                socket.connect(address);
                mTcpSocket = socket;
                isTcpFristPackage = true;
                fd = 2;
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (fd > 0) {
                tcp_server_connect_times = 0;
                peer_connect_state = true;
                peer_tcp_fd = fd;
            }
        }

        if ((peer_tcp_fd > 0) && (peer_tcp_task_state == 0)) {
            size = mPeerLink.connect_link_by_b(buf, 1);
            if (size > 0) {
                peer_send(peer_tcp_fd, buf, size);
                peer_tcp_task_state = 1;
                peer_task_cnt = 0;
            }
        }

        if (peer_tcp_task_state == 3)   //匹配成功
        {
            request_m7_config();
        }

        if (peer_handshake_ack && peer_handshake_request)  //B端和C端握手成功
        {
            peer_task_cnt = 0;
            if(switch_btn_state && (!request_server_td_finish)) {

            }else{
                peer_task_timeout = 0;
            }
        }

        if(switch_btn_state) {
            //通知获取C端与服务器间速率
            if(!peer_get_all_server_td_flag) {
                byte[] data = new byte[1];
                data[0]= 0x19;
                size = mPeerLink.pack_peer_frame(payload, PacketQueue.PEER_ID, 0x07, data, 1);
                peer_send(peer_tcp_fd, payload, size);
            }


            if(!get_all_server_td_flag) {
                mServerInfos = ConfigUtil.request_server_list(mContext);
                if(mServerInfos != null){
                    get_all_server_speed(mServerInfos);
                    get_all_server_td_flag = true;
                    peer_task_timeout = 0;
                }
            }else{
                if((peer_get_all_server_td_flag) || (peer_task_timeout >= 40)) {
                    peer_task_timeout = 0;
                    sort_tcp_server_by_speed();
                    request_server_td_finish = true;
                }
            }

            if(request_select_server_flag) {
                request_select_server_flag = false;
                set_select_server(select_server_index);
            }
        }

        if ((peer_task_cnt >= 5) && (peer_tcp_task_state == 1)) {
            peer_tcp_task_state = 0;
        }

        if(tcp_notify_test_end_state == 1) {
            //通知C端断开中转服务器
            size = pack_notify_test_end(buf);
            if(size > 0) {
                peer_send(peer_tcp_fd, buf, size);
            }
        }

        if ((peer_task_timeout > 60) || (tcp_server_connect_times >= 5)) {
            //MLog.d(TAG,"tcp link timeout");
            //LOG_WRITE(LOG_DEBUG, "Tcp link timeout, peer_task_timeout=%d, tcp_server_connect_times=%d", peer_task_timeout, tcp_server_connect_times);
            peer_state_clear();
        }
    }

    //通知切换中转服务器
    int pack_notify_test_end(byte[] payload) {
        byte[] data = new byte[512];
        int offset = 0;
        int size = 0;
        data[offset++] = 0x18;
        return mPeerLink.pack_peer_frame(payload, PacketQueue.PEER_ID, 0x07, data, offset);
    }

    void notify_switch_server() {
        byte[] payload = new byte[512];
        int size = 0;

        size = mPeerLink.send_peer_ack(payload, PacketQueue.PEER_ID, 0x07, 0x18, 0x01);
        peer_send(peer_tcp_fd, payload, size);
        //不考虑B端是否收到应答，即使未收到应答，会因为心跳超时，断开连接
        peer_tcp_task_clear();
    }

    void set_peer_info_result(String ip, String port, int result) {
        if (result < 0) {
            if (result == -2) {
                ip_limit_flag = 1;
            }

            clear_get_info_state();
        } else {
            MLog.d(TAG,"local ip=" + ip + ",port=" + port);
            local_ip = ip;
            local_port = Integer.parseInt(port);
            get_peer_info_state = 2;
            peer_task_cnt = 0;
        }
    }

    void set_request_peer_test_result(String ip, String port, int result) {
        //匹配不成功，则等待对方上线
        if(result == 0x01) {
            MLog.d(TAG,"peer ip=" + ip + ",port" + port);
            peer_ip = ip;
            peer_port = Integer.parseInt(port);
            request_peer_test_state = 2;
            peer_task_cnt = 0;
            peer_task_timeout = 0;

            if(peer_mode == PEER_UDP) {
                peer_task_state = PEER_UDP_CONNECT;
            }else{
                peer_task_timeout = 0;
                peer_task_cnt = 0;
                peer_task_state = PEER_TCP_CONNECT;
            }
        }else if((result >= 6) && (result <= 9)) {
            //任务已失效，需重新获取任务
            clear_get_info_state();
            peer_task_state = GET_MY_TASK;
        }
    }

    //type为success后下一阶段值
    void request_tcp_connect_result(int result, int type) {
        byte[] payload = new byte[512];
        int size = 0;

        if (result < 0) {
            peer_tcp_task_state = 0;
            peer_task_cnt = 0;
        } else {
            peer_tcp_task_state = type;
            if (type == 3) {
                size = mPeerLink.send_peer_ack(payload, (byte) 0x00, 0x03, 0x00, 0x01);
                peer_send(peer_tcp_fd, payload, size);
            }
        }
    }

    void send_handshake() {
        byte[] payload = new byte[512];
        int size = 0;

        size = mPeerLink.pack_handshake(payload, PacketQueue.PEER_ID);
        if (peer_mode == PEER_UDP) {
            peer_udp_send_to_peer(peer_udp_fd, payload, size);
        } else {
            peer_send(peer_tcp_fd, payload, size);
        }
    }

    void send_notify_config(int result) {
        byte[] payload = new byte[512];
        int size = 0;
       // size = mPeerLink.pack_notify_config(payload, PacketQueue.PEER_ID, (byte) result);
        if (peer_mode == PEER_UDP) {
            peer_udp_send_to_peer(peer_udp_fd, payload, size);
        } else {
            peer_send(peer_tcp_fd, payload, size);
        }
    }


    void request_m7_config() {
        int ret = 0;

        if (!peer_handshake_ack) {
            send_handshake();
        }
/*
        if (peer_handshake_ack && peer_handshake_request && (peer_request_config_state == PEER_HANDSHAKE)) {
            peer_request_config_state = PEER_CONFIG_GET_TASK;
        }

        if(peer_request_config_state == PEER_CONFIG_GET_TASK) {
            mTaskInfo = ConfigUtils.getMyTask();
            if(mTaskInfo != null) {
                if(mTaskInfo.is_auto == 0) {
                    peer_request_config_state = PEER_CONFIG_REQUEST;
                }else{
                    peer_request_config_state = PEER_CONFIG_SMART_XML;//PEER_CONFIG_SMART_TASK_FIRE;
                    Message msg = Message.obtain();
                    msg.what = MsgConstants.WAIT_FIRE_PAGE;
                    sendClient(msg);
                    // set_page(WAIT_FIRE_PAGE);
                }
            }
        }

        if(peer_request_config_state == PEER_CONFIG_SMART_XML) {
            //智能任务识别
            ret = McuControl.getInstance().smart_find_can();
            if(ret >= 0) {
                peer_request_config_state = PEER_CONFIG_UPLOAD_XML;
            }
        }

        if(peer_request_config_state == PEER_CONFIG_UPLOAD_XML) {
            ret = ConfigUtils.uploadCarConfig();
            if(ret >= 0) {
                peer_request_config_state = PEER_CONFIG_REQUEST;
            }else{
                MLog.e(TAG,"upload_car_config xml error");
            }
        }

        if (peer_request_config_state == PEER_CONFIG_REQUEST) {

            if(!reset_arm) {
                ConfigUtils.xxResetPower();
                McuControl.getInstance().set_mcu_mode(0);
                reset_arm = true;
                McuControl.getInstance().send_jump_link(false);
            }

            if (ConfigUtils.getVehicleConfigInfo(mContext)) {
                mConfig = null;
                is_auto_baud();
            } else {
                Message msg = Message.obtain();
                // msg.obj = mContext.getString(R.string.not_download_sn);
                msg.what = MsgConstants.SHOW_STATUS;
                msg.arg1 = 7;
                sendClient(msg);
            }
        }

        if (peer_request_config_state == PEER_CONFIG_AUTO) {
            request_auto_baud();
        }

        if (peer_request_config_state == PEER_CONFIG_CAR) {
            request_config_car();
        }

        if (peer_request_config_state == PEER_CONFIG_NOTIFY) {
            send_notify_config(local_config_state);
        }*/
    }

    void get_all_server_speed(ArrayList<ServerInfos> serverInfos) {
        if (serverInfos == null || serverInfos.size() == 0) {
            return;
        }
       // ArrayList<Long> times = new ArrayList<>();
        byte[] sendBuf = new byte[128];
        byte[] data = new byte[1];
        data[0] = 1;
        int sendLen = mPeerLink.pack_peer_frame(sendBuf, 0x00, 0x09, data, 1);

        byte[] rectData = new byte[128];
        int sum = 0;
        DataInputStream inputStream = null;
        DataOutputStream outputStream = null;
        for (int i = 0; i < serverInfos.size(); i++) {
            boolean getTime = false;
            Socket socket = null;
            ServerInfos serverInfo = serverInfos.get(i);
            try {
                int port = Integer.parseInt(serverInfo.port);
                InetAddress address = InetAddress.getByName(serverInfo.domain);
                socket = new Socket(address, port);
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
                socket.setSoTimeout(500);
                Long startTime = System.currentTimeMillis();
                outputStream.write(sendBuf, 0, sendLen);
                MLog.d(TAG, "testServerSpeed:" + serverInfo.domain + ",port=" + serverInfo.port);
                outputStream.flush();
                sum = inputStream.read(rectData);
                if (sum > 7) {
                    int id = rectData[7] & 0xFF;
                    if (id == 0x89) {
                        getTime = true;
                       // times.add(System.currentTimeMillis() - startTime);
                        serverInfo.btd = System.currentTimeMillis() - startTime;
                        MLog.d(TAG, "testServerSpeed:time=" + serverInfo.btd);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception e) {
                }
            }
            if (!getTime) {
                serverInfo.btd = 500L;
            }
        }
    }


    @Override
    public int sendData(byte[] buf, int size) {
        // MLog.e(TAG, "PeerTask 发送数据:" + DataTools.bytesToHexString(buf, 0, size));
        return peer_send_to_peer(buf, size);
    }

    public void add_send_bytes(long value) {
        send_bytes += value;
    }

    void clear_send_recv_bytes() {
        send_bytes = 0;
        recv_bytes = 0;
    }

    public void add_recv_bytes(long value) {
        recv_bytes += value;
    }


    public void release() {
        MLog.d(TAG, "release");
        if(mIsNotExit) {
            mIsNotExit = false;
        }
        reconnect_status = false;
        peer_state_clear();
        PacketQueue.getMcuSendQueue().clear();
        PacketQueue.getPeerTcpQueue().clear();
    }

    public TaskInfo getTaskInfo() {
        return mTaskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.mTaskInfo = taskInfo;
    }

    public void joinSendQueue(byte[] buf, int pos, int length) {
        if(!is_match_ok()) {
            return;
        }
        mPeerTask.add_send_bytes(length - pos);
        int peer_mode = get_peer_mode();
        //
        if (peer_mode == 0) {
            SendQueue sendQueue = SendQueue.getInstance();
            sendQueue.send(buf, length, (byte) 0);
        } else {
            PacketQueue packetQueue = PacketQueue.getPeerTcpQueue();
            byte[] temp = new byte[length + 256];
            int size = Packet.getTestData(temp, PacketQueue.PEER_ID, buf, length, 0);
            packetQueue.insert(temp, pos, size);
           // if((pos + length) > 6) {
           //     cur_seq_index = buf[6];
           // }
        }
    }

    public ArrayList<ServerInfos> getServerInfos() {
        return mServerInfos;
    }

    public void set_select_server_index(int index) {
        request_select_server_flag = true;
        select_server_index = index;
    }

    public void setUdpIpAndPort(String ip, int port) {
        mUdpIp = ip;
        mUdpPort = port;
    }

    public long get_tart_diagnose_time() {
        return start_diagnose_time;
    }

    public int getNet_time() {
        return net_time;
    }

    public void setNet_time(int net_time) {
        this.net_time = net_time;
    }

    public long getSend_bytes() {
        return send_bytes;
    }

    public long getRecv_bytes() {
        return recv_bytes;
    }

    public void set_swicth_btn_state(boolean value) {
        reconnect_status = true;
        switch_btn_state = value;
        if(switch_btn_state) {
            get_all_server_td_flag = false;
            peer_get_all_server_td_flag = false;
            request_server_td_finish = false;
        }
    }

    public boolean get_request_server_td_finish() {
        return request_server_td_finish;
    }

    public int get_select_server_index() {
        return select_server_index;
    }

    public void set_reconnect_status() {
        reconnect_status = true;
    }
}
