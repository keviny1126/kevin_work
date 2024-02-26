package com.cnlaunch.physics.smartlink;

import com.cnlaunch.physics.utils.DataTools;
import com.cnlaunch.physics.utils.MLog;

import java.util.ArrayList;

import static com.cnlaunch.physics.smartlink.PeerTask.GET_PEER_INFO;
import static com.cnlaunch.physics.smartlink.PeerTask.PEER_CONFIG_ANSWER;
import static com.cnlaunch.physics.smartlink.PeerTask.PEER_TCP_CONNECT;
import static com.cnlaunch.physics.smartlink.PeerTask.PEER_UDP;

public class PeerLink {
    public static final String TAG = "PeerLink";
    final int MAX_TTL = 500;     //500ms
    static short peer_frame_count = 0;  //消息计数器
    long ping_triptime = 0;    //心跳发送时间
    boolean ping_flag = false;   //心跳发送标志
    PeerTask mPeerTask;

    public PeerLink() {
        mPeerTask = PeerTask.getInstance();
    }

    public static int pack_peer_frame(byte[] packageData, int dest, int id, byte[] data, int dataLen) {
        long time = System.currentTimeMillis() / 1000;
        int length = dataLen + 13;
        int packagelength = length - 3;//不需要协议号 及包长度的字节 即:[目标地址 + 源地址 + 计数器 + 业务ID + 数据 + 时间戳 + 校验] 总长度，小端模式

        //byte[] packageData = new byte[length];
        int index = 0;
        packageData[index++] = (byte) (0x01 & 0xFF);
        packageData[index++] = (byte) (packagelength & 0xFF);//小端模式
        packageData[index++] = (byte) (packagelength >> 8 & 0xFF);//小端模式
        packageData[index++] = (byte) dest;//目标地址 0x00:中转服务器  0x01:C端  0x02: B端
        packageData[index++] = 0x02;//源地址  0x00:中转服务器  0x01:C端  0x02: B端
        packageData[index++] = (byte) (peer_frame_count & 0xFF);//计数器 小端模式
        packageData[index++] = (byte) (peer_frame_count >> 8 & 0xFF);//计数器 小端模式
        packageData[index++] = (byte) (id & 0xFF);
        //   packageData[index++] = (byte) (businessId >> 8 & 0xFF);
        System.arraycopy(data, 0, packageData, index, dataLen);
        index += dataLen;

        packageData[index++] = (byte) (time & 0xFF);
        packageData[index++] = (byte) (time >> 8 & 0xFF);
        packageData[index++] = (byte) (time >> 16 & 0xFF);
        packageData[index++] = (byte) (time >> 24 & 0xFF);

        // [协议版本 + 包长度 + 目标地址 + 源地址 + 计数器 + 业务ID + 数据 + 时间戳] 异或运算
        int crc= DataTools.getRemoteCrcByDataLength(packageData,length -1);
        packageData[index++] = (byte) (crc & 0xFF);
        return index;
    }

    //type=0为UPD打洞协议，type=1为TCP中转协议
    int heartbeat(byte[] payload, int dest, int type) {
        byte[] data = new byte[512];
        short offset = 0;

        if (type == 0) {
            data[offset++] = 12;
            String sn = PeerTask.getInstance().mSerialNo;
            System.arraycopy(sn.getBytes(), 0, data, offset, sn.getBytes().length);
            // memcpy(&data[offset], global_sn, 12);
            offset += 12;
        }

        data[offset++] = 0x01;

        if (!mPeerTask.is_handshake_ok()) {
            ping_triptime = System.currentTimeMillis();
            ping_flag = true;
        }

        return pack_peer_frame(payload, dest, (byte) 0x00, data, offset);
    }

    //B端与C端心跳包
    int peer_heartbeat(byte[] payload) {
        byte[] data = new byte[1];
        data[0] = 0x15;

        ping_triptime = System.currentTimeMillis();
        ping_flag = true;
        return pack_peer_frame(payload, PacketQueue.PEER_ID, (byte) 0x07, data, 1);
    }
    

