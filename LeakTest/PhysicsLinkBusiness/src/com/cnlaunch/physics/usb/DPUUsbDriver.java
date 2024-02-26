package com.cnlaunch.physics.usb;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import com.cnlaunch.physics.utils.MLog;

/**
 * DPU USB Driver<br/>
 * 
 * @author LiangHua 
 * @version V1.0, 2015.08 
 */
public class DPUUsbDriver implements IUsbOperate{
	private final static String TAG="DPUUSBDriver";
	private Context mContext = null;
    private PendingIntent mPermissionPendingIntent = null;
	private boolean _DEBUG = true;
	private Integer mStatus = Connector.STATE_CLOSED;//device state

	private UsbManager mUsbManager = null;
	private UsbAccessory[] mUsbAccessory = null;
	private UsbDeviceConnection mUsbConnection = null;
	private UsbDevice mUsbDevice = null;
	private HashMap<String, UsbDevice> mDeviceList = null;
	private UsbInterface[] mUsbInterfaces = null;
	private int mBulkIntfaceIndex=-1;// Bulk interface index
	private int mInterruptIntfaceIndex=-1;// Bulk interface index
	private UsbEP mInterruptEp=null;
	private UsbEP mBulkEpIn=null;
	private UsbEP mBulkEpOut=null;
	private String mDeviceName = null;//device name
    private String mUsbPermission="";
	private UsbRequest mUsbRequest;
	private List<UsbId> mUsbIdList;

	public byte stopBit;
	public byte dataBit;
	public byte parity;
	public byte flowControl;

	private class UsbEP {
		UsbEndpoint ep;
		int interfaceIndex;
	}
	
	public DPUUsbDriver(Context context, String permisson,List<UsbId> usbIdList) {
		mContext = context;
		if (null != permisson && !permisson.isEmpty()) {
			mUsbPermission = permisson;
		}
		mUsbIdList = usbIdList;
		mUsbRequest=null;
		InstanceUsbManager();
	}

	/**
	 * @param: Intent intent: BroadcastReceiver->onReceive->intent
	 * @return:
	 * 		返回错误码，0为成功，错误<0
	 */
	public int connect(Intent intent) {
		UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		if (null == device) {
			return Connector.STATE_UNKNOWN;
		}
    	return openDevice(device);
    }

