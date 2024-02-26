package com.cnlaunch.physics.utils.remote;
import java.util.Arrays;

import com.cnlaunch.physics.utils.ByteBufferStream;
import com.cnlaunch.physics.utils.MLog;

/**
 * 远程诊断信息接收发送缓冲
 * 
 * @author xiefeihong
 *
 */
public class DPUCommandStream extends  ByteBufferStream {
	private static final String TAG = "DPUCommandStream";
	private final byte[] START_CODE;
	public DPUCommandStream() {
		super();
		START_CODE = new byte[] {0x55, (byte)0xaa, (byte)0xf8, (byte)0xf0};// LH add 2015.10.22
	}
	/**
	 * 读取需考虑并发处理
	 * @return
	 */
	@Override
	public synchronized void write(byte[] writeBuffer, int offset, int count) {
		super.write(writeBuffer, offset, count);
	}
/**
 * 读取需考虑并发处理
 * @return
 */
	public synchronized DPUCommand  readDPUCommand() {
		DPUCommand message = null;
		return message;
	}
}