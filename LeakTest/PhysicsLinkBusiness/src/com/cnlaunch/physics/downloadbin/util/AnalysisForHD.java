package com.cnlaunch.physics.downloadbin.util;

import android.util.Log;

import com.cnlaunch.physics.entity.AnalysisData;
import com.cnlaunch.physics.listener.OnDownloadBinListener;
import com.cnlaunch.physics.utils.ByteHexHelper;

import java.util.ArrayList;


/**
 * 解析手机蓝牙与远征设备蓝牙数据
 */
public class AnalysisForHD extends Analysis {
	private static AnalysisForHD instance;
	private AnalysisForHD() {
		
	}
	public static AnalysisForHD getInstance() {
		if (instance == null) {
			instance = new AnalysisForHD();
		}
		return instance;
	}

	public AnalysisData analysis(byte[] sendCommand, byte[] receive) {
		return analysis(sendCommand,receive,null);
	}
	
	private void sendStatus(OnDownloadBinListener mOnDownloadBinListener,String str){
		if(mOnDownloadBinListener!=null)
			mOnDownloadBinListener.OnDownloadBinCmdListener(str);
	}
	
	public AnalysisData analysis(byte[] sendCommand, byte[] receive,OnDownloadBinListener mOnDownloadBinListener) {
		AnalysisData analysisData = new AnalysisData();
//		Log.e("wzx", "AnalysisData analysis-->sendCommand = " + ByteHexHelper.bytesToHexString(sendCommand));
//		Log.e("wzx", " AnalysisData analysis-->receive = "+ByteHexHelper.bytesToHexString(receive));
		int feedbackBagLength = 0;// 返回指令的包长度
		byte[] pSendBuffer = null;

		do {
			System.out.println("开始校验相应字节数组...");
			sendStatus(mOnDownloadBinListener,"开始校验相应字节数组...");
			if (!(receive != null && receive.length > 0)) {
				analysisData.setState(false);
				System.out.println("接收指令为空！");
				errCount++;
				if(errCount>4)
					mOnDownloadBinListener.OnDownloadBinListener(-99,0,0);//加个判断 ，超过3次则发送关闭事件
				else
					sendStatus(mOnDownloadBinListener,"接收指令为空!");
					break;
				}
			if (!(receive.length > 1
					&& ByteHexHelper.byteToHexString(receive[0])
							.equalsIgnoreCase("55"))) {
				analysisData.setState(false);
				System.out.println("首字节 0x55 fail!");
				sendStatus(mOnDownloadBinListener,"首字节校验失败!");
				break;
			}
			System.out.println("首字节 0x55 OK!");
			sendStatus(mOnDownloadBinListener,"首字节校验通过...");
			if (!(receive.length > 2
					&& ByteHexHelper.byteToHexString(receive[1])
							.equalsIgnoreCase("aa"))) {
				analysisData.setState(false);
				System.out.println("包头校验fail!");
				sendStatus(mOnDownloadBinListener,"包头校验失败!");
				break;
			}

			// ==========================
			System.out.println("包头校验 OK!");
			sendStatus(mOnDownloadBinListener,"包头校验校验通过...");
			if (!(receive.length > 6)) {

				analysisData.setState(false);
				System.out.println("命令获取不完整，没有获取到包长度！");
				break;

			}
			feedbackBagLength = receive[2] * 256 + receive[3];
			System.out.println("包长度为：" + feedbackBagLength);
			if (!(receive.length > 8)) {

				analysisData.setState(false);
				System.out.println("命令获取不完整，没有获取到包长度！");
				sendStatus(mOnDownloadBinListener,"命令获取不完整!");
				break;

			}
			// byte[] bagLengthByte1 = { receive[4], receive[5] };// 包长度byte[]in
			int feedbackBagLength1 = receive[4] * 256 + receive[5];
			System.out.println("包长度为：" + feedbackBagLength1);
			if (feedbackBagLength == (~feedbackBagLength1)) {

				analysisData.setState(false);
				System.out.println("bao chang du  xin xi cuowu！");
				break;

			}
			if (!(feedbackBagLength > 0 && receive.length >= feedbackBagLength + 2)) {

				analysisData.setState(false);
				System.out.println("命令获取不完整");
				sendStatus(mOnDownloadBinListener,"命令获取不完整!");
				break;

			}
			System.out.println("命令字校验 OK!");
			byte[] pReceiveBuffer = new byte[feedbackBagLength - 4];
			int flag = 0;
			for (int i = 0; i <  feedbackBagLength - 4; i++) {
//						Log.e("wzx", "AnalysisData analysis-->receive["+i+6+"] = "+ByteHexHelper.byteToHexString(receive[i+6]));
				pReceiveBuffer[flag] = receive[i+6];
				flag++;
			}
			System.out.println("收到命令数据成功!");
			sendStatus(mOnDownloadBinListener,"收到命令数据成功!");
			analysisData.setState(true);
			analysisData.setpReceiveBuffer(pReceiveBuffer);
			analysisData.setpRequestBuffer(pSendBuffer);
			//analysisData.setRequestWordStr(requestWordStr);
			return analysisData;			
		} while (false);
		return analysisData;
	}

