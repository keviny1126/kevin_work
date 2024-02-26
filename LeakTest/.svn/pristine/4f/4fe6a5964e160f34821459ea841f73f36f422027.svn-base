package com.cnlaunch.physics.bluetooth;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.RemoteException;

import com.cnlaunch.physics.IPhysicsOutputStreamBufferWrapper;
import com.cnlaunch.physics.bluetooth.remote.IRemoteBluetoothManager;
import com.cnlaunch.physics.io.AbstractPhysicsOutputStream;

/**
 * 
 *
 * 蓝牙输出流代理.
 */
final class  BluetoothOutputStreamProxy extends AbstractPhysicsOutputStream {
    IRemoteBluetoothManager mRemoteBluetoothManager;
	OutputStream mBlueOutputStream;
	boolean mIsRemote;
	public BluetoothOutputStreamProxy(IRemoteBluetoothManager remoteBluetoothManager,IPhysicsOutputStreamBufferWrapper physicsOutputStreamBufferWrapper) {
		super(physicsOutputStreamBufferWrapper);
		mRemoteBluetoothManager =  remoteBluetoothManager;
		mBlueOutputStream = null;
		mIsRemote = true;
    }
	public BluetoothOutputStreamProxy(OutputStream blueOutputStream,IPhysicsOutputStreamBufferWrapper physicsOutputStreamBufferWrapper) {
		super(physicsOutputStreamBufferWrapper);
		mRemoteBluetoothManager =  null;
		mBlueOutputStream = blueOutputStream;
		mIsRemote = false;
	}
   public void close() throws IOException {
	   try {
		   if (mIsRemote) {
			   mRemoteBluetoothManager.outputStream_close();
		   } else {
			   mBlueOutputStream.close();
		   }
	   } catch (RemoteException e) {
		   e.printStackTrace();
	   }
   }
   public void flush()  throws IOException {
	   try {
		   if (mIsRemote) {
			   mRemoteBluetoothManager.outputStream_flush();
		   }
		   else{
			   mBlueOutputStream.flush();
		   }
		} catch (RemoteException e) {
			e.printStackTrace();
		}
   }    
    public void write(int oneByte) throws IOException {       
    	try {
			if (mIsRemote) {
				mRemoteBluetoothManager.outputStream_write0(oneByte);
			}
			else{
				mBlueOutputStream.write(oneByte);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }

    public void write(byte[] buffer) throws IOException {
    	write(buffer, 0, buffer.length);
    }
    public void write(byte[] b, int offset, int count) throws IOException {
		if (b == null) {
			throw new NullPointerException("wrapperBuffer is null");
		}
		if ((count) < 0 || count > b.length - offset) {
			throw new IndexOutOfBoundsException("invalid offset or length");
		}
    	try {
			byte[] wrapperBuffer = null;
			if(mPhysicsOutputStreamBufferWrapper!=null && mPhysicsOutputStreamBufferWrapper.isNeedWrapper()){
				wrapperBuffer = mPhysicsOutputStreamBufferWrapper.writeBufferWrapper(b,offset,count);
				if (wrapperBuffer == null) {
					throw new NullPointerException("wrapperBuffer is null");
				}
				if (mIsRemote) {
					mRemoteBluetoothManager.outputStream_write1(wrapperBuffer, 0, wrapperBuffer.length);
				}
				else{
					mBlueOutputStream.write(wrapperBuffer, 0, wrapperBuffer.length);
				}
			}
			else{
				if (mIsRemote) {
					mRemoteBluetoothManager.outputStream_write1(b, offset, count);
				}
				else{
					mBlueOutputStream.write(b, offset, count);
				}
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		}
    }
}
