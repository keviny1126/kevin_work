package com.cnlaunch.physics.simulator;

import com.cnlaunch.physics.utils.ByteHexHelper;

import java.util.List;

public class SimulatorDPUCommand {
	public static final int FIXED_LENGTH = 7;
	public static final byte[] REQ_START_CODE = new byte[] {0x55, (byte)0xaa, (byte)0xf0, (byte)0xf8};// 
	public static final byte[] ANS_START_CODE = new byte[] {0x55, (byte)0xaa, (byte)0xf8, (byte)0xf0};//
	public static final byte[] COMMAND_WORD_6701 = new byte[] {0x67,0x01};
	private byte[] reqCommand;
	private byte[] ansCommand;
	public SimulatorDPUCommand(){
		reqCommand = null;
		ansCommand = null;
	}
	public byte[] getReqCommand(){
		return reqCommand;
	}
	public void setReqCommand(byte[] command){
		reqCommand= command;
	}

	public byte[] getAnsCommand(){
		return ansCommand;
	}
	public void setAnsCommand(byte[] command){
		ansCommand= command;
	}
	public void setAnsCommand(String commandHexString){
		ansCommand= ByteHexHelper.hexStringToBytes(commandHexString);
	}
	/**
	 * 打包成完整的命令<br/>
	 * 
	 * 完整的命令数据格式:<br\>
	 * <命令格式>：<起始标志>+<目标地址>+<源地址>+<包长度>+<计数器>+<命令字>+<数据区>+<包校验> <br/>
	 * <起始标志>：2个字节，为0x55 0xaa <br/>
	 * <目标地址>: 1个字节，当前帧数据发送后需要到达的网络中的唯一地址 <br/>
	 * <源地址>:	 1个字节，当前帧发送设备在此网络通讯中的唯一地址 <br/>
	 * <包长度>：2个字节，为“<计数器>+<命令字>+<数据区>”三部分数据长度之和。 <br/>
	 * <计数器>：1个字节，从0开始，客户端每次通信循环累加1，诊断接头以收到的计数器字节直接返回 <br/>
	 * <命令字>：2个字节，分为主功能命令字及子功能命令字<br/>
	 * <数据区>：若干字节，为命令字后的设置内容 <br/>
	 * <包校验>：对“<目标地址>+<源地址>+<包长度>+<计数器>+<命令字>+<数据区>”等部分按字节进行异或运算，其结果等于“校验值 <br/>
	 * <br/>
	 * <br/>
	 * */
	public static byte[] package2701AnswerCommand(byte[] ansCommand,int length,byte counter){
		return packageDPUCommand(ansCommand,length,COMMAND_WORD_6701, counter);
	}
	public static byte[] packageReservedAnswerCommand(byte[] ansCommand,int length,byte counter){
		return packageDPUCommand(ansCommand,length,null, counter);
	}
	public static byte[] packageNot2701AnswerCommand(byte[] ansCommand,int length,byte[] commandWord,byte counter) {
		return packageDPUCommand(ansCommand,length,commandWord, counter);
	}
	private static byte[] packageDPUCommand(byte[] command,int length,byte[] commandWord,byte counter) {
			int packageLength = length;
			//整条命令长 = <起始标志>+<包长度>+<计数器>+<数据区>+<包校验>
			byte[] answerCommand = null;
			if (commandWord == null) {
				answerCommand = new byte[ANS_START_CODE.length + 2 + 1 + packageLength + 1];
			} else {
				answerCommand = new byte[ANS_START_CODE.length + 2 + 1 + commandWord.length + packageLength + 1];
			}
			int pos = 0;
			System.arraycopy(ANS_START_CODE, 0, answerCommand, pos, ANS_START_CODE.length);// 起始标志
			pos += ANS_START_CODE.length;
			if (commandWord == null) {
				packageLength = packageLength + 1;
			} else {
				packageLength = packageLength + 1 + commandWord.length;
			}
			answerCommand[pos] = (byte) ((packageLength >> 8) & 0xff);// 包长度（高位）
			pos += 1;

			answerCommand[pos] = (byte) (packageLength & 0xff);// 包长度(低位)
			pos += 1;
			answerCommand[pos] = counter; // 计数器
			pos += 1;
			if (commandWord == null) {
				System.arraycopy(command, 0, answerCommand, pos, length);// 拷贝数据
			} else {
				System.arraycopy(commandWord, 0, answerCommand, pos, commandWord.length);// 拷贝数据
				pos += commandWord.length;
				System.arraycopy(command, 0, answerCommand, pos, length);// 拷贝数据
			}
			byte verify = answerCommand[ANS_START_CODE.length - 2];
			for (int j = ANS_START_CODE.length - 1; j < answerCommand.length - 1; j++) {// 校验数据(不包含校验字节本身)
				verify ^= answerCommand[j];
			}
			answerCommand[answerCommand.length - 1] = verify;// 校验值放在最后一字节中
			return answerCommand;
	}
	