	/**
	 *
	 * Smartbox30 Linux校验方式
	 * @param sendCommand
	 * @param receive
	 * @return
	 */
	public AnalysisData analysisSmartbox30LinuxCommand(byte[] sendCommand, byte[] receive) {
		return analysisSmartbox30LinuxCommand(sendCommand, receive, null);
	}

	/**
	 * Smartbox30 Linux校验方式
	 * 因为没有校验源地址与目标地址，所以仍采用原来标准的校验方式
	 * @param sendCommand
	 * @param receive
	 * @param mOnDownloadBinListener
	 * @return
	 */
	public AnalysisData analysisSmartbox30LinuxCommand(byte[] sendCommand, byte[] receive, OnDownloadBinListener mOnDownloadBinListener) {
		return analysis(sendCommand, receive, mOnDownloadBinListener);
	}
	static int errCount = 0;
	public boolean analysisData(byte[] sendCommand, byte[] receive) {
		AnalysisData analysisData = new AnalysisData();
		Log.e("wll", "AnalysisData analysis-->sendCommand[7]" + sendCommand);
		// byte[] requestWord = { sendCommand[6], sendCommand[7] };// 发送指令 命令字
		// // byte[]
		// String requestWordStr =
		// ByteHexHelper.bytesToHexString(requestWord);// 发送指令
		// Log.e("wll", "AnalysisData requestWordStr-->"+requestWordStr); // 命令字
		// string
		// System.out.println("发送的命令字：" + requestWordStr);
		// int requestCounter = ByteHexHelper.byteToInt(sendCommand[6]);//
		// 发送指令计数器
		int feedbackBagLength = 0;// 返回指令的包长度
		byte[] pSendBuffer = null;

		// if (sendCommand.length > 6) {
		// byte[] sendbagLengthByte = { sendCommand[], sendCommand[5] };//
		// 包长度byte[]
		// int sendBagLength = ByteHexHelper.intPackLength(sendbagLengthByte);
		// if (sendBagLength > 0 && sendCommand.length >= sendBagLength + 7) {
		// pSendBuffer = new byte[sendBagLength - 3];
		// int flag = 0;
		// for (int i = 9; i < 9 + sendBagLength - 3; i++) {
		// Log.e("wll", "AnalysisData analysis-->forxunhuan");
		// pSendBuffer[flag] = sendCommand[i];
		// flag++;
		// }
		// }
		// }

		// System.out.println("发送指令计数器值 = " + requestCounter);
		// 55 aa 00 07 ff f8 60 20 40
		do {
			System.out.println("开始校验相应字节数组...");
			if (!(receive != null && receive.length > 0)) {
				analysisData.setState(false);
				System.out.println("接收指令为空！");
				break;
			}
			if (!(receive.length > 1
					&& ByteHexHelper.byteToHexString(receive[0])
							.equalsIgnoreCase("55"))) {
				analysisData.setState(false);
				System.out.println("首字节 0x55 fail!");
				break;
			}
			System.out.println("首字节 0x55 OK!");
			if (!(receive.length > 2
					&& ByteHexHelper.byteToHexString(receive[1])
							.equalsIgnoreCase("aa"))) {
				analysisData.setState(false);
				System.out.println("包头校验fail!");
				break;
			}

			// ==========================
			System.out.println("包头校验 OK!");
			if (!(receive.length > 6)) {

				analysisData.setState(false);
				System.out.println("命令获取不完整，没有获取到包长度！");
				break;

			}
			// byte[] bagLengthByte = { receive[2], receive[3] };// 包长度byte[]
			// feedbackBagLength = ByteHexHelper
			// .intPackLength(bagLengthByte);
			feedbackBagLength = receive[2] * 256 + receive[3];
			System.out.println("包长度为：" + feedbackBagLength);

			if (!(receive.length > 8)) {

				analysisData.setState(false);
				System.out.println("命令获取不完整，没有获取到包长度！");
				break;

			}
			// byte[] bagLengthByte1 = { receive[4], receive[5] };// 包长度byte[]in
			int feedbackBagLength1 = receive[4] * 256 + receive[5];
			System.out.println("包长度为：" + feedbackBagLength1);
			if (feedbackBagLength == (~feedbackBagLength1)) {

				analysisData.setState(false);
				System.out.println("bao chang du  xin xi cuowu！");
				break;

			}
			if (!(feedbackBagLength > 0 && receive.length >= feedbackBagLength + 2)) {

				analysisData.setState(false);
				System.out.println("命令获取不完整");
				break;

			}

			//if (ByteHexHelper.byteToInt(receive[6]) == requestCounter) {
				//System.out.println("计数器校验 OK!");
				//byte[] feedbackWord = { receive[7], receive[8] };// 反馈指令
																	// 命令字
																	// byte[]
//				String feedbackWordStr = ByteHexHelper
//						.bytesToHexString(feedbackWord);// 命令字
//														// string
//				if (feedbackWordStr.substring(0, 1).equalsIgnoreCase("6")
//						&& feedbackWordStr.substring(1).equalsIgnoreCase(
//								requestWordStr.substring(1))) {
					System.out.println("命令字校验 OK!");
					byte[] pReceiveBuffer = new byte[feedbackBagLength - 4];
					int flag = 0;
					for (int i = 0; i <  feedbackBagLength - 4; i++) {
						Log.e("wll", "AnalysisData analysis-->forxunhuan@@@");
						pReceiveBuffer[flag] = receive[i+6];
						flag++;
					}
					System.out.println("收到命令数据成功!");
					analysisData.setState(true);
					analysisData.setpReceiveBuffer(pReceiveBuffer);
					analysisData.setpRequestBuffer(pSendBuffer);
					//analysisData.setRequestWordStr(requestWordStr);
					return true;
		

			
		} while (false);
		return false;
	}

