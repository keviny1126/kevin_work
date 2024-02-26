/*
    Launch Android Client, OnBluetoothListener
    Copyright (c) 2014 LAUNCH Tech Company Limited
    http:www.cnlaunch.com
*/

package com.cnlaunch.physics.listener;


/**
 * DownLoadBin升级回调
 * [A brief description]
 * 
 * @author bichuanfeng
 * @version 1.0
 * @date 2014-3-7
 * 
 *
 */
public interface OnDownloadBinListener {

	/**
	 * 
	 * @param state 下载状态
	 * @param progress 下载进度
	 */
	public void OnDownloadBinListener(int state, long progress,long length);
	/**
	 * 
	 * @param state 状态
	 * @param version 下位机版本号 例:V11.16
	 */
	public void OnDownloadBinListener(int state, String version);
	
	/**
	 * 监听数据收发，提高页面交互效果
	 * @param data
	 */
	public void OnDownloadBinCmdListener(String data);
}