    int pack_handshake(byte[] payload, byte dest) {
        byte[] buf = new byte[20];
        short offset = 0;
        buf[offset++] = 0x12;
        buf[offset++] = (byte) 0xB1;    //C端为0xC0， 硬B端为0xB0， 软B端为0xB1
        buf[offset++] = 00;

        return pack_peer_frame(payload, dest, (byte) 0x07, buf, offset);
    }


    int pack_doip_notify(byte[] payload, byte type) {
        byte[] buf = new byte[2];
        int offset = 0;

        if (payload == null) {
            MLog.e(TAG,"error parameter");
            return -1;
        }

        buf[offset++] = 0x17;
        buf[offset++] = type;
        return pack_peer_frame(payload, PacketQueue.PEER_ID, (byte) 0x07, buf, offset);
    }

    int send_stop_test(byte[] payload, byte dest) {
        byte[] data = new byte[1];
        data[0] = 0x0D;

        return pack_peer_frame(payload, dest, (byte) 0x05, data, 1);
    }

    int send_udp_pass_msg(byte[] payload, byte cmd, byte[] data, int size) {
        byte[] buf = new byte[4096];
        int offset = 0;

        if (payload == null) {
            MLog.d(TAG,"error parameter");
            return -1;
        }

        buf[offset++] = 12;
        // memcpy(&buf[offset], global_sn, 12);
        System.arraycopy(PeerTask.getInstance().mSerialNo.getBytes(), 0, buf, offset, 12);
        offset += 12;
        buf[offset++] = 12;
        String sn = mPeerTask.mTaskInfo.sn;
        System.arraycopy(sn.getBytes(), 0, buf, offset, 12);
        //memcpy(&buf[offset], peer_sn, 12);
        offset += 12;
        DataTools.bigEndShortToByte(buf, offset, (short) (1 + size));
        offset += 2;
        buf[offset++] = cmd;

        if (data != null) {
            System.arraycopy(data, 0, buf, offset, size);
            offset += size;
        }

        return pack_peer_frame(payload, PacketQueue.PEER_ID, (byte) 0x05, buf, offset);
    }

    int send_request_server_speed(byte[] payload) {
        byte[] data = new byte[1];
        data[0] = 0x01;
        return pack_peer_frame(payload, 0x00, 0x09, data, 1);
    }

    int send_peer_ack(byte[] payload, byte dest, int id, int cmd, int result) {
        byte[] data = new byte[2];
        short size = 0;

        if (cmd != 0)  //子命令
        {
            data[0] = (byte) (cmd + 0x80);
            data[1] = (byte) result;
            size = 2;
        } else {
            id += 0x80;
            data[0] = (byte) result;
            size = 1;
        }

        return pack_peer_frame(payload, dest, id, data, size);
    }

    int match_result_ack(byte[] payload) {

        byte[] data = new byte[512];
        short offset = 0;

        data[offset++] = 12;
        System.arraycopy(mPeerTask.mSerialNo.getBytes(), 0, data, offset, 12);
        offset += 12;
        data[offset++] = 0x01;   //成功
        data[offset++] = 12;
        System.arraycopy(mPeerTask.mTaskInfo.sn.getBytes(), 0, data, offset, 12);
        offset += 12;

        return pack_peer_frame(payload, 0x00, 0x84, data, offset);
    }

    void peer_msg_recv(byte[] data, int length) {
        int ret = peer_msg_check(data, length);
        if(ret >= 0) {
            peer_msg_dispatch(data, length);
        }
    }

    int peer_msg_check(byte[] buf, int size) {
        int offset = 3;
        byte dest = 0, src = 0;
        byte crc = 0;

        dest = buf[offset];
        offset++;
        src = buf[offset];
        offset++;

        if (size < 9) {
            MLog.d(TAG,"peer min size =" + size);
            return -1;
        }

        if (dest != 0x02) {
            MLog.d(TAG,"peer dest address error");
            return -1;
        }

        if ((src != 0x00) && (src != 0x01)) {
            MLog.d(TAG,"peer src address error, src=" + src);
            return -1;
        }

        crc = (byte) DataTools.getCrcByData(buf, 0, size - 1);
        // crc = CheckCode::Xor(buf, size - 1);
        if (crc != buf[size - 1]) {
            MLog.d(TAG,"peer msg crc code error, crc");
            return -1;
        }

        return 0;
    }

