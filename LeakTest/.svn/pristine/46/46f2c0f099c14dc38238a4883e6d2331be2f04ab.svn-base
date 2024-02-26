package com.cnlaunch.physics.utils;

/**
 * DPU 的短整形数据
 * @see DPU通信协议 v1.06
 * @author weizewei
 *
 */
public class DPU_Short {
	public final static int TYPE_LEN = 2;// 2 字节 
	short value;
	
	public DPU_Short(short i)
	{
		this.value = i;
	}
	
	public byte[] toBytes()
	{
		byte[] ret = new byte[TYPE_LEN];
		ret[0] = (byte) (value >> 8);
		ret[1] = (byte) (value);
		return ret;
	}
	public int getLength()
	{
		return 2;
	}
	
	public static short bytesToDPUShort(byte[] data)
	{
		short val = (short) ((data[0]&0xff)<<8 | (data[1]&0xff));
		return val;
	}
	// test 
	public static void main(String[] args)
	{
		DPU_Short sh = new DPU_Short((short) 123);
		System.out.println(DPU_Short.bytesToDPUShort(sh.toBytes()));
	}
}