	/**
	 * 跳转至download 代码入口
	 * 
	 * @param analysisData
	 * @return
	 */
	public Boolean analysis2111(AnalysisData analysisData) {
		// 命令校验状态
		boolean state = analysisData.getState();
		// 返回的数据是否正确
		boolean isSuccess = false;
		if (state) {
			byte[] receiverBuffer = analysisData.getpReceiveBuffer();
			String result = ByteHexHelper.bytesToHexString(receiverBuffer);
			if (result.equalsIgnoreCase("00")) {
				isSuccess = true;
			} else {
				isSuccess = false;
			}
			System.out.println("解析2111返回的数据===" + result);

		} else {
			isSuccess = false;
			System.out.println("2111命令校验未通过");
		}

		return isSuccess;
	}

	/**
	 * 复位接头反馈
	 * 
	 * @param analysisData
	 * @return
	 */
	public Boolean analysis2505(AnalysisData analysisData) {
		// 命令校验状态
		boolean state = analysisData.getState();
		// 返回的数据是否正确
		boolean isSuccess = false;
		if (state) {
			isSuccess = true;
			System.out.println("复位成功");

		} else {
			isSuccess = false;
			System.out.println("2505命令校验未通过");
		}

		return isSuccess;
	}

	/**
	 * 断开连接
	 * 
	 * @param analysisData
	 * @return
	 */
	public Boolean analysis2504(AnalysisData analysisData) {
		// 命令校验状态
		boolean state = analysisData.getState();
		// 返回的数据是否正确
		boolean isSuccess = false;
		if (state) {
			isSuccess = true;
			System.out.println("断开连接成功");

		} else {
			isSuccess = false;
			System.out.println("2504命令校验未通过");
		}

		return isSuccess;
	}

