package com.cnlaunch.physics.utils.remote;

import com.cnlaunch.physics.impl.IPhysics;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 加密设备接收数据流
 * Created by xiefeihong on 2018/11/21.
 */

public class EncryptionDiagnoseDataStream extends ReadByteDataStream {
    public EncryptionDiagnoseDataStream(IPhysics iPhysics, InputStream inStream, OutputStream outStream) {
        super(iPhysics, inStream, outStream);
    }

    @Override
    public void clearTotalBuffer() {
        super.clearTotalBuffer();
    }

    @Override
    public void run() {
        super.run();
    }

    @Override
    public void cancel() {
        super.cancel();
    }
}