    void peer_msg_dispatch(byte[] buf, int size) {
        int id = 0;
        byte src = buf[4];
        int length = 0;
        id = buf[7] & 0xFF;
      //  MLog.d(TAG, String.format("peer_msg_dispatch=%x", id));
        switch (id) {
            case 0x07:
                pass_mcu_frame(buf, size);
                break;
            case 0x00:  //对方心跳包
                byte[] payload = new byte[512];
                length = send_peer_ack(payload, src, 0x00, (byte) 0x00, (byte) 0x01);
                mPeerTask.peer_send_to_peer(payload, length);
                break;
            case 0x80:
                heartbeat_recv(buf, size);
                break;
            case 0x81:
                request_connect_recv(buf, size);
                break;
            case 0x03:
            case 0x83:
                match_result(buf, size);
                break;
            case 0x05:
                pass_udp_server_frame(buf, size);
                break;
            //case 0x87:
            //    notify_config_recv(buf, size);
            //    break;
            default:
                break;
        }
    }

    void heartbeat_recv(byte[] buf, int size) {
        byte cmd = 0;
        long ms = System.currentTimeMillis();
        int td = 0;

        cmd = buf[7];

        if (cmd == 0x80) {
            mPeerTask.clear_heartbeat(0);
        } else {
            mPeerTask.clear_heartbeat(1);
        }

        if (mPeerTask.is_handshake_ok() && (cmd == 0x80)) {
            //握手成功,服务器心跳不计算延迟
            return;
        }

        if (ms - ping_triptime > MAX_TTL) {
            td = MAX_TTL;
        } else {
            td = (int) (ms - ping_triptime);
        }

        ping_flag = false;

        set_net_td(td);
    }

    void request_connect_recv(byte[] buf, int size) {

        int task_state = mPeerTask.get_peer_task_state();
        int offset = 0;
        byte state = buf[8];
        byte length = 0;
        String ip = null;
        String port = null;
        offset = 9;
        if (task_state == GET_PEER_INFO) {
            if (state == 0x01) {
                length = buf[offset];
                offset++;
                ip = DataTools.getText(buf, offset, length);
                //memcpy(ip, &buf[offset], length);
                offset += length;
                length = buf[offset];
                offset++;
                port = DataTools.getText(buf, offset, length);

                mPeerTask.set_peer_info_result(ip, port, 0);
            } else if (state == 10) {
                mPeerTask.set_peer_info_result(ip, port, -2);  //IP区域限制
            } else {
                mPeerTask.set_peer_info_result(ip, port, -1);
            }
        } else if (task_state == PEER_TCP_CONNECT) {
            if (state == 0x01) {
                MLog.d(TAG,"tcp connect successs");
                mPeerTask.request_tcp_connect_result(0, 2);
            } else {
                MLog.d(TAG,"tcp connect error");
                mPeerTask.request_tcp_connect_result(-1, 0);
            }
        }
    }

    void match_result(byte[] buf, int size) {
        int task_state = mPeerTask.get_peer_task_state();
        int offset = 0;
        byte state = buf[8];
        byte length = 0;
        String sn;
        String ip;
        String port;
        String server_ip = null;
        int server_port = 0;
        int mode = 0;
        offset = 9;
        MLog.d(TAG,task_state +",match_result state="+state);
        if (task_state == GET_PEER_INFO) {
            if(state == 0x01) {
                length = buf[offset];
                offset++;
                ip = DataTools.getText(buf, offset, length);
                // memcpy(ip, &buf[offset], length);
                offset += length;

                length = buf[offset];
                offset++;
                port = DataTools.getText(buf, offset, length);
                //memcpy(port, &buf[offset], length);
                offset += length;
                mPeerTask.set_request_peer_test_result(ip, port, state);
            } else {
                mPeerTask.set_request_peer_test_result(null, null, state);
            }
        } else if (task_state == PEER_TCP_CONNECT) {
            if (state == 0x01) {
                MLog.d(TAG,"match result successs");
                mPeerTask.request_tcp_connect_result(0, 3);
            } else {
                MLog.d(TAG,"match result error");
                mPeerTask.request_tcp_connect_result(-1, 0);
            }
        }
    }

