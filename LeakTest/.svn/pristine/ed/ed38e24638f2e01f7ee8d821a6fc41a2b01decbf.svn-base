package com.cnlaunch.physics.simulator;
import java.io.IOException;
import com.cnlaunch.physics.io.AbstractPhysicsInputStream;
import com.cnlaunch.physics.utils.MLog;

/**
 * BluetoothBLEInputStream.
 *
 * Used to write to a BluetoothBLEDevice.
 */
 final class SimulatorInputStream extends AbstractPhysicsInputStream {
	private SimulatorManager mSimulatorManager;
    public SimulatorInputStream(SimulatorManager s) {
        mSimulatorManager = s;
    }

    public void close() throws IOException {
        mSimulatorManager.inputStreamClose();
    }

    /**
     * Reads a single byte from this stream and returns it as an integer in the
     * range from 0 to 255. Returns -1 if the end of the stream has been
     * reached. Blocks until one byte has been read, the end of the source
     * stream is detected or an exception is thrown.
     *
     * @return the byte read or -1 if the end of stream has been reached.
     * @throws IOException
     *             if the stream is closed or another IOException occurs.
     * @since Android 1.5
     */
    public int read() throws IOException {
        byte b[] = new byte[1];
        int ret = read(b, 0, 1);
        if (ret < 0) {
        	throw new IOException();
        } else {
            return ret;
        }
    }
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }
    /**
     * Reads at most {@code length} bytes from this stream and stores them in
     * the byte array {@code b} starting at {@code offset}.
     *
     * @param b
     *            the byte array in which to store the bytes read.
     * @param offset
     *            the initial position in {@code buffer} to store the bytes
     *            read from this stream.
     * @param length
     *            the maximum number of bytes to store in {@code b}.
     * @return the number of bytes actually read or -1 if the end of the stream
     *         has been reached.
     * @throws IOException
     *             if {@code offset < 0} or {@code length < 0}, or if
     *             {@code offset + length} is greater than the length of
     *             {@code b}.
     * @throws IOException
     *             if the stream is closed or another IOException occurs.
     * @since Android 1.5
     */
    public int read(byte[] b, int offset, int length) throws IOException {
        if (b == null) {
            throw new NullPointerException("byte array is null");
        }
        if ((length) < 0 || length > b.length - offset) {
            throw new ArrayIndexOutOfBoundsException("invalid offset or length");
        }
        int ret = mSimulatorManager.read(b,offset,length);
        if (ret < 0) {
        	throw new IOException();
        } else {
            return ret;
        }
    }
}
