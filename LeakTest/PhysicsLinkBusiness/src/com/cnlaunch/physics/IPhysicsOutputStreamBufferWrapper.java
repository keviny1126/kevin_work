package com.cnlaunch.physics;

/**
 *通讯设备输出流写缓冲包装器接口
 * Created by xiefeihong on 2018/11/26.
 */

public interface IPhysicsOutputStreamBufferWrapper {
    boolean isNeedWrapper();
    byte[] writeBufferWrapper(byte[] buffer);
    byte[] writeBufferWrapper(byte[] buffer,int offset,int length);
}