    void pass_mcu_frame(byte[] buf, int size) {
        byte src = buf[4];
        byte cmd = buf[8];
        int length = size - 1 - 2 - 1 - 1 - 2 - 1 - 1 - 4 - 1;
        int offset = 0;
        long seq = 0;
      //  MLog.d(TAG, "pass_mcu_frame cmd="+cmd);
        int peer_mode = mPeerTask.get_peer_mode();
        int type = 0;
        if (cmd == 0x11) {
            mPeerTask.clear_heartbeat(1);
            mPeerTask.add_recv_bytes(size);
            offset = 9;
            seq = DataTools.bigEndByteToInt(buf, offset);//BigEndian::ByteToInt(&buf[offset]);
            offset += 4;
            type = buf[offset];
            offset++;
            MLog.d("pass_mcu_frame", type + ",seq="+seq);
            if (peer_mode == PEER_UDP) {
                RecvQueue.getInstance().recv(buf, offset, length - 4 - 1, seq, type);
            } else {
                Packet.dispatchPacket(buf, offset, length - 4 - 1, type);
            }
        } else if (cmd == 0x12) {
            offset = 10;
            int vin_size = buf[offset];
            offset ++;
            if(vin_size == 17) {
                String vin = DataTools.getText(buf, offset, 17);
                mPeerTask.mTaskInfo.vin = vin;
            }
            mPeerTask.handshake_request_recv();
        }  else if (cmd == 0x14) {
            mPeerTask.set_peer_config_result(src, buf[9]);
        } else if (cmd == 0x15) {
            byte[] payload = new byte[size + 256];
            length = send_peer_ack(payload, src, 0x07, (byte) 0x15, (byte) 0x01);
            mPeerTask.peer_send_to_peer(payload, length);
        } else if (cmd == 0x16) {
            seq = DataTools.bigEndByteToInt(buf, 9);
            RecvQueue.getInstance().syncRecv(seq);
        } else if (cmd == 0x18) {
            mPeerTask.notify_switch_server();
        } {
            notify_config_recv(buf, size);
        }
    }

    void notify_config_recv(byte[] buf, int size) {
        byte cmd = buf[8];
        short status = buf[9];
        cmd -= 0x80;

        if (cmd == 0x11) {
            SendQueue.getInstance().setAckSeq(buf, 9, size - 5);
        } else if ((cmd == 0x12) && (status == 0x01)) {
            mPeerTask.handshake_ack_recv();//收到對方握手0712
/*            Message msg = Message.obtain();
            msg.what = MsgConstants.UPDATE_STATUS_ICON;
            msg.arg1 = 3;
            mPeerTask.sendHandleMessage(msg);*/
        } else if ((cmd == 0x14) && (status == 0x01)) {
            mPeerTask.set_peer_request_config_state(PEER_CONFIG_ANSWER);
            //mPeerTask.enterDiagnoseFragment();
        } else if (cmd == 0x15) {
            heartbeat_recv(buf, size);
        } else if (cmd == 0x16) {
            SendQueue.getInstance().syncRecv();
        } else if (cmd == 0x18) {
            mPeerTask.peer_tcp_task_clear();
        }else if(cmd == 0x19) {
            get_server_td_response(buf, 9, size);
        }
    }

    void check_triptime() {
        long ms = System.currentTimeMillis();
        int td = 0;

        if ((ms - ping_triptime > MAX_TTL) && ping_flag) {
            // PRINT_DEBUG("timeout");
            ping_flag = false;
            td = MAX_TTL;
            set_net_td(td);
        }
    }

    void set_net_td(int value) {
        mPeerTask.setNet_time(value);
/*        Message msg = Message.obtain();
        msg.what = MsgConstants.UPDATE_NET_SPEED;
        msg.arg1 = value;
        mPeerTask.sendHandleMessage(msg);*/
    }

