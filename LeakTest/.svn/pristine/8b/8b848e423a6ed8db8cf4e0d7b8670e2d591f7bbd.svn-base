/*
 Launch Android Client, SerialPortManager
 Copyright (c) 2014 LAUNCH Tech Company Limited
 http:www.cnlaunch.com
 */

package com.cnlaunch.physics.serialport;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.InsulationModuleControl;
import com.cnlaunch.physics.LinkParameters;
import com.cnlaunch.physics.ProductType;
import com.cnlaunch.physics.impl.IAssitsPhysics;
import com.cnlaunch.physics.impl.IAssitsPhysicsMatcher;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.io.PhysicsInputStreamWrapper;
import com.cnlaunch.physics.io.PhysicsOutputStreamWrapper;
import com.cnlaunch.physics.utils.Constants;
import com.cnlaunch.physics.utils.Tools;
import com.cnlaunch.physics.utils.remote.ReadByteDataStream;
import com.power.baseproject.utils.EasyPreferences;
import com.power.baseproject.utils.log.LogUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * [串口的控制类]
 *
 * @author nixiaoyan
 * @version 4.0
 * @date 2014-10-31
 * <p>
 * * @author 谢飞虹
 * @date 2016-10-10
 **/
public class SerialPortManager implements IPhysics, IAssitsPhysics {
    private static final String TAG = "SerialPortManager";
    private static String mReadData = "";
    private Context mContext;
    private ReadByteDataStream mReadByteDataStreamThread;
    private SerialPortCheckRunnable mSerialPortCheckRunnable;
    private SerialPort serialPort; // 串口

    private int mState;
    private boolean commandWait = true;
    private boolean isFix;
    private DeviceFactoryManager mDeviceFactoryManager;
    private String mSerialNo;
    private boolean mIsTruckReset;
    private String mProductType;
    private InputStream inputStream;
    private OutputStream outputStream;
    private LinkParameters.SerialPortParameters mSerialPortParameters;
    private IAssitsPhysicsMatcher mAssitsPhysicsMatcher;
    private boolean mIsRemoteClientDiagnoseMode;
    private boolean mIsSupportOneRequestMoreAnswerDiagnoseMode;
    private boolean mbIsSetCommandOfMainLink = false;
    private String mDevice;

    public SerialPortManager(DeviceFactoryManager deviceFactoryManager,
                             Context context, String serialNo,String device) {
        // 引用diagnoseactivity
        // 会使使用activity作为接头的fragment释放不掉
        mContext = context.getApplicationContext();
        this.mDevice = device;
        mDeviceFactoryManager = deviceFactoryManager;
        mReadByteDataStreamThread = null;
        mSerialPortCheckRunnable = null;
        mState = STATE_NONE;
        serialPort = null;
        mSerialNo = serialNo;
        mIsTruckReset = false;
        mProductType = EasyPreferences.Companion.getInstance().get(Constants.PRODUCTTYPE_KEY, "");
        inputStream = null;
        outputStream = null;
        mSerialPortParameters = null;
        mAssitsPhysicsMatcher = null;
        mIsRemoteClientDiagnoseMode = false;
        mIsSupportOneRequestMoreAnswerDiagnoseMode = false;
    }

    @Override
    protected void finalize() {
        try {
            LogUtil.INSTANCE.e(TAG, "finalize SerialPortManager");
            mHandler = null;
            serialPort = null;
            mSerialPortParameters = null;
            mAssitsPhysicsMatcher = null;
            super.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取读取到的完整指令
     *
     * @return
     */
    public String getCommand() {
//        LogUtil.INSTANCE.e(TAG, "获取读取到的完整指令" + mReadData);
        return mReadData;
    }

    /**
     * 设置完整指令内容
     */
    public void setCommand(String command) {
        mReadData = command;
        if (mbIsSetCommandOfMainLink) mDeviceFactoryManager.send(command);
    }

    public void setCommand(String command, boolean isSupportSelfSend) {
        if (isSupportSelfSend) mReadData = command;
        else setCommand(command);
    }

    /**
     * 获取读数据流
     */
    public InputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new PhysicsInputStreamWrapper(serialPort.getInputStream());
        }
        return inputStream;
    }

    /**
     * 获取写数据流
     */
    public OutputStream getOutputStream() {
        if (outputStream == null) {
            outputStream = new PhysicsOutputStreamWrapper(serialPort.getOutputStream(), mDeviceFactoryManager.getIPhysicsOutputStreamBufferWrapper());
        }
        return outputStream;
    }

