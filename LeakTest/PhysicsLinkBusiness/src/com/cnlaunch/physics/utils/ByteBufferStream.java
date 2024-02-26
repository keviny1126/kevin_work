package com.cnlaunch.physics.utils;

/**
 * 字节流输入输出缓冲
 * 
 * @author xiefeihong
 *
 */
public class ByteBufferStream {
	private static final String TAG = "ByteBufferStream";
	protected byte[] buffer;
	protected int position;
	protected int length;
	protected int capacity;

	public ByteBufferStream() {		
		position = 0;
		length = 0;
		capacity = 5120; //1024*5 默认大小
		buffer = new byte[capacity];
	}
	@Override
	protected void finalize() {
		try {	
			buffer = null;
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从此输入流中读取下一个数据字节。返回一个 0 到 255 范围内的 int 字节值。如果因为到达流末尾而没有可用的字节，则返回值 -1。
	 * @return
	 */
	protected int  readByte() {
		if (this.position >= this.length) {
			return -1 ;
		}
		return (this.buffer[this.position++]) & 0XFF ;
	}
	protected byte[] readBytes(int count) {
		int num = this.length - this.position;
		if (num > count) {
			num = count;
		}
		if (num <= 0) {
			return null;
		}
		byte[] buffer = new byte[num];
		System.arraycopy(this.buffer, this.position, buffer, 0, num);
		this.position += num;
		return buffer;
	}
	protected int  readBytes(byte[] readbuffer) {
		int readbufferLength = 0;
		if(readbuffer!=null){
			readbufferLength = readbuffer.length;
		}
		return readBytes(readbuffer,0,readbufferLength);
	}
	/**
	 * 从起始位置读取制定的字节数到读缓冲中
	 * @param readbuffer
	 * @param offset
	 * @param count
	 * @return 实际读取的字节数
	 */

	protected int  readBytes(byte[] readbuffer,int offset,int count) {
		if ((count) < 0 || count > readbuffer.length - offset) {
			throw new IndexOutOfBoundsException("invalid offset or length");
		}
		this.position = 0;
		int num = this.length;
		if (num > count) {
			num = count;
		}
		if (num <= 0) {
			return 0;
		}
		if (MLog.isDebug) {
			MLog.d(TAG, String.format("readBytes this.position=%d", this.position));
		}
		System.arraycopy(this.buffer, this.position, readbuffer, offset, num);
		remove(num);
		return num;
	}
	private void ensureCapacity(int value) {		
		if (value <= this.capacity - this.length)
			return;
		// ********* end 观察效果中 *****************

		int num1 = value;
		if (num1 < 0x100)
			num1 = 0x100;
		if (num1 < (this.capacity * 2))
			num1 = this.capacity * 2;
		byte[] buffer1 = new byte[num1];
		if (this.length > 0)
			System.arraycopy(this.buffer, 0, buffer1, 0, this.length);
		this.buffer = buffer1;
		this.capacity = num1;
	}
	protected  void write(byte[] writeBuffer) {
		int writeBufferLength = 0;
		if(writeBuffer!=null){
			writeBufferLength = writeBuffer.length;
		}
		write(writeBuffer, 0, writeBufferLength);
	}
	/**
	 * 写入在实际逻辑中需考虑并发处理
	 * @param writeBuffer
	 * @param offset
	 * @param count
	 */
	protected  void write(byte[] writeBuffer, int offset, int count) {
		if ((count) < 0 || count > writeBuffer.length - offset) {
			throw new IndexOutOfBoundsException("invalid offset or length");
		}
		if (writeBuffer.length - offset < count) {
			count = writeBuffer.length - offset;
		}
		ensureCapacity(writeBuffer.length + count);
		//无需增加系统负担，无效语句
		//Arrays.fill(buffer, length, capacity,(byte)0);
		System.arraycopy(writeBuffer, offset, buffer, this.length, count);
		this.length += count;
		if(MLog.isDebug){
			MLog.d(TAG, "write this.length ="+this.length);
		}
	}
	
	protected void remove(int count) {
		if (count > 0) {
			if (length >= count) {
				System.arraycopy(buffer, count, buffer, 0, length - count);
				length -= count;
				// 无需增加系统负担，无效语句
				// Arrays.fill(buffer, length, capacity,(byte)0);
			} else {
				length = 0;
				// 无需增加系统负担，无效语句
				// Arrays.fill(buffer, 0, capacity,(byte)0);
			}
		}
	}
}