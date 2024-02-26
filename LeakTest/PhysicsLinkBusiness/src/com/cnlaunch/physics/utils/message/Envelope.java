package com.cnlaunch.physics.utils.message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 将需要传递的信息通过Envelope类中提供的方法转换为byte[]类型，实现封包发送的功能
 * 将需要接受的信息通过Envelope类中提供的方法解析出来，实现拆包的功能 letter代表信件类，封装了业务需要传递的具体信息 消息类别为32位，4字节
 */
public class Envelope implements IEnvelope {
	private final int MESSAGE_CATEGORY_LENGTH = 4;
	private ILetter letter; // letter代表信件类，封装了业务需要传递的具体信息
	private int category = -1;
	private int source = -1;

	public Envelope() {
		letter = null;
		category = -1;
		source = -1;
	}

	public Envelope(int source, int category, ILetter letter) {
		this.source = source; // MessageSource 消息来源
		this.category = category; // MessageCategory 消息种类
		this.letter = letter;
	}

	public Envelope(byte[] binaryData) // 将二进制信息解析
	{
		source = getSource(binaryData);
		category = getCategory(binaryData);
		this.letter = Letter.fromString(getData(binaryData));
	}

	private String getData(byte[] binaryData) {
		return new String(binaryData, MESSAGE_CATEGORY_LENGTH, binaryData.length - MESSAGE_CATEGORY_LENGTH);
	}

	private int getCategory(byte[] binaryData) {
		// 读取消息类型信息
		if (binaryData.length < MESSAGE_CATEGORY_LENGTH) {
			throw new RuntimeException("消息数据太短，无法识别消息类型");
		}
		return ((((binaryData[0])) | (binaryData[1] << 0x8)) | (binaryData[2] << 0x10));
	}

	private int getSource(byte[] binaryData)// 读取消息来源信息
	{
		if (binaryData.length < MESSAGE_CATEGORY_LENGTH) {
			throw new RuntimeException("消息数据太短，无法识别消息来源");
		}
		return binaryData[3];
	}

	@Override
	public void setLetter(ILetter letter) {
		this.letter = letter;
	}

	@Override
	public ILetter getLetter() {
		return this.letter;
	}

	@Override
	public void setCategory(int category) {
		this.category = category; // MessageCategory 消息种类
	}

	@Override
	public int getCategory() {
		return this.category;
	}

	@Override
	public void setSource(int source) {
		this.source = source; // MessageSource 消息来源
	}

	@Override
	public int getSource() {
		return this.source;
	}

	@Override
	public byte[] toBytes() {
		if (letter != null) {
			int type = ((source << 0x18) | category);
			byte[] typeBuffer = RemoteMessage.intToBytes(type);
			byte[] letterContent = letter.toJSONString().getBytes();
			byte[] binaryData = new byte[typeBuffer.length + letterContent.length];
			System.arraycopy(typeBuffer, 0, binaryData, 0, typeBuffer.length);
			System.arraycopy(letterContent, 0, binaryData, typeBuffer.length, letterContent.length);
			return binaryData;
		} else {
			throw new RuntimeException("无效的消息");
		}
	}

}
