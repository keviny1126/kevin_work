package com.cnlaunch.physics.utils.remote;

import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.message.MessageStream;
import com.cnlaunch.physics.utils.message.RemoteMessage;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 使用短距离远程两端设备接收数据流
 * 比如手机跟手机
 * Created by xiefeihong on 2018/11/21.
 */

public class RemoteClientDiagnoseDataStreamProcessor {
    private static final String TAG = RemoteClientDiagnoseDataStreamProcessor.class.getSimpleName();
    private IPhysics mIPhysics;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private MessageStream messageStream;
    private ReadByteDataStream readByteDataStream;
    public RemoteClientDiagnoseDataStreamProcessor(ReadByteDataStream readByteDataStream,IPhysics iPhysics, InputStream inStream, OutputStream outStream) {
        mInStream = inStream;
        mOutStream = outStream;
        mIPhysics = iPhysics;
        messageStream = null;
        this.readByteDataStream = readByteDataStream;
    }
    public void dataItemProcess() {
        if (MLog.isDebug) {
            MLog.d(TAG, "RemoteClientDiagnoseDataStreamProcessor  dataItemProcess() ");
        }
        if (messageStream == null) {
            messageStream = new MessageStream();
        }
        messageStream.write(readByteDataStream.buffer, 0, readByteDataStream.bytes);
        while (true) {
            RemoteMessage message = messageStream.readMessage();
            if (message != null) {
                String result = ByteHexHelper.bytesToHexStringWithSearchTable(message.toBytes());
                if (MLog.isDebug) {
                    MLog.d(TAG, "remoteClientDiagnoseModeProcess  result = " + result);
                }
                mIPhysics.setCommand(result);
            } else {
                break;
            }
        }
    }
    public void clearTotalBuffer(){}

    public void cancel() {}
}