	/**
	 * 验证安全密码指令
	 * 
	 * @param analysisData
	 * @return
	 */
	public Boolean analysis2110(AnalysisData analysisData) {
		// 命令校验状态
		boolean state = analysisData.getState();
		// 返回的数据是否正确
		boolean isSuccess = false;
		if (state) {
			byte[] receiverBuffer = analysisData.getpReceiveBuffer();
			String result = ByteHexHelper.bytesToHexString(receiverBuffer);
			if (result.equalsIgnoreCase("00")) {
				isSuccess = true;
			} else {
				isSuccess = false;
			}
			System.out.println("解析2110返回的数据===" + result);
		} else {
			isSuccess = false;
			System.out.println("2110命令校验未通过");
		}
		return isSuccess;
	}

	/**
	 * 请求连接
	 * 
	 * @param analysisData
	 * @return
	 */
	public String analysis2502(AnalysisData analysisData) {
		// 命令校验状态
		boolean state = analysisData.getState();
		String result = "";
		if (state) {

			byte[] receiverBuffer = analysisData.getpReceiveBuffer();
			byte[] requestBuffer = analysisData.getpRequestBuffer();
			if (requestBuffer.length > 0) {
				String reqParam = ByteHexHelper
						.byteToHexString(requestBuffer[0]);
				String resParam = ByteHexHelper
						.byteToHexString(receiverBuffer[0]);
				if (reqParam.equals(resParam)) {
					if (resParam.equalsIgnoreCase("01")) {
						byte[] src = new byte[3];
						for (int i = 0; i < 3; i++) {
							src[i] = receiverBuffer[i];
						}
						result = ByteHexHelper.bytesToHexString(src);

					} else if (resParam.equalsIgnoreCase("02")) {
						byte[] src = new byte[5];
						for (int i = 0; i < 5; i++) {
							src[i] = receiverBuffer[i];
						}
						result = ByteHexHelper.bytesToHexString(src);
					}
					System.out.println("2502 校验码==" + result);
				} else {
					System.out.println("2502 请求参数与响应参数不同");
				}
			} else {
				System.out.println("2502发送命令长度不大于0");
			}
		} else {
			System.out.println("2502命令校验未通过");
		}
		return result;
	}

	/**
	 * 安全校验
	 * 
	 * @param analysisData
	 * @return
	 */
	public Boolean analysis2503(AnalysisData analysisData) {
		// 命令校验状态
		boolean state = analysisData.getState();
		// 返回的数据是否正确
		boolean isSuccess = false;
		if (state) {
			byte[] request = analysisData.getpRequestBuffer();
			byte[] receiverBuffer = analysisData.getpReceiveBuffer();
			if (request.length > 0 && receiverBuffer.length > 0) {
				String reqParam = ByteHexHelper.byteToHexString(request[0]);
				String resParam = ByteHexHelper
						.byteToHexString(receiverBuffer[0]);
				if (reqParam.equalsIgnoreCase(resParam)) {
					if (receiverBuffer.length > 1) {
						String res = ByteHexHelper
								.byteToHexString(receiverBuffer[1]);
						if (res.equalsIgnoreCase("00")) {
							isSuccess = true;
							System.out.println("解析2503返回的数据===" + "成功获得授权");
						} else if (res.equalsIgnoreCase("01")) {
							isSuccess = false;
							System.out.println("解析2503返回的数据===" + "拒绝，校验字节错误");
						} else if (res.equalsIgnoreCase("02")) {
							isSuccess = false;
							System.out.println("解析2503返回的数据===" + "其他错误");
						}
						System.out.println("解析2503返回的数据===" + res);
					} else {
						System.out.println("2503请求参数长度小于1");
					}
				} else {
					System.out.println("2503请求校验等级与响应校验等级不同");
				}

			} else {
				System.out.println("2503请求参数长度小于0");
			}

		} else {
			isSuccess = false;
			System.out.println("2503命令校验未通过");
		}
		return isSuccess;
	}

