package com.cnlaunch.physics.utils.message;
/**
 * 远程相关诊断信息信封接口
 * @author xiefeihong
 *
 */
public interface IEnvelope {    
	void setLetter(ILetter letter);

	ILetter getLetter();

	void setCategory(int category);

	int getCategory();

	void setSource(int source);

	int getSource();

	byte[] toBytes();
}