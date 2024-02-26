package com.cnlaunch.physics.usb;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.cnlaunch.bluetooth.R;
import com.cnlaunch.physics.utils.MLog;

import org.xmlpull.v1.XmlPullParserException;

public class DPUUSBDevice {
	private static final String TAG = "USBDevice";
	/* Direction */
	public final static int USB_DIR_IN = 1;//H2D
	public final static int USB_DIR_OUT = 0;//D2H

	/* req type */
	public final static int USB_STD_REQ = 1;
	public final static int USB_CLASS_REQ = 1;
	public final static int USB_VEND_REQ = 2;

	/* who receiver */
	public final static int USB_DEV_REC = 0;
	public final static int USB_INTF_REC = 1;
	public final static int USB_EP_REC = 2;

	/* bit/mask */
	public final static int USB_DIR_BIT = 7;
	public final static int USB_REQ_BIT = 5;
	public final static int USB_REC_BIT = 0;
	public final static int USB_DIR_MASK = 0x01;
	public final static int USB_REQ_MASK = 0x03;
	public final static int USB_REC_MASK = 0x1f;

	private List<UsbId> mUsbIdList;
	private List<UsbId> mStandAloneChipUsbIdList;
	private List<UsbId> mStandAloneChipEthernetUsbIdList;
    private List<UsbId> mFTDIUsbIdList;
	private Context mContext;
	private IUsbOperate mDPUUsbDriver;
	private String mPermisson;

