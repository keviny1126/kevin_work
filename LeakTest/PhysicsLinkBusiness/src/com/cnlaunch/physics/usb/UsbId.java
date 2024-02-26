package com.cnlaunch.physics.usb;

class UsbId {
    public final static int DEFAULT_VID = 6790;// default VID
    public final static int DEFAULT_PID = 21972;// default PID
    public final static int INVALID_ID = -1;
    int mVid;
    int mPid;

    public UsbId() {
        this(INVALID_ID,INVALID_ID);
    }

    public UsbId(int vid, int pid) {
        this.mVid = vid;
        this.mPid = pid;
    }

    @Override
    public String toString() {
        return "UsbId{" +
                "mVid=" + mVid +
                ", mPid=" + mPid +
                '}';
    }
}
