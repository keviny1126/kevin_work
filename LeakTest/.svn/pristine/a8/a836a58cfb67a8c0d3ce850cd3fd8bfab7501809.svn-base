package com.cnlaunch.rj45link;

import android.util.Log;
import com.cnlaunch.physics.utils.MLog;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * UDP和TCP连接管理类
 * Created by zhangshengda on 2016/4/19.
 * 扩展的多tcp/ip协议支持
 * xfh 2018/07/18 add
 */
public class RJ45LinkManager {
    private static RJ45LinkManager instance = null;
    private TCPSocketController socketController = null;
    //TCPSocketController管理映射，一个索引号对应一个TCPSocketController
    //扩展的多tcp/ip协议支持
    private AtomicInteger tcpSocketControllerIndexAtomicInteger; //记录TCPSocketController序号
    private ConcurrentHashMap<Integer, TCPSocketController> mTCPSocketControllerMap ;
    private UDPController udpController = null;
    //此方法用于多线程需要考虑并发控制
    public static synchronized RJ45LinkManager getInstance() {
        if (instance == null) {
            instance = new RJ45LinkManager();
        }
        return instance;
    }

    private RJ45LinkManager() {
        tcpSocketControllerIndexAtomicInteger = new AtomicInteger(0);
        mTCPSocketControllerMap = new ConcurrentHashMap<Integer, TCPSocketController>();
        socketController = new TCPSocketController();
        udpController = new UDPController();
    }

    /**
     * 连接TCP服务器
     *
     * @param ip
     * @param port
     */
    public void connectTCPLink(String ip, int port, TCPHandler tcpHandler) {
        socketController.connect(ip, port, tcpHandler);
    }

    /**
     * 断开连接
     */
    public void breakTCPLink() {
        socketController.close();
    }

    /**
     * 发送TCP数据
     *
     * @param data
     */
    public int send(byte[] data,int offset, int length) {
        return socketController.send(data,offset,length);
    }

    /**
     * 清空TCP数据
     *
     */
    public void clearReceiveData() {
        socketController.clearReceiveData();
    }

    //设置最大超时
    public void setMaxWaitTime(int maxTimeout)
    {
        socketController.setMaxWaitTime(maxTimeout);
    }

    public byte[] recevice(int iMaxWaitTime) {
        return socketController.getReceiveData(iMaxWaitTime);
    }

    /********扩展的多tcp/ip协议支持start *******/
    /**
     *
     * @param strIP
     * @param nPort
     * @return 返回-1为失败。其他值为对应socket的SocketIndex。
     */
    public int connectTCPLinkEx( String strIP, int nPort){
        int socketIndex = 0;
        TCPSocketController tcpSocketController = new TCPSocketController();
        if(tcpSocketController.connectEx(strIP, nPort, null)) {
            socketIndex = tcpSocketControllerIndexAtomicInteger.incrementAndGet();
            if (MLog.isDebug) {
                MLog.d("RJ45LinkManager", "connectTCPLinkEx end tcpSocketControllerIndexAtomicInteger=" + tcpSocketControllerIndexAtomicInteger + " socketIndex=" + socketIndex + " RJ45LinkManager=" + RJ45LinkManager.this.toString());
            }
            mTCPSocketControllerMap.put(socketIndex, tcpSocketController);
        }
        else{
            socketIndex = -1;
        }
        if(MLog.isDebug){
            dumpAllTcpSocketController();
        }
        return socketIndex;
    }

    public void breakTCPLinkEx(int SocketIndex) {
        TCPSocketController tcpSocketController =mTCPSocketControllerMap.get(SocketIndex);
        if(tcpSocketController != null){
            if(MLog.isDebug) {
                MLog.d("RJ45LinkManager", "breakTCPLinkEx SocketIndex=" + SocketIndex+" TCPSocketController="+tcpSocketController.toString());
            }
            tcpSocketController.close();
            mTCPSocketControllerMap.remove(SocketIndex);
            tcpSocketController = null;
        }
    }

