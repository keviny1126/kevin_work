package com.cnlaunch.physics.downloadbin.util;

import java.io.File;
import java.util.Locale;

import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.DPU_Long;
import com.cnlaunch.physics.utils.DPU_Short;
import com.cnlaunch.physics.utils.DPU_String;

public class DpuOrderUtils {
    /**
     * 过滤出返回的完整数据包
     * @param data
     * @return
     */	
	public static byte[] filterReturnDataPackage(byte[] data) {
		if (data.length > 0) {
			if (isValidPackageHeader(data)) {
				// 数据包长度
				int low8 = (int) (data[5] & 0xFF);
				int high8 = (int) (data[4] & 0xFF);
				int len = ((high8 << 8) | low8);
				int total_len = len + 7;// 总的数据包长度
				if (total_len < data.length)// 确保数组操作不会越界
				{
					byte[] Package = new byte[total_len];
					for (int i = 0; i < total_len; i++) {
						Package[i] = data[i];
					}
					return Package;
				}
			}
		}
		return data;// 放弃处理
	}
	/**
	 * @author  过滤出数据包的命令参数字节
	 * */
	public static byte[] filterOutCmdParameters(byte[] data) {
		byte[] pkg = filterReturnDataPackage(data);
		int pkg_total_len = pkg.length;
		int param_start_pos = 0;// 参数起始位置
		int param_end_pos = pkg_total_len - 2; // 参数结束位置
		int param_total_len = param_end_pos - param_start_pos + 1; // 参数字节数
		byte[] parameters = new byte[param_total_len];
		for (int i = 0, index = param_start_pos; i < param_total_len;) {
			parameters[i++] = pkg[index++];
		}
		return parameters;
	}
	public static int filterOutPackageLen(byte[] data) {
		if (isValidPackageHeader(data)) {
			// 数据包长度
			int low8 = (int) (data[5] & 0xFF);
			int high8 = (int) (data[4] & 0xFF);
			int len = ((high8 << 8) | low8);
			return len;
		}
		return 0; // 无效的数据包不处理 返回0长度
	}

	public static boolean isValidPackageHeader(byte[] data) {
		if (data[0] == 0x55 && data[1] == (byte) 0xaa) {
			return true;
		}
		return false;
	}
	/**
	 * 针对 2402命令的参数转换
	 * @param name 文件名
	 * @param file 文件对象，提供文件长度信息
	 * @return
	 */
	public static byte[] fileNameAndLength(String name,File file)
	{
		if(name ==null || file ==null)
			throw new NullPointerException("file name and file obj should not be null!");
		
		DPU_String fileName = new DPU_String(name.toUpperCase(Locale.US));// 文件名称,都是大写
		DPU_Long file_len = new DPU_Long(file.length());// 文件长度
		byte[] params = ByteHexHelper.appendByteArray(fileName.toBytes(),
									file_len.toBytes());
		return params;
	}
	
	/**
	 * 2403 命令需要的参数
	 * @param writePos
	 * @param dataChunk
	 * @param dataLen
	 * @return
	 */
	public static byte[] dataChunkParams(int writePos,byte[] dataChunk, int dataLen) 
	{
		DPU_Long write_pos = new DPU_Long(writePos);// 写入位置
		DPU_Short data_len = new DPU_Short((short) dataLen);// 数据长度
		byte[] params = null;
		params = ByteHexHelper.appendByteArray(write_pos.toBytes(),data_len.toBytes());
		params = ByteHexHelper.appendByteArray(params, dataChunk);
		return params;
	}
}