	/**
	 * 读取当前状态 Bootloader =0x00/download=0x01
	 * 
	 * @param analysisData
	 * @return
	 */
	public String analysis2114(AnalysisData analysisData) {
		String runningmode = "";
		Object ret;
		// 返回的数据是否正确
		byte[] receiverBuffer = analysisData.getpReceiveBuffer();
		runningmode = ByteHexHelper.byteToHexString(receiverBuffer[0]);
		System.out.println("2114 返回的数据==" + runningmode);
		return runningmode;
	}
	/**
	 * 解析downloadbin版本信息
	 * 
	 * @param analysisData
	 * @return
	 */
//	public ArrayList<String> analysis2105(AnalysisData analysisData) {
//		// 命令校验状态
//		boolean state = analysisData.getState();
//		// 返回的数据是否正确
//		String downLoadBinVer = "";
//		
//		ArrayList<String> versionInfo = null;
//		if (state) {
//			// 收到的数据
//			byte[] receiverBuffer = analysisData.getpReceiveBuffer();
//			byte[] params = DpuOrderUtils
//					.filterOutCmdParameters(receiverBuffer);
////			Log.e("wzx", "analysis2105------->>>>>params"+ByteHexHelper.bytesToHexString(params));
//			if (params != null && params.length >= 3) {
////						versionInfo = ByteHexHelper.toStringArray(params);
//				downLoadBinVer = ByteHexHelper.bytesToHexString(params).substring(6, 10);//取出软件版本号
//				//downLoadBinVer = versionInfo.get(1);
//				System.out.println("设备上的 download.bin版本== " + downLoadBinVer);
////				Log.e("wzx", "设备上的 download.bin版本== " + downLoadBinVer);
//				return downLoadBinVer;
//			}
//
//		} else {
//			downLoadBinVer = "";
//			System.out.println("设备上的 download.bin版本为空");
//		}
//		return downLoadBinVer;
//	}	
	/**
	 * 解析dpu 软件版本信息
	 * 
	 * @param analysisData
	 * @return
	 */
	public ArrayList<String> analysis2105(AnalysisData analysisData) {
		// 命令校验状态
		boolean state = analysisData.getState();
		ArrayList<String> versionInfo = null;
		if (state) {
			//收到的数据
			byte[] receiverBuffer = analysisData.getpReceiveBuffer();
			byte[] params = DpuOrderUtils.filterOutCmdParameters(receiverBuffer);			
			if(params!=null && params.length >= 3)
			{
				String receiveParams = ByteHexHelper.bytesToHexString(params);
				versionInfo = new ArrayList<String>();
				versionInfo.add(receiveParams);
			}
			
		} 
		return versionInfo;
	}
	/**
	 * 切换到boot升级模式
	 */

	public Boolean analysis2407(AnalysisData analysisData) {
		String status = "";
		boolean state = analysisData.getState();
		// 返回的数据是否正确
		boolean isSuccess = false;
		byte[] receiverBuffer = analysisData.getpReceiveBuffer();
		if (state) {
			status = ByteHexHelper.byteToHexString(receiverBuffer[0]);
			if (status.equalsIgnoreCase("00")) {
				isSuccess = true;
			} else {
				isSuccess = false;
			}
		} else {
			isSuccess = false;
			System.out.println("2407返回失败");
		}
		System.out.println("2407返回的数据==" + isSuccess);
		return isSuccess;
	}

	public Boolean analysis2402(AnalysisData analysisData) {
		String status = "";
		boolean state = analysisData.getState();
		// 返回的数据是否正确
		boolean isSuccess = false;
		byte[] receiverBuffer = analysisData.getpReceiveBuffer();
		if (state) {
			status = ByteHexHelper.byteToHexString(receiverBuffer[0]);
			if (status.equalsIgnoreCase("00")) {
				isSuccess = true;
			} else {
				isSuccess = false;
			}
		} else {
			isSuccess = false;
			System.out.println("2402返回失败");
		}
		System.out.println("2402返回的数据==" + isSuccess);
		return isSuccess;
	}