    private void setState(int state) {
        mState = state;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public synchronized boolean getCommand_wait() {
        return commandWait;
    }

    @Override
    public synchronized void setCommand_wait(boolean wait) {
        commandWait = wait;
    }

    /**
     * 获取上下文
     */
    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public String getDeviceName() {
        return null;
    }

    @Override
    public void closeDevice() {
        if (mReadByteDataStreamThread != null) {
            mReadByteDataStreamThread.cancel();
            mReadByteDataStreamThread = null;
            if (mAssitsPhysicsMatcher == null) {
                mContext.sendBroadcast(new Intent(ACTION_DIAG_UNCONNECTED));
            }
        }

        if (mSerialPortCheckRunnable != null) {
            mSerialPortCheckRunnable.setStopState(true);
            mSerialPortCheckRunnable = null;
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        mSerialPortParameters = null;
        mAssitsPhysicsMatcher = null;
        setState(STATE_NONE);
    }

    /**
     * 打开串口，立即返回连接状态
     *
     * @return
     */
    public int connect() {
        if (mReadByteDataStreamThread != null) {
            mReadByteDataStreamThread.cancel();
            mReadByteDataStreamThread = null;
        }

        if (mSerialPortCheckRunnable != null) {
            mSerialPortCheckRunnable.setStopState(true);
            mSerialPortCheckRunnable = null;
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
            LogUtil.INSTANCE.d(TAG, "先关闭打开的串口");
        }
        serialPort = new SerialPort(mContext, mSerialPortParameters);
        serialPort.openSerialPort(mDevice);
        if (serialPort.getState() == SerialPort.OPEN) {
            //先检查诊断板是否上电
            if ((mProductType.toUpperCase(Locale.ENGLISH).contains(ProductType.HTT) && SerialPort.httDiagnoseSerialPortCheck(mProductType) == 2) ||
                    (mProductType.toUpperCase(Locale.ENGLISH).contains(ProductType.XXTOOL) && SerialPort.xxSerialPortCheckByOBDVoltage() == 2)) {
                connectionFailed(mContext.getString(R.string.msg_serialport_connect_state_fail_with_no_power));
                setState(STATE_CONNECTED);
                return getState();
            }
            LogUtil.INSTANCE.d(TAG, "打开串口成功开启读取数据的线程 mProductType=" + mProductType + " isFix=" + isFix);
            mReadByteDataStreamThread = new ReadByteDataStream(this, serialPort.getInputStream(), serialPort.getOutputStream());
            new Thread(mReadByteDataStreamThread).start();
            LogUtil.INSTANCE.d(TAG, "SerialPort connected success,starting transfer data ");
            mHandler.sendEmptyMessage(0);
            setState(STATE_CONNECTED);
            //监听诊断板状态
            if (mProductType.toUpperCase(Locale.ENGLISH).contains(ProductType.HTT)) {
                LogUtil.INSTANCE.d(TAG, "打开串口成功开启HTT串口电压值状态读取数据线程");
                mSerialPortCheckRunnable = new HTTSerialPortCheckRunnable();
                new Thread(mSerialPortCheckRunnable).start();
            } else if (mProductType.toUpperCase(Locale.ENGLISH).contains(ProductType.XXTOOL)) {
                LogUtil.INSTANCE.d(TAG, "打开串口成功开启XX串口电压值状态读取数据线程");
                mSerialPortCheckRunnable = new XXSerialPortCheckRunnable();
                new Thread(mSerialPortCheckRunnable).start();
            } else if (Tools.isNeedControlDiagnosePower(mContext) && !isFix &&
                    (mDeviceFactoryManager.isNeedExcludeVoltageValidCheck() == false)) {
                LogUtil.INSTANCE.d(TAG, "打开串口成功开启串口电压值状态读取数据线程");
                mSerialPortCheckRunnable = new ProLiteSerialPortCheckRunnable();
                new Thread(mSerialPortCheckRunnable).start();
            }
            return getState();
        } else {
            //connectionFailed();
            setState(STATE_NONE);
            return getState();
        }
    }

    public void clearTotalBuffer() {
        if (mReadByteDataStreamThread != null) {
            mReadByteDataStreamThread.clearTotalBuffer();
        }
    }

    private void connectionFailed() {
        connectionFailed(null);
    }

    private void connectionFailed(String message) {
        setState(STATE_NONE);
        //辅助通讯设备不能通过发送广播与外界通讯
        if (mAssitsPhysicsMatcher == null) {
            // 发送连接失败广播
            Intent intent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_FAIL);
            intent.putExtra(IS_CONNECT_FAIL, true);
            intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
            if (message == null) {
                intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.msg_serialport_connect_state_fail));
            } else {
                intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, message);
            }
            mContext.sendBroadcast(intent);
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                //辅助通讯设备不能通过发送广播与外界通讯
                if (mAssitsPhysicsMatcher == null) {
                    Intent intent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_SUCCESS);
                    intent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
                    intent.putExtra(IPhysics.CONNECT_MESSAGE_KEY, mContext.getString(R.string.msg_serialport_connect_state_success));
                    mContext.sendBroadcast(intent);
                    // 发送广播通知连接成功
                    mContext.sendBroadcast(new Intent(ACTION_DIAG_CONNECTED));
                }
            } else if (msg.what == 1) {
                //辅助通讯设备不能通过发送广播与外界通讯
                if (mAssitsPhysicsMatcher == null) {
                    Intent disconnectIntent = new Intent(IPhysics.ACTION_DPU_DEVICE_CONNECT_DISCONNECTED);
                    disconnectIntent.putExtra(IPhysics.IS_DOWNLOAD_BIN_FIX, isFix);
                    mContext.sendBroadcast(disconnectIntent);
                }
            }
        }
    };

    @Override
    public void setSerialNo(String serialNo) {
        mSerialNo = serialNo;
    }

    @Override
    public String getSerialNo() {
        return mSerialNo;
    }

    @Override
    public synchronized void setIsTruckReset(boolean isTruckReset) {
        mIsTruckReset = isTruckReset;
    }

    @Override
    public synchronized boolean isTruckReset() {
        return mIsTruckReset;
    }

    @Override
    public void userInteractionWhenDPUConnected() {
        if (mHandler != null) {
            Message message = mHandler.obtainMessage(0, 0, 0);
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void setIsFix(boolean isFix) {
        this.isFix = isFix;
    }

    @Override
    public void physicalCloseDevice() {
        closeDevice();
    }


    private abstract class SerialPortCheckRunnable implements Runnable {
        private boolean stopState;

        public synchronized boolean isStopState() {
            return stopState;
        }

        public synchronized void setStopState(boolean stopState) {
            this.stopState = stopState;
        }

    }

    private class XXSerialPortCheckRunnable extends SerialPortCheckRunnable {
        @Override
        public void run() {
            int state;
            while (isStopState() == false) {
                state = SerialPort.xxSerialPortCheckByOBDVoltage();
                if (state == 2) {
                    mHandler.sendEmptyMessage(1);
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class HTTSerialPortCheckRunnable extends SerialPortCheckRunnable {
        @Override
        public void run() {
            int state = 0;
            while (isStopState() == false) {
                state = SerialPort.httDiagnoseSerialPortCheck(mProductType);
                if (state == 2) {
                    mHandler.sendEmptyMessage(1);
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ProLiteSerialPortCheckRunnable extends SerialPortCheckRunnable {
        @Override
        public void run() {
            boolean state = false;
            while (isStopState() == false) {
                state = InsulationModuleControl.isVoltageValid();
                if (state == false) {
                    mHandler.sendEmptyMessage(1);
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void setIsRemoteClientDiagnoseMode(boolean isRemoteClientDiagnoseMode) {
        mIsRemoteClientDiagnoseMode = isRemoteClientDiagnoseMode;
    }

    @Override
    public boolean getIsRemoteClientDiagnoseMode() {
        return mIsRemoteClientDiagnoseMode;
    }

    //IAssitsPhysics 实现
    @Override
    public void setAssitsPhysicsMatcher(IAssitsPhysicsMatcher assitsPhysicsMatcher) {
        mAssitsPhysicsMatcher = assitsPhysicsMatcher;
    }

    @Override
    public IAssitsPhysicsMatcher getAssitsPhysicsMatcher() {
        return mAssitsPhysicsMatcher;
    }

    @Override
    public int getLinkMode() {
        return DeviceFactoryManager.LINK_MODE_COM;
    }

    @Override
    public void setLinkParameters(LinkParameters linkParameters) {
        mSerialPortParameters = linkParameters.getSerialPortParameters();
    }

    @Override
    public IPhysics getPhysics() {
        return this;
    }

    @Override
    public void setIsSupportOneRequestMoreAnswerDiagnoseMode(boolean isSupportOneRequestMoreAnswerDiagnoseMode) {
        mIsSupportOneRequestMoreAnswerDiagnoseMode = isSupportOneRequestMoreAnswerDiagnoseMode;
    }

    @Override
    public boolean getIsSupportOneRequestMoreAnswerDiagnoseMode() {
        return mIsSupportOneRequestMoreAnswerDiagnoseMode;
    }

    /**
     * @param isSetCommandOfMainLink 设置数据是否传给蓝牙主连接（针对蓝牙一对多的项目）
     */
    @Override
    public void setCommandStatus(boolean isSetCommandOfMainLink) {
        mbIsSetCommandOfMainLink = isSetCommandOfMainLink;
    }

    @Override
    public boolean getCommandStatus() {
        return mbIsSetCommandOfMainLink;
    }

    private String dataType;

    @Override
    public void setDataType(String type) {
        dataType = type;
    }

    @Override
    public String getDataType() {
        return dataType;
    }
}
