package com.cnlaunch.physics.utils.remote;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.MLog;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * obd 标定诊断数据接收数据流
 * 用于平板与OBD接头通讯方式
 *
 * Created by xiefeihong on 2018/11/21.
 *
 */
public class OBDCheckDiagnoseDataStreamProcessor extends CommonDiagnoseDataStreamProcessor  {
    private static final String TAG = OBDCheckDiagnoseDataStreamProcessor.class.getSimpleName();
    public OBDCheckDiagnoseDataStreamProcessor(ReadByteDataStream readByteDataStream,IPhysics iPhysics,InputStream inStream,OutputStream outStream) {
        super(readByteDataStream,iPhysics,inStream, outStream);
    }
    /**
     * 缓存接收到的数据
     */
    protected void cacheData() {
        if (totalBytes + readByteDataStream.bytes <= readByteDataStream.maxbufferSize) {
            System.arraycopy(readByteDataStream.buffer, 0, readByteDataStream.totalBuffer, totalBytes, readByteDataStream.bytes);
        } else {
            if(MLog.isDebug) {
                MLog.d(TAG, "allocation totalBuffer totalBytes=" + (totalBytes + readByteDataStream.bytes));
            }
            byte[] tempTotalBuffer = readByteDataStream.totalBuffer;
            readByteDataStream.totalBuffer=new byte[totalBytes + readByteDataStream.bytes];
            System.arraycopy(tempTotalBuffer, 0, readByteDataStream.totalBuffer, 0, totalBytes);
            System.arraycopy(readByteDataStream.buffer, 0, readByteDataStream.totalBuffer, totalBytes, readByteDataStream.bytes);
            tempTotalBuffer = null;
        }
    }
    /**
    *小车车型或者二合一车型通讯数据接收处理
     */
    @Override
    protected void carOrCarAndHeavydutyDataProcess(){
        if (MLog.isDebug) {
            MLog.d(TAG, "OBDCheckDiagnoseDataStreamProcessor  carOrCarAndHeavydutyDataProcess()");
        }
        // 验证包正确性
        isAvailable = false;
        while (true) {
            int index = ByteHexHelper.bytesIndexOf(readByteDataStream.totalBuffer, START_CODE, 0, totalBytes);// 数据中是否包含0x55,0xaa,0xf8,x0f0
            if(MLog.isDebug) {
                MLog.d(TAG, "totalBuffer totalBytes=" + totalBytes +" index =" +index);
            }
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
						    if (MLog.isDebug) {
							    MLog.e(TAG, "valid command="+command);
						    }
                            // MLog.d(TAG,"bytesToHexStringWithSearchTable end ");
                            mIPhysics.setCommand_wait(false);
                        }
                        // 复位原状态
                        totalBytes = totalBytes - totalLength;
                        if (totalBytes > 0) {
                            System.arraycopy(readByteDataStream.totalBuffer, totalLength, readByteDataStream.totalBuffer, 0, totalBytes);
                            continue;
                        }
                    }
                }
            }
            break;
        }
    }
}
