package com.cnlaunch.physics.smartlink;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cnlaunch.physics.DeviceFactoryManager;
import com.cnlaunch.physics.impl.IPhysics;
import com.cnlaunch.physics.utils.MLog;
import com.cnlaunch.physics.utils.remote.ReadByteDataStream;

import java.io.InputStream;
import java.io.OutputStream;

public class DPUSocketManager implements IPhysics {
    private static final String TAG = "DPUSocketManager";
    private Context mContext;
    private int mState;
    private String mSerialNo;
    private DeviceFactoryManager mDeviceFactoryManager;
    private static volatile boolean commandWait = true;
    private PeerTask mPeerTask;
    private InputData mInputData;
    private OutputData mOutputData;
    private ReadByteDataStream mReadByteDataStreamThread;
    private static volatile String mReadData;

    public DPUSocketManager(DeviceFactoryManager deviceFactoryManager, Context context, String serialNo) {
        mDeviceFactoryManager = deviceFactoryManager;
        mContext = context.getApplicationContext();
        mSerialNo = serialNo;
        mPeerTask = PeerTask.getInstance();
        mState = STATE_NONE;
    }

    public void createConnect() {
        mPeerTask.release();
        mPeerTask.setSn(mSerialNo);
        mPeerTask.peer_init(null, mContext);
        PacketQueue.packetQueueInit(null, mPeerTask);
        mState = STATE_CONNECTING;
        mInputData = new InputData();
        mOutputData = new OutputData();
        mReadByteDataStreamThread = new ReadByteDataStream(this,mInputData, mOutputData);
        new Thread(mReadByteDataStreamThread).start();
    }

    @Override
    public int getState() {
        if(mPeerTask.is_match_ok()) {
            return STATE_CONNECTED;
        }
        return mState;
    }

    @Override
    public synchronized String getCommand() {
      //  Log.d(TAG, "getCommand=" +mReadData);
        return mReadData;
    }

    @Override
    public synchronized void setCommand(String command) {
       // Log.d(TAG, "setCommand =" + command);
        mReadData = command;
        mDeviceFactoryManager.send(command);
    }

    @Override
    public InputStream getInputStream() {
        return mInputData;
    }

    @Override
    public OutputStream getOutputStream() {
        return mOutputData;
    }

    @Override
    public synchronized boolean getCommand_wait() {
      //  Log.d(TAG, "getCommand_wait =" + commandWait);
        return commandWait;
    }

    @Override
    public synchronized void setCommand_wait(boolean wait) {
        commandWait = wait;
      //  Log.d(TAG, "setCommand_wait =" + commandWait);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public String getDeviceName() {
        return "socket_device";
    }

    @Override
    public void closeDevice() {
        release();
    }

    @Override
    public void physicalCloseDevice() {
        release();
    }

    private void release() {
        if (mReadByteDataStreamThread != null) {
            mReadByteDataStreamThread.cancel();
            mReadByteDataStreamThread = null;
        }
        mPeerTask.release();
    }

    @Override
    public void setSerialNo(String serialNo) {
        mSerialNo = serialNo;
    }

    @Override
    public String getSerialNo() {
        return mSerialNo;
    }

    @Override
    public void setIsTruckReset(boolean isTruckReset) {

    }

    @Override
    public boolean isTruckReset() {
        return false;
    }

    @Override
    public void userInteractionWhenDPUConnected() {

    }

    @Override
    public void setIsFix(boolean isFix) {

    }

    @Override
    public void setIsRemoteClientDiagnoseMode(boolean isRemoteClientDiagnoseMode) {

    }

    @Override
    public boolean getIsRemoteClientDiagnoseMode() {
        return false;
    }

    @Override
    public void setIsSupportOneRequestMoreAnswerDiagnoseMode(boolean isSupportOneRequestMoreAnswerDiagnoseMode) {

    }

    @Override
    public boolean getIsSupportOneRequestMoreAnswerDiagnoseMode() {
        return false;
    }

    @Override
    public void setCommand(String command, boolean isSupportSelfSend) {

    }

    @Override
    public void setCommandStatus(boolean isSetCommandOfMainLink) {

    }

    @Override
    public boolean getCommandStatus() {
        return false;
    }
    private String dataType;
    @Override
    public void setDataType(String type) {
        dataType = type;
    }

    @Override
    public String getDataType() {
        return dataType;
    }
}
