package com.cnlaunch.physics.downloadbin.util;

import java.util.ArrayList;

import com.cnlaunch.physics.entity.AnalysisData;
import com.cnlaunch.physics.listener.OnDownloadBinListener;
/**
 * 解析手机蓝牙与元征设备蓝牙数据
 */
public abstract class Analysis {

	public abstract AnalysisData analysis(byte[] sendCommand, byte[] receive);
	public abstract AnalysisData analysis(byte[] sendCommand, byte[] receive,OnDownloadBinListener mOnDownloadBinListener);
	public abstract AnalysisData analysisSmartbox30LinuxCommand(byte[] sendCommand, byte[] receive);
	public abstract AnalysisData analysisSmartbox30LinuxCommand(byte[] sendCommand, byte[] receive,OnDownloadBinListener mOnDownloadBinListener);
	public abstract boolean analysisData(byte[] sendCommand, byte[] receive);

	/**
	 * 跳转至download 代码入口
	 * 
	 * @param analysisData
	 * @return
	 */
	public abstract Boolean analysis2111(AnalysisData analysisData);
	
	/**
	 * 复位接头反馈
	 * 
	 * @param analysisData
	 * @return
	 */
	public abstract Boolean analysis2505(AnalysisData analysisData);
	
	/**
	 * 断开连接
	 * 
	 * @param analysisData
	 * @return
	 */
	public abstract Boolean analysis2504(AnalysisData analysisData);

	/**
	 * 验证安全密码指令
	 * 
	 * @param analysisData
	 * @return
	 */
	public abstract Boolean analysis2110(AnalysisData analysisData);

	/**
	 * 请求连接
	 * 
	 * @param analysisData
	 * @return
	 */
	public abstract String analysis2502(AnalysisData analysisData);

	/**
	 * 安全校验
	 * 
	 * @param analysisData
	 * @return
	 */
	public abstract Boolean analysis2503(AnalysisData analysisData);

	/**
	 * 读取当前状态
	 * Bootloader =0x00/download=0x01
	 * @param analysisData
	 * @return
	 */
	public abstract String analysis2114(AnalysisData analysisData);
	/**
	 * 解析downloadbin版本信息
	 * 
	 * @param analysisData
	 * @return
	 */
	public abstract ArrayList<String> analysis2105(AnalysisData analysisData);
	
	/**
	 * 切换到boot升级模式
	 */
	
	public abstract Boolean analysis2407(AnalysisData analysisData);
	
	public abstract Boolean analysis2402(AnalysisData analysisData);
	public abstract Boolean analysis2404(AnalysisData analysisData);
	public abstract Boolean analysis2405(AnalysisData analysisData);
	public abstract Boolean analysis2403(AnalysisData analysisData);

	/**
	 * 取DPU接头硬件版本信息
	 * 
	 * @param analysisData
	 * @return
	 */
	public abstract String[] analysis2103(AnalysisData analysisData);

	/**
	 * 复位DPU运行模式
	 * 
	 * @param analysisData
	 * @return
	 */
	public abstract Boolean analysis2109(AnalysisData analysisData);

	/**
	 * 判断蓝牙反馈的成功与失败的状态信息
	 * 
	 * @param returnValue
	 */
	public abstract void returnParam(String returnValue);
	/**
	 * 读当前接头是否已经烧录
	 * Bootloader =0x00/download=0x05
	 * @param analysisData
	 * @return
	 */
	public abstract  String analysis2107(AnalysisData analysisData);

	/**
	 *  解析车辆电压
	 * @author weizhongxuan
	 * @time 2016/10/13 14:41
	 */
	public abstract ArrayList<String> analysis2120(AnalysisData analysisData);
	public abstract String analysis2131(AnalysisData analysisData);
	public abstract Boolean analysis21xx(AnalysisData analysisData);
}
