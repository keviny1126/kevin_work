package com.cnlaunch.physics.utils.remote;
import android.os.Environment;

import com.cnlaunch.physics.RomoteLocalSwitch;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.MLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 通用诊断数据接收数据流
 * 用于平板与OBD接头通讯方式
 *
 * Created by xiefeihong on 2018/11/21.
 *
 */
public class CommonDiagnoseDataStreamProcessor {
    private static final String TAG = CommonDiagnoseDataStreamProcessor.class.getSimpleName();
    protected IPhysics mIPhysics;
    protected InputStream mInStream;
    protected OutputStream mOutStream;
    protected final int FIXED_LENGTH = 7;
    protected final int HEAVYDUTY_FIXED_LENGTH = 2;
    protected static final String UPDATE_OK_RECEIVE_COMMAND = "4f4b21";
    protected static final String RESET_CONNECTOR_RECEIVE_COMMAND = "3f";
    protected final byte[] START_CODE;
    protected final byte[] SMARTBOX30_LINUX_START_CODE;
    protected final byte[] HEAVYDUTY_START_CODE;

    protected boolean isTruck = false;
    protected boolean isCarAndHeavyduty = false;
    protected BufferedWriter bufferedWriter;
    protected SimpleDateFormat mSimpleDateFormat;
    protected boolean isCommunicateTest;

    protected boolean isUpdate;
    protected boolean isAvailable;

