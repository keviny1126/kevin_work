package com.cnlaunch.physics.wifi;
import android.os.Parcel;
import android.os.Parcelable;

import com.cnlaunch.physics.wifi.settings.PskType;

public class DPUWiFiAPConfig implements Parcelable {
    //信道	 SSID广播	AP名称长度	 AP名称	  加密方式	 密码长度	密码
    //1个字节	 1个字节	1个字节	     String	  1个字节	 1个字节	String
    //信道
    private int channel;
    //SSID广播
    private boolean isSSIDBroadcastDisplay;
    //AP名称长度
    //AP名称
    private String SSID;
    //加密方式
    private PskType pskType;

    public DPUWiFiAPConfig() {
    }

    protected DPUWiFiAPConfig(Parcel in) {
        channel = in.readInt();
        isSSIDBroadcastDisplay = in.readByte() != 0;
        SSID = in.readString();
    }

    public static final Creator<DPUWiFiAPConfig> CREATOR = new Creator<DPUWiFiAPConfig>() {
        @Override
        public DPUWiFiAPConfig createFromParcel(Parcel in) {
            return new DPUWiFiAPConfig(in);
        }

        @Override
        public DPUWiFiAPConfig[] newArray(int size) {
            return new DPUWiFiAPConfig[size];
        }
    };

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public boolean isSSIDBroadcastDisplay() {
        return isSSIDBroadcastDisplay;
    }

    public void setSSIDBroadcastDisplay(boolean SSIDBroadcastDisplay) {
        isSSIDBroadcastDisplay = SSIDBroadcastDisplay;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public PskType getPskType() {
        return pskType;
    }

    public void setPskType(PskType pskType) {
        this.pskType = pskType;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(channel);
        dest.writeByte((byte) (isSSIDBroadcastDisplay ? 1 : 0));
        dest.writeString(SSID);
    }
}
