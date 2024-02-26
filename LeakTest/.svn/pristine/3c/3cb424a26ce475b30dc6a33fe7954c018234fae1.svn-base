package com.cnlaunch.physics.io;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 *
 * 蓝牙输入流代理.
 */
 public final class PhysicsInputStreamWrapper extends AbstractPhysicsInputStream {
 private InputStream mInputStream;

 public PhysicsInputStreamWrapper(InputStream inputStream) {
  mInputStream = inputStream;
 }


 @Override
 public int available() throws IOException {
  return mInputStream.available();
 }

 @Override
 public void close() throws IOException {
  mInputStream.close();
 }

 @Override
 public int read(byte[] buffer) throws IOException {
  return mInputStream.read(buffer);
 }

 @Override
 public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
  return mInputStream.read(buffer, byteOffset, byteCount);
 }

 @Override
 public int read() throws IOException {
  return mInputStream.read();
 }
}