	public static SimulatorDPUCommand generateSimulator27016020Command(String serialNo){
		SimulatorDPUCommand dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x27,0x01,0x60,0x20});
		dpuCommand.setAnsCommand(String.format("67010001000B4E%s010900060209", ByteHexHelper.parseAscii(serialNo)));
		return dpuCommand;
	}
	public static SimulatorDPUCommand generateSimulator2103Command(String serialNo){
		SimulatorDPUCommand dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x21,0x03});
		//dpuCommand.setAnsCommand(new byte[]{0x61,0x03,0x00,0x19,0x34,0x39,0x30,0x30,0x32,0x39,0x30,0x30,0x30,0x34,0x35,0x31,0x33,0x34,0x33,0x33,0x33,0x37,0x33,0x39,0x33,0x37,0x33,0x39,0x00,0x00,0x0D,0x39,0x38,0x35,0x36,0x39,0x31,0x30,0x30,0x30,0x37,0x30,0x30,0x00,0x00,0x0A,0x56,0x31,0x2E,0x30,0x30,0x2E,0x30,0x30,0x30,0x00,0x00,0x09,0x32,0x30,0x31,0x36,0x30,0x35,0x30,0x37,0x00,0x00,0x03,0x31,0x30,0x00});
		dpuCommand.setAnsCommand(String.format("6103001934393030323930303034353133343333333733393337333900000D%s00000A56312E30302E3030300000093230313630353037000003313000",
				ByteHexHelper.parseAscii(serialNo)));
		return dpuCommand;
	}
	public static SimulatorDPUCommand generateSimulator2105Command(){
		SimulatorDPUCommand dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x21,0x05});
		dpuCommand.setAnsCommand("6105000A56312E32302E3030370000075639392E39390000075639392E393900000100");
		return dpuCommand;
	}

	/**
	 * 2131命令支持
	 * @return
	 */
	public static SimulatorDPUCommand generateSimulator2131Command(){
		SimulatorDPUCommand dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x21,0x31});
		dpuCommand.setAnsCommand("613100");
		return dpuCommand;
	}

	/**
	 * 2114命令支持
	 * @return
	 */
	public static SimulatorDPUCommand generateSimulator2114Command(){
		SimulatorDPUCommand dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x21,0x14});
		dpuCommand.setAnsCommand("611400");
		return dpuCommand;
	}
	public static void initSimulatorDPUCommands(List<SimulatorDPUCommand> commandArrayList){
		SimulatorDPUCommand dpuCommand;
		dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x25,0x05});
		dpuCommand.setAnsCommand(new byte[]{0x65,0x05,0x00});
		commandArrayList.add(dpuCommand);
		
		dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x21,0x17});
		dpuCommand.setAnsCommand(new byte[]{0x61,0x17,0x00});
		commandArrayList.add(dpuCommand);

		dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x27,0x01,0x60,0x21});
		dpuCommand.setAnsCommand(new byte[]{0x67,0x01,0x00,0x07});
		commandArrayList.add(dpuCommand);
		
		dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x21,0x18,0x01,0x01});
		dpuCommand.setAnsCommand(new byte[]{0x61,0x18,0x01,0x01,0x00});
		commandArrayList.add(dpuCommand);
		
		dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x21,0x18,0x01,0x02,0x00,0x06,0x4C,0x61, 0x75, 0x6E, 0x63, 0x68,0x01,0x00,0x0B,0x4C, 0x61, 0x75, 0x6E, 0x63, 0x68, 0x40, 0x67, 0x6F, 0x6C, 0x6F});
		dpuCommand.setAnsCommand(new byte[]{0x61,0x18,0x01,0x02,0x00});
		commandArrayList.add(dpuCommand);
		
		dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x21,0x18,0x01,0x02,0x00,0x06,0x4C,0x61, 0x75, 0x6E, 0x63, 0x68,0x00,0x00,0x00});
		dpuCommand.setAnsCommand(new byte[]{0x61,0x18,0x01,0x02,0x00});
		commandArrayList.add(dpuCommand);
		
		dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x21,0x19,0x01});
		dpuCommand.setAnsCommand(new byte[]{0x61,0x19,0x01,0x00,0x01});
		//dpuCommand.setAnsCommand(new byte[]{0x61,0x19,0x01,0x00,0x02,0x00,0x06,0x4C,0x61, 0x75, 0x6E, 0x63, 0x68,0x01,0x00,0x0B,0x4C, 0x61, 0x75, 0x6E, 0x63, 0x68, 0x40, 0x67, 0x6F, 0x6C, 0x6F});
		commandArrayList.add(dpuCommand);
		dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x21,0x19,0x02});
		dpuCommand.setAnsCommand(new byte[]{0x61,0x19,0x02,0x00});
		commandArrayList.add(dpuCommand);
		
		dpuCommand = new SimulatorDPUCommand();
		dpuCommand.setReqCommand(new byte[]{0x21,0x14});
		dpuCommand.setAnsCommand(new byte[]{0x61,0x14,0x00});
		commandArrayList.add(dpuCommand);
	}
}
