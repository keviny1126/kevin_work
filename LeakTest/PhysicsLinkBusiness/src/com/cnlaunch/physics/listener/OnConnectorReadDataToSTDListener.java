package com.cnlaunch.physics.listener;

public interface OnConnectorReadDataToSTDListener {
	/**
	 * 接收接头数据转发给STD动态库
	 * @param pBuffer
	 */
	public void OnConnectorReadDataToSTDCallback(byte[] pBuffer);
}
