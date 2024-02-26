package com.cnlaunch.physics.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.cnlaunch.physics.DPUDeviceType;
import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.ProductType;
import com.cnlaunch.physics.downloadbin.DownloadBinUpdate;
import com.cnlaunch.physics.downloadbin.util.Analysis;
import com.cnlaunch.physics.downloadbin.util.MyFactory;
import com.cnlaunch.physics.entity.AnalysisData;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.listener.OnDownloadBinListener;
import com.power.baseproject.utils.EasyPreferences;
import com.power.baseproject.utils.SystemPropertiesInvoke;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Tools {
    private static final String TAG = Tools.class.getSimpleName();
    //命令发送最大等待时间 单位ms
    private static final int MAX_COMMAND_WAIT_TIME = 5000;

    /**
     * 获取当前接头是否为一代重卡接头
     *
     * @return
     */
    @Deprecated
    public static boolean isTruck() {
        return isTruck(null, null);
    }

    /**
     * 获取当前接头是否为一代重卡接头
     *
     * @param context  上下文
     * @param serialNo 当前接头序列号
     * @return
     */
    public static boolean isTruck(Context context, String serialNo) {
        boolean result = isMatchConfigKeyPrefixWithSerialNo(context, serialNo, "heavyduty_serialNo_Prefix");
        MLog.d(TAG, "isTruck.result=" + result);
        return result;
    }

    /**
     * 判断当前接头序列号是否为柴汽一体接头
     *
     * @return
     */
    @Deprecated
    public static boolean isCarAndHeavyduty() {
        return isCarAndHeavyduty(null, null);
    }

    /**
     * 判断当前接头序列号是否为柴汽一体接头
     *
     * @param context  上下文
     * @param serialNo 当前接头序列号
     * @return
     */
    public static boolean isCarAndHeavyduty(Context context, String serialNo) {
        boolean result = isMatchConfigKeyPrefixWithSerialNo(context, serialNo, "car_and_heavyduty_prefix");
        MLog.d(TAG, "isCarAndHeavyduty.result=" + result);
        return result;
    }

    /**
     * 判断当前接头序列号是否为带wifi功能接头
     *
     * @return
     */
    @Deprecated
    public static boolean isWiFiSupportDPU() {
        return isWiFiSupportDPU(null, null);
    }

    /**
     * 判断当前接头序列号是否为带wifi功能接头
     *
     * @param context  上下文
     * @param serialNo 当前接头序列号
     * @return
     */
    public static boolean isWiFiSupportDPU(Context context, String serialNo) {
        boolean result = isMatchConfigKeyPrefixWithSerialNo(context, serialNo, "wifi_support_serialno_prefix");
        MLog.d(TAG, "isWiFiSupportDPU.result=" + result);
        return result;
    }

    /**
     * 增加当前接头是否匹配通用配置项前缀匹配规则
     *
     * @param serialNo    当前接头序列号
     * @param configValue 配置值
     * @return
     */
    public static boolean isMatchPrefixRule(String serialNo, String configValue) {
        if (serialNo == null) {
            return false;
        }
        boolean result = isMatchCommonPrefixRule(serialNo, configValue);
        MLog.d(TAG, "isMatchPrefixRule.result=" + result);
        return result;
    }

    /**
     * 向下位机发送指令
     *
     * @param sendOrder
     * @param iPhysics
     */
    public static void writeDPUCommand(byte[] sendOrder, IPhysics iPhysics) {
        writeDPUCommand(sendOrder, iPhysics, MAX_COMMAND_WAIT_TIME);
    }

    /**
     * 完整发送数据包
     *
     * @param sendOrder
     * @param iPhysics
     * @param maxWaitTime 为发送指令后的最大等待时间
     */
    public static void writeDPUCommand(byte[] sendOrder, IPhysics iPhysics, int maxWaitTime) {
        writeDPUCommand(sendOrder, iPhysics, maxWaitTime, false);
    }

    /**
     * 数据包间断发送,只用于测试
     *
     * @param sendOrder
     * @param iPhysics
     * @param maxWaitTime     为发送指令后的最大等待时间
     * @param isDiscontinuous 是否需要间断测试
     */
    public static void writeDPUCommand(byte[] sendOrder, IPhysics iPhysics, int maxWaitTime, boolean isDiscontinuous) {
        writeDPUCommand(sendOrder, iPhysics, maxWaitTime, isDiscontinuous, null);
    }

    /**
     * 数据包间断发送,只用于测试
     *
     * @param sendOrder
     * @param iPhysics
     * @param maxWaitTime        为发送指令后的最大等待时间
     * @param isDiscontinuous    是否需要间断测试
     * @param rateTestParameters 通讯收发数据测试参数
     */
    synchronized public static void writeDPUCommand(byte[] sendOrder, IPhysics iPhysics, int maxWaitTime, boolean isDiscontinuous, RateTestParameters rateTestParameters) {
        try {
            iPhysics.setCommand_wait(true);
            iPhysics.setCommand("");
            OutputStream outputStream = iPhysics.getOutputStream();
            if (!isDiscontinuous) {
                if (MLog.isDebug) {
                    MLog.d(TAG, "writeDPUCommand write start");
                }
                outputStream.write(sendOrder);
                if (rateTestParameters != null) {
                    rateTestParameters.sendDataSum += sendOrder.length;
                    rateTestParameters.sendDataTime = (new Date()).getTime() - rateTestParameters.sendDataStartTime;
                }
                if (MLog.isDebug) {
                    MLog.d(TAG, "writeDPUCommand write end");
                }
            } else {
                int length = sendOrder.length;
                int maxCount = (int) (Math.random() * 10);

                int index = 0;
                int endIndex = 0;
                int count = 1;
                int step = 5;
                while (true) {
                    //计数超过随机最大计数器
                    if (count >= maxCount) {
                        endIndex = length;
                    } else {
                        endIndex = index + count * step;
                        if (endIndex > length) {
                            endIndex = length;
                        }
                    }
                    byte[] divisionSender = Arrays.copyOfRange(sendOrder, index, endIndex);
                    outputStream.write(divisionSender);
                    if (rateTestParameters != null) {
                        rateTestParameters.sendDataSum += divisionSender.length;
                        rateTestParameters.sendDataTime = (new Date()).getTime() - rateTestParameters.sendDataStartTime;
                    }
                    if (MLog.isDebug) {
                        MLog.d(TAG, "writeDPUCommand Discontinuous divisionSender  = " + ByteHexHelper.bytesToHexString(divisionSender));
                    }
                    if (endIndex == length) {
                        break;
                    }
                    index = endIndex;
                    count++;
                    try {
                        if ((count % 2) == 0)
                            Thread.sleep(20);
                        else
                            Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            /**
             * 用于测试有线，可以在测试注销此处内容
             */
			/*if(DeviceFactoryManager.getInstance().getLinkMode() == DeviceFactoryManager.LINK_MODE_USB 
					&& DeviceFactoryManager.getInstance().getResetStatus()){
				((DPUUSBManager)iPhysics).usbDeviceReset();
			}*/
            long milliSeconds = (new Date()).getTime();
            while (waitCommand(iPhysics, milliSeconds, maxWaitTime) == false) {
                //Verify that the serial number is correct
                String backOrder = iPhysics.getCommand();
                if (TextUtils.isEmpty(backOrder) == false) {
                    byte[] request = ByteHexHelper.hexStringToBytes(backOrder);
                    if (MLog.isDebug) {
                        MLog.d(TAG, String.format("writeDPUCommand sendOrder[6]=%x  request[6]=%x ", sendOrder[6], request[6]));
                    }
                    if (sendOrder[6] == request[6]) {
                        break;
                    }
                }
                iPhysics.setCommand_wait(true);
                iPhysics.setCommand("");
            }
            if (rateTestParameters != null && iPhysics.getCommand().isEmpty() == false) {
                rateTestParameters.receiveDataSum += iPhysics.getCommand().length() / 2;
                rateTestParameters.receiveDataTime = (new Date()).getTime() - rateTestParameters.receiveDataStartTime;
            }
        } catch (Exception e) {
            e.printStackTrace();
            iPhysics.setCommand_wait(false);
            iPhysics.setCommand("");
            return;
        }
    }

    /**
     * 等待当前命令
     *
     * @param iPhysics
     * @param startMilliseconds
     * @param maxWaitTime
     * @return
     */
    private static boolean waitCommand(IPhysics iPhysics, long startMilliseconds, int maxWaitTime) {
        boolean isTimeOut = false;
        while (iPhysics.getCommand_wait()) {
            if (((new Date()).getTime() - startMilliseconds) > maxWaitTime) {
                iPhysics.setCommand_wait(false);
                iPhysics.setCommand("");
                isTimeOut = true;
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return isTimeOut;
    }

    /**
     * 读取当前状态Bootloader =0x00/download=0x01
     *
     * @return
     */
    public static String currentState2114(IPhysics iPhysics, OnDownloadBinListener onDownloadBinListener) {
        return DownloadBinUpdate.currentState2114(iPhysics, onDownloadBinListener);
    }

    /**
     * 切换到boot升级模式，启动更新固件命令,不可开放，只能私有
     */
	/*public static  boolean switchtoBootMode(IPhysics iPhysics,OnDownloadBinListener onDownloadBinListener) {
		return DownloadBinUpdate.SwitchtoBootMode(iPhysics,onDownloadBinListener);
	}*/

    /**
     * 安全切换到boot模式，不同于固件升级切换到boot模式2407命令
     *
     * @param iPhysics
     * @return
     */
    public static boolean saftSwitchToBootMode(IPhysics iPhysics) {
        boolean state = false;
        // 先确保当前模式位于boot模式
        String mode = Tools.currentState2114(iPhysics, null);
        if (!mode.equalsIgnoreCase("00")) {
            if (!Tools.resetDPUDevice2505(iPhysics)) {
                MLog.e(TAG, "复位失败");
                state = false;
            } else {
                state = true;
            }
            // 切换成功之后,等待设备稳定
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            state = true;
        }
        return state;
    }

    /**
     * 接头复位,不支持一代重卡,有线诊断模式，复位标志为失败
     *
     * @param iPhysics
     * @return
     */
    public static Boolean resetDPUDevice2505(IPhysics iPhysics) {
        return DownloadBinUpdate.resetDPUDevice2505(iPhysics);
    }

    /**
     * 通讯收发数据测试参数类
     */
    public static class RateTestParameters {
        public long receiveDataSum;
        public long receiveDataTime;
        public long receiveDataStartTime;
        public long sendDataSum;
        public long sendDataTime;
        public long sendDataStartTime;

        public RateTestParameters() {
            receiveDataSum = 0;
            receiveDataTime = 0;
            receiveDataStartTime = 0;
            sendDataSum = 0;
            sendDataTime = 0;
            sendDataStartTime = 0;
        }

    }

    /**
     * 是否匹配new_car_prefix配置前缀
     *
     * @param context
     * @param serialNo
     * @return
     */
    public static boolean isMatchNewCarPrefix(Context context, String serialNo) {
        boolean result = isMatchConfigKeyPrefixWithSerialNo(context, serialNo, "new_car_prefix");
        if (result == false) {
            //继续判断是否符合serialNo_car_Prefix_new配置
            result = isMatchConfigKeyPrefixWithSerialNo(context, serialNo, "serialNo_car_Prefix_new");
        }
        MLog.d(TAG, "isMatchNewCarPrefix.result=" + result);
        return result;
    }

    /**
     * 通用配置项前缀匹配规则 规则为 配置值=前缀1,前缀2,前缀3,...
     * 需检查匹配的字符前缀是否匹配分割的配置值中的一项
     *
     * @param myString    需检查匹配的字符
     * @param configValue 配置值
     * @return
     */
    public static boolean isMatchCommonPrefixRule(String myString, String configValue) {
        boolean result = false;
        //加入异常管控
        try {
            if (!TextUtils.isEmpty(configValue)) {
                if (configValue.contains(",")) {
                    String[] prefixes = configValue.split(",");
                    if (prefixes != null) {
                        for (String prefix : prefixes) {
                            if (myString.startsWith(prefix)) {
                                result = true;
                                break;
                            }
                        }
                    }
                } else {
                    if (myString.startsWith(configValue)) {
                        result = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        if (MLog.isDebug) {
            MLog.d(TAG, "isMatchCommonPrefixRule.result=" + result);
        }
        return result;
    }

    /**
     * 当前序列号是否匹配配置项（使用通用配置项前缀匹配规则）
     *
     * @param context
     * @param serialNo  当前序列号
     * @param configKey
     * @return
     */
    private static boolean isMatchConfigKeyPrefixWithSerialNo(Context context, String serialNo, String configKey) {
        Context myContext = null;
        if (context == null) {
            myContext = DeviceFactoryManager.getInstance().getContext();
        } else {
            myContext = context;
        }
        //加入异常管控
        try {
            if (myContext == null) {
                if (MLog.isDebug) {
                    MLog.d(TAG, "isMatchConfigKeyPrefix myContext=null");
                }
                return false;
            }
            String configValue = PhysicsCommonUtils.getProperty(myContext, configKey);
            if (MLog.isDebug) {
                MLog.d(TAG, String.format("isMatchConfigKeyPrefix configKey = %s configValue =%s", configKey, configValue));
            }
            String mySerialNo = null;
            if (serialNo == null) {
                mySerialNo = DeviceFactoryManager.getInstance().getDeviceName();
            } else {
                mySerialNo = serialNo;
            }
            if (TextUtils.isEmpty(mySerialNo)) {
                return false;
            } else {
                return isMatchCommonPrefixRule(mySerialNo, configValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断当前接头序列号是否为需要EasyDiag30加密接头
     * 97699 easydiag3
     * 97709 技师3.0
     *
     * @param context
     * @param serialNo
     * @return
     */
    public static boolean isMatchEasyDiag30AndMasterEncryptPrefix(Context context, String serialNo) {
        boolean result = isMatchConfigKeyPrefixWithSerialNo(context, serialNo, Constants.EASYDIAG30_AND_MASTER30_PREFIX);
        if (result == false) {
            result = isMatchCommonPrefixRule(serialNo, "97699,97709,98981");
        }
        MLog.d(TAG, "isMatchEasyDiag30AndMasterEncryptPrefix.result=" + result);
        return result;
    }

    /**
     * 判断当前接头序列号是否为需要EasyDiag40加密接头
     * 98942 easydiag40
     * 97986  技师40
     *
     * @param serialNo
     * @return
     */
    public static boolean isMatchEasyDiag40AndMasterEncryptPrefix(String serialNo) {
        boolean result = isMatchCommonPrefixRule(serialNo, Constants.ED4);
        if (MLog.isDebug) {
            MLog.d(TAG, "isMatchEasyDiag40AndMasterEncryptPrefix.result=" + result);
        }
        return result;
    }

    /**
     * 判断当前接头序列号是否为需要为MaxliteA M7芯片加密的接头
     * 97988
     *
     * @param serialNo
     * @return
     */
    public static boolean isNeedEncryptPrefixForMaxliteAM7(Context context, String serialNo) {
        int values = EasyPreferences.Companion.getInstance().get("needencryptprefixformaxliteA", -1);
        MLog.e(TAG, "isNeedEncryptPrefixForMaxliteAM7 values:" + values);
        if (values == -1) {
            String bootVersion = EasyPreferences.Companion.getInstance().get("bootVersion", "");
            MLog.e(TAG, "isNeedEncryptPrefixForMaxliteAM7 serialNo:" + serialNo + " bootVersion:" + bootVersion);
            if (!TextUtils.isEmpty(serialNo) && serialNo.startsWith("97988") && !TextUtils.isEmpty(bootVersion) && bootVersion.contains("1.22")) {
                EasyPreferences.Companion.getInstance().put("needencryptprefixformaxliteA", 1);
                MLog.e(TAG, "isNeedEncryptPrefixForMaxliteAM7 values -1 1:" + 1);
                return true;
            }
            EasyPreferences.Companion.getInstance().put("needencryptprefixformaxliteA", 0);
            MLog.e(TAG, "isNeedEncryptPrefixForMaxliteAM7 values -1 0:" + 0);
            return false;
        } else if (values == 1) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 判断当前接头序列号是否为需要为Maxlite 403芯片加密的接头;添加euro mini支持403芯片，直接在这个方法上面修改
     * 97988
     * 98749 euromini号段
     *
     * @param serialNo
     * @return
     */
    public static boolean isNeedEncryptPrefixForMaxlite403(Context context, String serialNo) {
        int values = EasyPreferences.Companion.getInstance().get("needencryptprefixformaxlite", -1);
        MLog.e(TAG, "isNeedEncryptPrefixForMaxlite403 values:" + values);
        if (values == -1) {
            String bootVersion = EasyPreferences.Companion.getInstance().get("bootVersion", "");
            MLog.e(TAG, "isNeedEncryptPrefixForMaxlite403 serialNo:" + serialNo + " bootVersion:" + bootVersion);
            if (!TextUtils.isEmpty(serialNo) && (serialNo.startsWith("97988") || serialNo.startsWith("98749")) && !TextUtils.isEmpty(bootVersion) && bootVersion.contains("1.23")) {
                EasyPreferences.Companion.getInstance().put("needencryptprefixformaxlite", 1);
                MLog.e(TAG, "isNeedEncryptPrefixForMaxlite403 values -1 1:" + 1);
                return true;
            }
            EasyPreferences.Companion.getInstance().put("needencryptprefixformaxlite", 0);
            MLog.e(TAG, "isNeedEncryptPrefixForMaxlite403 values -1 0:" + 0);
            return false;
        } else if (values == 1) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 判断当前接头序列号是否为需要Smartbox30接头
     *
     * @param context
     * @param serialNo
     * @return
     */
    public static boolean isMatchSmartbox30SupportSerialnoPrefix(Context context, String serialNo) {
        boolean result = isMatchConfigKeyPrefixWithSerialNo(context, serialNo, Constants.SMARTBOX30_SUPPORT_SERIALNO_PREFIX);
        MLog.d(TAG, "isMatchSmartbox30SupportSerialnoPrefix.result=" + result);
        return result;
    }

    /**
     * 判断是否是SmartHt接头
     *
     * @param context
     * @param serialNo
     * @return
     */
    public static boolean isSmartLinkCSerial(Context context, String serialNo) {
        boolean result = isMatchConfigKeyPrefixWithSerialNo(context, serialNo, Constants.SMARTLINKC_SUPPORT_SERIALNO_PREFIX);
        MLog.d(TAG, "isSmartLinkHTSerial=" + result);
        return result;
    }

    /**
     * EASYDIAG30新加密方案downloadbin文件生成，
     *
     * @param binVersion bin版本号
     * @param zipPath    压缩文件路径
     * @param unZipDir   解压文件路径 约定路径不包含Diagnostic\Configure\Download中的任何一个
     * @return 生成是否成功
     */
    public static boolean easyDiag30DownloadbinGenerate(String binVersion, String zipPath, String unZipDir) {
        boolean state = false;
        //Diagnostic\Configure\Download\DOWNLOAD.bin
        //Diagnostic\Configure\Download\DOWNLOAD.ini
        StringBuilder sb = new StringBuilder(unZipDir);
        if (unZipDir.endsWith(File.separator) == false) {
            sb.append(File.separator);
        }
        sb.append("Diagnostic");
        sb.append(File.separator);
        sb.append("Configure");
        sb.append(File.separator);
        sb.append("Download");
        File binFile = new File(String.format("%s%s%s", sb.toString(), File.separator, "DOWNLOAD.bin"));
        File iniFile = new File(String.format("%s%s%s", sb.toString(), File.separator, "DOWNLOAD.ini"));
        try {
            if (binFile.getParentFile().exists() == false) {
                binFile.getParentFile().mkdirs();
            }
            //更改压缩文件名为DOWNLOAD.bin
            File srcFile = new File(zipPath);
            boolean success = srcFile.renameTo(binFile);
            if (MLog.isDebug) {
                if (success) {
                    MLog.d(TAG, String.format("easyDiag30DownloadbinGenerate file rename success  source path：%1$s target path：%2$s", srcFile.getAbsolutePath(), binFile.getAbsolutePath()));
                } else {
                    MLog.d(TAG, String.format("easyDiag30DownloadbinGenerate file rename fail  source path：%1$s  target path：%2$s", srcFile.getAbsolutePath(), binFile.getAbsolutePath()));
                }
            }
            if (success) {
                FileOutputStream fileOutputStream = new FileOutputStream(iniFile);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, Charset.forName("US-ASCII"));
                /*[Info]
                Version=11.60*/
                outputStreamWriter.write("[Info]\n");
                outputStreamWriter.write(String.format("Version=%s", binVersion));
                outputStreamWriter.close();
                fileOutputStream.close();
                state = true;
            } else {
                state = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (binFile.exists()) {
                binFile.delete();
            }
            if (iniFile.exists()) {
                iniFile.delete();
            }
            state = false;
        }
        return state;
    }

    /**
     * 通用下位机指令交互操作
     *
     * @param iPhysics
     * @param sendOrder
     * @return
     */
    public static byte[] dpuCommonCommandOperation(IPhysics iPhysics, byte[] sendOrder) {
        return dpuCommonCommandOperation(iPhysics, sendOrder, MAX_COMMAND_WAIT_TIME);
    }

    /**
     * 通用下位机指令交互操作
     *
     * @param iPhysics
     * @param sendOrder
     * @param maxWaitTime
     * @return
     */
    public static byte[] dpuCommonCommandOperation(IPhysics iPhysics, byte[] sendOrder, int maxWaitTime) {
        return dpuCommandOperation(iPhysics, sendOrder, maxWaitTime, DPUDeviceType.STANDARD);
    }

    /**
     * Smartbox30下位机指令交互操作
     *
     * @param iPhysics
     * @param sendOrder
     * @return
     */
    public static byte[] dpuSmartbox30CommandOperation(IPhysics iPhysics, byte[] sendOrder) {
        return dpuSmartbox30CommandOperation(iPhysics, sendOrder, MAX_COMMAND_WAIT_TIME);
    }

    /**
     * Smartbox30下位机指令交互操作
     *
     * @param iPhysics
     * @param sendOrder
     * @param maxWaitTime
     * @return
     */
    public static byte[] dpuSmartbox30CommandOperation(IPhysics iPhysics, byte[] sendOrder, int maxWaitTime) {
        return dpuCommandOperation(iPhysics, sendOrder, maxWaitTime, DPUDeviceType.SMARTBOX30);
    }

    private static byte[] dpuCommandOperation(IPhysics iPhysics, byte[] sendOrder, int maxWaitTime, int dpuDeviceType) {
        String backOrder = "";
        byte[] receiveBuffer = null;
        if (MLog.isDebug) {
            MLog.d(TAG, "dpuCommandOperation .sendOrder = " + ByteHexHelper.bytesToHexString(sendOrder));
        }
        int flag = 0;
        if (sendOrder.length <= 0) {
            return receiveBuffer;
        }
        while (flag < 3) {
            Tools.writeDPUCommand(sendOrder, iPhysics, maxWaitTime);
            backOrder = iPhysics.getCommand();
            if (TextUtils.isEmpty(backOrder)) {
                flag++;
                continue;
            }
            if (MLog.isDebug) {
                MLog.d(TAG, "dpuCommandOperation.backOrder = " + backOrder);
            }
            byte[] receiveOrder = ByteHexHelper.hexStringToBytes(backOrder);
            Analysis analysis = MyFactory.creatorForAnalysis();
            AnalysisData analysisData = null;
            if (dpuDeviceType == DPUDeviceType.SMARTBOX30) {
                analysisData = analysis.analysisSmartbox30LinuxCommand(sendOrder, receiveOrder);
            } else {
                analysisData = analysis.analysis(sendOrder, receiveOrder);
            }
            if (analysisData.getState()) {
                receiveBuffer = analysisData.getpReceiveBuffer();
                if (MLog.isDebug) {
                    MLog.d(TAG, "dpuCommandOperation .data receiveBuffer = " + ByteHexHelper.bytesToHexString(receiveBuffer));
                }
                break;
            } else {
                flag++;
            }
        }
        return receiveBuffer;
    }

    /**
     * 判断当前平板设备是否支持双wifi(系统wifi+自定义wifi)
     *
     * @param context
     * @return
     */
    public static boolean isSupportDualWiFi(Context context) {
        boolean result = false;
        String is_support_dual_wifi = PhysicsCommonUtils.getProperty(context, Constants.IS_SUPPORT_DUAL_WIFI);
        if (!TextUtils.isEmpty(is_support_dual_wifi)) {
            result = Boolean.parseBoolean(is_support_dual_wifi);
        }
        MLog.d(TAG, "isSupportDualWiFi.result=" + result);
        return result;
    }

    /**
     * 判断当前平板设备是否支持wifi优先
     *
     * @param context
     * @return
     */
    public static boolean isSupportWiFiPriority(Context context) {
        boolean result = false;
        String is_support_wifi_priority = PhysicsCommonUtils.getProperty(context, Constants.IS_SUPPORT_WIFI_PRIORITY);
        if (!TextUtils.isEmpty(is_support_wifi_priority)) {
            result = Boolean.parseBoolean(is_support_wifi_priority);
        }
        MLog.d(TAG, "isSupportWiFiPriority.result=" + result);
        return result;
    }

    public static boolean copyDownloadBlklistFromAssert(Context context, String path) {
        boolean isSuccess = false;
        try {
            InputStream instream = context.getResources().getAssets().open("DOWNLOAD_BLKLIST.zip");
            ZipInputStream downloadZipInputStream = new ZipInputStream(instream);
            int index = 0, count = 0, bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            while (true) {
                ZipEntry downloadZipEntry = downloadZipInputStream.getNextEntry();
                if (downloadZipEntry == null) {
                    break;
                } else {
                    String zipEntryName = downloadZipEntry.getName();
                    if (MLog.isDebug) {
                        MLog.d(TAG, "copyDownloadBlklistFromAssert .zipEntry Name=" + zipEntryName);
                    }
                    if (zipEntryName != null && zipEntryName.contains("DOWNLOAD_BLKLIST.bin")) {
                        //先删除文件
                        File vaildDownload = new File(path);
                        if (vaildDownload.exists()) {
                            if (vaildDownload.delete()) {
                                if (MLog.isDebug) {
                                    MLog.d(TAG, "copyDownloadBlklistFromAssert .vaildDownload=" + path + " delete successful");
                                }
                            } else {
                                if (MLog.isDebug) {
                                    MLog.d(TAG, "copyDownloadBlklistFromAssert .vaildDownload=" + path + " delete fail");
                                }
                                break;
                            }
                        } else {
                            if (vaildDownload.getParentFile().exists() == false) {
                                vaildDownload.getParentFile().mkdirs();
                            }
                        }
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
                        while ((count = downloadZipInputStream.read(buffer, 0, bufferSize)) != -1) {
                            bos.write(buffer, 0, count);
                        }
                        bos.flush();
                        bos.close();
                        if (MLog.isDebug) {
                            MLog.d(TAG, "copyDownloadBlklistFromAssert zipEntry read successful");
                        }
                        isSuccess = true;
                        break;
                    }
                }
            }
            downloadZipInputStream.close();
            instream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }

    /**
     * 从Assert资源获取zip压缩文件解压到目的地
     *
     * @param context
     * @param unzipPath          解压目的地，需要带路径结束符号/结尾
     * @param assertResourcePath Assert资源路径
     * @return
     */
    public static boolean unzipResourceFromAssert(Context context, String unzipPath, String assertResourcePath) {
        boolean isSuccess = false;
        try {
            InputStream instream = context.getResources().getAssets().open(assertResourcePath);
            ZipInputStream assertZipInputStream = new ZipInputStream(instream);
            ZipEntry fentry;
            String zipFilename;
            int count, bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            while ((fentry = assertZipInputStream.getNextEntry()) != null) {
                zipFilename = unzipPath + fentry.getName();
                if (fentry.isDirectory()) {
                    Log.d(TAG, "unzipResourceFromAssert fentry.isDirectory() " + zipFilename);
                    File dir = new File(zipFilename);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                } else {
                    Log.d(TAG, "unzipResourceFromAssert fentry.is file " + zipFilename);
                    try {
                        File currentFile = new File(zipFilename);
                        if (!currentFile.getParentFile().exists()) {
                            currentFile.getParentFile().mkdirs();
                        }
                        FileOutputStream out = new FileOutputStream(zipFilename);
                        while ((count = assertZipInputStream.read(buffer)) != -1) {
                            out.write(buffer, 0, count);
                        }
                        out.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            assertZipInputStream.close();
            instream.close();
            isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }

    /**
     * 查询某个类是否存在
     *
     * @param className 完整的类名包含包名
     * @return
     */
    public static boolean checkClassExists(String className) {
        boolean isExists = false;
        try {
            if (MLog.isDebug) {
                MLog.d(TAG, " checkClassExists " + className);
            }
            Class.forName(className);
            isExists = true;
        } catch (ClassNotFoundException e1) {
            if (MLog.isDebug) {
                MLog.d(TAG, "checkClassMethodExists error: " + e1.toString());
            }
            isExists = false;
        } catch (Exception e) {
            if (MLog.isDebug) {
                MLog.d(TAG, "checkClassMethodExists error: " + e.toString());
            }
            isExists = false;
        }
        return isExists;
    }

    public static byte[] readECU211801(IPhysics iPhysics) {
        return DownloadBinUpdate.getSEInformation(iPhysics);
    }

    public static byte[] sendEncResult211802(IPhysics iPhysics, String encResult) {
        return DownloadBinUpdate.seSecurityAuthenticationRequest(iPhysics, ByteHexHelper.hexStringToBytes(encResult));
    }

    /**
     * 判断是否需要手动控制电源，一般用于DIY设备
     *
     * @return
     */
    public static boolean isNeedControlDiagnosePower(Context context) {
        String productType = EasyPreferences.Companion.getInstance().get(Constants.PRODUCTTYPE_KEY, "");
        if (productType.toUpperCase(Locale.ENGLISH).contains(ProductType.PROLITE) || productType.toUpperCase(Locale.ENGLISH).contains(ProductType.MAXLITE)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * * 从Assert资源获取zip压缩文件解压到目的地
     *
     * @param context
     * @param path               文件复制目的地
     * @param assertResourcePath Assert资源路径
     * @return
     */
    public static boolean copyResourceFromAssert(Context context, File path, String assertResourcePath) {
        boolean isSuccess = false;
        byte[] buffer = new byte[1024];
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            inStream = context.getResources().getAssets().open(assertResourcePath);
            outStream = new FileOutputStream(path);
            int count = 0;
            do {
                count = inStream.read(buffer);
                outStream.write(buffer, 0, count);
            } while (count == 1024);
            isSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isSuccess;
    }

    /**
     * xx项目博盛科设备文件值读取方法
     *
     * @param deviceName
     * @return
     */
    public static String xxProjectBSKReadValueByDeviceFile(String deviceName) {
        BufferedReader bufferedReader = null;
        String lineTxt = null;
        String ch = null;
        try {
            File file = new File(deviceName);
            if (file.isFile() && file.exists()) {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    ch = lineTxt;
                }
                //"0" 已上电,"1" 未上电 ,其他 "状态未知"
                if (MLog.isDebug) {
                    MLog.d(TAG, String.format("xxProjectBSKReadValueByDeviceFile deviceName=%s,value=%s", deviceName, ch));
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                    bufferedReader = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
        return ch;
    }

    /**
     * xx项目博盛科设备文件值写入方法
     *
     * @param deviceName
     * @param value
     * @return
     */
    public static void xxProjectBSKWriteValueByDeviceFile(String deviceName, String value) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(deviceName));
            out.write(value);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取xx项目博盛科OBD 电压
     *
     * @return
     */
    public static double getXXProjectBSKOBDVoltage() {
        String obdVoltageS = xxProjectBSKReadValueByDeviceFile("/sys/devices/platform/odm/odm:bsk_misc/batt_val");
        double obdVoltageD;
        try {
            obdVoltageD = Double.parseDouble(obdVoltageS);
        } catch (Exception e) {
            obdVoltageD = 0;
        }
        double obdVoltage = 1.45 / 4096 * obdVoltageD / 6.98 * 171.98;
        if (MLog.isDebug) {
            MLog.d(TAG, String.format(Locale.ENGLISH, "getXXProjectBSKOBDVoltage=%.3f", obdVoltage));
        }
        return obdVoltage;
    }

    /**
     * xx项目博盛科诊断板硬复位方法
     *
     * @return
     */
    public static void xxProjectBSKOBDHardwareReset() {
        String resetPath = "/sys/devices/platform/odm/odm:bsk_misc/mcu_reset_control";//诊断版复位
        xxProjectBSKWriteValueByDeviceFile(resetPath, "1");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        xxProjectBSKWriteValueByDeviceFile(resetPath, "0");
    }

    /**
     * xx项目博盛科获取Eth1广播地址方法
     *
     * @return
     */
    public static String getXXProjectBSKEth1BroadcastAddress() {
        return getBSKEth1BroadcastAddress();
    }

    private static String getBSKEth1IPV4Address() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().toLowerCase().equals("bsketh1")) {
                    if (MLog.isDebug) {
                        MLog.d(TAG, "InterfaceAddresses size" + intf.getInterfaceAddresses().size());
                    }
                    for (InterfaceAddress interfaceAddress : intf.getInterfaceAddresses()) {
                        InetAddress inetAddress = interfaceAddress.getAddress();
                        if (inetAddress != null && inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    private static String getBSKEth1BroadcastAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().toLowerCase().equals("bsketh1")) {
                    for (InterfaceAddress interfaceAddress : intf.getInterfaceAddresses()) {
                        if (interfaceAddress.getBroadcast() != null) {
                            return interfaceAddress.getBroadcast().toString().substring(1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    /**
     * X431 PRO3 V5.0/X431 PRO V5.0项目博盛科获取广播地址方法
     *
     * @return
     */
    public static String getX431PRO3V5ProjectBSKEth1BroadcastAddress() {
        return getBSKEth1BroadcastAddress();
    }

    /**
     * X431 PRO3 V5.0/X431 PRO V5.0项目博盛科获取外挂网卡ip地址方法
     *
     * @return
     */
    public static String getX431PRO3V5ProjectBSKEth1IPV4Address() {
        String ipAddress = getBSKEth1IPV4Address();
        if (MLog.isDebug) {
            MLog.d(TAG, "getX431PRO3V5ProjectBSKEth1IPAddress IP=" + ipAddress);
        }
        return ipAddress;
    }

    public static boolean isEncyptDownloadBin(Context context, String serialNo) {
        boolean result = isMatchConfigKeyPrefixWithSerialNo(context, serialNo, Constants.EASYDIAG30_AND_MASTER30_PREFIX);
        if (result == false) {
            result = isMatchCommonPrefixRule(serialNo, Constants.DOWNLOADBIN_ENCRY);
        }
        MLog.d(TAG, "isMatchEasyDiag30AndMasterEncryptPrefix.result=" + result);
        return result;
    }

    public static boolean isBSKA83SeriesProduct() {
        boolean isBSKA83Series = false;
        String deviceType = SystemPropertiesInvoke.getString("cnlaunch.product.type");
        MLog.d(TAG, String.format("isBSKA83SeriesProduct deviceType=%s,Build.VERSION.SDK_INT=%d", deviceType, Build.VERSION.SDK_INT));
        if (deviceType != null && deviceType.length() > 0
                && Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
            String cputype = SystemPropertiesInvoke.getString("ro.sys.cputype");
            MLog.d(TAG, "isBSKA83SeriesProduct cputype=" + cputype);
            if (cputype != null && cputype.length() > 0 && cputype.toLowerCase(Locale.ENGLISH).contains("ultraocta-a83")) {
                isBSKA83Series = true;
            }
        }
        MLog.d(TAG, "isBSKA83SeriesProduct " + isBSKA83Series);
        return isBSKA83Series;
    }

    public static boolean isNeedTraditionBluetooth() {
        boolean result = false;
        String model = Build.MODEL;
        MLog.e(TAG, "isNeedTraditionBluetooth model=" + model);
        if (model.equals("Lenovo TB2-X30M_PRC_YZ_A")) {
            result = true;
        } else if (model.equals("Lenovo TB2-X30F_YZA") || model.equals("Lenovo TB2-X30F_ROW_YZ_A")) {
            result = true;
        }
        return result;
    }

    /**
     * 判断当前接头序列号是否为euromini403加密接头
     * 9874966,9874967  403加密芯片
     * 9874960/9874961/9874962/9874963 老的207非加密芯片
     *
     * @param context
     * @param serialNo
     * @return
     */
    public static boolean isMatchEuroMini403EncryptPrefix(Context context, String serialNo) {
        boolean result = isMatchConfigKeyPrefixWithSerialNo(context, serialNo, Constants.EUROMINI403_PREFIX);
        MLog.d(TAG, "isMatchEuroMini403EncryptPrefix" + result);
        return result;
    }
}
