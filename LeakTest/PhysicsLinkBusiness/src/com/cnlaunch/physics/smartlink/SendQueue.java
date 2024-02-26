package com.cnlaunch.physics.smartlink;


import com.cnlaunch.physics.utils.DataTools;
import com.cnlaunch.physics.utils.MLog;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SendQueue {
    public static final String TAG = "SendQueue";
    private static SendQueue instance;
    private final Lock queueLock = new ReentrantLock();
    private final Lock ackLock = new ReentrantLock();
    private Condition notEmpty = queueLock.newCondition();
    private SendInterface _output;
    private LinkedList<Packet> queue;
    private Set<Long> loseQueue;
    private long id;
    private long lastTime;   //最后发送时间
    private long lastInitTime;//最后序号初始化发送时间
    private long ackSeq;     //应答SEQ
    private boolean syncFlag; //SEQ同步标志
    private boolean errFlag;  //错误帧标志
    private boolean lastSyncFlag;
    private int number;       //未发送包数
    private PeerTask mPeerTask;

    private SendQueue() {
        queue = new LinkedList<>();
        loseQueue = new HashSet<>();
        id = 1;
        lastTime = 0;
        lastInitTime = 0;
        ackSeq = 0;
        number = 0;
        syncFlag = true;
        lastSyncFlag = true;
        errFlag = false;
        mPeerTask = PeerTask.getInstance();
    }

    public static SendQueue getInstance() {
        if(instance == null) {
            synchronized (SendQueue.class) {
                if(instance == null) {
                    instance = new SendQueue();
                }
            }
        }
        return instance;
    }

    public void set_output(SendInterface _output) {
        this._output = _output;
    }

    public void clear() {
        queueLock.lock();
        try {
            queue.clear();
        } finally {
            queueLock.unlock();
        }

        ackLock.lock();
        try {
            loseQueue.clear();
        } finally {
            ackLock.unlock();
        }
        id = 1;
        lastTime = 0;
        lastInitTime = 0;
        ackSeq = 0;
        number = 0;
        syncFlag = true;
        lastSyncFlag = true;
        errFlag = false;
    }

    public void setAckSeq(byte[] buf, int offset, int size){
        long seq = 0, value = 0;
        long num = 0;

        seq = DataTools.bigEndByteToInt(buf, offset);
        offset += 4;

        if(seq >= ackSeq){
            ackSeq = seq;
            MLog.e(TAG, "setAckSeq ackSeq=" + ackSeq);
        }

        if(buf[offset] == 0){
            //正确帧
            return ;
        }
        MLog.e(TAG, "不正確setAckSeq ackSeq=" + ackSeq);
        MLog.e(TAG,  DataTools.bytesToHexString(buf, offset - 4, size));

        offset++;
        num = DataTools.bigEndByteToShort(buf, offset);
        offset += 2;
        for(int i = 0; i < num; i++){
            if(offset >= size) {
                break;
            }
            value = DataTools.bigEndByteToInt(buf, offset);
            offset += 4;
            if(value > ackSeq){
                insert(value);
            }
        }
        errFlag = true;
    }

    public void insert(long seq){
        ackLock.lock();
        try {
            MLog.e(TAG, "insert 丢包 Seq =" + seq);
            loseQueue.add(seq);
        } finally {
            ackLock.unlock();
        }
    }

    //重发丢失包
    public void reSend(){
        if(loseQueue.isEmpty()) {
            return;
        }
        ackLock.lock();
        try {
            Iterator<Long> iterator = loseQueue.iterator();
            while (iterator.hasNext()) {
                long seq = iterator.next();
                //MLog.e(TAG, "丢包 reSend seq" + seq);
                if(seq <= ackSeq) {
                    iterator.remove();
                } else {
                    if(find(seq)) {
                        MLog.e(TAG, "重新发送后立即删除 seq" + seq);
                        iterator.remove();  //重新发送后立即删除
                    }else {
                        //发送队列中找不到相应的包
                        MLog.e(TAG, "error, can not find id" + seq);
                        syncFlag = false;
                        break;
                    }
                }
            }
        } finally {
            ackLock.unlock();
        }
    }

    boolean find(long seq){
        Packet pkt;
        queueLock.lock();
      //  PeerTask peerTask = PeerTask.getInstance();
        try {
            Iterator<Packet> iterator = queue.iterator();
            while (iterator.hasNext()) {
                pkt = iterator.next();
                if(pkt.getSeq() == seq) {
                    //MLog.d(TAG,"send data in find seq" +pkt.getSeq());
                    _output.sendData(pkt.getData(), pkt.getSize());
                    lastTime = System.currentTimeMillis();
                    pkt.setSendTime(lastTime);
                    mPeerTask.add_send_bytes(pkt.getSize());
                    return true;
                }
            }
        } finally {
            queueLock.unlock();
        }
        return false;
    }

   public void delete(){
        Packet pkt;
        queueLock.lock();
        try {
            Iterator<Packet> iterator = queue.iterator();
            while (iterator.hasNext()) {
                pkt = iterator.next();
                if((pkt.getSeq() <= ackSeq) || pkt.overtime()) {
                   // MLog.d(TAG,"delete seq=" +pkt.getSeq());
                    lastTime = System.currentTimeMillis();
                    iterator.remove();
                } else {
                    break;
                }
            }
        } finally {
            queueLock.unlock();
        }
    }

    public void flushBuffer(byte[] data, int size, byte[] buf, int[] used, int max){
        if(size + used[0] < max){
            System.arraycopy(data, 0, buf, used[0], size);
            used[0] += size;
        }else{
            //MLog.e(TAG,"flushUnlock01 send size="+used[0]);
            //MLog.d(TAG,"send data in flushBuffer");
            _output.sendData(buf, used[0]);
           // Arrays.fill(buf,0, max, (byte) 0);
            used[0] = 0;
            System.arraycopy(data, 0, buf, used[0], size);
            used[0] += size;
        }
    }

   public void flushUnlock(int mode){
        byte[] buf = new byte[1024];
        int[] offset = new int[1];
       offset[0] = 0;

        Packet pkt;
        if(_output == null) {
            MLog.e(TAG,"error, init callback function fail");
            return ;
        }

        if(!syncFlag) {
            MLog.e(TAG,"flushUnlock syncFlag =flase");
            return;
        }
    //   PeerTask peerTask = PeerTask.getInstance();
       Iterator<Packet> iterator = queue.iterator();
       while (iterator.hasNext()) {
           if(errFlag) {
               errFlag = false;
               break;
           }
           pkt = iterator.next();
           boolean isGetSendData = false;
           if(!pkt.isSend()) {
               isGetSendData = true;
               number--;
           }else if(number <= 0 && pkt.isSend() && (System.currentTimeMillis() - lastTime > 50)){
               isGetSendData = true;
           }else if(mode == 1){
               isGetSendData = true;
           }

           if(isGetSendData) {
               //MLog.d(TAG,"flushUnlock will send seq="+pkt.getSeq());
               flushBuffer(pkt.getData(), pkt.getSize(), buf, offset, buf.length);
               lastTime = System.currentTimeMillis();
               pkt.setSendTime(System.currentTimeMillis());
               mPeerTask.add_send_bytes(pkt.getSize());
           }

       }

        if(offset[0] > 0) {
            //MLog.e(TAG,"flushUnlock00 send size="+offset[0]);
            //MLog.d(TAG,"send data in flushUnlock");
            _output.sendData(buf, offset[0]);
        }
    }

   public void flushLock(int mode){
        queueLock.lock();
        try {
            flushUnlock(mode);
        } finally {
            queueLock.unlock();
        }
    }

    public void syncRecv(){
        ackLock.lock();
        try {
            loseQueue.clear();
            syncFlag = true;
        } finally {
            ackLock.unlock();
        }
    }

    public void sendInitSeq(){
        byte[] data = new byte[256];
        byte[] payload = new byte[512];
        int offset = 0, size = 0;
        Packet pkt;

        data[offset++] = 0x16;

        if(queue.size() != 0){
            pkt = queue.get(0);
            DataTools.bigEndIntToByte(data, offset, (int)pkt.getSeq());
            MLog.d(TAG, queue.size() + ",sendInitSeq id=" +  id);
        }else{
            DataTools.bigEndIntToByte(data, offset, (int)id);
            MLog.d(TAG,  "sendInitSeq id=" +  id);
        }

        offset += 4;
        size = PeerLink.pack_peer_frame(payload, PacketQueue.PEER_ID, 0x07, data, offset);
        if(_output != null && (size > 0)) {
            _output.sendData(payload, size);
        }
    }

    public void getPacket() {
        delete();
        reSend();
        queueLock.lock();
        try {
            while(queue.isEmpty()){
                notEmpty.await();
            }
           // MLog.e(TAG, "flushUnlock queue size=" +queue.size());
            flushUnlock(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            queueLock.unlock();
        }
    }

    public void run() {
        if(!syncFlag){
            //发送SEQ同步帧
            long curTime = System.currentTimeMillis();
            if(curTime - lastInitTime > 25){
                sendInitSeq();
                lastInitTime = curTime;
            }
            return ;
        }

        if(syncFlag != lastSyncFlag){
            MLog.e(TAG, "syncFlag != lastSyncFlag");
            delete();
            reSend();
            flushLock(1);   //重新发送队列中所有数据
            lastSyncFlag = syncFlag;
            return;
        }

        lastSyncFlag = syncFlag;
    }

    public void send(byte[] data, int size, byte type) {
        send(data, 0, size, type);
    }

    public void send(byte[] data, int pos, int size, byte type) {
        queueLock.lock();
        try {

            byte[] packageData = new byte[size + 256];
            int offset = 0;
            if((data == null) || (size <= 0)) {
                return;
            }

            int dataLen = size + 6;
            long time = System.currentTimeMillis() / 1000;
            int length = dataLen + 13;
            int packagelength = length - 3;//不需要协议号 及包长度的字节 即:[目标地址 + 源地址 + 计数器 + 业务ID + 数据 + 时间戳 + 校验] 总长度，小端模式

            int index = 0;
            packageData[index++] = (byte) (0x01 & 0xFF);
            packageData[index++] = (byte) (packagelength & 0xFF);//小端模式
            packageData[index++] = (byte) (packagelength >> 8 & 0xFF);//小端模式
            packageData[index++] = (byte) PacketQueue.PEER_ID;//目标地址 0x00:中转服务器  0x01:C端  0x02: B端
            packageData[index++] = 0x02;//源地址  0x00:中转服务器  0x01:C端  0x02: B端
            packageData[index++] = 0; //(byte) (peer_frame_count & 0xFF);//计数器 小端模式
            packageData[index++] = 0;//(byte) (peer_frame_count >> 8 & 0xFF);//计数器 小端模式
            packageData[index++] = (byte) (0x07 & 0xFF);

            packageData[index++] = 0x11;
            DataTools.bigEndIntToByte(packageData, index, (int)id);
            index += 4;
            packageData[index++] = type;
            System.arraycopy(data, pos, packageData, index, size);
            index += size;

            packageData[index++] = (byte) (time & 0xFF);
            packageData[index++] = (byte) (time >> 8 & 0xFF);
            packageData[index++] = (byte) (time >> 16 & 0xFF);
            packageData[index++] = (byte) (time >> 24 & 0xFF);

            // [协议版本 + 包长度 + 目标地址 + 源地址 + 计数器 + 业务ID + 数据 + 时间戳] 异或运算
            int crc= DataTools.getRemoteCrcByDataLength(packageData,length -1);
            packageData[index++] = (byte) (crc & 0xFF);

            Packet pkt = new Packet(packageData, index, id);
            queue.addLast(pkt);
            number++;
            id++;
            notEmpty.signal();;
        } finally {
            queueLock.unlock();
        }
    }
    public boolean isNoSendData() {
        return queue.isEmpty();
    }
}
