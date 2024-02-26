package com.cnlaunch.physics.utils.message;

import com.cnlaunch.physics.utils.ByteBufferStream;
import com.cnlaunch.physics.utils.MLog;

/**
 * 远程诊断信息接收发送缓冲
 * 
 * @author xiefeihong
 *
 */
public class MessageStream extends  ByteBufferStream {
	private static final String TAG = "MessageStream";

	public MessageStream() {		
		super();
	}	
	private int readInt() {
		int num = this.position += 4;
		if (num > this.length) {
			this.position = this.length;
			return -1;
		}
		return RemoteMessage.bytesToInt(buffer,  this.position-4);
	}
	@Override
	public synchronized void write(byte[] writeBuffer, int offset, int count) {
		super.write(writeBuffer, offset, count);
	}
/**
 * 读取需考虑并发处理
 * @return
 */
	public synchronized RemoteMessage readMessage() {
		RemoteMessage message = null;
		this.position = 0;
		if (this.length > 6) {
			message = new RemoteMessage();
			int currentByte = readByte();
			message.setTypecode((byte)currentByte );
			currentByte = readByte();
			message.setFlag((byte)currentByte);
			message.setSize(readInt());
			if(MLog.isDebug){
				MLog.d(TAG, "readMessage()="+message.getSize()+" this.length ="+this.length+" this.position ="+ this.position);
			}
			if (message.getSize() <= 0 || message.getSize() <= this.length - this.position) {
				if (message.getSize() > 0) {
					message.setContent(readBytes(message.getSize()));
				}
				remove(message.getSize() + 6);
			} 
			else{
				message = null;
			}
		} 
		return message;
	}
}