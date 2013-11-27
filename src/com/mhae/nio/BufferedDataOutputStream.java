package com.mhae.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;


/**
 * A buffered DataOutputStream using a direct ByteBuffer internally for performance.
 * Use flush() to enforce boundaries or trigger the write over the socket. 
 * @author michaelhaeuptle
 *
 */
public class BufferedDataOutputStream  {

	/** the channel the buffer is written to */
  private WritableByteChannel channel;
  
	/** Default buffer size */
  private static int DEFAULT_BUFFER_SIZE = 1024 * 32;

  /** The underlying (direct) buffer */
  protected ByteBuffer buffer;

  /** Size of the allocated buffer */
  protected final int size;

  
  public BufferedDataOutputStream(WritableByteChannel channel) throws IOException {
    this(channel, DEFAULT_BUFFER_SIZE);
  }



  public BufferedDataOutputStream(WritableByteChannel channel, int bufferSize)
    throws IOException {
  	this.channel = channel;
  	this.size = bufferSize;
    allocateBuffer();
  }


  private static int writeDataFully(WritableByteChannel channel,
      ByteBuffer buffer) throws IOException {

      int ret = 0;

      while (buffer.remaining() > 0) {
        ret += channel.write(buffer);
      }

      return ret;
    }
  

  /**
   * Flush the current contents of the buffer over the socket
   * @throws IOException
   */
  public void flush() throws IOException {
    buffer.flip();
    writeDataFully(channel, buffer);
    buffer.clear();
  }



  /**
   * Allocates or flushes the buffer
   * @throws IOException
   */
  protected void allocateBuffer() throws IOException {
    if (buffer == null) {
      buffer = ByteBuffer.allocateDirect(size);
    }
    else {
      flush();
    }
  }

  
  public void close() throws IOException {
    if (buffer != null) return;
    flush();
    channel.close();
  }
  


  public void write(int b) throws IOException {
    if (buffer.remaining() < 1) {
      allocateBuffer();
    }
    buffer.put((byte)(b & 0xff));
  }



  public void write(byte b[]) throws IOException {
    write(b, 0, b.length);
  }



  public void write(byte b[], int off, int len) throws IOException {
    while (len > 0) {
      if (buffer.remaining() < len) {
        allocateBuffer();
      }
      int putLen = Math.min(len, buffer.remaining());

      buffer.put(b, off, putLen);
      len -= putLen;
      off += putLen;
    }
  }



  public void writeBoolean(boolean v) throws IOException {
    if (buffer.remaining() < 1) {
      allocateBuffer();
    }
    buffer.put((byte)(v ? 1 : 0));
  }



  public void writeByte(int v) throws IOException {
    if (buffer.remaining() < 1) {
      allocateBuffer();
    }
    buffer.put((byte)(v & 0xff));
  }



  public void writeShort(int v) throws IOException {
    if (buffer.remaining() < 2) {
      allocateBuffer();
    }
    buffer.putShort((short)(v & 0xffff));
  }



  public void writeChar(int v) throws IOException {
    if (buffer.remaining() < 2) {
      allocateBuffer();
    }
    buffer.putChar((char)v);
  }



  public void writeInt(int v) throws IOException {
    if (buffer.remaining() < 4) {
      allocateBuffer();
    }
    buffer.putInt(v);
  }



  public void writeLong(long v) throws IOException {
    if (buffer.remaining() < 8) {
      allocateBuffer();
    }
    buffer.putLong(v);
  }



  public void writeFloat(float v) throws IOException {
    if (buffer.remaining() < 4) {
      allocateBuffer();
    }
    buffer.putFloat(v);
  }



  public void writeDouble(double v) throws IOException {
    if (buffer.remaining() < 8) {
      allocateBuffer();
    }
    buffer.putDouble(v);
  }



  public void writeBytes(String s) throws IOException {
    byte[] bytes = s.getBytes();

    if (buffer.remaining() < bytes.length) {
      allocateBuffer();
    }
    buffer.put(bytes);
  }



  public void writeChars(String s) throws IOException {
    throw new UnsupportedOperationException();
  }



  public void writeUTF8(String str) throws IOException {
    byte[] bytes = str.getBytes("UTF-8");

    writeShort(bytes.length); // indicate the number of bytes
    
    int offset = 0;
    int bytesRemaining = bytes.length;

    do {
      int bytesInBuffer = buffer.remaining();

      // flush the buffer
      if (bytesInBuffer <= 0) {
        allocateBuffer();
        bytesInBuffer = buffer.remaining();
      }

      int bytesToWrite = Math.min(bytesInBuffer, bytesRemaining);

      buffer.put(bytes, offset, bytesToWrite);

      bytesRemaining -= bytesToWrite;
      offset += bytesToWrite;
    }
    while (bytesRemaining > 0);

  }

  
}
