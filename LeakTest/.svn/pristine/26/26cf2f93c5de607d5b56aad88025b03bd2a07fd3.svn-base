package com.cnlaunch.physics.wifi;
import java.nio.charset.Charset;
import java.util.Arrays;

import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;

import com.cnlaunch.physics.utils.Constants;
import com.cnlaunch.physics.downloadbin.util.Analysis;
import com.cnlaunch.physics.downloadbin.util.MyFactory;
import com.cnlaunch.physics.entity.AnalysisData;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.listener.OnWiFiModeListener;
import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.NetworkUtil;
import com.cnlaunch.physics.utils.Tools;

public class StandardDPUWiFiModeSettings implements IWiFiModeSettings{
	private final static String  TAG = "StandardDPUWiFiModeSettings";
	public StandardDPUWiFiModeSettings(){
	}
	/**
	 * 异步设置wifi工作模式
	 * @param dpuWiFiModeConfig
	 */
	public void setDPUWiFiModeAsync(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener,DPUWiFiModeConfig dpuWiFiModeConfig){
		DPUWiFiModeConfigSetterRunnable mDPUWiFiModeConfigSetterRunnable = new DPUWiFiModeConfigSetterRunnable(iPhysics,onWiFiModeListener,dpuWiFiModeConfig);
		Thread t=new Thread(mDPUWiFiModeConfigSetterRunnable);
		t.start();
	}
	/**
	 * 异步获取wifi工作模式
	 *
	 */
	public void getDPUWiFiModeAsync(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener){
		DPUWiFiModeConfigGetterRunnable mDPUWiFiModeConfigGetterRunnable = new DPUWiFiModeConfigGetterRunnable(iPhysics,onWiFiModeListener);
		Thread t=new Thread(mDPUWiFiModeConfigGetterRunnable);
		t.start();
	}
	private class DPUWiFiModeConfigSetterRunnable implements Runnable {
		private DPUWiFiModeConfig mDPUWiFiModeConfig;
		private  OnWiFiModeListener mOnWiFiModeListener;
		private IPhysics  mIPhysics;
		public DPUWiFiModeConfigSetterRunnable(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener,DPUWiFiModeConfig dpuWiFiModeConfig) {
			mDPUWiFiModeConfig = dpuWiFiModeConfig;
			mOnWiFiModeListener = onWiFiModeListener;
			mIPhysics = iPhysics;
		}