	public Boolean analysis2404(AnalysisData analysisData) {
		String status = "";
		boolean state = analysisData.getState();
		// 返回的数据是否正确
		boolean isSuccess = false;
		byte[] receiverBuffer = analysisData.getpReceiveBuffer();
		if (state) {
			status = ByteHexHelper.byteToHexString(receiverBuffer[0]);
			if (status.equalsIgnoreCase("00")) {
				isSuccess = true;
			} else {
				isSuccess = false;
			}
		} else {
			isSuccess = false;
			System.out.println("2404返回失败");
		}
		System.out.println("2404返回的数据==" + isSuccess);
		return isSuccess;
	}

	public Boolean analysis2405(AnalysisData analysisData) {
		String status = "";
		boolean state = analysisData.getState();
		// 返回的数据是否正确
		boolean isSuccess = false;
		byte[] receiverBuffer = analysisData.getpReceiveBuffer();
		if (state) {
			status = ByteHexHelper.byteToHexString(receiverBuffer[0]);
			if (status.equalsIgnoreCase("00")) {
				isSuccess = true;
			} else {
				isSuccess = false;
			}
		} else {
			isSuccess = false;
			System.out.println("2405返回失败");
		}
		System.out.println("2405返回的数据==" + isSuccess);
		return isSuccess;
	}

	public Boolean analysis2403(AnalysisData analysisData) {
		String status = "";
		boolean state = analysisData.getState();
		// 返回的数据是否正确
		boolean isSuccess = false;
		byte[] receiverBuffer = analysisData.getpReceiveBuffer();
		if (state) {
			status = ByteHexHelper.byteToHexString(receiverBuffer[0]);
			if (status.equalsIgnoreCase("00")) {
				isSuccess = true;
			} else {
				isSuccess = false;
			}
		} else {
			isSuccess = false;
			System.out.println("2403返回失败");
		}
		System.out.println("2403返回的数据==" + isSuccess);
		return isSuccess;
	}

	/**
	 * 取DPU接头硬件版本信息
	 * 
	 * @param analysisData
	 * @return
	 */
	public String[] analysis2103(AnalysisData analysisData) {
		// 命令校验状态
		boolean state = analysisData.getState();
		//修正为字符数组链表更准确
		ArrayList<String> buffer = new  ArrayList<String>();
		//StringBuffer buffer = new StringBuffer();
		String[] content = null;
		String pcbVerNum = null;
		if (state) {
			byte[] receiverBuffer = analysisData.getpReceiveBuffer();

			int receiverLength = receiverBuffer.length;
			if (receiverLength > 0) {
				if (receiverLength > 1) {
					int len = 1;
					int pcbVerLength = 4;
					// pcb 版本号
					if (receiverLength > len + pcbVerLength) {
						int flag = 0;
						byte[] pcb = new byte[pcbVerLength];
						for (int i = len; i < len + pcbVerLength; i++) {
							pcb[flag] = receiverBuffer[i];
							flag++;
						}
						pcbVerNum = ByteHexHelper.bytesToHexString(pcb);
						buffer.add(pcbVerNum);
						System.out.println("pcbVerNum = " + pcbVerNum);
					}					
				} else {
					System.out.println("2103返回数据长度==" + receiverLength);
				}
			} else {
				System.out.println("2103===接收数据不正确");
			}
		} else {
			System.out.println("2103返回数据长度小于0");
		}
		if (buffer.size() > 0) {
			content = buffer.toArray(new String[buffer.size()]);
		}
		return content;
	}

	/**
	 * 复位DPU运行模式
	 * 
	 * @param analysisData
	 * @return
	 */
	public Boolean analysis2109(AnalysisData analysisData) {
		// 命令校验状态
		boolean state = analysisData.getState();
		// 返回的数据是否正确
		boolean isSuccess = false;
		if (state) {
			byte[] receiverBuffer = analysisData.getpReceiveBuffer();
			byte[] requestBuffer = analysisData.getpRequestBuffer();
			if (receiverBuffer.length > 0) {
				if (requestBuffer.length > 0) {
					String res = ByteHexHelper
							.byteToHexString(receiverBuffer[0]);
					String req = ByteHexHelper
							.byteToHexString(receiverBuffer[0]);
					if (res.equalsIgnoreCase(req)) {
						System.out.println("2109 请求参数与返回参数相同==" + req);
						if (receiverBuffer.length > 1) {
							String response = ByteHexHelper
									.byteToHexString(receiverBuffer[1]);
							if (response.equalsIgnoreCase("00")) {
								isSuccess = true;
								System.out.println("复位dpu运行模式成功！");
							} else {
								returnParam(response);
							}
						} else {
							System.out.println("2109 返回的参数长度小于1");
						}
					} else {
						System.out.println("2109 请求参数与返回参数不同");
					}

				} else {
					System.out.println("2109 请求参数长度小于0");
				}
			} else {
				System.out.println("2109 返回的参数长度小于0");
			}

		}
		return isSuccess;
	}

