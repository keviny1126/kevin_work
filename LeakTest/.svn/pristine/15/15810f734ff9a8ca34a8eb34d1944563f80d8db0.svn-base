package com.cnlaunch.physics.smartlink;

import com.cnlaunch.physics.utils.DataTools;
import com.cnlaunch.physics.utils.MLog;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PacketQueue {
    public String TAG = "PacketQueue";
    public static final byte PEER_ID = 01;

    private final Lock queueLock = new ReentrantLock();
    private LinkedList<Packet> _queue;
    private Condition _cond = queueLock.newCondition();
    private boolean _ui_flag;      //参与计算界面发送字节数
    private SendInterface _output;
    private static PacketQueue s_mcuSendQueue;  //发送到MCU数据队列
    private static PacketQueue s_peerTcpQueue;  //超级远程诊断中转队列

    private PacketQueue() {
        _ui_flag = false;
        _queue = new LinkedList<Packet>();
    }

    public void clear() {
        queueLock.lock();
        try {
            _queue.clear();
        } finally {
            queueLock.unlock();
        }
    }

    public void insert(byte[] data, int size) {
        queueLock.lock();
        try {
            _queue.addLast(new Packet(data, size, 0));
           // MLog.e(TAG, "PacketQueue insert _queue size="+_queue.size());
            _cond.signal();
        } finally {
            queueLock.unlock();
        }
    }

    public void insert(byte[] data, int pos, int size) {
        queueLock.lock();
        try {
            _queue.addLast(new Packet(data, pos,  size, 0));
           // MLog.e(TAG, "PacketQueue insert _queue size="+_queue.size());
            _cond.signal();
        } finally {
            queueLock.unlock();
        }
    }

    public void setUiFlag(boolean value) {
        _ui_flag = value;
    }

    public void run() {
      //  PeerTask peerTask = PeerTask.getInstance();
        queueLock.lock();
        try {
           while (_queue.isEmpty()) {
                _cond.await();
            }
           // MLog.e(TAG, "PacketQueue run 收到通知_queue size="+_queue.size());
            if (_output != null) {
                Packet pkt;
                Iterator<Packet> iterator = _queue.iterator();
                while (iterator.hasNext()) {
                    pkt = iterator.next();
                   // MLog.e(TAG, "PacketQueue run start send data");
                    _output.sendData(pkt.getData(), pkt.getSize());
                   // MLog.e(TAG, "PacketQueue run end send data"+ _output.toString());
/*                    if (_ui_flag) {
                        peerTask.add_send_bytes(pkt.getSize());
                    }*/
                    iterator.remove();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            queueLock.unlock();
        }
    }

    public static PacketQueue getMcuSendQueue() {
        if(s_mcuSendQueue == null) {
            s_mcuSendQueue = new PacketQueue();
        }
        return s_mcuSendQueue;
    }

    public static PacketQueue getPeerTcpQueue()
    {
        if(s_peerTcpQueue == null) {
            s_peerTcpQueue = new PacketQueue();
        }
        return s_peerTcpQueue;
    }

    public static void packetQueueInit(SendInterface mcuSend, SendInterface tcpSend) {
        PacketQueue mcu = getMcuSendQueue();
        mcu._output = mcuSend;
        mcu.TAG = "mcu";

        PacketQueue tcp = getPeerTcpQueue();
        tcp.setUiFlag(true);
        tcp.TAG = "tcp";
        tcp._output = tcpSend;
    }

    public int available() {
        int size = 0;
        queueLock.lock();
        try {
            if(_queue.isEmpty()) {
                return 0;
            }
            Packet pkt;
            Iterator<Packet> iterator = _queue.iterator();
            while (iterator.hasNext()) {
                pkt = iterator.next();
                // MLog.e(TAG, "PacketQueue run start send data");
                size += pkt.getSize();
            }
        } finally {
            queueLock.unlock();
        }
        return size;
    }

    public String read() {
        queueLock.lock();
        try {
            if(_queue.isEmpty()) {
                return "";
            }
            Packet pkt;
            Iterator<Packet> iterator = _queue.iterator();
            int size = 0;
            while (iterator.hasNext()) {
                pkt = iterator.next();
                // MLog.e(TAG, "PacketQueue run start send data");
                size += pkt.getSize();
            }
            byte[] buf = new byte[size];
            int index = 0;
            iterator = _queue.iterator();
            while (iterator.hasNext()) {
                pkt = iterator.next();
                // MLog.e(TAG, "PacketQueue run start send data");
                System.arraycopy(pkt.getData(), 0, buf, index,  pkt.getSize());
                index += pkt.getSize();
            }
            _queue.clear();
            String temp = DataTools.bytesToHexString(buf, 0, size);
             MLog.e(TAG, "mcu recv=" + temp);
            return temp;
        } finally {
            queueLock.unlock();
        }
    }

    public int readBlock(byte buf[], int off, int len) {
        //  PeerTask peerTask = PeerTask.getInstance();
        int ret = 0;
        queueLock.lock();
        try {
            while (_queue.isEmpty()) {
                _cond.await();
            }
            Packet pkt;
           // int maxSize = len - off;
            int index = off;
            int size = 0;
            Iterator<Packet> iterator = _queue.iterator();
            while (iterator.hasNext()) {
                pkt = iterator.next();
                if(index > len) {
                    break;
                }
                if(index + pkt.getSize() > len) {
                    break;
                }
                System.arraycopy(pkt.getData(), 0, buf, index,  pkt.getSize());
                index += pkt.getSize();
                ret += pkt.getSize();
                iterator.remove();
                // MLog.e(TAG, "PacketQueue run start send data");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            queueLock.unlock();
        }
        return ret;
    }
}