    /**
     * 发送TCP数据
     *
     * @param data
     */
    public int sendEx(byte[] data,int offset, int length,int SocketIndex) {
        int sendCount = 0;
        TCPSocketController tcpSocketController =mTCPSocketControllerMap.get(SocketIndex);
        if(tcpSocketController != null){
            sendCount = tcpSocketController.send(data,offset,length);
        }
        else{
            sendCount = 0;
        }
        return sendCount;
    }
    /**
     * 清空TCP数据
     *
     */
    public void clearReceiveDataEx(int SocketIndex) {
        TCPSocketController tcpSocketController =mTCPSocketControllerMap.get(SocketIndex);
        if(tcpSocketController != null){
            tcpSocketController.clearReceiveData();
        }
    }

    /**
     * 设置最大超时
     */
    public void setMaxWaitTimeEx(int maxTimeout,int SocketIndex) {
        TCPSocketController tcpSocketController =mTCPSocketControllerMap.get(SocketIndex);
        if(tcpSocketController != null){
            tcpSocketController.setMaxWaitTime(maxTimeout);
        }
    }
    public byte[] receviceEx(int iMaxWaitTime,int SocketIndex) {
        byte[] returnData = null;
        TCPSocketController tcpSocketController =mTCPSocketControllerMap.get(SocketIndex);
        if(tcpSocketController != null){
            returnData = tcpSocketController.getReceiveData(iMaxWaitTime);
        }
        else{
            returnData = new byte[0];
        }
        return returnData;
    }
    public boolean getSocketStateEx(int SocketIndex) {
        TCPSocketController tcpSocketController =mTCPSocketControllerMap.get(SocketIndex);
        if(tcpSocketController != null){
            return  tcpSocketController.isValid();
        }
        else{
            return false;
        }
    }
    public synchronized void dumpAllTcpSocketController(){
        try{
            if (MLog.isDebug)  {
                MLog.d("RJ45LinkManager", "RJ45LinkManager dumpAllTcpSocketController start");
                for (ConcurrentHashMap.Entry<Integer, TCPSocketController> entry : mTCPSocketControllerMap.entrySet()) {
                    MLog.d("RJ45LinkManager", "RJ45LinkManager dumpAllTcpSocketController  socketIndex = " + entry.getKey() + ", tcpSocketController = " + entry.getValue().toString());
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    /********扩展的多tcp/ip协议支持end *******/

    /**
     * 连接并且发送UDP数据
     *
     * @param timeout  (超时时间 毫秒)
     * @param port
     * @param sendData
     */
    public byte[] sendUDPLink(int timeout, int port, byte [] sendData,int offset, int length) {
        return sendUDPLink(timeout,port, sendData,offset,length,"");
    }
    public byte[] sendUDPLink(int timeOut, int port, byte []sendData,int offset, int length,final String broadcastAddress) {
        byte []receiveData = null;
        try {
            receiveData = udpController.sendUDPData(timeOut, port, sendData,offset,length,broadcastAddress);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Sanda", "host is unkonw!");
        }
        return receiveData;
    }
    public byte[] receiveUDPData(int timeOut, int port) {
        byte []receiveData = null;
        try {
            receiveData = udpController.receiveUDPData(timeOut, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return receiveData;
    }
    public byte[] receiveUDPData(int timeOut, int port,final String broadcastAddress,final String subNetMask) {
        byte []receiveData = null;
        try {
            receiveData = udpController.receiveUDPData(timeOut, port,broadcastAddress, subNetMask);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return receiveData;
    }
    public String GetServerIP()
    {
        return udpController.GetServerIP();
    }

    public int GetServerPort()
    {
        return udpController.GetServerPort();
    }


    /**
     * 数据接收接口
     */
    public interface UDPDataReceiver {
        void messageReceived(RJ45ReceiveMessage message);
        void error();
    }


}