    void pass_udp_server_frame(byte[] buf, int size) {
        int offset = 8;
        int cmd = 0;
        String sn = null;
        int length = buf[offset];   //发送序列号
        offset++;
        offset += length;
        length =  buf[offset];   //接收序列号
        offset++;
        offset += length;
        length = DataTools.bigEndByteToShort(buf,offset);
        offset +=2;
        cmd = buf[offset] & 0xFF;
        offset++;
        MLog.d(TAG, String.format("pass_udp_server_frame=%x", cmd));
        if(cmd == 0x81) {
            get_server_td_response(buf, offset, size);
        }
    }
    void get_server_td_response(byte[] buf,int offset, int size) {
        int total = 0;
        int speed = 0;
        MLog.d(TAG, "get_server_td_response");
        if(mPeerTask.get_peer_get_all_server_td_flag())
            return ;

       // std::lock_guard<std::mutex> lock(server_list_mutex);
        total = DataTools.bigEndByteToShort(buf, offset);
        offset += 2;
        ArrayList<Integer> times = new ArrayList<>();
        for(int i = 0; i < total; i++) {
            if(offset + 2 <= size) {
                speed = DataTools.bigEndByteToShort(buf, offset);
                offset += 2;
                times.add(speed);
            }else{
                break;
            }
        }
        MLog.d(TAG, "get_server_td_response="+times.toString());
        ArrayList<ServerInfos> infos = mPeerTask.getServerInfos();
        if(infos != null){
            for(int i = 0; i < times.size(); i++) {
                if(i < infos.size()) {
                    infos.get(i).ctd = times.get(i);
                }
            }
        }

        mPeerTask.set_peer_get_all_server_td_flag(true);
    }

    int send_test_request(byte[] payload, int mode, String ip, int port){
        byte[] data = new byte[512];
        int offset = 0;
        int size = 0;

        String sn = PeerTask.getInstance().mSerialNo;
        size = sn.getBytes().length;
        data[offset++] = (byte) size;
        System.arraycopy(sn.getBytes(), 0, data, offset, size);
        offset += size;

        sn = PeerTask.getInstance().mTaskInfo.sn;
        size = sn.getBytes().length;
        data[offset++] = (byte) size;
        System.arraycopy(sn.getBytes(), 0, data, offset, size);
        offset += size;

        String taskId = PeerTask.getInstance().mTaskInfo.task_id;
        size = taskId.getBytes().length;
        data[offset++] = (byte) (size + 1);
        data[offset++] = (byte) mode;
        System.arraycopy(taskId.getBytes(), 0, data, offset, size);
        offset += size;

        if(mode == PEER_UDP) {
            data[offset++] = 0;
        }else{
            size = ip.getBytes().length;
            data[offset++] = (byte) (size + 2);
            DataTools.bigEndShortToByte(data, offset, (short) port);
            offset +=2;
            System.arraycopy(ip.getBytes(), 0, data, offset, size);
            offset += size;
        }

        return pack_peer_frame(payload, 0x00, 0x03, data, offset);
    }

    public int connect_link_by_b(byte[] payload, int type) {
        byte[] data = new byte[512];
        short offset = 0;
        byte size = 0;
        String key = "123456789";
        String token = "123456789";
        String sn = PeerTask.getInstance().mSerialNo;
        TaskInfo taskInfo = PeerTask.getInstance().mTaskInfo;
        if(type == 1) {
            size = (byte) token.getBytes().length;
            data[offset++] = size;
            System.arraycopy(token.getBytes(), 0, data, offset, size);
            offset += size;

            size = (byte) taskInfo.task_id.getBytes().length;
            data[offset++] = size;
            System.arraycopy(taskInfo.task_id.getBytes(), 0, data, offset, size);
            offset += size;
        }
        if(type == 0) {
            size = (byte) sn.getBytes().length;
            data[offset++] = size;
            System.arraycopy(sn.getBytes(), 0, data, offset, size);
            offset += size;
        } else {
            size = (byte) taskInfo.sn.getBytes().length;
            data[offset++] = size;
            System.arraycopy(taskInfo.sn.getBytes(), 0, data, offset, size);
            offset += size;
        }
        size = (byte) key.getBytes().length;
        data[offset++] = size;
        System.arraycopy(key.getBytes(), 0, data, offset, size);
        offset += size;

        return pack_peer_frame(payload, (byte) 0x00, (byte) 0x01, data, offset);
    }
}
