package com.cnlaunch.physics;

import android.text.TextUtils;

import com.cnlaunch.physics.utils.MLog;
import com.power.baseproject.utils.log.LogUtil;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;

public class InsulationModuleControl {
    private static final String TAG = InsulationModuleControl.class.getSimpleName();
    private static final String COMMAND_SH = "sh";
    private static final String COMMAND_LINE_END = "\n";
    private static final String COMMAND_EXIT = "exit\n";

    public static final String BST_MT8167_OBDNODE_PATH = "/sys/devices/platform/voltage_detect/voltage";
    private static boolean isCheckBSTMT8167Info = false;
    private static boolean isBSTMT8167Platform = false;
    public static final String oscilloscope_CMD = "/sys/devices/platform/bsk_misc/bsk_oscilloscope_pwr_en";


    public static String readPowerState() {
        LogUtil.INSTANCE.d(TAG, "<读取电源状态............................开始>");
        String result = "0";
        Process process = null;
        DataOutputStream os;
        try {
            process = Runtime.getRuntime().exec(COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            String operationCommand = "cat /sys/devices/platform/bsk_misc/bsk_irtester_pwr_en" + "\n";
            os.writeBytes(operationCommand);
            os.writeBytes(COMMAND_LINE_END);
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            os.close();
            String s;
            try {
                BufferedReader successResult = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                while ((s = successResult.readLine()) != null) {
                    result = s;
                }
                successResult.close();
                BufferedReader errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
                while ((s = errorResult.readLine()) != null) {
                    LogUtil.INSTANCE.e(TAG, "指令错误信息 Result Error: " + s);
                }
                errorResult.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            process.waitFor();
            LogUtil.INSTANCE.d(TAG, "<读取电源状态............................>结果:" + result);
        } catch (Exception e) {
            e.printStackTrace();
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 1 表示上电
     * 0 表示关闭电源
     *
     * @param state
     */
    public static void powerOperation(int state) {
        LogUtil.INSTANCE.i(TAG, "<绝缘板电量控制指令............................开始>:" + state);
        Process process = null;
        DataOutputStream os;
        try {
            process = Runtime.getRuntime().exec(COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            String operationCommand = "echo" + " " + state + " > " + "/sys/devices/platform/bsk_misc/bsk_irtester_pwr_en" + "\n";
            os.writeBytes(operationCommand);
            os.writeBytes(COMMAND_LINE_END);
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            os.close();
            process.waitFor();
            LogUtil.INSTANCE.i(TAG, "<绝缘板电量控制指令............................结束>");
        } catch (Exception e) {
            e.printStackTrace();
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void powerOperationOther(int state, String cmd) {
        Process process = null;
        DataOutputStream os;
        try {
            process = Runtime.getRuntime().exec(COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            String operationCommand = "echo" + " " + state + " > " + cmd + "\n";
            os.writeBytes(operationCommand);
            os.writeBytes(COMMAND_LINE_END);
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            os.close();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断串口是否上电，通过读取电压值大小来判断
     *
     * @return
     */
    public static boolean isVoltageValid() {
        String line = "";
        Process process = null;
        DataOutputStream os = null;
        BufferedReader bufferedreader = null;
        try {
            process = Runtime.getRuntime().exec(COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            if (isBSTMT8167()) {
                os.writeBytes("/system/bin/cat /sys/devices/platform/voltage_detect/voltage" + "\n");
            } else {
                os.writeBytes("/system/bin/cat /sys/odb_voltage/odbvoltage" + "\n");
            }
            os.writeBytes(COMMAND_LINE_END);
            os.writeBytes(COMMAND_EXIT);
            os.flush();
            os.close();
            try {
                InputStreamReader inputstreamreader = new InputStreamReader(process.getInputStream());
                bufferedreader = new BufferedReader(inputstreamreader);
                StringBuilder sb = new StringBuilder(line);
                while ((line = bufferedreader.readLine()) != null) {
                    sb.append(line);
                }
                line = sb.toString();
                bufferedreader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            process.waitFor();
            if (MLog.isDebug) {
                MLog.d(TAG, "voltage =" + line);
            }
            if (TextUtils.isEmpty(line)) {
                return false;
            } else {
                try {
                    int voltage = Integer.parseInt(line);
                    if (voltage > 6500) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            return false;
        }
    }

    public static boolean isBSTMT8167() {
        if (!isCheckBSTMT8167Info) {
            isBSTMT8167Platform = new File(BST_MT8167_OBDNODE_PATH).exists();
            isCheckBSTMT8167Info = true;
        }
        return isBSTMT8167Platform;
    }
}
