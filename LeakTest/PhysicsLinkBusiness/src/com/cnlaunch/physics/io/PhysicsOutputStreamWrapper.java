package com.cnlaunch.physics.io;
import com.cnlaunch.physics.IPhysicsOutputStreamBufferWrapper;

import java.io.OutputStream;
import java.io.IOException;

/**
 * 
 *
 * 蓝牙输出流代理.
 */
public final class PhysicsOutputStreamWrapper extends AbstractPhysicsOutputStream {
    private OutputStream mOutputStream;
    public PhysicsOutputStreamWrapper(OutputStream outputStream, IPhysicsOutputStreamBufferWrapper physicsOutputStreamBufferWrapper) {
       super(physicsOutputStreamBufferWrapper);
        mOutputStream = outputStream;
    }
    @Override
    public void close() throws IOException {
        mOutputStream.close();
    }

    @Override
    public void flush() throws IOException {
        mOutputStream.flush();
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        mOutputStream.write(buffer,0,buffer.length);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        if (buffer == null) {
            throw new NullPointerException("wrapperBuffer is null");
        }
        if ((count) < 0 || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException("invalid offset or length");
        }
        byte[] wrapperBuffer = null;
        if(mPhysicsOutputStreamBufferWrapper!=null && mPhysicsOutputStreamBufferWrapper.isNeedWrapper()){
            wrapperBuffer = mPhysicsOutputStreamBufferWrapper.writeBufferWrapper(buffer,offset,count);
            if (wrapperBuffer == null) {
                throw new NullPointerException("wrapperBuffer is null");
            }
            mOutputStream.write(wrapperBuffer, 0, wrapperBuffer.length);
        }
        else {
            mOutputStream.write(buffer, offset, count);
        }
    }

    @Override
    public void write(int oneByte) throws IOException {
        mOutputStream.write(oneByte);
    }
}
