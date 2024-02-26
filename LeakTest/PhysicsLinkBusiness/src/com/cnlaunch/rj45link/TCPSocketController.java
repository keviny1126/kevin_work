package com.cnlaunch.rj45link;

import android.util.Log;

import com.cnlaunch.physics.utils.MLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by zhangshengda on 2016/4/27.
 */
public class TCPSocketController {
    //private Thread thread = null;
    private Socket socket = null;
    private TCPHandler tcpHandler = null;

    private int temp = 0;
    private int returnValue = 0;

    //设置最大超时
    public void setMaxWaitTime(int maxTimeout) {
        if (socket != null) {
            try {
                socket.setSoTimeout(maxTimeout);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取socket通道中的数据
     *
     * @param iMaxWaitTime 超时时间 毫秒
     * @return
     */
    public byte[] getReceiveData(int iMaxWaitTime) {
        if (socket == null) {
            MLog.e("XEE", "getReceiveData socket =null");
        }
        final long maxWaitTime = iMaxWaitTime;
        final byte buffer[] = new byte[4 * 1024];
        setMaxWaitTime(iMaxWaitTime);
        //thread = new Thread(new Runnable() {
        //    @Override
        //    public void run() {
                Date startDate = new Date(System.currentTimeMillis());
                long diff = 0;
                try {
                    // 创建一个Socket对象，并指定服务端的IP及端口号
                    InputStream inputStream = socket.getInputStream();
                    // 创建一个byte类型的buffer字节数组，用于存放读取的本地文件
                    temp = 0;
                    // Log.e("zxb", "Client: wait to receive data...");
                    Date curDate = new Date(System.currentTimeMillis());
                    diff = curDate.getTime() - startDate.getTime();
                    while (diff < maxWaitTime) {
                        //   Log.e("XEE","开始等待接受数据："+diff+" < ? "+maxWaitTime);
                        if ((temp = inputStream.read(buffer)) != -1) {
                            break;
                        }
                        else{
                            //如果socket已经异常，不需在尝试读写数据直到超时 xfh20180718 add
                            if(socket.isConnected()==false || socket.isClosed()){
                                if (tcpHandler != null) {
                                    tcpHandler.socketTimeOut();
                                }
                                break;
                            }
                        }
                        curDate = new Date(System.currentTimeMillis());
                        diff = curDate.getTime() - startDate.getTime();
                         if(MLog.isDebug){
                             MLog.e("XEE","ReceiveData count="+temp+" diff："+diff+" < ? "+maxWaitTime);
                         }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //提示超时
                    if (diff > maxWaitTime && tcpHandler != null) {
                        Log.e("XEE", "超时 maxWaitTime=diff=" + diff + " tcpHandler=null?" + (tcpHandler == null ? null : false));
                        if (tcpHandler != null) {
                            tcpHandler.socketTimeOut();
                        }
                    }
                }
            //}
        //});
        /*thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        if (temp > 0)
            return Arrays.copyOfRange(buffer, 0, temp);
        else {
            //           Log.e("XEE","超时xxxxx");
            byte[] returnData = null;
            returnData = new byte[0];
            return returnData;
        }
    }

    /**
     * 清空缓冲区
     * socket相关的连接，数据处理无需放在工作线程中，因为thread.join本身会阻塞调用线程。
     * 因此功能调用肯定会放在工作线程中，再创建工作线程会造成资源浪费 xfh2021/10/18 修改。
     * @return
     */
    public void clearReceiveData() {

        //thread = new Thread(new Runnable() {
        //    @Override
        //    public void run() {
                //清空缓冲区
                final byte buffer[] = new byte[1024];
                final int count;
                try {
                    if (socket != null && socket.getInputStream().available() > 0) {
                        count = socket.getInputStream().read(buffer);
                        while (count == 1024) {
                            socket.getInputStream().read(buffer);
                        }
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            //}
        //});
        /*thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    /**
     * 根据ip和端口连接服务端
     *
     * @param strIP
     * @param nPort
     * @return
     */
    public void connect(final String strIP, final int nPort, TCPHandler tcpHandler) {
        connectEx(strIP, nPort, tcpHandler);
    }

    /**
     * 根据ip和端口连接服务端
     * socket相关的连接，数据处理无需放在工作线程中，因为thread.join本身会阻塞调用线程。
     * 因此功能调用肯定会放在工作线程中，再创建工作线程会造成资源浪费 xfh2021/10/18 修改。
     * @param strIP
     * @param nPort
     * @return
     */
    public boolean connectEx(final String strIP, final int nPort, TCPHandler tcpHandler) {
        this.tcpHandler = tcpHandler;
        boolean isSuccess = false;
        //thread = new Thread(new Runnable() {
        //    @Override
        //    public void run() {
                try {
                    close();
                    socket = new Socket(strIP, nPort);
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            //}
        //});
        /*thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        try {
            if (socket != null && socket.isConnected()) {
                if (tcpHandler != null) {
                    tcpHandler.connectSuccess();
                }
                isSuccess = true;
            } else {
                if (tcpHandler != null) {
                    tcpHandler.connectFailed();
                }
                isSuccess = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (tcpHandler != null) {
                tcpHandler.connectFailed();
            }
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * 发送数据
     * socket相关的连接，数据处理无需放在工作线程中，因为thread.join本身会阻塞调用线程。
     * 因此功能调用肯定会放在工作线程中，再创建工作线程会造成资源浪费 xfh2021/10/18 修改。
     * @param sendBuffer
     * @return
     */
    public int send(final byte[] sendBuffer,final int offset,final int length) {
        if (socket == null || sendBuffer == null) {
            return 0;
        }
        //thread = new Thread(new Runnable() {
        //    @Override
        //    public void run() {
                try {
                    // 获取Socket的OutputStream对象用于发送数据。
                    OutputStream outputStream = socket.getOutputStream();
                    if(MLog.isDebug){
                        MLog.d("TCPSocketController", String.format("sendTCPData length:%s,offset:%s,input length:%s",sendBuffer.length,offset, length));
                    }
                    outputStream.write(sendBuffer, offset, length);
                    // 发送读取的数据到服务端
                    outputStream.flush();
                    returnValue = length;
                } catch (UnknownHostException e) {
                    returnValue = -2;
                    e.printStackTrace();
                } catch (IOException e) {
                    returnValue = -3;
                    e.printStackTrace();
                } finally {

                }

        //    }
        //});
       /* thread.start();
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return returnValue;
    }

    /**
     * 断开tcp
     */
    public void close() {
        if (tcpHandler != null) {
            tcpHandler.connectClosed();
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket = null;
            }
        }
    }

    /**
     * 检查socket是否可用
     * @return
     */
    public boolean isValid(){
        if(socket!=null) {
            if (socket.isConnected() == false || socket.isClosed()) {
                return false;
            }
            else{
                return true;
            }
        }
        else{
            return false;
        }
    }
}
