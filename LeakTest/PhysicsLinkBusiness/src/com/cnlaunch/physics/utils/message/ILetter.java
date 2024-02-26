package com.cnlaunch.physics.utils.message;
/**
 * 远程相关诊断信息信件接口
 * @author xiefeihong
 *
 */
public interface ILetter {
	void setReceiver(final String receiver);

	String getReceiver();

	void setSender(final String sender);

	String getSender();

	void setContent(final String content);

	String getContent();
	String toJSONString();
}