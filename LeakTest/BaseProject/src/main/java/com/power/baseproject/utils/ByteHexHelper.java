package com.power.baseproject.utils;

import com.power.baseproject.utils.log.LogUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 各种数据类型转换工具
 * 该工具可能会用于跨进程通讯，请不要加入显示程序相关内容
 * @author weizewei
 * 
 */
public class ByteHexHelper {
	public static final  String  TAG = "ByteHexHelper";
	private static String[]   hexStringTable=new String[256];
	private static byte[]     charByteTable = new byte[127];
	static{
		String hv;
		for (int i = 0; i < 256; i++) {
			hv = Integer.toHexString(i);
			if (hv.length() < 2) {
				hexStringTable[i]="0"+hv;
			}
			else{
				hexStringTable[i] = hv;
			}
			if(i<127){
				charByteTable[i] = charToByte(i);
			}
		}
		/*for(int i=0;i<127;i++){
			Log.d("ByteHexHelper",String.format("[%d,%02x]",i,charByteTable[i]));
		}*/
	}
	//字符串转十六进制
	public static String stringToHexString(String string){
		char[] c = string.toCharArray();
		String hesStr = "";
		for (int i = 0; i < c.length; i++) {
			hesStr = hesStr + Integer.toHexString(c[i]);
		}
		return hesStr;
	}
	/**
	 * 将byte数组转换成16进制字符串 Convert byte[] to hex
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 * 
	 * @param src
	 *            byte[] data
	 * @return hex string
	 */
	public static String bytesToHexString(byte[] src) {

		/*StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return "";
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();*/
		return bytesToHexStringWithSearchTable(src);
	}
	public static String bytesToHexStringWithSearchTable(byte[] src){
		if (src == null){
			return "";
		}else {
			return bytesToHexStringWithSearchTable(src, 0, src.length);
		}
	}
	public static String bytesToHexStringWithSearchTable(byte[] src,int offset,int length) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0  || offset+length>src.length) {
			return "";
		}
		for (int i = offset; i < offset+length; i++) {
			int v = src[i] & 0xFF;	
			stringBuilder.append(hexStringTable[v]);
		}
		return stringBuilder.toString();
	}

	/**
	 *  将16进制字符串转换为byte数组
	 * @param hexString the hex string
	 * @param reserveMaxBuffer 预留最大的buffer
	 * @return 实际长度
	 */
	public static int  hexStringToBytes(String hexString,byte[] reserveMaxBuffer) {
		int validData=0;
		if (hexString == null || hexString.equals("")) {
			validData=0;
		}
		hexString = hexString.toUpperCase(Locale.ENGLISH);
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			reserveMaxBuffer[i] = (byte) (charByteTable[hexChars[pos]] << 4 | charByteTable[hexChars[pos + 1]]);
		}
		validData=length;
		return validData;
	}
	/**
	 * 将16进制字符串转换为byte数组
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			byte[] bytes = new byte[0];
			return bytes;
		}
		hexString = hexString.toUpperCase(Locale.ENGLISH);
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charByteTable[hexChars[pos]] << 4 | charByteTable[hexChars[pos + 1]]);
		}
		return d;
	}	
	/**
	 * 将byte转换成16进制字符串
	 * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
	 * 
	 * @param src
	 *            byte data
	 * @return hex string
	 */
	public static String byteToHexString(byte src) {
		StringBuilder stringBuilder = new StringBuilder("");
		int v = src & 0xFF;
		String hv = Integer.toHexString(v);
		if (hv.length() < 2) {
			stringBuilder.append(0);
		}
		stringBuilder.append(hv);
		return stringBuilder.toString();
	}

	/**
	 * 将byte转换成10进制数
	 * 
	 * @param src
	 *            byte data
	 * @return int
	 */
	public static int byteToInt(byte src) {
		return src & 0xFF;
	}

	/**
	 * 十进制转为1个字节的byte[]
	 * 
	 * @param id
	 * @return
	 */
	public static byte[] intToHexBytes(int id) {
		String hexString = Integer.toHexString(id);
		int len = hexString.length();
		while (len < 2) {
			hexString = "0" + hexString;
			len = hexString.length();
		}
		return ByteHexHelper.hexStringToBytes(hexString);
	}

	/**
	 * 十进制转为2个字节的byte[]
	 * 
	 * @param id
	 * @return
	 */
	public static byte[] intToTwoHexBytes(int id) {
		String hexString = Integer.toHexString(id);
		int len = hexString.length();
		while (len < 4) {
			hexString = "0" + hexString;
			len = hexString.length();
		}
		return ByteHexHelper.hexStringToBytes(hexString);
	}

	/**
	 * 十进制转为4个字节的byte[]
	 * 
	 * @param id
	 * @return
	 */
	public static byte[] intToFourHexBytes(int id) {
		String hexString = Integer.toHexString(id);
		int len = hexString.length();
		while (len < 8) {
			hexString = "0" + hexString;
			len = hexString.length();
		}
		return ByteHexHelper.hexStringToBytes(hexString);
	}

	/**
	 * 十进制转为4个字节的byte[] 在后面补零
	 * 
	 * @param id
	 * @return
	 */
	public static byte[] intToFourHexBytesTwo(int id) {
		String hexString = Integer.toHexString(id);
		int len = hexString.length();
		if (len < 2) {
			hexString = "0" + hexString;
			len = hexString.length();
		}
		while (len < 8) {
			hexString = hexString + "0";
			len = hexString.length();
		}
		return ByteHexHelper.hexStringToBytes(hexString);
	}

	/**
	 * 十进制转为1个字节的byte
	 * 
	 * @param id
	 * @return
	 */
	public static byte intToHexByte(int id) {
		String hexString = Integer.toHexString(id);
		int len = hexString.length();
		while (len < 2) {
			hexString = "0" + hexString;
			len = hexString.length();
		}
		return ByteHexHelper.hexStringToByte(hexString);
	}

	public static String intToHexString(int id) {
		String hexString = Integer.toHexString(id);
		int len = hexString.length();
		while (len < 2) {
			hexString = "0" + hexString;
			len = hexString.length();
		}
		return hexString;
	}

	/**
	 * 将16进制字符串转换为byte
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte hexStringToByte(String hexString) {
		hexString = hexString.toUpperCase(Locale.ENGLISH);
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d[0];
	}
	/**
	 * 转换char类型到byte类型
	 * 
	 * @param c
	 * @return byte
	 */
	private static byte charToByte(int c) {
		int charIndex = "0123456789ABCDEF".indexOf(c);
		//支持大小写字符需要
		if(charIndex==-1){
			charIndex = "0123456789abcdef".indexOf(c);
		}
		return (byte)charIndex;
	}

	/**
	 * 将16进制字符串中每两位之间进行异或校验
	 * 
	 * @param hex
	 * @return
	 */
	public static String XOR(String hex) {
		byte bytes = (byte) (0x00);
		if (hex.length() > 0) {
			for (int i = 0; i < hex.length() / 2; i++) {
				bytes = (byte) (bytes ^ (ByteHexHelper.hexStringToByte(hex.substring(2 * i, 2 * i + 2))));
			}
		}
		byte[] bbb = { bytes };
		return ByteHexHelper.bytesToHexStringWithSearchTable(bbb);
	}

	/**
	 * 求校验和的算法 (ADD8)
	 *
	 * @param b 需要求校验和的字节数组
	 * @return 校验和
	 */
	public static String sumCheck(byte[] b, int len){
		int sum = 0;
		for(int i = 0; i < len; i++){
			sum = sum + b[i];
		}
//		LogUtil.INSTANCE.e("ykw","------文件校验值----sum："+sum+",sumCheck:"+Integer.toHexString(sum));
		return byteToHexString((byte) (sum & 0xff)) ;
	}

	public static String singleFileADD8sum(File file) throws Exception {
		FileInputStream fis = new FileInputStream(file);
		int readLen = 0;
		int sum = 0;
		while ((readLen = fis.read()) != -1) {
			sum = sum + readLen;
		}
		fis.close();// 务必关闭
		String hexString = Integer.toHexString(sum);
		int len = hexString.length();
		LogUtil.INSTANCE.e("ykw","------文件校验值----初始："+hexString+",sum:"+sum);
		while (len < 8) {
			hexString = "0" + hexString;
			len = hexString.length();
		}
		LogUtil.INSTANCE.e("ykw","------文件校验值----结果："+hexString);
		return hexString;
	}


	/**
	 * 获取到当前的年 月 日 时 分 秒 星期
	 * 
	 * @return
	 */
	public static String currentData() {
		StringBuffer stringBuffer = new StringBuffer();
		DecimalFormat decimalFormat = new DecimalFormat("00");
		Calendar calendar = Calendar.getInstance();
		String year = decimalFormat.format(calendar.get(Calendar.YEAR));
		String month = decimalFormat.format(calendar.get(Calendar.MONTH) + 1);
		String day = decimalFormat.format(calendar.get(Calendar.DAY_OF_MONTH));
		String hour = decimalFormat.format(calendar.get(Calendar.HOUR_OF_DAY));
		String minute = decimalFormat.format(calendar.get(Calendar.MINUTE));
		String second = decimalFormat.format(calendar.get(Calendar.SECOND));
		String week = decimalFormat.format(calendar.get(Calendar.DAY_OF_WEEK) - 1);
		stringBuffer.append(year.substring(2, year.length())).append(month).append(day).append(hour).append(minute).append(second).append(week);
		System.out.println(stringBuffer.toString());
		return stringBuffer.toString();
	}

	/**
	 * 生成1到100随机数,长度为2位
	 * 2016/06/30 xfh修改,因为诊断软件第一条复位指令计数器为00,为避免指令长度计数器与复位指令一致，所以指令计数器不能从0开始
	 *
	 * @return
	 */
	public static String RandomMethod() {
		int random = (int) (Math.random() * 100);
		if (random == 0){
			random =100;
		}
		String hexString = Integer.toHexString(random);
		int len = hexString.length();
		while (len < 2) {
			hexString = "0" + hexString;
			len = hexString.length();
		}
		return hexString;
	}

	/**
	 * 根据字符串得到十六进制包长度
	 * 
	 * @param str
	 * @return
	 */
	public static String packLength(String str) {
		String hexLength = Integer.toHexString(str.length() / 2);// 十进制转换为十六进制字符串
		int len = hexLength.length();
		while (len < 4) {
			hexLength = "0" + hexLength;
			len = hexLength.length();
		}
		return hexLength;
	}

	/**
	 * 根据长度值十六进制包长度
	 * 
	 * @param str
	 * @return
	 */
	public static String checkedSite(int site) {
		String hexLength = Integer.toHexString(site);// 十进制转换为十六进制字符串
		int len = hexLength.length();
		while (len < 2) {
			hexLength = "0" + hexLength;
			len = hexLength.length();
		}
		return hexLength;
	}

	/**
	 * 根据长度值十六进制包长度
	 * 
	 * @param str
	 * @return
	 */
	public static String packLength(int dataLen) {
		String hexLength = Integer.toHexString(dataLen);// 十进制转换为十六进制字符串
		int len = hexLength.length();
		while (len < 4) {
			hexLength = "0" + hexLength;
			len = hexLength.length();
		}
		return hexLength;
	}

	public static String packLength(long dataLen) {
		String hexLength = Long.toHexString(dataLen);// 十进制转换为十六进制字符串
		int len = hexLength.length();
		while (len < 4) {
			hexLength = "0" + hexLength;
			len = hexLength.length();
		}
		return hexLength;
	}

	/**
	 * 十进制包长度 十六进制字符串转换为十进制
	 * 
	 * @param str
	 * @return
	 */
	public static int intPackLength(String str) {
		int intLength = Integer.valueOf(str, 16);// 十六进制字符串转换为十进制
		return intLength;
	}

	/**
	 * 十进制包长度
	 * 将byte[]通过bytesToHexString方法转换成16进制string，然后再通过Integer.valueOf(str,16
	 * )将十六进制字符串转换为十进制
	 * 
	 * @param str
	 * @return intLength int
	 */
	public static int intPackLength(byte[] str) {
		String byteStr = bytesToHexString(str);
		int intLength = Integer.valueOf(byteStr, 16);// 十六进制字符串转换为十进制
		return intLength;
	}

	/**
	 * 包校验
	 * 
	 * @param target
	 * @param source
	 * @param packLengths
	 * @param counter
	 * @param commandWord
	 * @param dataArea
	 * @return
	 */
	public static String packVerify(String target, String source, String packLengths, String counter, String commandWord, String dataArea) {
		String verify = ByteHexHelper.XOR(target + source + packLengths + counter + commandWord + dataArea);
		return verify;
	}

	/**
	 * 把String转换为DPUstring
	 * 
	 * @param str
	 * @return
	 */
	public static String dpuString(String str) {
		String buffer = "";
		if (str != null && str.length() > 0) {
			byte[] src = (str + "\0").getBytes();
			String result = ByteHexHelper.bytesToHexString(src);
			String resultLength = ByteHexHelper.packLength(result);
			buffer = resultLength + result;
			System.out.println("resultLength==" + buffer);
		}
		return buffer;
	}

	/**
	 * 合并两个byte数组
	 * 
	 * @param pByteA
	 * @param pByteB
	 * @return
	 */
	public static byte[] getMergeBytes(byte[] pByteA, byte[] pByteB) {
		int aCount = pByteA.length;
		int bCount = pByteB.length;
		byte[] b = new byte[aCount + bCount];
		for (int i = 0; i < aCount; i++) {
			b[i] = pByteA[i];
		}
		for (int i = 0; i < bCount; i++) {
			b[aCount + i] = pByteB[i];
		}
		return b;
	}

	/**
	 * 截取byte数据
	 * 
	 * @param b
	 *            是byte数组
	 * @param j
	 *            是大小
	 * @return
	 */
	public static byte[] cutOutByte(byte[] b, int j) {
		if (b.length == 0 || j == 0) {
			return null;
		}
		byte[] bjq = new byte[j];
		for (int i = 0; i < j; i++) {
			bjq[i] = b[i];
		}
		return bjq;
	}
	/**
	 * byte[]转成文字
	 * 
	 * @param hexString
	 * @return
	 */
	public static String byteToWord(byte[] data) {
		String word = "";
		if (data != null) {
			try {
				Locale locale = Locale.getDefault();
				//switch (AndroidToLan.languages(locale.getCountry())) {
				switch (1) {
				case 1:// 1简体中文
					word = new String(data, "GB2312");
					break;
				case 0:// 0英文
					word = new String(data, "GB2312");
					break;
				case 4:// 4香港或台港（繁体中文）
					word = new String(data, "BIG5");
					break;
				case 2:// 2日语
					word = new String(data, "EUC-JP");
					break;
				case 20:// 20韩语
					word = new String(data, "euc-kr");
					break;
				case 3:// 3德语
					word = new String(data, "ISO-8859-1");
					break;
				case 5:// 5法语
					word = new String(data, "ISO-8859-1");
					break;
				case 6:// 6葡萄牙
					word = new String(data, "ISO-8859-1");
					break;
				case 8:// 8意大利
					word = new String(data, "ISO-8859-1");
					break;
				case 9:// 9西班牙
					word = new String(data, "ISO-8859-1");
					break;
				case 12:// 12荷兰
					word = new String(data, "ISO-8859-1");
					break;
				case 18:// 18丹麦语
					word = new String(data, "ISO-8859-1");
					break;
				case 21:// 21芬兰语
					word = new String(data, "ISO-8859-1");
					break;
				case 22:// 22瑞典语
					word = new String(data, "ISO-8859-1");
					break;
				case 10:// 10波兰
					word = new String(data, "ISO-8859-2");
					break;
				case 14:// 14匈牙利语
					word = new String(data, "ISO-8859-2");
					break;
				case 23:// 23捷克语
					word = new String(data, "ISO-8859-2");
					break;
				case 7:// 7俄罗斯
					word = new String(data, "ISO-8859-5");
					break;
				case 11:// 11土耳其
					word = new String(data, "ISO-8859-9");
					break;
				case 13:// 13希腊
					word = new String(data, "ISO-8859-7");
					break;
				case 15:// 15阿拉伯语
					word = new String(data, "ISO-8859-6");
					break;
				case 19:// 19波斯语
					word = new String(data, "windows-1256");
					break;
				case 16:// 16塞尔维亚语
					word = new String(data, "ISO-8859-5");
					break;
				case 17:// 17罗马尼亚语
					word = new String(data, "ISO-8859-2");
					break;
				default:
					word = new String(data, "GB2312");
					break;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return word;
	}

	/**
	 * 十六进制字符串转成文字
	 * 
	 * @param hexString
	 * @return
	 */
	public static String hexStringToWord(String hexString) {
		return byteToWord(hexStringToBytes(hexString));
	}

	/**
	 * 二进制字符串转16进制字符串
	 * 
	 * @param bString
	 * @return
	 */
	public static String binaryString2hexString(String bString) {
		if (bString == null || bString.equals("")) {
			return "";
		}
		if (bString.length() % 8 != 0) {
			int addLen = 8 - bString.length() % 8;
			for (int i = 0; i < addLen; i++) {
				bString = bString + "0";
			}
			System.out.println("choiceItem = " + bString);
		}
		StringBuffer tmp = new StringBuffer();
		int iTmp = 0;
		for (int i = 0; i < bString.length(); i += 4) {
			iTmp = 0;
			for (int j = 0; j < 4; j++) {
				iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
			}
			tmp.append(Integer.toHexString(iTmp));
		}
		System.out.println("tmp.toString() = " + tmp.toString());
		return tmp.toString();
	}

	/**
	 * 16进制字符串转二进制字符串
	 * 
	 * @param bString
	 * @return
	 */
	public static String hexString2binaryString(String hexString) {
		if (hexString == null || hexString.length() % 2 != 0)
			return null;
		String bString = "", tmp;
		for (int i = 0; i < hexString.length(); i++) {
			tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
			bString += tmp.substring(tmp.length() - 4);
		}
		return bString;
	}

	/**
	 * android 去除字符串中的前后空格、回车、换行符、制表符
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest.trim();
	}

	// add by weizewei
	public static String replaceBlank1(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}

		if (dest.length() > 6) {
			String destfenge = dest.substring(0, 2);
			String destfenge1 = dest.substring(2, 4);
			String destfenge2 = dest.substring(4, 6);
			String destfenge3 = dest.substring(6, dest.length());
			dest = destfenge + "," + destfenge1 + "," + destfenge2 + "," + destfenge3;
		}
		return dest.trim();
	}

	// add end

	/**
	 * @author 将DPU_String数组类 转换为 String数组类
	 * */
	public static ArrayList<String> toStringArray(byte[] data) {
		if (data != null) {
			int total_bytes = data.length;
			if (total_bytes >= 3) {// 最少一个DPU_String
				int walkthrough = 0;
				ArrayList<String> result_strings = new ArrayList<String>();
				while (walkthrough < (total_bytes - 1)) {
					int temp_len = data[walkthrough] << 8 | data[walkthrough + 1];
					byte[] str_bytes = new byte[temp_len - 1];
					System.arraycopy(data, walkthrough + 2, str_bytes, 0, temp_len - 1);
					result_strings.add(new String(str_bytes));
					walkthrough += temp_len + 2;// 忽略2个头字节
				}
				return result_strings;
			}
		}
		return null;
	}

	/**
	 * @author
	 * @see 字节数组操作 System.arraycopy() 将data数组的数据附加到src数组之后
	 * */
	public static byte[] appendByteArray(byte[] src, byte[] data) {
		if (src.length > 0 && data.length > 0) {
			byte[] ret = new byte[src.length + data.length];
			System.arraycopy(src, 0, ret, 0, src.length);// copy source
			System.arraycopy(data, 0, ret, src.length, data.length);// copy data
			return ret;
		} else
			throw new IllegalArgumentException("字节数组参数错误");
	}

	// 单个文件的校验方法
	public static String calculateSingleFileMD5sum(File file) throws Exception {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		FileInputStream fis = new FileInputStream(file);
		int readLen = 0;
		byte[] buff = new byte[256];
		while ((readLen = fis.read(buff)) != -1) {
			md5.update(buff, 0, readLen);
		}
		fis.close();// 务必关闭
		StringBuilder sb = new StringBuilder();
		byte[] data = md5.digest();
		for (byte b : data) {
			sb.append(new Formatter().format("%02x", b));
		}
		return sb.toString();
	}

	// add by weizewei
	private static String toHexUtil(int n) {
		String rt = "";
		switch (n) {
		case 10:
			rt += "A";
			break;
		case 11:
			rt += "B";
			break;
		case 12:
			rt += "C";
			break;
		case 13:
			rt += "D";
			break;
		case 14:
			rt += "E";
			break;
		case 15:
			rt += "F";
			break;
		default:
			rt += n;
		}
		return rt;
	}

	public static String toHex(int n) {
		StringBuilder sb = new StringBuilder();
		if (n / 16 == 0) {
			return toHexUtil(n);
		} else {
			String t = toHex(n / 16);
			int nn = n % 16;
			sb.append(t).append(toHexUtil(nn));
		}
		return sb.toString();
	}

	public static String parseAscii(String str) {
		StringBuilder sb = new StringBuilder();
		byte[] bs = str.getBytes();
		for (int i = 0; i < bs.length; i++)
			sb.append(toHex(bs[i]));
		return sb.toString();
	}
	
	public static String packVerifyforjili(String packLengths, String dataArea) {
  		String verify = ByteHexHelper.XOR(packLengths+dataArea);
  		return verify;
  	}
	// add end
	/**
	 * 在src中查找dst，并返回dst在src中的起始位置<br/>
	 * <br/>
	 * 
	 * @param src 查找数据源
	 * @param dst 被查找数据
	 * @return src中存在dst时返回dst在src的开始起始(>=0)，否则返回-1
	 * @author lianghua
	 * @date 2015.10.21
	 */
	public static int bytesIndexOf(byte[] src, byte[] dst) {
		return bytesIndexOf(src, dst, 0,src.length);
	}

	/**
	 * 在src中查找dst，并返回dst在src中的起始位置<br/>
	 * <br/>
	 * 
	 * @param src 查找数据源
	 * @param dst 被查找数据
	 * @param startInSrc 在源数据中查找的起始位置
	 * @return src中存在dst时返回dst在src的开始起始(>=0)，否则返回-1
	 * @author lianghua
	 * @date 2015.10.21
	 */
	public static int bytesIndexOf(byte[] src, byte[] dst,int startInSrc, int srcLength) {
		if (null == src || startInSrc+srcLength > src.length || null == dst ) {
			return -1;
		}
		int codeLen = dst.length;
		for (int i=startInSrc; i < startInSrc+srcLength; i++) {
			int pos = i;
			// 源数据中剩余数量必须>=被查找数据个数，否则源中不存在被查找的数据
			if ((pos + codeLen) > (startInSrc+srcLength)) {
				return -1;
			}			
			for (int j=0; j<codeLen; j++) {
				if (src[pos] == dst[j]) {
					// 完成查找
					if (j == (codeLen - 1)) {
						return i;
					}
					else {
						pos++;
						continue;
					}
				}
				// 当前比较失败，跳出
				else {
					break;
				}
			}
		}		
		return -1;
	}

	public static int bytesHeadIndexOf(byte[] src, byte[] dst,int startInSrc, int srcLength) {
		if (null == src || startInSrc+srcLength > src.length || null == dst ) {
			return -1;
		}
		int codeLen = dst.length;
		int temp = -1;
		for (int i=startInSrc; i < startInSrc+srcLength; i++) {
			int pos = i;
			// 源数据中剩余数量必须>=被查找数据个数，否则源中不存在被查找的数据
			if ((pos + codeLen) > (startInSrc+srcLength)) {
				return temp;
			}
			for (int j=0; j<codeLen; j++) {
				if (src[pos] == dst[j]) {
					// 完成查找
					if (j == (codeLen - 1)) {
						temp = i;
//						LogUtil.INSTANCE.i("ykw","--------temp1111:"+temp);
//						return i;
					}
					else {
						pos++;
					}
				}
				// 当前比较失败，跳出
				else {
					break;
				}
			}
		}
		LogUtil.INSTANCE.i("ykw","--------temp2222:"+temp);
		return temp;
	}
	/**
	 * 16进制转带空格的字符串
	 * @param src
	 * @return
	 */
	public static String bytesToHexStrWithSwap(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
			stringBuilder.append(" ");
		}
		return stringBuilder.toString();
	}
	/**
	 * DSTRING 长度转化为两个字节，高字节在前，低字节在后
	 * length = byte[0]*256+byte[1]
	 * @param length 长度不超过
	 * @return
	 */
	/**
	 * 
	 * @param length
	 * @return
	 */
	public static byte[] convertLengthToTwoBytes(int length){
		byte[] lengthsBytes = new byte[2]; 
		if(length<65535){
			lengthsBytes[1] = (byte)(length&0xFF); 
			lengthsBytes[0] = (byte)(length>>8); 
		}
		else{
			lengthsBytes[1] = 0; 
			lengthsBytes[0] = 0; 
		}
		return lengthsBytes;
	}

	public static String convertHexStringNumBits(String hex, int num) {
		int len = hex.length();
		while (len < num) {
			hex = "0" + hex;
			len = hex.length();
		}
		return hex;
	}
}
