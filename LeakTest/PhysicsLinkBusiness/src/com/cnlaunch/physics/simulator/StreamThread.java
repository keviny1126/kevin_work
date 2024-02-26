package com.cnlaunch.physics.simulator;

import android.util.Log;
import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.utils.ByteHexHelper;
import com.cnlaunch.physics.utils.MLog;
import java.util.ArrayList;

public class StreamThread extends Thread {
	private static String TAG = "StreamThread";
	private boolean isStop;
	private int maxBufferSize;
	private int totalBytes;
	private final int bufferSize = 1024 * 5;//返回包最大数据帧长度不超过5500，所以定为5120*2;
	private byte[] buffer;
	private byte[] totalBuffer;
	private SimulatorManager mSimulatorManager;
	private byte counter; //计数器
	ArrayList<SimulatorDPUCommand> mReservedCommandArrayList;
	private byte[] notSupportCommandFor2701;
	public StreamThread(SimulatorManager simulatorManager) {
		isStop = false;
		mReservedCommandArrayList = new ArrayList<SimulatorDPUCommand>();
		buffer = new byte[bufferSize];
		maxBufferSize = bufferSize * 2;
		totalBuffer = new byte[maxBufferSize];
		totalBytes = 0;
		SimulatorDPUCommand.initSimulatorDPUCommands(mReservedCommandArrayList);
		mSimulatorManager = simulatorManager;
		mReservedCommandArrayList.add(SimulatorDPUCommand.generateSimulator27016020Command(mSimulatorManager.getSerialNo()));
		mReservedCommandArrayList.add(SimulatorDPUCommand.generateSimulator2103Command(mSimulatorManager.getSerialNo()));
		mReservedCommandArrayList.add(SimulatorDPUCommand.generateSimulator2105Command());
		mReservedCommandArrayList.add(SimulatorDPUCommand.generateSimulator2131Command());
		mReservedCommandArrayList.add(SimulatorDPUCommand.generateSimulator2114Command());
		notSupportCommandFor2701=new byte[]{(byte)0xFF,(byte)0x02};
	}

	@Override
	public void run() {
		diagnoseRequestMonitor();
	}

	public void diagnoseRequestMonitor() {
		int bytes = 0;
		int totalLength = 0;
		int length = 0;
		try {
			//接收数据并检查帧的正确性
			while (getStopFlag() == false) {
				bytes = mSimulatorManager.getDiagnoseRequestDataByteBufferStream().read(buffer, 0, bufferSize);
				if (MLog.isDebug) {
					MLog.d(TAG, "Diagnose Request Data  buffer count = " + bytes);
				}
				if (bytes > 0) {
					if (totalBytes + bytes <= maxBufferSize) {
						System.arraycopy(buffer, 0, totalBuffer, totalBytes, bytes);
					} else {
						totalBytes = 0;
						System.arraycopy(buffer, 0, totalBuffer, totalBytes, bytes);
					}
				} else {
					if (bytes == 0) {
						continue;
					} else {
						break;
					}
				}
				totalBytes += bytes;
				// 验证包正确性,数据中是否包含0x55,0xaa,0xf0,x0f8
				int index = ByteHexHelper.bytesIndexOf(totalBuffer, SimulatorDPUCommand.REQ_START_CODE, 0, totalBytes);
				if (index >= 0) {
					if (index > 0) {
						int newTotalBytes = totalBytes - index;
						System.arraycopy(totalBuffer, index, totalBuffer, 0, newTotalBytes);
						totalBytes = newTotalBytes;
					}
					if (totalBytes >= 6) {
						length = (totalBuffer[4] & 0xff) * 256 + (totalBuffer[5] & 0xff);
						totalLength = SimulatorDPUCommand.FIXED_LENGTH + length;
						//Log.d(TAG, "totalBytes=" + totalBytes + " totalLength=" + totalLength);
						if (totalBytes >= totalLength && counter != totalBuffer[6]) {
							write(totalBuffer, totalLength);
							// 复位原状态
							totalBytes = totalBytes - totalLength;
							if (totalBytes > 0) {
								System.arraycopy(totalBuffer, totalLength, totalBuffer, 0, totalBytes);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void write(byte[] buffer, int length) {
		//从命令模拟队列中找到匹配的命令
		SimulatorDPUCommand command;
		counter = buffer[6];
		boolean isReserverCommand = false;
		byte[] answer = null;
		byte[] diagnoseAnswer = null;
		for (int i = 0; i < mReservedCommandArrayList.size(); i++) {
			command = mReservedCommandArrayList.get(i);
			if (ByteHexHelper.bytesIndexOf(buffer, command.getReqCommand(), 7, length - 7) == 7) {
				answer = SimulatorDPUCommand.packageReservedAnswerCommand(command.getAnsCommand(), command.getAnsCommand().length, counter);
				mSimulatorManager.getNetworkAnswerDataByteBufferStream().write(answer, 0, answer.length);
				isReserverCommand = true;
				break;
			}
		}
		if (isReserverCommand == false) {
			ISimulatorDataProcessor simulatorDataProcessor = DeviceFactoryManager.getInstance().getSimulatorDataProcessor();
			if (simulatorDataProcessor != null) {
				diagnoseAnswer = simulatorDataProcessor.getDiagnoseAnswerData(buffer, SimulatorDPUCommand.FIXED_LENGTH+2, length-SimulatorDPUCommand.FIXED_LENGTH-3);
				if (diagnoseAnswer != null && diagnoseAnswer.length > 0) {
					answer = SimulatorDPUCommand.package2701AnswerCommand(diagnoseAnswer, diagnoseAnswer.length, counter);
					mSimulatorManager.getNetworkAnswerDataByteBufferStream().write(answer, 0, answer.length);
				} else {
					answer = SimulatorDPUCommand.package2701AnswerCommand(notSupportCommandFor2701, notSupportCommandFor2701.length, counter);
					mSimulatorManager.getNetworkAnswerDataByteBufferStream().write(answer, 0, answer.length);
				}
			}
		}
	}

	private synchronized boolean getStopFlag() {
		return isStop;
	}

	public synchronized void stopThread() {
		if (MLog.isDebug) {
			MLog.d(TAG, "connect is close");
		}
		isStop = true;
	}
}