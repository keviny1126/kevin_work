package com.cnlaunch.physics.usb;

import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbRequest;

public interface IUsbOperate {
    int connect(Intent intent);

    int disconnect(Intent intent);

    //int getPermisson();

    int open();

    void close();

    int read(byte[] buffer, int length, int timeout);
    int readWithAsync(byte[] buffer, int length, int timeout);
    int write(byte[] buffer, int length, int timeout);

    /*
     * requestType : request type for this transaction
     * request 	request ID for this transaction
     * value 	value field for this transaction
     * index 	index field for this transaction
     * buffer 	buffer for data portion of transaction, or null if no data needs to be sent or received
     * length 	the length of the data to send or receive
     * timeout 	in milliseconds
     *
     * return: length of data transferred (or zero) for success, or negative value for failure
     * */
    int controlTransfer(int requestType, int request, int value, int index, byte[] buffer, int length, int timeout);

    int getStatus();

    void setStatus(int status);

    String getDeviceName();
}
