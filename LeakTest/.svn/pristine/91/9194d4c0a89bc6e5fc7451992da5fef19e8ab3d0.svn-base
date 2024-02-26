package com.cnlaunch.physics;

import android.text.TextUtils;

import com.cnlaunch.physics.utils.MLog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 第二wifi控制相关
 */
public class SecondWifiControl {
    public static  void powerOn() {
        powerOperation("1");
    }
    public static  void powerOff() {
        powerOperation("0");
    }
    /**
     * 1 表示上电
     * 0 表示关闭电源
     * @param state
     */
    private static  void powerOperation(String state) {
        String phoneModel = android.os.Build.MODEL;
        if(TextUtils.isEmpty(phoneModel) || !(phoneModel.contains("PAD VII"))){
            return;
        }
        if(MLog.isDebug) {
            MLog.d("SecondWifiControl", "powerOperation start state="+state);
        }
        String commandStr =  "1";//on:1 off:0
        File file_wifi = new File("/sys/usb_switch/usbwifi");
        try {
            FileWriter fr_wifi = new FileWriter(file_wifi);
            fr_wifi.write(commandStr);
            fr_wifi.close();
            MLog.d("SecondWifiControl", "powerOperation end state="+state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
