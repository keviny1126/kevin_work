package com.cnlaunch.physics;

/**
 * 设备建立连接参数类
 */
public class LinkParameters {
    public static class SerialPortParameters{
        /**
         * 设备名称
         */
        private String   deviceName;
        /**
         * 串口波特率
         */
        private  int     baudRate;
        private  boolean isNeedHardwareFlowControl;
        public SerialPortParameters(){
            this("",0,false);
        }
        public SerialPortParameters(String deviceName, int baudRate) {
            this(deviceName,baudRate,false);
        }
        public SerialPortParameters(String deviceName, int baudRate,boolean isNeedHardwareFlowControl) {
            this.deviceName = deviceName;
            this.baudRate = baudRate;
            this.isNeedHardwareFlowControl=isNeedHardwareFlowControl;
        }
        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public int getBaudRate() {
            return baudRate;
        }

        public void setBaudRate(int baudRate) {
            this.baudRate = baudRate;
        }

        public boolean getIsNeedHardwareFlowControl() {
            return isNeedHardwareFlowControl;
        }

        public void setIsNeedHardwareFlowControl(boolean needHardwareFlowControl) {
            this.isNeedHardwareFlowControl = needHardwareFlowControl;
        }

        @Override
        public String toString() {
            return "SerialPortParameters{" +
                    "deviceName='" + deviceName + '\'' +
                    ", baudRate=" + baudRate +
                    ", isNeedHardwareFlowControl=" + isNeedHardwareFlowControl +
                    '}';
        }
    }
    SerialPortParameters serialPortParameters;

    public SerialPortParameters getSerialPortParameters() {
        return serialPortParameters;
    }

    public void setSerialPortParameters(SerialPortParameters serialPortParameters) {
        this.serialPortParameters = serialPortParameters;
    }
}
