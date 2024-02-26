package com.cnlaunch.physics.utils;

import java.util.Formatter;
public class DPU_Long {
	public final static int TYPE_LEN = 4;
	long value;
	
	public DPU_Long(long value)
	{
		this.value = value;
	}
	
	public byte[] toBytes()
	{
		byte[] ret = new byte[TYPE_LEN];
		ret[0] = (byte) (value >> 24);
		ret[1] = (byte) (value >> 16);
		ret[2] = (byte) (value >> 8);
		ret[3] = (byte) (value);
		return ret;
	}
	
	public int getLength()
	{
		return 4;
	}
	
	public static long bytesToDPULong(byte[] data)
	{
		long val = (long) ((data[0]&0xFF)<<24 | (data[1]&0xFF)<<16 | (data[2]&0xFF)<<8 | (data[3]&0xFF));
		return val;
	}
	
	public static String bytesToHex(byte[] data)
	{
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {
			sb.append(new Formatter().format("%02x-", b));
		}
		return sb.toString();
	}
	
	// test 
	public static void main(String[] args)
	{
		DPU_Long lon = new DPU_Long((long) 123456);
		System.out.println(bytesToHex(lon.toBytes()));
		System.out.println(DPU_Long.bytesToDPULong(lon.toBytes()));
	}
}
