package com.cnlaunch.physics.smartlink;

import com.cnlaunch.physics.utils.DataTools;
import com.cnlaunch.physics.utils.MLog;

public class Packet {
    private byte[] data;
    private int size;
    private long createTime;
    private long sendTime;
    private long seq;
    private int times;   //发送次数
    private int type;   //消息类型

    public Packet(byte[] data, int size, long seq) {
        if (data != null) {
            this.data = data;
            this.size = size;
            this.seq = seq;
            times = 0;
            createTime = System.currentTimeMillis();
            sendTime = createTime;
        }
    }

    public Packet(byte[] data, int pos, int size, long seq) {
        if (data != null) {
            this.data = new byte[size];
            this.size = size;
            this.seq = seq;
            times = 0;
            createTime = System.currentTimeMillis();
            sendTime = createTime;
            System.arraycopy(data, pos, this.data, 0, size);
        }
    }

    public boolean isSend() {
        if (times == 0) {
            return false;
        }
        return true;
    }

    public boolean overtime() {
        if ((System.currentTimeMillis() - createTime) >= 2000) {
            return true;
        }
        return false;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.times ++;
        this.sendTime = sendTime;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static void dispatchPacket(byte[] data, int pos, int size, int type) {
      //  DoipControl diop = DoipControl.getInstance();
        PacketQueue mcu = PacketQueue.getMcuSendQueue();
        byte ack = 0;

        if (type == 0) {
            //频繁发生的事情优先处理
            mcu.insert(data, pos, size);
        } else if (type == 1) {
            //diop.insertUdpQueue(data, pos, size);
        } else if (type == 2) {
            //diop.insertTcpQueue(data, pos, size);
        } else if ((type >= 3) && (type <= 6)) {
            ack = data[pos];
            if (ack == 0) {
              //  diop.notifyRecv(type);
            } else {
               // diop.notifyAckRecv(type);
            }
        } else {
            mcu.insert(data, pos,size);
        }
    }
    public static int getTestData(byte[] packageData, byte dest, byte[] data, int size, int type) {
      //  byte[] buf = new byte[4096];
       // short offset = 0;
        if (data == null) {
            return -1;
        }

        int dataLen = size + 6;
        long time = System.currentTimeMillis() / 1000;
        int length = dataLen + 13;
        int packagelength = length - 3;//不需要协议号 及包长度的字节 即:[目标地址 + 源地址 + 计数器 + 业务ID + 数据 + 时间戳 + 校验] 总长度，小端模式

        int index = 0;
        packageData[index++] = (byte) (0x01 & 0xFF);
        packageData[index++] = (byte) (packagelength & 0xFF);//小端模式
        packageData[index++] = (byte) (packagelength >> 8 & 0xFF);//小端模式
        packageData[index++] = (byte) dest;//目标地址 0x00:中转服务器  0x01:C端  0x02: B端
        packageData[index++] = 0x02;//源地址  0x00:中转服务器  0x01:C端  0x02: B端
        packageData[index++] = 0; //(byte) (peer_frame_count & 0xFF);//计数器 小端模式
        packageData[index++] = 0;//(byte) (peer_frame_count >> 8 & 0xFF);//计数器 小端模式
        packageData[index++] = (byte) (0x07 & 0xFF);

        packageData[index++] = 0x11;
        DataTools.bigEndIntToByte(packageData, index, (int)07);
        index += 4;
        packageData[index++] = (byte)type;
        System.arraycopy(data, 0, packageData, index, size);
        index += size;

        packageData[index++] = (byte) (time & 0xFF);
        packageData[index++] = (byte) (time >> 8 & 0xFF);
        packageData[index++] = (byte) (time >> 16 & 0xFF);
        packageData[index++] = (byte) (time >> 24 & 0xFF);

        // [协议版本 + 包长度 + 目标地址 + 源地址 + 计数器 + 业务ID + 数据 + 时间戳] 异或运算
        int crc= DataTools.getRemoteCrcByDataLength(packageData,length -1);
        packageData[index++] = (byte) (crc & 0xFF);

        return index;
        //return PeerLink.getInstance().pack_peer_frame(payload, dest, 0x07, buf, offset);
    }
}
