/*
    Launch Android Client, SerialPort
    Copyright (c) 2014 LAUNCH Tech Company Limited
    http:www.cnlaunch.com
 */

package com.cnlaunch.physics.serialport;

import android.content.Context;
import android.os.Build;

import com.cnlaunch.physics.LinkParameters;
import com.cnlaunch.physics.ProductType;
import com.cnlaunch.physics.serialport.util.CTTools;
import com.cnlaunch.physics.serialport.util.HTT2Tools;
import com.cnlaunch.physics.serialport.util.LeakTestTools;
import com.cnlaunch.physics.serialport.util.LibraryLoader;
import com.cnlaunch.physics.serialport.util.XXTools;
import com.cnlaunch.physics.utils.Tools;
import com.power.baseproject.utils.log.LogUtil;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * [串口 实体类]
 *
 * @author nixiaoyan
 * @version 1.0
 * @date 2014-11-7
 **/
public class SerialPort {
    /**
     *
     **/
    private static final String TAG = "SerialPort";
    public FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private int state;
    private Context mContext;
    private LinkParameters.SerialPortParameters mSerialPortParameters;
    public final static int OPEN = 3;
    public final static int CLOSE = 0;

    static {
        LibraryLoader.load(LeakTestTools.SERIALPORT_LIB);
    }

    public SerialPort(Context context) {
        this(context, null);
    }

    public SerialPort(Context context, LinkParameters.SerialPortParameters serialPortParameters) {
        this.mContext = context;
        this.mSerialPortParameters = serialPortParameters;
        state = 0;
        setState(CLOSE);
    }
    public void openSerialPort() {
        openSerialPort(LeakTestTools.LEAK_DEVICE);
    }

    public void openSerialPort(String device) {
        LogUtil.INSTANCE.i(TAG, "open Serial Port");
        try {
            if (mSerialPortParameters != null) {
                LogUtil.INSTANCE.d(TAG, mSerialPortParameters.toString());
                openSerialPort(mSerialPortParameters.getDeviceName(), mSerialPortParameters.getBaudRate(), 0, mSerialPortParameters.getIsNeedHardwareFlowControl());
            } else {
                try {
                    LogUtil.INSTANCE.d(TAG, "open Serial Port ,device："+device);
                    if (device.equals(LeakTestTools.FACTORY_DEVICE)){
                        openSerialPort(LeakTestTools.FACTORY_DEVICE_NAME, LeakTestTools.FACTORY_BAUD_RATE, 0);
                    }else {
                        openSerialPort(LeakTestTools.DEVICE_NAME, LeakTestTools.BAUD_RATE, 0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //LogUtil.INSTANCE.d(TAG, "open Serial Port by mtk chip default serial devices");
                    //openSerialPort(LeakTestTools.DEVICE_NAME, LeakTestTools.BAUD_RATE, 0);
                }
            }
            setState(OPEN);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
            setState(CLOSE);
        }
    }

    public void openSerialPort(String device, int baudrate, int flags) throws SecurityException, IOException {
        openSerialPort(device, baudrate, flags, false);
    }

    public void openSerialPort(String device, int baudrate, int flags, boolean isNeedHardwareFlowControl) throws SecurityException, IOException {
        mFd = open(device, baudrate, flags);
        if (mFd == null) {
            LogUtil.INSTANCE.e(TAG, "open Serial Port fail ");
            throw new IOException();
        } else {
            LogUtil.INSTANCE.e(TAG, "open Serial Port success");
        }
        if (isNeedHardwareFlowControl) {
            openHardwareFlowControl();
        }
        tcflush();
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    private native static FileDescriptor open(String path, int baudrate, int flags);

    //open Hardware Flow Control
    public native void openHardwareFlowControl();

    public native void close();

    public native void tcflush();

    public native int getReadBufferByte();

    /**
     * htt类型设备检查诊断板是否上电，诊断板通过串口与系统板连接
     * 因为用于多线程，为控制文件访问，需要同步锁
     *
     * @return 0 正常 1 不支持该功能 2 断开
     */
    public synchronized static int httDiagnoseSerialPortCheck() {
        return httDiagnoseSerialPortCheck("HTT");
    }

    /**
     * htt类型设备检查诊断板是否上电，诊断板通过串口与系统板连接
     * 因为用于多线程，为控制文件访问，需要同步锁
     *
     * @return 0 正常 1 不支持该功能 2 断开
     */
    public synchronized static int httDiagnoseSerialPortCheck(String productType) {
        if (Build.MODEL.toUpperCase(Locale.ENGLISH).contains(ProductType.HTT_2) || productType.toUpperCase(Locale.ENGLISH).contains(ProductType.HTT_2)) {
            return htt2SerialPortCheck();
        } else {
            return serialPortCheck();
        }
    }

    private static int htt2SerialPortCheck() {
        //文件使用ascii '1'表示高电平，‘0’表示低电平
        int state = 0;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(HTT2Tools.VOLTAGE_SENSING_DEVICE_NAME);
            int voltage_sensing_value = fileReader.read();
            LogUtil.INSTANCE.d(TAG, "htt2SerialPortCheck count =" + voltage_sensing_value);
            if ((char) voltage_sensing_value == '1') {
                state = 0;
            } else {
                state = 2;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            state = 1;
        } catch (Exception e) {
            e.printStackTrace();
            state = 1;
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return state;
        }
    }


    /**
     * xx类型设备检查诊断板是否上电，诊断板通过串口与系统板连接
     * 因为用于多线程，为控制文件访问，需要同步锁
     *
     * @return 0 正常 1 不支持该功能 2 断开
     */
    public static int xxSerialPortCheck() {
        String ch = Tools.xxProjectBSKReadValueByDeviceFile(XXTools.VOLTAGE_SENSING_DEVICE_NAME);
        int state = 0;
        if (ch != null) {
            if (ch.equalsIgnoreCase("0")) {
                state = 0;
            } else if (ch.equalsIgnoreCase("1")) {
                state = 2;
            } else {
                state = 1;
            }
        } else {
            state = 1;
        }
        return state;
    }

    /**
     * xx类型设备检查诊断板是否上电,通过读取obd电压来判断
     * 因为用于多线程，为控制文件访问，需要同步锁
     *
     * @return 0 正常 1 不支持该功能 2 断开
     */
    public static int xxSerialPortCheckByOBDVoltage() {
        //没有12V时，应该只有0点几V，不会超过1V
        double obdVoltage = Tools.getXXProjectBSKOBDVoltage();
        int state = 0;
        if (obdVoltage == 0) {
            state = 1;
        } else if (obdVoltage <= 2) {
            state = 2;
        } else {
            state = 0;
        }
        return state;
    }

    /**
     * htt类型设备检查诊断板是否上电，诊断板通过串口与系统板连接
     *
     * @return 0 正常 1 不支持该功能 2 断开
     */
    private native static int serialPortCheck();
}