	private int openDevice(UsbDevice usbDevice) {
		if (null == mUsbManager) {
			return Connector.STATE_NO_INSTANCE;
		}
		int count = mUsbIdList.size();
		if (count <= 0) {
			return Connector.STATE_GET_SUPPORT_DEV_ID_FAILED;
		}
		int state = Connector.STATE_UNKNOWN;
		boolean isExclusiveAccess = false;
		state = getStatus();
		if (Connector.STATE_RUNNING == state) {//already running
			if (_DEBUG) {
				MLog.e(TAG, "open() -> device is already running");
			}
			return Connector.STATE_SUCCESS;
		}
		if (null == mUsbManager) {
			MLog.e(TAG, "open() -> no usbManager");
			return Connector.STATE_NO_INSTANCE;
		}

		state = queryDevice(usbDevice);
		if (Connector.STATE_SUCCESS != state) {//If no specified device of I want
			return state;//return error state
		}

		state = getPermisson();
		if (Connector.STATE_SUCCESS != state) {//If no specified device of I want
			return state;//return error state
		}

		/* Open my USB device */
		mUsbConnection = mUsbManager.openDevice(mUsbDevice);
		if (null != mUsbConnection) {
//			int protocol = mUsbDevice.getDeviceProtocol();
//			int deviceClass = mUsbDevice.getDeviceClass();
//			int subclass = mUsbDevice.getDeviceSubclass();
//			String serial = mUsbConnection.getSerial();
			if (_DEBUG)
				MLog.e(TAG, "open() -> open device successed!");

			/* Try to Exclusive access device */
			isExclusiveAccess = exclusiveAccess(true);//Exclusive access my device
			if (!isExclusiveAccess) {
				MLog.e(TAG, "open() -> Try to access device failed!");
				return Connector.STATE_NO_EXCLUSIVE_ACCESS;
			}
			if (mUsbDevice.getProductId() == 21972) {//CH9102F USB转串口芯片 需要设置波特率
				boolean init =UartInit();
				Log.e(TAG,"--------UartInit----------结果："+init);
				if (init){
					dataBit = 8;
					stopBit = 1;
					parity = 0;
					flowControl = 0;
					SetConfig(4800,dataBit,stopBit,parity,flowControl);
				}
			}
			mUsbRequest = new UsbRequest();
			mUsbRequest.initialize(mUsbConnection, mBulkEpIn.ep);
		} else {
			MLog.e(TAG, "open() -> open device failed!");
			return Connector.STATE_OPEN_FAILED;
		}
		setStatus(Connector.STATE_RUNNING);
		return Connector.STATE_SUCCESS;
	}
	private int closeDevice(UsbDevice usbDevice) {
		if (null == mUsbDevice || null == mUsbConnection || null == mUsbManager) {
			CleanupDevice();
			MLog.e(TAG, "close() -> no usbManager or device not opened");
		}
		else {
			if(usbDevice==null || (usbDevice!=null && usbDevice.getVendorId()==mUsbDevice.getVendorId()
					&& usbDevice.getProductId()==mUsbDevice.getProductId())) {
				if (_DEBUG)
					MLog.e(TAG, "Device [" + String.format("0x%x", mUsbDevice.getVendorId()) + "," + String.format("0x%x", mUsbDevice.getProductId()) + "] Closed!");
				synchronized (mUsbConnection) {
					try {
						releaseExclusiveAccess();//Release exclussive access
						mUsbConnection.close();
						releaseExclusiveAccess();//Release exclussive access
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					} finally {
						setStatus(Connector.STATE_CLOSED);
					}
				}
				//为保证关闭usb环境正确，去掉releaseFlg标志 xfh 2016/01/14修改，usb有线发现错误检测到此问题
				CleanupDevice();
			}
			else{
				return Connector.STATE_DEVICE_NOT_SUPPORT;//not my USB device
			}
		}
		return Connector.STATE_SUCCESS;//my USB was detached
	}
	public int disconnect(Intent intent) {
		if (null == intent) {
			return Connector.STATE_ARG_ERROR;
		}
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		if (null == device) {
			return Connector.STATE_UNKNOWN;
		}
        return closeDevice(device);
    }

	private int getPermisson() {
		if (null == mUsbDevice || null == mUsbManager) {
			return Connector.STATE_NO_INSTANCE;
		}
		/* check if we have permission for access the device? */
		if (false == mUsbManager.hasPermission(mUsbDevice)) {// If we no permission to access the device
			mUsbManager.requestPermission(mUsbDevice, mPermissionPendingIntent);//try to request permisssion for access device
			/* check again */
			if (false == mUsbManager.hasPermission(mUsbDevice)) {// We have no permisson
				MLog.e(TAG, "getPermisson() -> Get device access permission failed!");
				return Connector.STATE_NO_PERMISSION;// No device access permission
			}
		}
		else {
			if (_DEBUG) {
				MLog.e(TAG, "getPermisson() -> Get device access permission success!");
			}
		}
		return Connector.STATE_SUCCESS;
	}
	