	/**
	 * 判断蓝牙反馈的成功与失败的状态信息
	 * 
	 * @param returnValue
	 */
	public void returnParam(String returnValue) {
		// 16进制转为10进制
		int value = Integer.parseInt(returnValue, 16);
		switch (value) {
		case 0:
			System.out.println("00==接收数据正确，或准备好可以升级");
			break;
		case 1:
			System.out.println("01==尚未获得安全校验，或安全校验错误");
			break;
		case 2:
			System.out.println("02==数据已接收，接头在忙");
			break;
		case 3:
			System.out.println("03==数据未接收，接头在忙");
			break;
		case 4:
			System.out.println("04==存储空间不足");
			break;
		case 5:
			System.out.println("05==文件已存在");
			break;
		case 6:
			System.out.println("06==已存在该软件,版本相同，升级完成");
			break;
		case 7:
			System.out.println("07==已存在该软件,版本相同，升级未完成");
			break;
		case 8:
			System.out.println("08==数据长度校验错误，写入位置与已接收数据长度不符");
			break;
		case 9:
			System.out.println("09==md5校验失败...");
			break;
		case 10:
			System.out.println("0a==文件数量不正确...");
			break;
		case 11:
			System.out.println("0b==其他异常....");
			break;
		case 12:
			System.out.println("0c==分配空间不足");
			break;
		case 13:
			System.out.println("0d==擦出失败");
			break;
		case 14:
			System.out.println("0e==写失败");
			break;
		case 15:
			System.out.println("0f==文件空");
			break;
		case 16:
			System.out.println("10==文件长度问题");
			break;
		case 17:
			System.out.println("11==设置出错");
			break;
		case 18:
			System.out.println("12==密码不正确");
			break;
		case 19:
			System.out.println("13==密码验证未通过");
			break;
		case 20:
			System.out.println("14==密码错误次数过多");
			break;

		default:
			break;
		}
	}
	/**
	 * 读当前接头是否已经烧录
	 * Bootloader =0x00/download=0x05
	 * @param analysisData
	 * @return
	 */
	public String analysis2107(AnalysisData analysisData) {
		String runningmode="";
		Object ret;
		// 返回的数据是否正确
		byte[] receiverBuffer = analysisData.getpReceiveBuffer();
		runningmode = ByteHexHelper.byteToHexString(receiverBuffer[0]);
		//System.out.println("2107 返回的数据==" + runningmode);
		return runningmode;
	}

	/**
	 * 解析车辆电压信息
	 *
	 */
	public ArrayList<String> analysis2120(AnalysisData analysisData) {
		// 命令校验状态
		boolean state = analysisData.getState();
		ArrayList<String> versionInfo = new ArrayList<String>();
		if (state) {
			byte[] receiverBuffer = analysisData.getpReceiveBuffer();
			byte[] params = DpuOrderUtils.filterOutCmdParameters(receiverBuffer);//去掉预留字节
			if (params != null && params.length >= 2) {
				String str = ByteHexHelper.bytesToHexString(params);
				int size = str.length();
				if (size == 10) {
					String str1 = str.substring(0, 4); //电压值
					versionInfo.add(str1);
					String str2 = str.substring(4, 8); //ADC原始数据
					versionInfo.add(str2);
					String str3 = str.substring(8, 10); //ADC精度
					versionInfo.add(str3);
				}
				return versionInfo;
			}
		} else {
			System.out.println("2120返回数据长度小于0");
		}

		return versionInfo;
	}
	public  String analysis2131(AnalysisData analysisData){
		return "";
	}
	public  Boolean analysis21xx(AnalysisData analysisData){
		return false;
	}
}