		@Override
		public void run() {
			try {
				// 首先切换到boot模式
				Boolean isBootModeState = Tools.saftSwitchToBootMode(mIPhysics);
				if (isBootModeState == false) {
					mOnWiFiModeListener.OnSetWiFiModeConfigListener(OnWiFiModeListener.DPU_SWITCH_MODE_FAIL);
					return;
				}
				Boolean state = setDPUWiFiMode2118(mIPhysics,mDPUWiFiModeConfig);
				if (mOnWiFiModeListener != null) {
					if (state) {
						mOnWiFiModeListener.OnSetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_SUCCESS);
					} else {
						mOnWiFiModeListener.OnSetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mOnWiFiModeListener != null) {
					mOnWiFiModeListener.OnSetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL);
				}
			}
		}
	}

	private class DPUWiFiModeConfigGetterRunnable implements Runnable {
		private  OnWiFiModeListener mOnWiFiModeListener;
		private IPhysics  mIPhysics;
		public DPUWiFiModeConfigGetterRunnable(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener) {
			mOnWiFiModeListener = onWiFiModeListener;
			mIPhysics = iPhysics;
		}

		@Override
		public void run() {
			try {
				Boolean isBootModeState = Tools.saftSwitchToBootMode(mIPhysics);
				if (isBootModeState == false) {
					mOnWiFiModeListener.OnGetWiFiModeConfigListener(OnWiFiModeListener.DPU_SWITCH_MODE_FAIL, null);
					return;
				}
				DPUWiFiModeConfig dpuWiFiModeConfig = getDPUWiFiMode2119(mIPhysics);
				if (mOnWiFiModeListener != null) {
					if (dpuWiFiModeConfig != null) {
						mOnWiFiModeListener.OnGetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_SUCCESS, dpuWiFiModeConfig);
					} else {
						mOnWiFiModeListener.OnGetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL, dpuWiFiModeConfig);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (mOnWiFiModeListener != null) {
					mOnWiFiModeListener.OnGetWiFiModeConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_FAIL, null);
				}
			}
		}
	}

	/**
	 * 生成设置wifi工作模式指令
	 * @return
	 */
	private static byte[] generateDPUWiFiModeSettingsCommand(DPUWiFiModeConfig dpuWiFiModeConfig){
		if(dpuWiFiModeConfig.getMode() == Constants.WIFI_WORK_MODE_WITH_AP){
			return MyFactory.creatorForOrderMontage().generateCommonCommand(
					new byte[]{0x21,0x18},
					new byte[]{0x01,0x01});
		}
		else if(dpuWiFiModeConfig.getMode() == Constants.WIFI_WORK_MODE_WITH_STA_MODE_NO_INTERACTION){
			WifiConfiguration config = dpuWiFiModeConfig.getConfig();
			if(dpuWiFiModeConfig.getConfig() == null){
				return null;
			}
			String SSID =  config.SSID;
			int securityType =  NetworkUtil.getWiFiAccessPointSecurity(config);
			String password = NetworkUtil.getWiFiAccessPointPassword(config, securityType);
			//
			if(MLog.isDebug){
				MLog.d(TAG, String.format(" WifiConfiguration SSID=%s Security=%d Password=%s",
						SSID,
						securityType,
						password)
				);
			}
			byte[] ssidBytes = SSID.getBytes(Charset.forName("US-ASCII"));
			int ssidLength = ssidBytes.length;
			byte[] ssidLengthBytes = ByteHexHelper.convertLengthToTwoBytes(ssidLength);

			byte[] passwordBytes = password.getBytes(Charset.forName("US-ASCII"));
			int passwordLength = passwordBytes.length;
			byte[] passwordLengthBytes = ByteHexHelper.convertLengthToTwoBytes(passwordLength);

			//总的wifi配置信息字符数组
			byte[] newDataBytes = new byte[2+2+ssidLength+1+2+passwordLength];
			newDataBytes[0] = 0x01;
			newDataBytes[1] = 0x02;
			int currentIndex = 2;

			System.arraycopy(ssidLengthBytes, 0, newDataBytes, currentIndex, 2);
			currentIndex+=2;
			System.arraycopy(ssidBytes, 0, newDataBytes, currentIndex, ssidLength);
			currentIndex+=ssidLength;
			newDataBytes[currentIndex++] = (byte) securityType;


			System.arraycopy(passwordLengthBytes, 0, newDataBytes, currentIndex, 2);
			currentIndex+=2;
			System.arraycopy(passwordBytes, 0, newDataBytes, currentIndex, passwordLength);
			currentIndex+=passwordLength;
			return MyFactory.creatorForOrderMontage().generateCommonCommand(
					new byte[]{0x21,0x18},
					newDataBytes);
		}
		else {
			return null;
		}
	}
	/**
	 * 配置wifi工作模式
	 */
	private static Boolean setDPUWiFiMode2118(IPhysics iPhysics,DPUWiFiModeConfig dpuWiFiModeConfig) {
		String backOrder = "";
		Boolean state = false;
		//记录是否已经生成过新流水号指令,用校验出错时判断
		Boolean hasGenerateNewCounter = false;
		byte[] sendOrder = generateDPUWiFiModeSettingsCommand(dpuWiFiModeConfig);
		if(MLog.isDebug) {
			MLog.d(TAG, "setDPUWiFiMode2118 .sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return state;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();

			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			if(MLog.isDebug) {
				MLog.d(TAG, "setDPUWiFiMode2118.backOrder = " + backOrder);
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				state=(Arrays.equals(new byte[]{0x01,0x01,0x00}, analysisData.getpReceiveBuffer()) ||
						Arrays.equals(new byte[]{0x01,0x02,0x00}, analysisData.getpReceiveBuffer()));
				break;
			}
			else {
				flag++;
			}
		}
		if(MLog.isDebug) {
			MLog.d(TAG, "setDPUWiFiMode2118. state = " + state);
		}
		return state;
	}
	/**
	 * 解析获取的wifi模式信息
	 * @param receiveBuffer 去掉了命令字与校验字的数据
	 * @return
	 */
	private static DPUWiFiModeConfig analysisDPUWiFiModeInformation(byte[] receiveBuffer){
		DPUWiFiModeConfig dpuWiFiModeConfig=null;
		// 0x21,0x19 获取接头wifi相关工作信息
		// ----------0x01 表示获取wifi工作模式
		// 回复信息
		// 0x61,0x19,0x01,0x00,表示获取信息成功
		// ------------------0x01 表示接头wifi工作模式为热点模式
		// ------------------0x02+ssid+安全类型+密码 表示接头wifi工作模式为网卡模式
		if (receiveBuffer[1] == 0) {
			dpuWiFiModeConfig = new DPUWiFiModeConfig();
			switch (receiveBuffer[2]) {
				case 0x01:
					dpuWiFiModeConfig.setMode(Constants.WIFI_WORK_MODE_WITH_AP);
					break;
				case 0x02:
					dpuWiFiModeConfig.setMode(Constants.WIFI_WORK_MODE_WITH_STA_MODE_NO_INTERACTION);
					int currentIndex = 3;
					int ssidLength = (receiveBuffer[currentIndex] & 0xFF)* 256+ (receiveBuffer[currentIndex + 1] & 0xFF);
					String networkSSID = new String(receiveBuffer,currentIndex + 2, ssidLength,Charset.forName("US-ASCII"));
					currentIndex += 2 + ssidLength;
					int securityType = receiveBuffer[currentIndex];
					currentIndex += 1;
					int passwordLength = (receiveBuffer[currentIndex] & 0xFF)* 256+ (receiveBuffer[currentIndex + 1] & 0xFF);
					String password = new String(receiveBuffer,currentIndex + 2, passwordLength,Charset.forName("US-ASCII"));
					WifiConfiguration config =DPUWiFiModeConfig.packageWifiConfiguration(networkSSID,
							securityType,
							password);
					dpuWiFiModeConfig.setConfig(config);
					if(MLog.isDebug){
						MLog.d(TAG, String.format(" analysisDPUWiFiModeInformation SSID=%s Security=%d Password=%s",
								networkSSID,
								securityType,
								password)
						);
					}
					break;
			}
			return dpuWiFiModeConfig;
		}
		else{
			return null;
		}
	}

	/**
	 * 获取wifi工作模式信息
	 */
	private static DPUWiFiModeConfig getDPUWiFiMode2119(IPhysics iPhysics) {
		String backOrder = "";
		DPUWiFiModeConfig dpuWiFiModeConfig = null;
		//记录是否已经生成过新流水号指令,用校验出错时判断
		Boolean hasGenerateNewCounter = false;
		byte[] sendOrder = MyFactory.creatorForOrderMontage().generateCommonCommand(
				new byte[]{0x21,0x19},
				new byte[]{0x01});
		if(MLog.isDebug) {
			MLog.d(TAG, "getDPUWiFiMode2119 .sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
		}
		int flag = 0;
		if (sendOrder.length <= 0) {
			return dpuWiFiModeConfig;
		}
		while (flag < 3) {
			Tools.writeDPUCommand(sendOrder, iPhysics);
			backOrder = iPhysics.getCommand();
			if (TextUtils.isEmpty(backOrder)) {
				flag++;
				continue;
			}
			if(MLog.isDebug) {
				MLog.d(TAG, "getDPUWiFiMode2119.backOrder = " + backOrder);
			}
			byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
			Analysis analysis = MyFactory.creatorForAnalysis();
			AnalysisData analysisData = analysis.analysis(sendOrder, receiveOrder);
			if (analysisData.getState()) {
				byte[] receiveBuffer = analysisData.getpReceiveBuffer();
				if(MLog.isDebug) {
					MLog.d(TAG, "getDPUWiFiMode2119 .data receiveBuffer = " + ByteHexHelper.bytesToHexString(receiveBuffer));
				}
				dpuWiFiModeConfig = analysisDPUWiFiModeInformation( receiveBuffer);
				break;
			}
			else {
				flag++;
			}
		}
		if(MLog.isDebug) {
			MLog.d(TAG, "getDPUWiFiMode2119. end " );
		}
		return  dpuWiFiModeConfig;
	}


	/**
	 * 生成获取wifista模式ip地址与端口请求数据包指令
	 * @param dpuWiFiModeConfig
	 * @return
	 */
	public static byte[] generateDPUWiFiDatagramRequestCommand(DPUWiFiModeConfig dpuWiFiModeConfig){
		String serialNo = dpuWiFiModeConfig.getSerialNo();
		return packageDPUWiFiDatagramRequestCommandWithVersion0101(serialNo,true);
	}
	/**
	 * 解析验证通过udp获取接头的响应数据包指令
	 * @param sendOrder      完整请求命令
	 * @param receiveOrder  完整响应命令
	 * @return 正确则返回true
	 */
	public static boolean analysisDPUWiFiDatagramAnswerCommand(byte[]sendOrder,byte[] receiveOrder){

		//byte[] requestWord = new byte[]{0x21,0x19,0x02};
		//byte[] answerWord = new byte[]{0x61,0x19,0x02};
		//获取发送数据包的序列号
		byte[] srcSerialNo=null;
		byte[] dstSerialNo=null;
		if(sendOrder.length<4){
			return false;
		}
		int serialNoLength =0;
		int currentIndex = 2;
		int contentLength = (sendOrder[currentIndex] & 0xFF)* 256+ (sendOrder[currentIndex+1] & 0xFF);
		if(contentLength == sendOrder.length-4-1){
			//01.01版本
			if(sendOrder[currentIndex+2]==0x01 && sendOrder[currentIndex+3]==0x01){
				if(sendOrder[currentIndex+4]==0x21 && sendOrder[currentIndex+5]==0x19 && sendOrder[currentIndex+6]==0x02){
					serialNoLength = (sendOrder[currentIndex+7] & 0xFF)* 256+ (sendOrder[currentIndex+8] & 0xFF);
					srcSerialNo = new byte[serialNoLength];
					System.arraycopy(sendOrder, currentIndex+9, srcSerialNo,0,serialNoLength);
					if(MLog.isDebug) {
						MLog.d(TAG, "analysisDPUWiFiDatagramAnswerCommand sendOrder serino = " + new String(srcSerialNo,Charset.forName("US-ASCII")));
					}
				}
			}
		}
		if(receiveOrder.length<4){
			return false;
		}
		currentIndex = 2;
		contentLength = (receiveOrder[currentIndex] & 0xFF)* 256+ (receiveOrder[currentIndex+1] & 0xFF);
		if(contentLength <= receiveOrder.length-4-1){
			//01.01版本
			if(receiveOrder[currentIndex+2]==0x01 && receiveOrder[currentIndex+3]==0x01){
				if(receiveOrder[currentIndex+4]==0x61 && receiveOrder[currentIndex+5]==0x19 && receiveOrder[currentIndex+6]==0x02){
					serialNoLength = (receiveOrder[currentIndex+7] & 0xFF)* 256+ (receiveOrder[currentIndex+8] & 0xFF);
					dstSerialNo = new byte[serialNoLength];
					System.arraycopy(receiveOrder, currentIndex+9, dstSerialNo,0,serialNoLength);
					if(MLog.isDebug) {
						MLog.d(TAG, "analysisDPUWiFiDatagramAnswerCommand receiveOrder serino = " + new String(dstSerialNo,Charset.forName("US-ASCII")));
					}
				}
			}
		}

		if(srcSerialNo != null && dstSerialNo !=null && Arrays.equals(srcSerialNo, dstSerialNo)){
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * 通过udp获取接头ip与端口打包完整命令<br/>
	 * 完整的命令数据格式:<br\>
	 * <命令格式>+<包头标记>+<包长度>+<版本号>+<命令字>+<数据区>+<包校验> <br/>
	 * <起始标志>2个字节，为0x55 0xaa <br/>
	 * <包长度>2个字节，为<版本号>+<命令字>+<数据区>数据长度之和。 <br/>
	 * <版本号><br/>
	 * <命令字><br/>
	 * <数据区><br/>
	 * <包校验>:对“<包长度>+<版本号>+<命令字>+<数据区>”等部分按字节进行异或运算，其结果等于校验值 <br/>
	 *
	 *
	 * DPU接头wifi模块广播数据包格式如下：
	 * 1 包头标记 串口常用包头 2 byte 0x55,0xaa
	 * 2 包长度长度 2 byte  高位在前，低位在后
	 * 3 包格式版本 2 byte  始版本为01.01,转成十六进制直接存储。存储格式为：[0x01][0x01]
	 * 4 命令字3 byte 请求命令字0x21,0x19,0x02 应答命令字0x61,0x19,0x02
	 * 5 接头序列号 DString DSTRING数据类型的内容为 <字符串长度><字符串> 字符串长度 高位在前，低位在后
	 * 比如我们接头序列号（识别码）使用ascii码表示比如985691000700表示为 {0x39,0x38,0x35,0x36,0x39,0x31,0x30,0x30,0x30,0x37,0x30,0x30}
	 * 所以表示为 0x00,0x0C, 0x39,0x38,0x35,0x36,0x39,0x31,0x30,0x30,0x30,0x37,0x30,0x30
	 * 6 校验字 从包长度开始，进行异或运算
	 * */
	private static byte[] packageDPUWiFiDatagramRequestCommandWithVersion0101(String sserialNo,boolean isRequest){

		byte[] headFlag = new byte[]{0x55,(byte)0xaa};
		byte[] version = new byte[]{0x01,0x01};
		byte[] serialNo = sserialNo.getBytes(Charset.forName("US-ASCII"));
		byte[] requestWord = new byte[]{0x21,0x19,0x02};
		byte[] answerWord = new byte[]{0x61,0x19,0x02};
		int serialNoLength = serialNo.length;
		int worldLength ;
		if(isRequest){
			worldLength = requestWord.length;
		}
		else{
			worldLength = answerWord.length;
		}
		//整条命令长 = <包头标记>+<包长度>+<版本号>+<命令字>+<数据区>+<包校验>
		int commandLendth  = headFlag.length+2+version.length+worldLength+2+serialNoLength+1;
		int packageLength = version.length+worldLength+(2+serialNoLength);
		int verifyPos = 0;
		byte[] command = new byte[commandLendth];
		int pos = 0;
		System.arraycopy(headFlag, 0, command, pos, headFlag.length);// 起始标志
		pos +=  headFlag.length;
		verifyPos  = pos;
		command[pos] = (byte) ((packageLength>>8)&0xff);// 包长度（高位）
		pos +=  1;
		command[pos] = (byte) (packageLength&0xff);// 包长度(低位)
		pos +=  1;
		//版本号
		System.arraycopy(version, 0, command, pos,version.length);
		pos +=  version.length;
		//命令字
		if(isRequest){
			System.arraycopy(requestWord, 0, command, pos,worldLength);
		}
		else{
			System.arraycopy(answerWord, 0, command, pos,worldLength);
		}
		pos +=  worldLength;
		//数据区
		//序列号
		//长度
		command[pos] = (byte) ((serialNoLength>>8)&0xff);// 包长度（高位）
		pos +=  1;
		command[pos] = (byte) (serialNoLength&0xff);// 包长度(低位)
		pos +=  1;
		System.arraycopy(serialNo, 0, command, pos,serialNoLength);
		byte verify = 0;
		//<包长度>+<版本号>+<数据区>
		for (int j=verifyPos; j<=verifyPos+2+packageLength; j++) {
			verify ^= command[j];
		}
		// 校验值放在最后一字节中
		command[commandLendth-1] = verify;
		return command;
	}

	@Override
	public void setDPUWiFiAPConfigAsync(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener, DPUWiFiAPConfig dpuWiFiAPConfig) {
		onWiFiModeListener.OnSetWiFiAPConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_SUCCESS);
	}

	@Override
	public void getDPUWiFiAPConfigAsync(IPhysics iPhysics, OnWiFiModeListener onWiFiModeListener) {
		onWiFiModeListener.OnGetWiFiAPConfigListener(OnWiFiModeListener.WIFI_MODE_CONFIG_SUCCESS,null);
	}
}
