package com.cnlaunch.physics.smartlink;

import android.util.Log;

import com.cnlaunch.physics.utils.MLog;

import java.io.IOException;
import java.io.OutputStream;

public class OutputData extends OutputStream {
    PeerTask mPeerTask;

    public OutputData() {
        mPeerTask = PeerTask.getInstance();
    }

    @Override
    public void write(int b) throws IOException {

    }

    @Override
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        mPeerTask.joinSendQueue(b, off, len);
    }
}
