/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cnlaunch.physics.usb;

import java.io.IOException;
import java.io.OutputStream;

import com.cnlaunch.physics.IPhysicsOutputStreamBufferWrapper;
import com.cnlaunch.physics.io.AbstractPhysicsOutputStream;
import com.cnlaunch.physics.utils.MLog;

/**
 * USBOutputStream.
 *
 * Used to read from USBDevice.
 *
 * @hide
 */
/*package*/ final class USBOutputStream extends AbstractPhysicsOutputStream {
    private DPUUSBDevice usbDevice;

    public USBOutputStream(DPUUSBDevice s, IPhysicsOutputStreamBufferWrapper physicsOutputStreamBufferWrapper) {
    	super(physicsOutputStreamBufferWrapper);
        usbDevice = s;
    }

    /**
     * Close this output stream and the socket associated with it.
     */
    public void close() throws IOException {
    	
    }

    /**
     * Writes a single byte to this stream. Only the least significant byte of
     * the integer {@code oneByte} is written to the stream.
     *
     * @param oneByte
     *            the byte to be written.
     * @throws IOException
     *             if an error occurs while writing to this stream.
     * @since Android 1.0
     */
    public void write(int oneByte) throws IOException {
        byte b[] = new byte[1];
        b[0] = (byte)oneByte;
        write(b, 0, 1);
    }

    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting
     * at position {@code offset} to this stream.
     *
     * @param b
     *            the buffer to be written.
     * @param offset
     *            the start position in {@code buffer} from where to get bytes.
     * @param count
     *            the number of bytes from {@code buffer} to write to this
     *            stream.
     * @throws IOException
     *             if an error occurs while writing to this stream.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if
     *             {@code offset + count} is bigger than the length of
     *             {@code buffer}.
     * @since Android 1.0
     */
    public void write(byte[] b, int offset, int count) throws IOException {
        if (b == null) {
            throw new NullPointerException("buffer is null");
        }
        if ((count) < 0 || count > b.length - offset) {
            throw new IndexOutOfBoundsException("invalid offset or length");
        }
        if(offset!=0){
        	throw new IllegalArgumentException("offset 必须为0");
        }
        if(MLog.isDebug) {
            MLog.d("USBOutputStream", "usbDevice.write start");
        }
        byte[] wrapperBuffer = null;
        if(mPhysicsOutputStreamBufferWrapper!=null && mPhysicsOutputStreamBufferWrapper.isNeedWrapper()){
            wrapperBuffer = mPhysicsOutputStreamBufferWrapper.writeBufferWrapper(b,offset,count);
            if (wrapperBuffer == null) {
                throw new NullPointerException("wrapperBuffer is null");
            }
            if(usbDevice.write(wrapperBuffer, 0, wrapperBuffer.length)<=0){
                throw new IOException();
            }
        }
        else{
            if(usbDevice.write(b, count, 0)<=0){
                throw new IOException();
            }
        }
        if(MLog.isDebug) {
            MLog.d("USBOutputStream", "usbDevice.write end");
        }
    }
    
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }
    /**
     * Wait until the data in sending queue is emptied. A polling version
     * for flush implementation. Use it to ensure the writing data afterwards will
     * be packed in the new RFCOMM frame.
     * @throws IOException
     *             if an i/o error occurs.
     * @since Android 4.2.3
     */
    public void flush()  throws IOException {
    	
    }
}
