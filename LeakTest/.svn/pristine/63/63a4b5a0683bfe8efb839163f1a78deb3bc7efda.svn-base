package com.cnlaunch.physics.smartlink;

import java.io.IOException;
import java.io.InputStream;

public class InputData extends InputStream {
    PacketQueue mPacketQueue;

    public InputData() {
        mPacketQueue = PacketQueue.getMcuSendQueue();
    }

    @Override
    public int read() throws IOException {
        return 0;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int size = mPacketQueue.readBlock(b, off, len);
        return size;
    }

    @Override
    public int available() throws IOException {
        return mPacketQueue.available();
    }
}
