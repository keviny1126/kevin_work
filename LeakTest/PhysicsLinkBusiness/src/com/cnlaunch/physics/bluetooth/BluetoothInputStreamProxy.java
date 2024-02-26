package com.cnlaunch.physics.bluetooth;

import java.io.IOException;
import java.io.InputStream;

import android.os.RemoteException;

import com.cnlaunch.physics.bluetooth.remote.IRemoteBluetoothManager;
import com.cnlaunch.physics.io.AbstractPhysicsInputStream;

/**
 * 
 *
 * 蓝牙输入流代理.
 */
 final class BluetoothInputStreamProxy extends AbstractPhysicsInputStream {
	IRemoteBluetoothManager mRemoteBluetoothManager;
	InputStream mBlueInputStream;
	boolean mIsRemote;
	public BluetoothInputStreamProxy(IRemoteBluetoothManager remoteBluetoothManager) {
		mRemoteBluetoothManager =  remoteBluetoothManager;
		mBlueInputStream = null;
		mIsRemote = true;
    }
	public BluetoothInputStreamProxy(InputStream blueInputStream) {
		mRemoteBluetoothManager =  null;
		mBlueInputStream = blueInputStream;
		mIsRemote = false;
	}
	public int available() throws IOException {
        try {
        	if(mIsRemote) {
				return mRemoteBluetoothManager.inputStream_available();
			}
			else{
        		return mBlueInputStream.available();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
    }
    public void close() throws IOException {
    	try {
			if(mIsRemote) {
				mRemoteBluetoothManager.inputStream_close();
			}
			else{
				mBlueInputStream.close();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }   
    public int read() throws IOException {
    	try {
			if(mIsRemote) {
				return mRemoteBluetoothManager.inputStream_read0();
			}
			else{
				return mBlueInputStream.read();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
    }
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }
    
    public int read(byte[] b, int offset, int length) throws IOException {
    	try {
			if(mIsRemote) {
				return mRemoteBluetoothManager.inputStream_read1(b, offset, length);
			}
			else{
				return mBlueInputStream.read(b, offset, length);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
    }

}