	private int InstanceUsbManager() {
		if (null == mUsbManager) {
			mUsbManager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
			if (null != mUsbManager) {
				mPermissionPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, new Intent(mUsbPermission), 0);
			}
			else {
				MLog.e(TAG, "USBDriver() -> Get UsbManager serveice failed");
				return Connector.STATE_INSTANCE_FAILED;
			}
		}
		return Connector.STATE_SUCCESS;
	}


	private int queryDevice(UsbDevice usbDevice) {
		int state = Connector.STATE_UNKNOWN;
		int count = mUsbIdList.size();
		if (count <= 0) {
			return Connector.STATE_GET_SUPPORT_DEV_ID_FAILED;
		}
		for (int i = 0; i < count; i++) {
			UsbId id = mUsbIdList.get(i);
			if (usbDevice == null) {
				state = deviceEnumerate(id.mVid, id.mPid);
				if (Connector.STATE_SUCCESS == state) {
					break;
				}
			} else {
				if (id.mVid == usbDevice.getVendorId() && id.mPid == usbDevice.getProductId()) {
					mUsbDevice = usbDevice;//Get my USB device
					mDeviceName = mUsbDevice.getDeviceName();
					if (_DEBUG) {
						MLog.e(TAG, "find USB device success, VID: 0x" + Integer.toHexString(mUsbDevice.getVendorId()) + ", PID: 0x" + Integer.toHexString(mUsbDevice.getProductId()));
					}
					state =Connector.STATE_SUCCESS;//get My USB device
					break;
				}
			}
		}
		if (Connector.STATE_SUCCESS != state) {
			return state;
		}

		state = getSpecifiedDevice();
		if (Connector.STATE_SUCCESS != state) {
			return state;
		}
		return state;
	}

	/* Open the Specified Device */
	public int open() {
		return openDevice(null);
	}
	/* Close the opened device */
	public void close() {
		closeDevice(null);
	}
	
	private void CleanupDevice() {
		mUsbInterfaces = null;
		mDeviceList = null;
		mBulkEpIn = null;
		mBulkEpOut = null;
		mInterruptEp = null;
		mUsbInterfaces = null;
		mUsbDevice = null;
		mUsbAccessory = null;
		mUsbConnection = null;
		mUsbRequest = null;
	}
	
	/* return: length of data transferred (or zero) for success, or negative value for failure */
	public int read(byte[] buffer, int length, int timeout) {
		int state = Connector.STATE_UNKNOWN;
		int readResult=0;//real transfer count
		if (null == buffer || 0 == buffer.length || 0 == length) {
			MLog.e(TAG, "read() --> param error!");
			return Connector.STATE_ARG_ERROR;
		}
		state = getStatus();
		if (Connector.STATE_RUNNING != state) {
			MLog.e(TAG, "read() --> device status error! error: " + state);
			return state;
		}
		if (null == mUsbManager || null == mUsbConnection || null == mBulkEpIn.ep) {
			MLog.e(TAG, "read() -> No USBDriver object's instance!");
			return Connector.STATE_NO_INSTANCE;//No UsbDriver object's instance
		}
		try {
			synchronized (mBulkEpIn.ep) {
				readResult = mUsbConnection.bulkTransfer(mBulkEpIn.ep, buffer, length, timeout);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (readResult < 0) {
				MLog.e(TAG, "read() --> read result: " + readResult);
			}
		}

		return readResult;
	}

	private UsbRequest getReadUsbRequest(){
		int state = Connector.STATE_UNKNOWN;
		state = getStatus();
		if (Connector.STATE_RUNNING != state) {
			MLog.e(TAG, "read() --> device status error! error: " + state);
			return null;
		}
		if (null == mUsbManager || null == mUsbConnection || null == mBulkEpIn.ep || null == mUsbRequest) {
			MLog.e(TAG, "read() -> No USBDriver object's instance!");
			return null;//No UsbDriver object's instance
		}
		return mUsbRequest;
	}

	private  UsbDeviceConnection getUsbConnection(){
		int state = Connector.STATE_UNKNOWN;
		state = getStatus();
		if (Connector.STATE_RUNNING != state) {
			MLog.e(TAG, "read() --> device status error! error: " + state);
			return null;
		}
		if (null == mUsbManager || null == mUsbConnection || null == mBulkEpIn.ep) {
			MLog.e(TAG, "read() -> No USBDriver object's instance!");
			return null;//No UsbDriver object's instance
		}
		return mUsbConnection;
	}
	/* return: length of data transferred (or zero) for success, or negative value for failure */
	public int write(byte[] buffer, int length, int timeout) {
		int state = Connector.STATE_UNKNOWN;
		int writeResult=0;//real transfer count
		
		if (null == buffer || 0 == buffer.length || 0 == length) {
			MLog.e(TAG, "write() --> param error!");
			return Connector.STATE_ARG_ERROR;
		}
		state = getStatus();
		if (Connector.STATE_RUNNING != state) {
			MLog.e(TAG, "write() --> device status error! error: " + state);
			return state;
		}

		if (null == mUsbManager || null == mUsbConnection || null == mBulkEpOut.ep) {
			MLog.e(TAG, "write() -> No USBDriver object's instance");
			return Connector.STATE_NO_INSTANCE;//No USBDriver object's instance
		}

		try {
			synchronized (mBulkEpOut.ep) {
				writeResult = mUsbConnection.bulkTransfer(mBulkEpOut.ep, buffer, length, timeout);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (writeResult < 0) {
				MLog.e(TAG, "write() -> write result: " + writeResult);
			}
		}
		return writeResult;
	}



	/*
	 * requestType : request type for this transaction
	 * request 	request ID for this transaction
	 * value 	value field for this transaction
	 * index 	index field for this transaction
	 * buffer 	buffer for data portion of transaction, or null if no data needs to be sent or received
	 * length 	the length of the data to send or receive
	 * timeout 	in milliseconds
	 * 
	 * return: length of data transferred (or zero) for success, or negative value for failure 
	 * */
	public int controlTransfer(int requestType, int request, int value, int index, byte[] buffer, int length, int timeout) {
		int state = Connector.STATE_UNKNOWN;
		if (null == buffer || 0 == buffer.length || 0 == length) {
			MLog.e(TAG, "controlTransfer() --> param error!");
			return Connector.STATE_ARG_ERROR;
		}
		state = getStatus();
		if (Connector.STATE_RUNNING != state) {
			return state;
		}
		
		if (null == mUsbManager || null == mUsbConnection) {
			MLog.e(TAG, "controlTransfer() -> No USBDriver object's instance");
			return Connector.STATE_NO_INSTANCE;//No USBDriver object's instance
		}
		try {
        	synchronized (mUsbConnection) {
        		state = mUsbConnection.controlTransfer(requestType, request, value, index, buffer, length, timeout);
        	}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (state < 0) {
				MLog.e(TAG, "controlTransfer() -> Transfer failed");
			}
		}
		return state;
	}

	/* Exclusive Access device */
	private boolean exclusiveAccess(boolean flg) {
		boolean b=false;
		
		if (null != mUsbConnection && null != mUsbInterfaces[mBulkIntfaceIndex]) {
    		b = mUsbConnection.claimInterface(mUsbInterfaces[mBulkIntfaceIndex], flg);
    		if (false == b) {
				MLog.e(TAG, "exclusiveAccess() -> Try to exclusive access device failed!");
    		}
		}
		
		return b;
	}

	/* release Exclusive Access device */
	private boolean releaseExclusiveAccess() {
		boolean b=false;
		if (null != mUsbConnection && null != mUsbInterfaces[mBulkIntfaceIndex]) {
			b = mUsbConnection.releaseInterface(mUsbInterfaces[mBulkIntfaceIndex]);
		}
		return b;
	}

	private int deviceEnumerate(int vid, int pid) {
		mDeviceList = mUsbManager.getDeviceList();//Get USB device list
		if (null == mDeviceList || 0 >= mDeviceList.size()) {//if no device
			MLog.e(TAG, "deviceEnumerate() -> Not detected USB device(0x" + Integer.toHexString(vid) + ", 0x" + Integer.toHexString(pid) + ")!");
			return Connector.STATE_NO_DEVICE_DETECTED;//Device detached or No usb device
		}

		/* Enumerate devices for get my USB device */
		Iterator<UsbDevice> deviceIterator = mDeviceList.values().iterator();
		while (deviceIterator.hasNext()) {
			UsbDevice device = (UsbDevice)deviceIterator.next();
			if (null == device) {
				continue;
			}

			/* Is my device ? */
			if (vid == device.getVendorId() && pid == device.getProductId()) {
				mUsbDevice = device;//Get my USB device
				mDeviceName = mUsbDevice.getDeviceName();
				if (_DEBUG)
					MLog.e(TAG, "deviceEnumerate() -> Get USB device success, VID: 0x" + Integer.toHexString(mUsbDevice.getVendorId()) + ", PID: 0x" + Integer.toHexString(mUsbDevice.getProductId()));
				return Connector.STATE_SUCCESS;//get My USB device
			}
		}
		return Connector.STATE_DEVICE_NOT_SUPPORT;//Not get my USB device
	}

	private int getSpecifiedDevice() {
		int intfCount=0;
		int epCount=0;
		int Intfidx=0;
		int Epidx=0;
		int type=-1;
		int dir=-1;
		
		mBulkEpIn = null;
		mBulkEpOut = null;
		mInterruptEp = null;

		/* get USB interfaces */
		intfCount = mUsbDevice.getInterfaceCount();
		if (null == mUsbInterfaces) {
			mUsbInterfaces = new UsbInterface[intfCount];//
		}
		
		if (_DEBUG) {
			MLog.e(TAG, "getSpecifiedDevice() -> Interface count: " + intfCount);
		}

		UsbEndpoint ep=null;
		UsbInterface intf=null;
		for (Intfidx=0; Intfidx<intfCount; Intfidx++) {
			intf = mUsbDevice.getInterface(Intfidx);
			if (null == intf) {
				continue;
			}
			mUsbInterfaces[Intfidx] = intf;
			epCount = intf.getEndpointCount();
			for (Epidx=0; Epidx<epCount; Epidx++) {
				ep = intf.getEndpoint(Epidx);
				if (null == ep) {
					continue;
				}
				
				type = ep.getType();
				dir = ep.getDirection();
				if (UsbConstants.USB_ENDPOINT_XFER_BULK == type) {//bulk ep
					if (UsbConstants.USB_DIR_IN == dir) {//ep IN
						if (null == mBulkEpIn) {
							mBulkEpIn = new UsbEP();
							mBulkEpIn.ep = ep;
							mBulkEpIn.interfaceIndex = Intfidx;
							mBulkIntfaceIndex = Intfidx;
						}
						if (_DEBUG) {
							MLog.e(TAG, "getSpecifiedDevice() -> ep-IN, dir: " + dir + "\nep size: " + ep.getMaxPacketSize());
						}
					}
					else if (UsbConstants.USB_DIR_OUT == dir) {//ep OUT
						if (null == mBulkEpOut) {
							mBulkEpOut = new UsbEP();
							mBulkEpOut.ep = ep;
							mBulkEpOut.interfaceIndex = Intfidx;
						}
						
						if (_DEBUG) {
							MLog.e(TAG, "getSpecifiedDevice() -> ep-OUT, dir: " + dir  + "\nep size: " + ep.getMaxPacketSize());
						}
					}
				}
				else if (UsbConstants.USB_ENDPOINT_XFER_INT == type) {// interrupt ep
					if (null == mInterruptEp) {
						if (UsbConstants.USB_DIR_IN == dir) {
							mInterruptEp = new UsbEP();
							mInterruptEp.ep = ep;
							mInterruptEp.interfaceIndex = Intfidx;
							mInterruptIntfaceIndex = Intfidx;
						}
					}
					
					if (_DEBUG) {
						MLog.e(TAG, "getSpecifiedDevice() -> ep-interrupt, dir: " + dir  + "\nep size: " + ep.getMaxPacketSize());
					}
				}
			}
		}
		if (0 == epCount) {
			MLog.e(TAG, "getSpecifiedDevice() -> No any ep!");
			return Connector.STATE_NO_ANY_EP;
		}
		else if (null == mBulkEpIn) {
			MLog.e(TAG, "getSpecifiedDevice() -> No any IN-ep!");
			return Connector.STATE_NO_EP_IN;
		}
		else if (null == mBulkEpOut) {
			MLog.e(TAG, "getSpecifiedDevice() -> No any OUT-ep!");
			return Connector.STATE_NO_EP_OUT;
		}
		return Connector.STATE_SUCCESS;
	}

	public int getStatus() {
		synchronized (this.mStatus) {
			return mStatus;
		}
	}
	
	public void setStatus(int status) {
		synchronized (this.mStatus) {
			this.mStatus = status;
		}
	}

	public String getDeviceName(){
		String deviceName = "";
		if (mUsbDevice != null) {
			deviceName = mUsbDevice.getDeviceName();
		}
		return deviceName;
	}
	public int readWithAsync(byte[] buffer, int length, int timeout) {
		int ret=0;
		UsbRequest readUsbRequest = getReadUsbRequest();
		if (readUsbRequest == null) {
			ret = -1;
		} else {
			ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, length);
			boolean isSuccess = readUsbRequest.queue(byteBuffer, length);
			if (!isSuccess) {
				ret = -1;
			} else {
				UsbDeviceConnection usbDeviceConnection = getUsbConnection();
				if (usbDeviceConnection == null) {
					ret = -1;
				} else {
					UsbRequest queueReuest = usbDeviceConnection.requestWait();
					if (queueReuest != null ) {
						if(queueReuest.equals(readUsbRequest)) {
							byte[] arrays = byteBuffer.array();
							int actuallength = byteBuffer.position();
							System.arraycopy(arrays, 0, buffer, 0, actuallength);
							ret = actuallength;
						}
						else{
							ret = 0;
						}
					}
					else{
						ret = -1;
					}
				}
			}
		}
		return ret;
	}

	public boolean UartInit() {
		int ret;
		int size = 8;
		byte[] buffer = new byte[size];
		Uart_Control_Out(VENDOR_SERIAL_INIT, 0x0000, 0x0000);
		ret = Uart_Control_In(VENDOR_VERSION, 0x0000, 0x0000, buffer, 2);
		if (ret < 0)
			return false;
		Uart_Control_Out(VENDOR_WRITE, 0x1312, 0xD982);
		Uart_Control_Out(VENDOR_WRITE, 0x0f2c, 0x0004);
		ret = Uart_Control_In(VENDOR_READ, 0x2518, 0x0000, buffer, 2);
		if (ret < 0)
			return false;
		Uart_Control_Out(VENDOR_WRITE, 0x2727, 0x0000);
		Uart_Control_Out(VENDOR_MODEM_OUT, 0x00ff, 0x0000);
		return true;
	}

	public int Uart_Control_Out(int request, int value, int index) {
		int retval = 0;
		Log.i("ykw","参数---Uart_Control_Out---request:"+request+",value:"+value+",index:"+index);
		retval = mUsbConnection.controlTransfer(64, request, value, index, null, 0, 500);
		Log.d("ykw","结果---Uart_Control_Out---retval:"+retval);

		return retval;
	}

	public int Uart_Control_In(int request, int value, int index, byte[] buffer, int length) {
		int retval = 0;
		Log.i(TAG,"参数---Uart_Control_In---request:"+request+",value:"+value+",index:"+index);

		retval = mUsbConnection.controlTransfer(192, request, value, index, buffer, length, 500);
		Log.d(TAG,"结果---Uart_Control_In---retval:"+retval);;
		return retval;
	}

	public boolean SetConfig(int baudRate, byte dataBit, byte stopBit, byte parity, byte flowControl){
		int value = 0;
		int index = 0;
		char valueHigh = 0, valueLow = 0, indexHigh = 0, indexLow = 0;
		switch(parity) {
			/*NONE*/
			case 1:/*ODD*/
				valueHigh |= 0x08;
				break;
			case 2:/*Even*/
				valueHigh |= 0x18;
				break;
			case 3:/*Mark*/
				valueHigh |= 0x28;
				break;
			case 4:/*Space*/
				valueHigh |= 0x38;
				break;
			default:/*None*/
				valueHigh = 0x00;
				break;
		}

		if(stopBit == 2) {
			valueHigh |= 0x04;
		}

		switch(dataBit) {
			case 5:
				valueHigh |= 0x00;
				break;
			case 6:
				valueHigh |= 0x01;
				break;
			case 7:
				valueHigh |= 0x02;
				break;
			case 8:
				valueHigh |= 0x03;
				break;
			default:
				valueHigh |= 0x03;
				break;
		}

		valueHigh |= 0xc0;
		valueLow = 0x9c;

		value |= valueLow;
		value |= (int)(valueHigh << 8);

		switch(baudRate) {
			case 50:
				indexLow = 0;
				indexHigh = 0x16;
				break;
			case 75:
				indexLow = 0;
				indexHigh = 0x64;
				break;
			case 110:
				indexLow = 0;
				indexHigh = 0x96;
				break;
			case 135:
				indexLow = 0;
				indexHigh = 0xa9;
				break;
			case 150:
				indexLow = 0;
				indexHigh = 0xb2;
				break;
			case 300:
				indexLow = 0;
				indexHigh = 0xd9;
				break;
			case 600:
				indexLow = 1;
				indexHigh = 0x64;
				break;
			case 1200:
				indexLow = 1;
				indexHigh = 0xb2;
				break;
			case 1800:
				indexLow = 1;
				indexHigh = 0xcc;
				break;
			case 2400:
				indexLow = 1;
				indexHigh = 0xd9;
				break;
			case 4800:
				indexLow = 2;
				indexHigh = 0x64;
				break;
			case 9600:
				indexLow = 2;
				indexHigh = 0xb2;
				break;
			case 19200:
				indexLow = 2;
				indexHigh = 0xd9;
				break;
			case 38400:
				indexLow = 3;
				indexHigh = 0x64;
				break;
			case 57600:
				indexLow = 3;
				indexHigh = 0x98;
				break;
			case 115200:
				indexLow = 3;
				indexHigh = 0xcc;
				break;
			case 230400:
				indexLow = 3;
				indexHigh = 0xe6;
				break;
			case 460800:
				indexLow = 3;
				indexHigh = 0xf3;
				break;
			case 500000:
				indexLow = 3;
				indexHigh = 0xf4;
				break;
			case 921600:
				indexLow = 7;
				indexHigh = 0xf3;
				break;
			case 1000000:
				indexLow = 3;
				indexHigh = 0xfa;
				break;
			case 2000000:
				indexLow = 3;
				indexHigh = 0xfd;
				break;
			case 3000000:
				indexLow = 3;
				indexHigh = 0xfe;
				break;
			default:// default baudRate "9600"
				indexLow = 2;
				indexHigh = 0xb2;
				break;
		}

		index |= 0x88 |indexLow;
		index |= (int)(indexHigh << 8);

		Uart_Control_Out(VENDOR_SERIAL_INIT, value, index);
//		if(flowControl == 1) {
//			Uart_Tiocmset(UartModem.TIOCM_DTR | UartModem.TIOCM_RTS, 0x00);
//		}
		return true;
	}
	private final int VENDOR_VERSION=0x5F;
	private final int VENDOR_SERIAL_INIT =0xA1;
	private final int VENDOR_WRITE=0x9A;
	private final int VENDOR_READ=0x95;
	private final int VENDOR_MODEM_OUT = 0xA4;
}
