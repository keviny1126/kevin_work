package com.cnlaunch.physics.serialport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.MLog;
import com.power.baseproject.utils.EasyPreferences;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class ReadSerialNoThread extends Thread {

	public static final String Builder = android.os.Build.DISPLAY;
//	public static int x = 0;
	//private String rText = "";
	/**** add by ygm 2014.5.19 */
	public static int recvCount = 0;
	public static int recvSize[] = new int[8];
	public static int recvLoop = 0;
	byte recvBuf[] = new byte[1024];

	public String getSerialportNO() {
		return serialportNO;
	}

	public void setSerialportNO(String serialportNO) {
		this.serialportNO = serialportNO;
	}

	public String getSerialportID() {
		return serialportID;
	}

	public void setSerialportID(String serialportID) {
		this.serialportID = serialportID;
	}

	private String serialportNO = "";
	private String serialportID = "";
	private String stringdownloadbin = "";
	/** 5.14 增加设备类型 */
	private String category = "";

	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private Context context;

	public OutputStream getmOutputStream() {
		return mOutputStream;
	}

	public void setmOutputStream(OutputStream mOutputStream) {
		this.mOutputStream = mOutputStream;
	}

	public ReadSerialNoThread(SerialPort serialPort, Context context) {
		this.mInputStream = serialPort.getInputStream();
		this.mOutputStream = serialPort.getOutputStream();
		this.context = context;
	}

	public void run() {
		super.run();
		while (!isInterrupted()) {
			int size;
			try {
				byte[] buffer = new byte[1024];
				for (int i = 0; i < 256; i++)
					buffer[i] = 0x00;
				if (mInputStream == null)
					return;
				size = mInputStream.read(buffer);
				if (size > 0) {
					onDataReceived(buffer, size);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
// 转化字符串为协议的指令
	public byte[] toByte(String str) {
		byte d[];
		int c, tmp;
		String str1 = str.replace(" ", "");
		d = new byte[str1.length() / 2];
		for (int i = 0; i < str1.length();) {
			tmp = (int) str1.substring(i, i + 1).getBytes()[0];
			if (tmp > 0x60)
				c = (tmp - 0x61 + 10) * 16;
			else if (tmp > 0x40)
				c = (tmp - 0x41 + 10) * 16;
			else
				c = (tmp - 0x30) * 16;
			i++;
			tmp = (int) str1.substring(i, i + 1).getBytes()[0];
			if (tmp > 0x60)
				c = c + (tmp - 0x61 + 10);
			else if (tmp > 0x40)
				c = c + (tmp - 0x41 + 10);
			else
				c = c + (tmp - 0x30);
			d[i / 2] = (byte) c;
			i++;
		}
		return d;
	}

	// 转化字符串为协议的指令(recvBuf)
	public String ByteToStr(final byte[] dat, int begin) {
		int i, count = 0;
		StringBuffer str = new StringBuffer();
		// Log.e("ByteToStr", "datLength = " + dat.length + "/ datBegin = " +
		// begin);
		for (i = begin; i < dat.length; i++) {

			if (dat[i] == 0)
				break;
			else {
				str.append((char) (dat[i]));
				count++;
			}
		}
		return (str.substring(0, count));
	}

	public int getCount(final byte[] dat, int begin) {
		int i, count = 0;
		for (i = begin; i < dat.length; i++) {
			if (dat[i] == 0)
				break;
			if (dat[i] == 0x55 && dat[i + 1] == 0xAA && dat[i + 2] == 0xF8
					&& dat[i + 3] == 0xF0) {
				count += 11;
			} else {
				count++;
			}
		}
		return count + 3;
	}

	/**
	 * 5.19 将结果添加到listNumber中
	 */
	private ArrayList<String> listNumber = new ArrayList<String>();

	public ArrayList<String> getListNumber(final byte[] dat) {
		ArrayList<String> numberList = new ArrayList<String>();
		for (int i = 11; i < dat.length;) {
			String strNo = ByteToStr(dat, i);
			numberList.add(strNo);
			int len = getCount(dat, i);
			// Log.e("getListNumber", "len = " + len);
			i = i + len;
		}
		return numberList;
	}

	protected void onDataReceived(final byte[] buffer, final int size) {

		StringBuffer rText = new StringBuffer();
		int c, d;
		String ary = "0123456789ABCDEF";
		for (int i = 0; i < size; i++) {
			// 高位
			d = (buffer[i] >> 4) & 0x000F;
			rText.append(ary.substring(d, d + 1));
			// 低位
			d = (buffer[i] & 0x000F);
			rText.append(ary.substring(d, d + 1));
			// 空格
			rText.append("");
			recvBuf[recvCount++] = buffer[i];
			if(recvBuf[0] != 0x55)
				recvCount--;
		}
//			MLog.i("zdy", "rText->Start*************" + rText);

		// 21 05 = 42 + 7 = 49
		// 21 03 = 71 + 7 = 78 127
		// 判断收到的字节数量

		if ((buffer[0] == 0x55) && (buffer[1] == (byte) (0xAA))) {
			// Log.e("zdy","接收帧" + recvLoop + "/帧长度" +
			// rText.substring(10,12));
			recvSize[recvLoop] += buffer[5] + 7;
			recvLoop++;
//			MLog.e("zdy", "接收帧" + recvLoop + "/帧长度 = 7 + "
//					+ recvSize[recvLoop - 1]);
		}
//		MLog.e("llq", "recvLoop=" + recvLoop);
//		MLog.e("llq", "recvCount=" + recvCount);
//		MLog.e("llq", "recvSize[0]=" + recvSize[0]);
//		MLog.e("llq", "recvSize[1]=" + recvSize[1]);
		
		// 判断是否收到2帧，并且数据完整

		if ((recvLoop >= 2) && (recvCount == recvSize[0] + recvSize[1])) {
//				MLog.e("zdy", "接收数据完毕");
		} else
			return;
		

		// 转变为字符串进行截取
		rText.setLength(0);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < recvCount; i++) {
			// 高位
			d = (recvBuf[i] >> 4) & 0x000F;
			rText.append(ary.substring(d, d + 1));
			// 低位
			d = (recvBuf[i] & 0x000F);
			rText.append(ary.substring(d, d + 1));
			// 空格
			rText.append("");
			sb = rText;
		}
//		MLog.i("zdy", "---------------check SN-----------------");
//			Log.i("zdy", "Dat: " + rText);
		
		// 5.19 添加，动态获取相关数据
		byte[] id1 = ByteHexHelper.hexStringToBytes(rText.toString());
		listNumber = getListNumber(id1);

		{
			serialportID = listNumber.get(0).substring(0, 12);
			EasyPreferences.Companion.getInstance().put("serialPortID", serialportID.trim());

		}

		{
			serialportNO = listNumber.get(1);
			EasyPreferences.Companion.getInstance().put("serialPortNO", serialportNO.trim());

		}

		{

			stringdownloadbin = listNumber.get(8);
			EasyPreferences.Companion.getInstance().put("downloadbinVersion",
					stringdownloadbin.trim());
		}

	
//		WriteData();
//		MLog.e("SerialPortNoThread", "读取完毕");
		interrupt();

	}

	// 创建DeviceInfo.txt
	public void WriteData() {
		MLog.e("SerialPortNoThread", "保存读取的串口信息"+serialportID+"  "+serialportNO+" "+stringdownloadbin);
		String filePath = Environment.getExternalStorageDirectory()
				+ "/cnlaunch/DeviceInfo.txt";
		String message = serialportID + "_" + serialportNO + "_"
				+ stringdownloadbin ;
		String fileDP = Environment.getExternalStorageDirectory() + "/cnlaunch";
		MLog.e("SerialPortNoThread   ", "fileDP  " + fileDP);
		File fileD = new File(fileDP);
		if (!fileD.exists())
			fileD.mkdirs();
		File file = new File(filePath);
		if (!file.exists()) {
			// 如果文件不存在则创建文件
			try {
				file.createNewFile();
				FileOutputStream fout = new FileOutputStream(file);
				byte[] bytes = message.getBytes();
				fout.write(bytes);
				fout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// 如果文件存在则创建并覆盖文件
			FileOutputStream fout;
			try {
				fout = new FileOutputStream(file);
				byte[] bytes = message.getBytes();
				fout.write(bytes);
				fout.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


}
