package com.cnlaunch.physics.impl;

public interface IAssitsPhysicsMatcher {
    /**
     * 是否匹配，这里以传入命令格式来匹配
     * @param commandBuffer
     * @param offset
     * @param count
     * @return
     */
    boolean isMatch(byte[] commandBuffer,int offset,int count);
}