	/**
	 * 
	 * DPUB USB接头类，此类继承自Connector抽象类，并实现所有抽象接口。<br/>
	 * 
	 */
	public DPUUSBDevice(Context context, String permisson) {
		mContext = context;
		mPermisson = permisson;
		try {
            GetSupportUsbId();
            GetSupportStandAloneChipUsbId();
            GetSupportStandAloneChipEthernetUsbId();
            getSupportFTDIUsbId();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        mDPUUsbDriver = null;
	}
     private void getSupportFTDIUsbId(){
         if (null == mFTDIUsbIdList) {
             mFTDIUsbIdList = new ArrayList<UsbId>();
         } else {
             mFTDIUsbIdList.clear();
         }
         mFTDIUsbIdList.add(new UsbId(0x0403, 0x6001)); // FT232RL
         mFTDIUsbIdList.add(new UsbId(0x0403, 0x6014)); // FT232H
         mFTDIUsbIdList.add(new UsbId(0x0403, 0x6010)); // FT2232C
         mFTDIUsbIdList.add(new UsbId(0x0403, 0x6010)); // FT2232D
         mFTDIUsbIdList.add(new UsbId(0x0403, 0x6010)); // FT2232HL
         mFTDIUsbIdList.add(new UsbId(0x0403, 0x6011)); // FT4232HL
         mFTDIUsbIdList.add(new UsbId(0x0403, 0x6015)); // FT230X
         mFTDIUsbIdList.add(new UsbId(0x0584, 0xB020)); // REX-USB60F
         mFTDIUsbIdList.add(new UsbId(0x0584, 0xB02F)); // REX-USB60MI
     }

	@Override
	protected void finalize() throws Throwable {
		close();
		mDPUUsbDriver = null;
	}

    //////////////////////////////public need instance///////////////////////////////////////////////////

    /**
	 * 获取接头运行状态<br/>
	 * 
	 * @return 参见STATE_xxx, xxx为具体的状态
	 */

	public int getStatus() {
		int state = Connector.STATE_NO_INSTANCE;
		if (null == mDPUUsbDriver) {
			return state;
		}
		state = mDPUUsbDriver.getStatus();
		return state;
	}


	/**
	 * 
	 * 当收到UsbManager.ACTION_USB_DEVICE_ATTACHED广播时调用此函数，以检查是否是DBSCAR接头插入设备<br/>
	 * 
	 * @param: Intent intent: BroadcastReceiver->onReceive->intent
	 * @return 参见STATE_xxx, xxx为具体的状态
	 */
	public int connect(Intent intent) {
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        initUsbOperate(device);
		if (null == mDPUUsbDriver) {
			return Connector.STATE_NO_INSTANCE;
		}
		int state = mDPUUsbDriver.connect(intent);
		if (Connector.STATE_SUCCESS != state) {
		}
		return state;
	}

	/**
	 * 当收到UsbManager.ACTION_USB_DEVICE_DETACHED广播时调用此函数，以检查是否是DBSCAR接头设备拔出
	 * 如果设备为DBSCAR设备，会自动调用colse()方法关闭设备<br/>
	 * 
	 * @param: Intent intent: BroadcastReceiver->onReceive->intent
	 * @return 参见STATE_xxx, xxx为具体的状态
	 */
	public int disconnect(Intent intent) {
		if (null == mDPUUsbDriver) {
			return Connector.STATE_NO_INSTANCE;
		}
		int state = mDPUUsbDriver.disconnect(intent);
		return state;
	}
    private boolean isFIDIDevice(UsbDevice usbDevice){
        UsbId usbId = queryDeviceWithUsbId(usbDevice);
        if(MLog.isDebug) {
			MLog.d(TAG, "isFIDIDevice usbId=" + usbId.toString());
		}
        if(usbId!=null){
            for (int i=0; i<mFTDIUsbIdList.size(); i++) {
                UsbId fidiUsbId =mFTDIUsbIdList.get(i);
				if(MLog.isDebug) {
					MLog.d(TAG, "isFIDIDevice fidiUsbId=" + fidiUsbId.toString());
				}
                if (fidiUsbId.mVid == usbId.mVid && fidiUsbId.mPid==usbId.mPid) {
                    return true;
                }
            }
            return false;
        }
        else {
            return false;
        }
    }
    private void initUsbOperate(UsbDevice usbDevice){
        if(mDPUUsbDriver==null) {
            if(isFIDIDevice(usbDevice)) {
				mDPUUsbDriver  = new FTDriver(mContext, mPermisson, mUsbIdList);
            }
            else{
				mDPUUsbDriver = new DPUUsbDriver(mContext, mPermisson, mUsbIdList);
            }
        }
    }

	/**
	 * 打开接头<br/>
	 * 
	 * @return 参见STATE_xxx, xxx为具体的状态
	 */
	public int open() {
        initUsbOperate(null);
		if (Connector.STATE_RUNNING == getStatus()) {
			close();
		}
        if (null == mDPUUsbDriver) {
            return Connector.STATE_NO_INSTANCE;
        }
        if(MLog.isDebug) {
			MLog.d(TAG, "open() mDPUUsbDriver=" + mDPUUsbDriver);
		}
        int state = mDPUUsbDriver.open();
		if(MLog.isDebug) {
			MLog.d(TAG, "mDPUUsbDriver.open() state=" + state);
		}
		return state;
	}

	/**
	 * 关闭USB设备
	 * 
	 * @return 参见STATE_xxx, xxx为具体的状态
	 */
	public int close() {
        if (null == mDPUUsbDriver) {
            return Connector.STATE_NO_INSTANCE;
        }
        mDPUUsbDriver.close();
		return Connector.STATE_SUCCESS;
	}

	/**
	 * 读数据
	 * 
	 * @return 参见STATE_xxx, xxx为具体的状态
	 */
	public int read(byte[] buffer, int length, int timeout) {
		if (null == mDPUUsbDriver) {
			return Connector.STATE_NO_INSTANCE;
		}
		int state = mDPUUsbDriver.read(buffer, length, timeout);
		return state;
	}

    public int readWithAsync(byte[] buffer, int length, int timeout) {
        if (null == mDPUUsbDriver) {
            return Connector.STATE_NO_INSTANCE;
        }
        int state = mDPUUsbDriver.readWithAsync(buffer, length, timeout);
        return state;
    }

	/**
	 * 写数据
	 * 
	 * @return 参见STATE_xxx, xxx为具体的状态
	 */
	public int write(byte[] buffer, int length, int timeout) {
		if (null == mDPUUsbDriver) {
			return Connector.STATE_NO_INSTANCE;
		}
		int state = mDPUUsbDriver.write(buffer, length, timeout);
		return state;
	}

	public int controlTransfer(int requestType, int request, int value,
			int index, byte[] buffer, int length, int timeout) {
		if (null == mDPUUsbDriver) {
			return Connector.STATE_NO_INSTANCE;
		}
		return mDPUUsbDriver.controlTransfer(requestType, request, value,
				index, buffer, length, timeout);
	}

	public int vendRequest(int dir, int VendNo, int value, byte buf[],
			int length, int timeOut) {
		int ret = -1;
		int requestType = 0;
		int request = 0;
		int index = 0x00;
		int timeout = timeOut;

		if (null == mDPUUsbDriver) {
			return Connector.STATE_NO_INSTANCE;
		}

		if (null == buf || 0 >= length) {
			return ret;
		}

		// bit7 bit6-bit5 bit4 bit3 bit2 bit1 bit0
		// DIR Vender dev 0x03 value index length
		requestType = ((dir & USB_DIR_MASK) << USB_DIR_BIT)
				| ((USB_VEND_REQ & USB_REQ_MASK) << USB_REQ_BIT)
				| ((USB_DEV_REC & USB_REC_MASK) << USB_REC_BIT);
		request = VendNo;
		ret = controlTransfer(requestType, request, value, index, buf, length,
				timeout);
		return ret;
	}

	public String getDeviceName(){
		String deviceName = "";
		if (mDPUUsbDriver != null) {
			deviceName = mDPUUsbDriver.getDeviceName();
		}
		return deviceName;
	}
    ////////////////////////////////public no need instance////////////////////////////////////////////////////
    /**
     * 查询接头<br/>
     *
     * @return 参见STATE_xxx, xxx为具体的状态
     */
    public int queryDevice() {
        int state = Connector.STATE_NO_INSTANCE;
        UsbId usbId = queryDeviceWithUsbId(null);
        if(usbId.mVid!=UsbId.INVALID_ID && usbId.mPid!=UsbId.INVALID_ID){
            state = Connector.STATE_SUCCESS;
        }
        return state;
    }

	/**
	 * 根据系统广播 UsbManager.ACTION_USB_DEVICE_ATTACHED，UsbManager.ACTION_USB_DEVICE_DETACHED intent
	 * 查询是否匹配dpu设备
	 *
	 */
	public boolean queryIsMatchDevice(Intent intent) {
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		UsbId usbId = queryDeviceWithUsbId(device);
		if(usbId.mVid!=UsbId.INVALID_ID && usbId.mPid!=UsbId.INVALID_ID){
			return true;
		}
		else{
			return  false;
		}
	}
	/**
	 * 判断是否为带独立芯片的usb设备
	 * @return
	 */
	public boolean isStandAloneChip() {
		UsbId usbId = queryDeviceWithUsbId(null);
		if (testStandAloneChip(usbId)) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 判断是否为带独立芯片的usb设备
	 * @return
	 */
	public boolean isStandAloneChip(Intent intent) {
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		UsbId usbId = queryDeviceWithUsbId(device);
		if (testStandAloneChip(usbId)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断是否为带独立芯片的usb以太网卡设备
	 * @return
	 */
	public boolean isStandAloneChipEthernet() {
		UsbId usbId = queryDeviceWithUsbId(null);
		if (testStandAloneChipEthernet(usbId)) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 判断是否为带独立芯片的usb以太网卡设备
	 * @return
	 */
	public boolean isStandAloneChipEthernet(Intent intent) {
        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		UsbId usbId = queryDeviceWithUsbId(device);
		if (testStandAloneChipEthernet(usbId)) {
			return true;
		} else {
			return false;
		}
	}

	////////////////////////////////////private /////////////////////////////////////////////////////////////


	private int GetSupportStandAloneChipUsbId() throws XmlPullParserException, IOException {
		if (null == mStandAloneChipUsbIdList) {
			mStandAloneChipUsbIdList = new ArrayList<UsbId>();
		}
		else {
			mStandAloneChipUsbIdList.clear();
		}
		return GetUsbId(mStandAloneChipUsbIdList, R.xml.stand_alone_chip_device);
	}
	private int GetSupportStandAloneChipEthernetUsbId() throws XmlPullParserException, IOException {
		if (null == mStandAloneChipEthernetUsbIdList) {
			mStandAloneChipEthernetUsbIdList = new ArrayList<UsbId>();
		}
		else {
			mStandAloneChipEthernetUsbIdList.clear();
		}
		return GetUsbId(mStandAloneChipEthernetUsbIdList,R.xml.stand_alone_chip_ethernet_device);
	}
	private int GetSupportUsbId() throws XmlPullParserException, IOException {
		if (null == mUsbIdList) {
			mUsbIdList = new ArrayList<UsbId>();
		}
		else {
			mUsbIdList.clear();
		}
		return GetUsbId(mUsbIdList,R.xml.filter_device);
	}
	/**
	 * read the file res/xml/filter_device.xml, which defined all USB devices supported
	 */
	private int GetUsbId(List<UsbId> usbIdList,int resid) throws XmlPullParserException, IOException {
		XmlResourceParser parser = mContext.getResources().getXml(resid);
		int eventType;

		if (null == parser) {// 加载文件失败，设置默认VID,PID
			UsbId id = new UsbId();
			id.mVid = UsbId.DEFAULT_VID;
			id.mPid = UsbId.DEFAULT_PID;
			usbIdList.add(id);
			MLog.e(TAG, "GetUsbId() -> Load res/xml/***_device.xml file failed! set default VID: 0x" + Integer.toHexString(UsbId.DEFAULT_VID) + ", PID: 0x" + Integer.toHexString(UsbId.DEFAULT_PID));
			return Connector.STATE_GET_SUPPORT_DEV_ID_FAILED;
		}
		int vid=UsbId.INVALID_ID,pid=UsbId.INVALID_ID;
		eventType = parser.getEventType();
		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if(eventType == XmlResourceParser.START_TAG) {
				String tagName = parser.getName();
				if (tagName.equals("usb-device")) {
					vid = parser.getAttributeIntValue(null, "vendor-id",UsbId.INVALID_ID);
					pid = parser.getAttributeIntValue(null, "product-id",UsbId.INVALID_ID);
					if (UsbId.INVALID_ID != vid && UsbId.INVALID_ID != pid) {
						UsbId id = new UsbId();
						id.mVid = vid;
						id.mPid = pid;
						usbIdList.add(id);
						MLog.w(TAG, "GetUsbId() -> Support USB device, VID: 0x" + Integer.toHexString(id.mVid)  + " PID: 0x" + Integer.toHexString(id.mPid));
					}
				}
			}
			eventType = parser.next();
		}
		parser.close();
		if (usbIdList.size() <= 0) {// 读取文件失败，或文件无数据，设置默认VID,PID
			UsbId id = new UsbId();
			MLog.e(TAG, "GetSupportUsbId() -> Read res/xml/***_device.xml file failed! set default VID: 0x" + Integer.toHexString(UsbId.DEFAULT_VID) + ", PID: 0x" + Integer.toHexString(UsbId.DEFAULT_PID));
			id.mVid = UsbId.DEFAULT_VID;
			id.mPid = UsbId.DEFAULT_PID;
			usbIdList.add(id);
			return Connector.STATE_GET_SUPPORT_DEV_ID_FAILED;
		}
		return Connector.STATE_SUCCESS;
	}

	private boolean getRootPermission(String filePath) {
		Process process = null;
		DataOutputStream dataOutputStream = null;

		MLog.e(TAG, "getRootPermission() -> filePath: " + filePath);

		String packageName;
		try {
			PackageInfo info = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			packageName = info.packageName;

			process = Runtime.getRuntime().exec("per-up");// PAD II超级用户命令(相当于"su", "su -"等命令)
			if (null == process) {
				MLog.e(TAG, "Get Process failed!");
				return false;
			}

			dataOutputStream = new DataOutputStream(process.getOutputStream());

			dataOutputStream.writeBytes("chmod 777 " + filePath + "\n");
			dataOutputStream.writeBytes("exit\n");

			dataOutputStream.flush();
			MLog.e(TAG, "Process waitFor...");
			int ret = process.waitFor();
			MLog.e(TAG, "Process waitFor return: " + ret);
			dataOutputStream.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			process.destroy();
		}

		return true;
	}

	/**
	 * 自动授权（不弹出授权对话框 ）
	 *
	 * 参考：http://www.crifan.com/android_try_to_auto_grant_usb_device_operation_permission/
	 * 参考：http://stackoverflow.com/questions/13647547/android-usb-automatically-grant-permission
	 * 参考：http://bMLog.csdn.net/mlj1668956679/article/details/14122787
	 */
	private boolean grantAutomaticPermission(UsbDevice usbDevice) {
		try
		{
			PackageManager pkgManager = mContext.getPackageManager();
			ApplicationInfo appInfo = pkgManager.getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);

			Class serviceManagerClass = Class.forName("android.os.ServiceManager");
			Method getServiceMethod = serviceManagerClass.getDeclaredMethod("getService", String.class);
			getServiceMethod.setAccessible(true);
			android.os.IBinder binder=(android.os.IBinder)getServiceMethod.invoke(null, Context.USB_SERVICE);

			Class iUsbManagerClass = Class.forName("android.hardware.usb.IUsbManager");
			Class stubClass = Class.forName("android.hardware.usb.IUsbManager$Stub");
			Method asInterfaceMethod = stubClass.getDeclaredMethod("asInterface", android.os.IBinder.class);
			asInterfaceMethod.setAccessible(true);
			Object iUsbManager = asInterfaceMethod.invoke(null, binder);

			// <uses-permission android:name="android.permission.MANAGE_USB" />
			MLog.e(TAG, "UID : " + appInfo.uid + " " + appInfo.processName + " " + appInfo.permission);
			final Method grantDevicePermissionMethod = iUsbManagerClass.getDeclaredMethod("grantDevicePermission", UsbDevice.class, int.class);
			grantDevicePermissionMethod.setAccessible(true);
			grantDevicePermissionMethod.invoke(iUsbManager, usbDevice, appInfo.uid);

			MLog.e(TAG, "Method OK : " + binder + "  " + iUsbManager);
			return true;
		}
		catch(Exception e)
		{
			MLog.e(TAG, "Error trying to assing automatic usb permission : ");
			e.printStackTrace();
			return false;
		}
	}
	private boolean testStandAloneChip(UsbId usbId){
		int count = mStandAloneChipUsbIdList.size();
		if (count <= 0) {
			return false;
		}
		for (int i=0; i<count; i++) {
			if (mStandAloneChipUsbIdList.get(i).mVid == usbId.mVid && mStandAloneChipUsbIdList.get(i).mPid == usbId.mPid) {
				MLog.e(TAG, "Device [" + String.format("0x%x", mStandAloneChipUsbIdList.get(i).mVid) + "," + String.format("0x%x", mStandAloneChipUsbIdList.get(i).mPid) + "] Attached!");
				return true;
			}
		}
		return false;
	}
	private boolean testStandAloneChipEthernet(UsbId usbId){
		int count = mStandAloneChipEthernetUsbIdList.size();
		if (count <= 0) {
			return false;
		}
		for (int i=0; i<count; i++) {
			if (mStandAloneChipEthernetUsbIdList.get(i).mVid == usbId.mVid && mStandAloneChipEthernetUsbIdList.get(i).mPid == usbId.mPid) {
				MLog.e(TAG, "Device [" + String.format("0x%x", mStandAloneChipEthernetUsbIdList.get(i).mVid) + "," + String.format("0x%x", mStandAloneChipEthernetUsbIdList.get(i).mPid) + "] Attached!");
				return true;
			}
		}
		return false;
	}
	private UsbId queryDeviceWithUsbId(UsbDevice device) {
		UsbId usbId=new UsbId();
		int count= mUsbIdList.size();
		if (count <= 0) {
			return usbId;
		}
		for (int i=0; i<count; i++) {
		    if(device!=null) {
                if (mUsbIdList.get(i).mVid == device.getVendorId() && mUsbIdList.get(i).mPid == device.getProductId()) {
                    MLog.e(TAG, "Device [" + String.format("0x%x", mUsbIdList.get(i).mVid) + "," + String.format("0x%x", mUsbIdList.get(i).mPid) + "] Attached!");
                    usbId.mVid = device.getVendorId();
                    usbId.mPid = device.getProductId();
                    return usbId;
                }
            }
		    else{
                UsbId id = mUsbIdList.get(i);
                UsbManager usbManager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
                if(usbManager==null){
                    return usbId;
                }
                HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();//Get USB device list
                if (null == deviceList || 0 >= deviceList.size()) {//if no device
                    MLog.e(TAG, "deviceEnumerate() -> Not detected USB device");
                    return usbId;
                }
                /* Enumerate devices for get my USB device */
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice deviceOne = (UsbDevice) deviceIterator.next();
                    if (null == deviceOne) {
                        continue;
                    }
                    /* Is my device ? */
                    if (id.mVid == deviceOne.getVendorId() && id.mPid == deviceOne.getProductId()) {
                        usbId.mVid = id.mVid;
                        usbId.mPid = id.mPid;
                        return usbId;
                    }
                }
            }
		}
		return usbId;
	}

}
