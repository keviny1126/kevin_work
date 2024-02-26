package com.cnlaunch.rj45link;
import android.text.TextUtils;
import android.util.Log;

import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.NetworkUtil;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

/**
 * UDP协议相关
 * Created by zhangshengda on 2016/4/18.
 */
public class UDPController {
    private static final int MAXNUM = 3;      //设置重发数据的最多次数，udp尝试需要3秒以上
    //private Thread udpThread = null;
    private boolean bReceiveUdpData = false;
    private byte[] rDataByte;
    private static String serverIP;
    private static int serverPort;
    public String GetServerIP()
    {
        return serverIP;
    }

    public int GetServerPort()
    {
        return serverPort;
    }

    /**
     * /**
     * UDP协议的发送<br/>
     * 无连接协议，收到响应之后就会断开
     *
     * @param timeout  发送超时时间（毫秒）
     * @param port
     * @param sendBuffer
     */
    public byte[] sendUDPData(final int timeout, final int port, final byte[] sendBuffer,final int offset, final int length) {
        return sendUDPData(timeout,port, sendBuffer,offset,  length,"");
    }

    /**
     * socket相关的连接，数据处理无需放在工作线程中，因为thread.join本身会阻塞调用线程。
     * 因此功能调用肯定会放在工作线程中，再创建工作线程会造成资源浪费 xfh2021/10/18 修改。
     * @param timeout
     * @param port
     * @param sendBuffer
     * @param broadcastAddress
     * @return
     */
    public byte[] sendUDPData(final int timeout, final int port, final byte[] sendBuffer,final int offset, final int length,final String broadcastAddress) {
        //udpThread = new Thread(new Runnable() {
        //    @Override
        //    public void run() {
                try {
                    //byte[] data = message.getData();
                    byte[] buf = new byte[1024];
//                    byte[] ip = new byte[]{(byte) 0xAC, 0x10, 0x41, 0x17};//172.16.65.23
//                    InetAddress ia = InetAddress.getByAddress(ip);
                    //广播的形式
                    DatagramSocket socket = new DatagramSocket();
                    //广播的形式标明端口
                    SocketAddress socketAddr = null;
                    if(TextUtils.isEmpty(broadcastAddress)==false){
                        socketAddr = new InetSocketAddress(InetAddress.getByName(broadcastAddress), port);
                    }
                    else {
                        socketAddr = new InetSocketAddress(InetAddress.getByName("255.255.255.255"), port);
                    }
                    //定义用来发送数据的DatagramPacket实例
                    if(MLog.isDebug) {
                        Log.d("UDPController", String.format("sendUDPData length:%s,offset:%s,input length:%s", sendBuffer.length, offset, length));
                    }
                    DatagramPacket dp_send = new DatagramPacket(sendBuffer,offset, length, socketAddr);
                    //定义用来接收数据的DatagramPacket实例
                    DatagramPacket dp_receive = new DatagramPacket(buf, 1024);
                    //数据发向本地3000端口
                    socket.setSoTimeout(timeout);          //设置接收数据时阻塞的最长时间
                    int tries = 0;                         //重发数据的次数
                    boolean receivedResponse = false;     //是否接收到数据的标志位
                    //直到接收到数据，或者重发次数达到预定值，则退出循环
                    while (!receivedResponse && tries < MAXNUM) {
                        //发送数据
                        socket.send(dp_send);
                        try {
                            //接收从服务端发送回来的数据
                            socket.receive(dp_receive);
                            //如果接收到的数据不是来自目标地址，则抛出异常
                            Log.e("Sanda", "dp_receive ip:" + dp_receive.getAddress().getHostAddress());
                            Log.e("Sanda", "dp_receive port:" + dp_receive.getPort());
                            serverIP = dp_receive.getAddress().getHostAddress();
                            serverPort =  dp_receive.getPort();
//                            if (!dp_receive.getAddress().equals(ia)) {
//                                throw new IOException("Received packet from an umknown source");
//                            }
                            //如果接收到数据。则将receivedResponse标志位改为true，从而退出循环
                            receivedResponse = true;
                        } catch (InterruptedIOException e) {
                            //如果接收数据时阻塞超时，重发并减少一次重发的次数
                            tries += 1;
                            Log.e("Sanda", "Time out," + (MAXNUM - tries) + " more tries...");
                        }
                    }
                    if (receivedResponse) {
                        //如果收到数据，则打印出来
                        rDataByte = Arrays.copyOfRange(dp_receive.getData(), 0, dp_receive.getLength());
//                        String receiveData = ByteHexHelper.bytesToHexStrWithSwap(rDataByte);
//                        Log.d("Sanda", "UDP_ANS:" + receiveData);
//                        RJ45ReceiveMessage message = new RJ45ReceiveMessage();
//                        message.setData(rDataByte);
//                        if (receiver != null) {
//                            receiver.messageReceived(message);
//                        }
                        //由于dp_receive在接收了数据之后，其内部消息长度值会变为实际接收的消息的字节数，
                        //所以这里要将dp_receive的内部消息长度重新置为1024
                        dp_receive.setLength(1024);

                        bReceiveUdpData = true;
                    } else {
                        //如果重发MAXNUM次数据后，仍未获得服务器发送回来的数据，则打印如下信息
                        Log.d("Sanda", "No response -- give up.");
//                        if (receiver != null) {
//                            receiver.error();
//                        }
                        rDataByte = new byte[0];
                        bReceiveUdpData = false;
                    }
                    socket.close();
                } catch (Exception e) {
                    rDataByte = new byte[0];
                    bReceiveUdpData = false;
                }
            //}
        //});
        /*udpThread.start();
        try {
            udpThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return  rDataByte;
    }

    /**
     * 判断目标服务器ip是否与广播地址段属于同一网络
     * @param aimServerIP
     * @param broadcastAddress
     * @param subNetMask
     * @return
     */
    public static boolean isSameNetworkWithBroadcastAddress(final String aimServerIP,final String broadcastAddress,final String subNetMask) {
        boolean isSameNetwork = true;
        if (!TextUtils.isEmpty(broadcastAddress) && !broadcastAddress.equals("255.255.255.255")) {
            byte[] serverIPBytes = NetworkUtil.ipToBytes(aimServerIP);
            int serverIPInt = NetworkUtil.bytesToInt(serverIPBytes);
            byte[] broadcastAddressBytes = NetworkUtil.ipToBytes(broadcastAddress);
            int broadcastAddressInt = NetworkUtil.bytesToInt(broadcastAddressBytes);
            byte[] subNetMaskBytes = NetworkUtil.ipToBytes(subNetMask);
            int subNetMaskInt = NetworkUtil.bytesToInt(subNetMaskBytes);
            Log.e("ReceiveUDPData", String.format("subNetMask  aimServerIPMask=%d,broadcastAddressMask=%d",
                    (serverIPInt & subNetMaskInt),(broadcastAddressInt & subNetMaskInt)));
            if ((serverIPInt & subNetMaskInt) != (broadcastAddressInt & subNetMaskInt)) {
                Log.e("ReceiveUDPData", String.format("ServerIP not in host network aimServerIP=%s,broadcastAddress=%s,subNetMask=%s",
                        aimServerIP, broadcastAddress, subNetMask));
                isSameNetwork = false;
            }
        }
        return isSameNetwork;
    }
    public byte[] receiveUDPData(final int timeout, final int port) {
        return receiveUDPData(timeout,port,true,"","");
    }
    public byte[] receiveUDPData(final int timeout, final int port,final String broadcastAddress,final String subNetMask) {
        return receiveUDPData(timeout,port,false,broadcastAddress,subNetMask);
    }

    /**
     * socket相关的连接，数据处理无需放在工作线程中，因为thread.join本身会阻塞调用线程。
     * 因此功能调用肯定会放在工作线程中，再创建工作线程会造成资源浪费 xfh2021/10/18 修改。
     * @param timeout
     * @param port
     * @param isAllNetwork
     * @param broadcastAddress
     * @param subNetMask
     * @return
     */
    public byte[] receiveUDPData(final int timeout, final int port, final boolean isAllNetwork, final String broadcastAddress, final String subNetMask) {
        //udpThread = new Thread(new Runnable() {
        //    @Override
        //    public void run() {
                try {
                    byte[] buf = new byte[1024];
                    //广播的形式
                    DatagramSocket socket = new DatagramSocket(port);
                    //定义用来接收数据的DatagramPacket实例
                    DatagramPacket dp_receive = new DatagramPacket(buf, 1024);
                    //数据发向本地3000端口
                    socket.setSoTimeout(timeout);              //设置接收数据时阻塞的最长时间
                    int tries = 0;                         //重发数据的次数
                    boolean receivedResponse = false;     //是否接收到数据的标志位
                    //直到接收到数据，或者重发次数达到预定值，则退出循环
                    while (!receivedResponse && tries < MAXNUM) {
                        //发送数据
                        try {
                            //接收从服务端发送回来的数据
                            socket.receive(dp_receive);
                            //如果接收到的数据不是来自目标地址，则抛出异常
                            Log.d("ReceiveUDPData", "dp_receive ip:" + dp_receive.getAddress().getHostAddress());
                            Log.d("ReceiveUDPData", "dp_receive port:" + dp_receive.getPort());
                            serverIP  = dp_receive.getAddress().getHostAddress();
                            serverPort  =  dp_receive.getPort();
                            if(!isAllNetwork) {
                                if (!isSameNetworkWithBroadcastAddress(serverIP,broadcastAddress,subNetMask)) {
                                    tries += 1;
                                    continue;
                                }
                            }
                            //如果接收到数据。则将receivedResponse标志位改为true，从而退出循环
                            receivedResponse = true;
                        } catch (InterruptedIOException e) {
                            //如果接收数据时阻塞超时，重发并减少一次重发的次数
                            tries += 1;
                            Log.e("ReceiveUDPData", "Time out," + (MAXNUM - tries) + " more tries...");
                        }
                    }
                    if (receivedResponse) {
                        //如果收到数据，则打印出来
                        rDataByte = Arrays.copyOfRange(dp_receive.getData(), 0, dp_receive.getLength());
                        //由于dp_receive在接收了数据之后，其内部消息长度值会变为实际接收的消息的字节数，
                        //所以这里要将dp_receive的内部消息长度重新置为1024
                        dp_receive.setLength(1024);
                        bReceiveUdpData = true;
                    } else {
                        //如果重发MAXNUM次数据后，仍未获得服务器发送回来的数据，则打印如下信息
                        Log.d("ReceiveUDPData", "No response -- give up.");
                        rDataByte = new byte[0];
                        bReceiveUdpData = false;
                    }
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    rDataByte = new byte[0];
                    bReceiveUdpData = false;
                }
            //}
        //});
        /*udpThread.start();
        try {
            udpThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return  rDataByte;
    }

}