    protected ReadByteDataStream readByteDataStream;
    protected int totalBytes; //当前totalBuffer 使用的总长度
    public CommonDiagnoseDataStreamProcessor(ReadByteDataStream readByteDataStream, IPhysics iPhysics, InputStream inStream, OutputStream outStream) {
        mInStream = inStream;
        mOutStream = outStream;
        mIPhysics = iPhysics;
        START_CODE = new byte[]{0x55, (byte) 0xaa, (byte) 0xf8, (byte) 0xf0};// LH add 2015.10.22
        SMARTBOX30_LINUX_START_CODE = new byte[]{0x55, (byte) 0xaa, (byte) 0xf8, (byte) 0xf1};// smartbox30 linux support
        HEAVYDUTY_START_CODE = new byte[]{0x55, (byte) 0xaa};// LH add 2015.10.22

        isTruck = RomoteLocalSwitch.getInstance().isTruck(mIPhysics.getSerialNo());
        isCarAndHeavyduty = RomoteLocalSwitch.getInstance().isCarAndHeavyduty(mIPhysics.getSerialNo());
        this.readByteDataStream = readByteDataStream;
        totalBytes = 0;
        //用于数据通讯测试 xfh2017/09/12
        isCommunicateTest = false;
        if (isCommunicateTest) {
            mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.ENGLISH);
            String testFile = Environment.getExternalStorageDirectory().getPath() + "/cnlaunch/dpu_data_store.txt";
            FileInputStream fis;
            try {
                fis = new FileInputStream(new File(testFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(testFile, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void clearTotalBuffer() {
        totalBytes = 0;
    }

    public void dataItemProcess() {
        if (MLog.isDebug) {
            MLog.d(TAG, "CommonDiagnoseDataStreamProcessor  dataItemProcess() ");
        }
        //用于数据通讯测试 xfh2017/09/12
        if (isCommunicateTest) {
            try {
                bufferedWriter.write("\nAnswer(" + mSimpleDateFormat.format(new Date()) + "):" + ByteHexHelper.bytesToHexStringWithSearchTable(readByteDataStream.buffer, 0, readByteDataStream.bytes));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cacheData();
        totalBytes += readByteDataStream.bytes;
        //控制日志输出，主要是一些平台资源受限 比如ait2（MT6739芯片）
		/*if (MLog.isDebug) {
				MLog.d(TAG, "ReadByteDataStream.run(). totalBuffer=" + ByteHexHelper.bytesToHexStringWithSearchTable(totalBuffer, 0, totalBytes));
		}*/
        if (isTruck && !isCarAndHeavyduty) {
            truckDataProcess();
        } else {
            carOrCarAndHeavydutyDataProcess();
        }
    }

    /**
     * 缓存接收到的数据
     */
    protected void cacheData() {
        if (totalBytes + readByteDataStream.bytes <= readByteDataStream.maxbufferSize) {
            System.arraycopy(readByteDataStream.buffer, 0, readByteDataStream.totalBuffer, totalBytes, readByteDataStream.bytes);
        } else {
            totalBytes = 0;
            System.arraycopy(readByteDataStream.buffer, 0, readByteDataStream.totalBuffer, totalBytes, readByteDataStream.bytes);
        }
    }

    public void cancel() {
        try {
            //用于数据通讯测试 xfh2017/09/12
            if (isCommunicateTest) {
                bufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 数据通讯过程中蓝牙连接中断
     */
    /*private void connectionLost() {
        if(mIPhysics != null){
            Context context =  mIPhysics.getContext();
            context.sendBroadcast(new Intent("DeviceConnectLost"));
        }
    }*/

    /**
     * 重卡车型通讯数据接收处理
     */
    protected void truckDataProcess() {
        String order = ByteHexHelper.bytesToHexStringWithSearchTable(readByteDataStream.totalBuffer, 0, totalBytes);
        // 复位接头,判断以3f,3f3f,3f3f3f,ff3f,ff3f3f,ff3f3f3f开头;
        if (mIPhysics.isTruckReset()) {
            if (("3f").equalsIgnoreCase(order) || ("3f3f").equalsIgnoreCase(order) || ("3f3f3f").equalsIgnoreCase(order) || "ff3f".equalsIgnoreCase(order) || ("ff3f3f").equalsIgnoreCase(order) || ("ff3f3f3f").equalsIgnoreCase(order) || ("3f3f3f3f").equalsIgnoreCase(order) || ("3f3f3f3f3f").equalsIgnoreCase(order) || ("3f3f3f3f3f3f").equalsIgnoreCase(order) || ("3f3f3f3f3f3f3f").equalsIgnoreCase(order)) {
                if (mIPhysics != null) {
                    mIPhysics.setCommand(RESET_CONNECTOR_RECEIVE_COMMAND);
                    mIPhysics.setCommand_wait(false);
                    mIPhysics.setIsTruckReset(false);
                }
                isUpdate = true;
                return;
            }
        }
        // return update result
        if ((order.indexOf(UPDATE_OK_RECEIVE_COMMAND) > -1) && isUpdate) {
            if (mIPhysics != null) {
                mIPhysics.setCommand(UPDATE_OK_RECEIVE_COMMAND);
                mIPhysics.setCommand_wait(false);
            }
            isUpdate = false;
            return;
        }
        // 验证包正确性
        int index = ByteHexHelper.bytesIndexOf(readByteDataStream.totalBuffer, HEAVYDUTY_START_CODE, 0, totalBytes);// 数据中是否包含0x55,0xaa,0xf8,x0f0
        // 数据中包含0x55,0xaa
        if (index >= 0) {
            if (index > 0) {
                int newTotalBytes = totalBytes - index;
                System.arraycopy(readByteDataStream.totalBuffer, index, readByteDataStream.totalBuffer, 0, newTotalBytes);
                totalBytes = newTotalBytes;
            }
            if (totalBytes >= 4) {
                int length = (readByteDataStream.totalBuffer[2] & 0xff) * 256 + (readByteDataStream.totalBuffer[3] & 0xff);
                int totalLength = HEAVYDUTY_FIXED_LENGTH + length;
                if (totalBytes >= totalLength) {
                    // MLog.d(TAG,"bytesToHexStringWithSearchTable start");
                    String command = ByteHexHelper.bytesToHexStringWithSearchTable(readByteDataStream.totalBuffer, 0, totalLength);
                    mIPhysics.setCommand(command);
                    //控制日志输出，主要是一些平台资源受限 比如ait2（MT6739芯片）
									/*if (MLog.isDebug) {
										MLog.e(TAG, command);
									}*/
                    // MLog.d(TAG,"bytesToHexStringWithSearchTable end");
                    mIPhysics.setCommand_wait(false);
                    // 复位原状态
                    totalBytes = totalBytes - totalLength;
                    if (totalBytes > 0) {
                        System.arraycopy(readByteDataStream.totalBuffer, totalLength, readByteDataStream.totalBuffer, 0, totalBytes);
                    }
                }
            }
        }
    }

    /**
     * 小车车型或者二合一车型通讯数据接收处理
     */
    protected void carOrCarAndHeavydutyDataProcess() {
        // 验证包正确性
        isAvailable = false;
        int index = ByteHexHelper.bytesIndexOf(readByteDataStream.totalBuffer, START_CODE, 0, totalBytes);// 数据中是否包含0x55,0xaa,0xf8,x0f0
        // 数据中包含0x55,0xaa,0xf8,x0f0
        if (index >= 0) {
            isAvailable = true;
        } else {
            index = ByteHexHelper.bytesIndexOf(readByteDataStream.totalBuffer, SMARTBOX30_LINUX_START_CODE, 0, totalBytes);// 数据中是否包含0x55,0xaa,0xf8,x0f1
            // 数据中包含0x55,0xaa,0xf8,x0f1
            if (index >= 0) {
                isAvailable = true;
            }
        }
        if (isAvailable) {
            if (index > 0) {
                int newTotalBytes = totalBytes - index;
                System.arraycopy(readByteDataStream.totalBuffer, index, readByteDataStream.totalBuffer, 0, newTotalBytes);
                totalBytes = newTotalBytes;
            }
            if (totalBytes >= 6) {
                int length = (readByteDataStream.totalBuffer[4] & 0xff) * 256 + (readByteDataStream.totalBuffer[5] & 0xff);
                int totalLength = FIXED_LENGTH + length;
                //控制日志输出，主要是一些平台资源受限 比如ait2（MT6739芯片）
                /*if(MLog.isDebug) {
                    MLog.d(TAG, "totalBytes=" + totalBytes + " totalLength=" + totalLength);
                }*/
                if (totalBytes >= totalLength) {
                    //为保证数据准确性，加入包校验内容
                    //<包校验>：对“<目标地址>+<源地址>+<包长度>+<计数器>+<命令字>+<数据区>”等部分按字节进行异或运算，其结果等于“校验值 “
                    byte verify = readByteDataStream.totalBuffer[2];//略过起始地址 55 aa
                    for (int i = 3; i < totalLength - 1; i++) {// 校验数据(不包含校验字节本身)
                        verify ^= readByteDataStream.totalBuffer[i];
                    }
                    if (verify == readByteDataStream.totalBuffer[totalLength - 1]) {
                        // MLog.d(TAG,"bytesToHexStringWithSearchTable start");
                        String command = ByteHexHelper.bytesToHexStringWithSearchTable(readByteDataStream.totalBuffer, 0, totalLength);
                        mIPhysics.setCommand(command);
                        //控制日志输出，主要是一些平台资源受限 比如ait2（MT6739芯片）
						/*if (MLog.isDebug) {
							MLog.e(TAG, command);
						}*/
                        // MLog.d(TAG,"bytesToHexStringWithSearchTable end ");
                        mIPhysics.setCommand_wait(false);
                    }
                    // 复位原状态
                    totalBytes = totalBytes - totalLength;
                    if (totalBytes > 0) {
                        System.arraycopy(readByteDataStream.totalBuffer, totalLength, readByteDataStream.totalBuffer, 0, totalBytes);
                    }
                }
            }
        }
    }
}
