package com.cnlaunch.physics.smartlink;

import com.cnlaunch.physics.utils.DataTools;
import com.cnlaunch.physics.utils.MLog;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RecvQueue {
    private static RecvQueue instance;
    private SendInterface _output;
    private final Lock queueLock = new ReentrantLock();
    private LinkedList<Packet> _queue;
    private long _csq;
    private int _rcv_num;         //当前接收的包数
    private long _last_time;      //最后发送时间
    private long _last_err_time;  //接收错误时间
    private boolean _err_flag;           //错误标志
    private long _max_seq;        //当前接收的最大序号
    private boolean _new_flag;           //是否有新数据到达
    private PeerTask peerTask;

    private RecvQueue() {
        _queue = new LinkedList<Packet>();
        _last_time = System.currentTimeMillis();
        _rcv_num = 0;
        _csq = 0;
        _new_flag = false;
        _max_seq = 0;
        _last_err_time = _last_time;
        _err_flag = false;
        peerTask = PeerTask.getInstance();
    }

    public static RecvQueue getInstance() {
        if(instance == null) {
            synchronized (SendQueue.class) {
                if(instance == null) {
                    instance = new RecvQueue();
                }
            }
        }
        return instance;
    }

    public void set_output(SendInterface _output) {
        this._output = _output;
    }

    public void clear(){
        queueLock.lock();
        try {
            _queue.clear();
        } finally {
            queueLock.unlock();
        }

        _last_time = System.currentTimeMillis();
        _csq = 0;
        _rcv_num = 0;
        _new_flag = false;
        _max_seq = 0;
        _last_err_time = _last_time;
        _err_flag = false;
    }

    public void insert(byte[] data, int pos, int size, long seq, int type) {
        Packet pNew = new Packet(data, pos, size, seq);
        pNew.setType(type);

        if(_queue.isEmpty()){
            //printf("Insert seq=%d\n", seq);
            _queue.addLast(pNew);
            return ;
        }

        queueLock.lock();
        try {
            for(int i = 0; i < _queue.size(); i++) {
                long curSeq = _queue.get(i).getSeq();
                if(curSeq == seq){
                    break;
                } else if(curSeq > seq) {
                    _queue.add(i, pNew);
                    return;
                }
            }
            _queue.addLast(pNew);
        } finally {
            queueLock.unlock();
        }
    }

    //队列处理
    public void process(){
        Packet pkt;
        if(_queue.isEmpty()) {
            return;
        }

        queueLock.lock();
        try {
            Iterator<Packet> iterator = _queue.iterator();
            while (iterator.hasNext()) {
                pkt = iterator.next();
                long curSeq = pkt.getSeq();
                if(curSeq <= _csq) {
                    iterator.remove();
                } else if(curSeq == _csq + 1) {
                    Packet.dispatchPacket(pkt.getData(), 0, pkt.getSize(), pkt.getType());
                    iterator.remove();
                    _csq += 1;
                } else {
                    break;
                }
            }
        } finally {
            queueLock.unlock();
        }
    }

    //1.处理当前接收帧
    //2.处理队列中的帧
    //关键功能：排序、去重
    public void recv(byte[] data, int pos, int size, long seq, int type) {
        String str = String.format( "recv  seq=%d,csq=%d,_rcv_num=%d", seq, _csq, _rcv_num);
        MLog.d("RecvQueue", str);
       // _last_time = System.currentTimeMillis();
        _rcv_num++;
        _new_flag = true;

        if(_max_seq < seq) {
            _max_seq = seq;
        }

        if(seq == _csq + 1){
            Packet.dispatchPacket(data, pos, size, type);
            _csq = seq;
        }else if(seq <= _csq){
            //回复ACK
            MLog.d("RecvQueue", "SendInfo ack csq=" + _csq);
            //LOG_WRITE(LOG_DEBUG, "SendInfo ack csq=%d", _csq);
            sendInfo(false, seq);
        }else{
            insert(data, pos, size, seq, type);   //按序插入
        }

        process();

        //发送错误帧
        if(_csq < seq) {
            if(!_err_flag) {
                _err_flag = true;
                _last_err_time = System.currentTimeMillis();
            }
        }

        if(_rcv_num > 10) {
            //LOG_WRITE(LOG_DEBUG, "SendInfo ack _csq=%d", _csq);
            sendInfo(false, seq);
        }
    }

    public boolean find(long seq)
    {
        if(_queue.isEmpty()) {
            return false;
        }
        queueLock.lock();
        try {
            Iterator<Packet> iterator = _queue.iterator();
            while (iterator.hasNext()) {
                Packet Pkt = iterator.next();
                if(Pkt.getSeq() == seq) {
                    return true;
                }
            }
        } finally {
            queueLock.unlock();
        }
        return false;
    }

    public void sendInfo(boolean err, long seq) {
        byte[] data = new byte[1024];
        byte[] payload = new byte[4096];
        int offset = 0, ops = 0;
        short num = 0;
        int size = 0;

        //清除连续接收到的包数
        _rcv_num = 0;

        data[offset++] = (byte)(0x11 + 0x80);
        DataTools.bigEndIntToByte(data, offset, _csq);
        offset += 4;

        if(err) {
            data[offset++] = 0x01;
            ops = offset;
            offset += 2;   //个数

            for(long i = _csq + 1; i < seq; i++) {
                if(!find(i)) {
                    DataTools.bigEndIntToByte(data, offset, (int)i);
                    offset += 4;
                    num++;
                    if(offset >= 1000) {
                        break;
                    }
                }
            }
            DataTools.bigEndShortToByte(data, ops, num);
        }else{
            data[offset++] = 0x00;
        }
        MLog.d("RecvQueue", "sendInfo 序号="+seq);
        size = PeerLink.pack_peer_frame(payload, PacketQueue.PEER_ID, 0x07, data, offset);
        if(_output != null) {
            _output.sendData(payload, size);
        }
    }

    public void run() {
        long curTime = System.currentTimeMillis();
        if((curTime - _last_err_time >= 10) && _err_flag) {
            //LOG_WRITE(LOG_DEBUG, "SendInfo err csq=%d, seq=%d", _csq, _max_seq);
            sendInfo(true, _max_seq);
            _err_flag = false;
            _last_time = curTime;
        }

        //检测无数据后应答对方ACK以清除对方发送队列
        if((curTime - _last_time >= 15) && _new_flag) {
            //LOG_WRITE(LOG_DEBUG, "SendInfo ack csq=%d", _csq);
            sendInfo(false, 0);
            _new_flag = false;
        }
    }

    public void syncRecv(long seq) {
        byte[] data = new byte[1];
        byte[] payload = new byte[128];
        int size = 0;

        data[0] = (byte)(0x16 + 0x80);

        queueLock.lock();
        try {
            _queue.clear();
        } finally {
            queueLock.unlock();
        }
        _last_time = System.currentTimeMillis();
        if(seq >= 1) {
            _csq = seq - 1;   //避免第一包丢弃
        }else{
            _csq = 0;
        }

        //LOG_WRITE(LOG_DEBUG, "SyncRecv csq=%d\n", _csq);

        size = PeerLink.pack_peer_frame(payload, PacketQueue.PEER_ID, 0x07, data, 1);
        if(_output != null) {
            _output.sendData(payload, size);
        }
    }

}
